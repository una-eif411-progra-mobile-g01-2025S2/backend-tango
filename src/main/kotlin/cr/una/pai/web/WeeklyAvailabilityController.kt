package cr.una.pai.web

import cr.una.pai.dto.WeeklyAvailabilityInput
import cr.una.pai.dto.WeeklyAvailabilityResult
import cr.una.pai.mapper.WeeklyAvailabilityMapper
import cr.una.pai.service.WeeklyAvailabilityService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.time.DayOfWeek
import java.util.*

@RestController
@RequestMapping("\${api.base-path}/availability")
@CrossOrigin(origins = ["\${spring.web.cors.allowed-origins}"])
@Validated
class WeeklyAvailabilityController(
    private val weeklyAvailabilityService: WeeklyAvailabilityService,
    private val weeklyAvailabilityMapper: WeeklyAvailabilityMapper
) {

    @GetMapping
    fun getAllAvailabilities(): ResponseEntity<List<WeeklyAvailabilityResult>> =
        ResponseEntity.ok(weeklyAvailabilityService.findAllResults())

    @GetMapping("/{id}")
    fun getAvailabilityById(@PathVariable id: UUID): ResponseEntity<WeeklyAvailabilityResult> = try {
        ResponseEntity.ok(weeklyAvailabilityService.findResultById(id))
    } catch (e: IllegalArgumentException) { ResponseEntity.notFound().build() }

    @GetMapping("/user/{userId}")
    fun getAvailabilitiesByUserId(@PathVariable userId: UUID): ResponseEntity<List<WeeklyAvailabilityResult>> =
        ResponseEntity.ok(weeklyAvailabilityService.findAllByUserId(userId).map(weeklyAvailabilityMapper::toResult))

    @GetMapping("/user/{userId}/day/{dayOfWeek}")
    fun getAvailabilitiesByUserIdAndDay(
        @PathVariable userId: UUID,
        @PathVariable dayOfWeek: DayOfWeek
    ): ResponseEntity<List<WeeklyAvailabilityResult>> =
        ResponseEntity.ok(weeklyAvailabilityService.findAllByUserIdAndDayOfWeek(userId, dayOfWeek).map(weeklyAvailabilityMapper::toResult))

    @PostMapping
    fun createAvailability(@Valid @RequestBody input: WeeklyAvailabilityInput): ResponseEntity<Any> = try {
        val created = weeklyAvailabilityService.create(input)
        ResponseEntity.status(HttpStatus.CREATED).body(created)
    } catch (e: IllegalArgumentException) {
        ResponseEntity.badRequest().body(mapOf("error" to (e.message ?: "Invalid data")))
    }

    @PutMapping("/{id}")
    fun updateAvailability(
        @PathVariable id: UUID,
        @Valid @RequestBody input: WeeklyAvailabilityInput
    ): ResponseEntity<Any> = try {
        val updated = weeklyAvailabilityService.updateDto(id, input)
        ResponseEntity.ok(updated)
    } catch (e: IllegalArgumentException) {
        ResponseEntity.badRequest().body(mapOf("error" to (e.message ?: "Invalid data")))
    }

    @DeleteMapping("/{id}")
    fun deleteAvailability(@PathVariable id: UUID): ResponseEntity<Any> = try {
        weeklyAvailabilityService.delete(id)
        ResponseEntity.noContent().build()
    } catch (e: IllegalArgumentException) {
        ResponseEntity.badRequest().body(mapOf("error" to (e.message ?: "Invalid data")))
    }
}
