package cr.una.pai.web

import cr.una.pai.domain.CalendarProvider
import cr.una.pai.dto.CalendarEventInput
import cr.una.pai.dto.CalendarEventResult
import cr.una.pai.mapper.CalendarEventMapper
import cr.una.pai.service.CalendarEventService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("\${api.endpoints.calendar}", produces = ["application/json"])
@CrossOrigin(origins = ["\${spring.web.cors.allowed-origins}"])
@Validated
class CalendarEventController(
    private val calendarEventService: CalendarEventService,
    private val calendarEventMapper: CalendarEventMapper
) {

    @GetMapping
    fun getAllCalendarEvents(): ResponseEntity<List<CalendarEventResult>> =
        ResponseEntity.ok(calendarEventService.findAllResults())

    @GetMapping("/{id}")
    fun getCalendarEventById(@PathVariable id: Long): ResponseEntity<CalendarEventResult> =
        try { ResponseEntity.ok(calendarEventService.findResultById(id)) }
        catch (e: IllegalArgumentException) { ResponseEntity.notFound().build() }

    @GetMapping("/external/{externalEventId}")
    fun getCalendarEventByExternalId(@PathVariable externalEventId: String): ResponseEntity<CalendarEventResult> =
        calendarEventService.findByExternalEventId(externalEventId)
            .map { ResponseEntity.ok(calendarEventMapper.toResult(it)) }
            .orElse(ResponseEntity.notFound().build())

    @GetMapping("/study-block/{studyBlockId}")
    fun getCalendarEventByStudyBlockId(@PathVariable studyBlockId: UUID): ResponseEntity<CalendarEventResult> =
        calendarEventService.findByStudyBlockId(studyBlockId)
            .map { ResponseEntity.ok(calendarEventMapper.toResult(it)) }
            .orElse(ResponseEntity.notFound().build())

    @GetMapping("/provider/{provider}")
    fun getCalendarEventsByProvider(@PathVariable provider: CalendarProvider): ResponseEntity<List<CalendarEventResult>> =
        ResponseEntity.ok(calendarEventService.findAllByProvider(provider).map(calendarEventMapper::toResult))

    @PostMapping(consumes = ["application/json"])
    fun createCalendarEvent(@Valid @RequestBody input: CalendarEventInput): ResponseEntity<Any> =
        try {
            val created = calendarEventService.create(input)
            ResponseEntity.status(HttpStatus.CREATED).body(created)
        } catch (e: IllegalArgumentException) {
            badRequest(e)
        }

    @PutMapping("/{id}", consumes = ["application/json"])
    fun updateCalendarEvent(@PathVariable id: Long, @Valid @RequestBody input: CalendarEventInput): ResponseEntity<Any> =
        try {
            val updated = calendarEventService.updateDto(id, input)
            ResponseEntity.ok(updated)
        } catch (e: IllegalArgumentException) {
            badRequest(e)
        }

    @PostMapping("/{id}/sync")
    fun syncCalendarEvent(@PathVariable id: Long): ResponseEntity<Any> =
        try {
            val synced = calendarEventMapper.toResult(calendarEventService.syncEvent(id))
            ResponseEntity.ok(synced)
        } catch (e: IllegalArgumentException) {
            badRequest(e)
        }

    @DeleteMapping("/{id}")
    fun deleteCalendarEvent(@PathVariable id: Long): ResponseEntity<Any> =
        try {
            calendarEventService.delete(id)
            ResponseEntity.noContent().build()
        } catch (e: IllegalArgumentException) {
            badRequest(e)
        }

    private fun badRequest(e: IllegalArgumentException): ResponseEntity<Any> =
        ResponseEntity.badRequest().body(mapOf("error" to (e.message ?: "Invalid data")))
}
