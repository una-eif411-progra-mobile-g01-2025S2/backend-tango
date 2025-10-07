package cr.una.pai.web

import cr.una.pai.domain.*
import cr.una.pai.service.WeeklyAvailabilityService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.DayOfWeek
import java.util.*

@RestController
@RequestMapping("\${api.base-path}/availability")
@CrossOrigin(origins = ["\${spring.web.cors.allowed-origins}"])
class WeeklyAvailabilityController(
    private val weeklyAvailabilityService: WeeklyAvailabilityService
) {

    @GetMapping
    fun getAllAvailabilities(): ResponseEntity<List<WeeklyAvailability>> {
        return ResponseEntity.ok(weeklyAvailabilityService.findAll())
    }

    @GetMapping("/{id}")
    fun getAvailabilityById(@PathVariable id: UUID): ResponseEntity<WeeklyAvailability> {
        return weeklyAvailabilityService.findById(id)
            .map { ResponseEntity.ok(it) }
            .orElse(ResponseEntity.notFound().build())
    }

    @GetMapping("/user/{userId}")
    fun getAvailabilitiesByUserId(@PathVariable userId: UUID): ResponseEntity<List<WeeklyAvailability>> {
        return ResponseEntity.ok(weeklyAvailabilityService.findAllByUserId(userId))
    }

    @GetMapping("/user/{userId}/day/{dayOfWeek}")
    fun getAvailabilitiesByUserIdAndDay(
        @PathVariable userId: UUID,
        @PathVariable dayOfWeek: DayOfWeek
    ): ResponseEntity<List<WeeklyAvailability>> {
        return ResponseEntity.ok(weeklyAvailabilityService.findAllByUserIdAndDayOfWeek(userId, dayOfWeek))
    }

    @PostMapping
    fun createAvailability(@RequestBody availability: WeeklyAvailability): ResponseEntity<Any> {
        return try {
            val created = weeklyAvailabilityService.create(availability)
            ResponseEntity.status(HttpStatus.CREATED).body(created)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    @PutMapping("/{id}")
    fun updateAvailability(
        @PathVariable id: UUID,
        @RequestBody availability: WeeklyAvailability
    ): ResponseEntity<Any> {
        return try {
            val updated = weeklyAvailabilityService.update(id, availability)
            ResponseEntity.ok(updated)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    @DeleteMapping("/{id}")
    fun deleteAvailability(@PathVariable id: UUID): ResponseEntity<Any> {
        return try {
            weeklyAvailabilityService.delete(id)
            ResponseEntity.noContent().build()
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }
}

