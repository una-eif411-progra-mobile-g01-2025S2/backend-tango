package cr.una.pai.web

import cr.una.pai.domain.*
import cr.una.pai.service.CalendarEventService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("\${api.endpoints.calendar}")
@CrossOrigin(origins = ["\${spring.web.cors.allowed-origins}"])
class CalendarEventController(
    private val calendarEventService: CalendarEventService
) {

    @GetMapping
    fun getAllCalendarEvents(): ResponseEntity<List<CalendarEvent>> {
        return ResponseEntity.ok(calendarEventService.findAll())
    }

    @GetMapping("/{id}")
    fun getCalendarEventById(@PathVariable id: UUID): ResponseEntity<CalendarEvent> {
        return calendarEventService.findById(id)
            .map { ResponseEntity.ok(it) }
            .orElse(ResponseEntity.notFound().build())
    }

    @GetMapping("/external/{externalEventId}")
    fun getCalendarEventByExternalId(@PathVariable externalEventId: String): ResponseEntity<CalendarEvent> {
        return calendarEventService.findByExternalEventId(externalEventId)
            .map { ResponseEntity.ok(it) }
            .orElse(ResponseEntity.notFound().build())
    }

    @GetMapping("/study-block/{studyBlockId}")
    fun getCalendarEventByStudyBlockId(@PathVariable studyBlockId: UUID): ResponseEntity<CalendarEvent> {
        return calendarEventService.findByStudyBlockId(studyBlockId)
            .map { ResponseEntity.ok(it) }
            .orElse(ResponseEntity.notFound().build())
    }

    @GetMapping("/provider/{provider}")
    fun getCalendarEventsByProvider(@PathVariable provider: CalendarProvider): ResponseEntity<List<CalendarEvent>> {
        return ResponseEntity.ok(calendarEventService.findAllByProvider(provider))
    }

    @PostMapping
    fun createCalendarEvent(@RequestBody calendarEvent: CalendarEvent): ResponseEntity<Any> {
        return try {
            val created = calendarEventService.create(calendarEvent)
            ResponseEntity.status(HttpStatus.CREATED).body(created)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    @PutMapping("/{id}")
    fun updateCalendarEvent(
        @PathVariable id: UUID,
        @RequestBody calendarEvent: CalendarEvent
    ): ResponseEntity<Any> {
        return try {
            val updated = calendarEventService.update(id, calendarEvent)
            ResponseEntity.ok(updated)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    @PostMapping("/{id}/sync")
    fun syncCalendarEvent(@PathVariable id: UUID): ResponseEntity<Any> {
        return try {
            val synced = calendarEventService.syncEvent(id)
            ResponseEntity.ok(synced)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    @DeleteMapping("/{id}")
    fun deleteCalendarEvent(@PathVariable id: UUID): ResponseEntity<Any> {
        return try {
            calendarEventService.delete(id)
            ResponseEntity.noContent().build()
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }
}

