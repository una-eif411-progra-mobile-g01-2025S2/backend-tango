package cr.una.pai.web

import cr.una.pai.domain.CalendarProvider
import cr.una.pai.dto.CalendarEventInput
import cr.una.pai.dto.CalendarEventResult
import cr.una.pai.mapper.CalendarEventMapper
import cr.una.pai.service.CalendarEventService
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("\${api.endpoints.calendar}")
@CrossOrigin(origins = ["\${spring.web.cors.allowed-origins}"])
@Validated
class CalendarEventController(
    private val calendarEventService: CalendarEventService,
    private val calendarEventMapper: CalendarEventMapper
) {

    @Operation(summary = "Obtiene todos los eventos de calendario")
    @GetMapping
    fun getAllCalendarEvents(): ResponseEntity<List<CalendarEventResult>> =
        ResponseEntity.ok(calendarEventService.findAllResults())

    @Operation(summary = "Obtiene un evento de calendario por su ID")
    @GetMapping("/{id}")
    fun getCalendarEventById(@PathVariable id: UUID): ResponseEntity<CalendarEventResult> = try {
        ResponseEntity.ok(calendarEventService.findResultById(id))
    } catch (e: IllegalArgumentException) { ResponseEntity.notFound().build() }

    @Operation(summary = "Obtiene un evento de calendario por su ID externo")
    @GetMapping("/external/{externalEventId}")
    fun getCalendarEventByExternalId(@PathVariable externalEventId: String): ResponseEntity<CalendarEventResult> =
        calendarEventService.findByExternalEventId(externalEventId)
            .map { ResponseEntity.ok(calendarEventMapper.toResult(it)) }
            .orElse(ResponseEntity.notFound().build())

    @Operation(summary = "Obtiene un evento de calendario vinculado a un bloque de estudio")
    @GetMapping("/study-block/{studyBlockId}")
    fun getCalendarEventByStudyBlockId(@PathVariable studyBlockId: UUID): ResponseEntity<CalendarEventResult> =
        calendarEventService.findByStudyBlockId(studyBlockId)
            .map { ResponseEntity.ok(calendarEventMapper.toResult(it)) }
            .orElse(ResponseEntity.notFound().build())

    @Operation(summary = "Obtiene los eventos de calendario por proveedor")
    @GetMapping("/provider/{provider}")
    fun getCalendarEventsByProvider(@PathVariable provider: CalendarProvider): ResponseEntity<List<CalendarEventResult>> =
        ResponseEntity.ok(calendarEventService.findAllByProvider(provider).map(calendarEventMapper::toResult))

    @Operation(summary = "Crea un nuevo evento de calendario")
    @PostMapping
    fun createCalendarEvent(@Valid @RequestBody input: CalendarEventInput): ResponseEntity<Any> = try {
        val created = calendarEventService.create(input)
        ResponseEntity.status(HttpStatus.CREATED).body(created)
    } catch (e: IllegalArgumentException) {
        ResponseEntity.badRequest().body(mapOf("error" to (e.message ?: "Invalid data")))
    }

    @Operation(summary = "Actualiza un evento de calendario por su ID")
    @PutMapping("/{id}")
    fun updateCalendarEvent(
        @PathVariable id: UUID,
        @Valid @RequestBody input: CalendarEventInput
    ): ResponseEntity<Any> = try {
        val updated = calendarEventService.updateDto(id, input)
        ResponseEntity.ok(updated)
    } catch (e: IllegalArgumentException) {
        ResponseEntity.badRequest().body(mapOf("error" to (e.message ?: "Invalid data")))
    }

    @Operation(summary = "Sincroniza un evento de calendario por su ID")
    @PostMapping("/{id}/sync")
    fun syncCalendarEvent(@PathVariable id: UUID): ResponseEntity<Any> = try {
        val synced = calendarEventMapper.toResult(calendarEventService.syncEvent(id))
        ResponseEntity.ok(synced)
    } catch (e: IllegalArgumentException) {
        ResponseEntity.badRequest().body(mapOf("error" to (e.message ?: "Invalid data")))
    }

    @Operation(summary = "Elimina un evento de calendario por su ID")
    @DeleteMapping("/{id}")
    fun deleteCalendarEvent(@PathVariable id: UUID): ResponseEntity<Any> = try {
        calendarEventService.delete(id)
        ResponseEntity.noContent().build()
    } catch (e: IllegalArgumentException) {
        ResponseEntity.badRequest().body(mapOf("error" to (e.message ?: "Invalid data")))
    }
}
