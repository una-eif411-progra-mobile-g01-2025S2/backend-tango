package cr.una.pai.web

import cr.una.pai.dto.AcademicPeriodInput
import cr.una.pai.dto.AcademicPeriodResult
import cr.una.pai.mapper.AcademicPeriodMapper
import cr.una.pai.service.AcademicPeriodService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("\${api.endpoints.periods}")
@CrossOrigin(origins = ["\${spring.web.cors.allowed-origins}"])
@Validated
class AcademicPeriodController(
    private val periodService: AcademicPeriodService,
    private val periodMapper: AcademicPeriodMapper
) {

    @GetMapping
    fun getAllPeriods(): ResponseEntity<List<AcademicPeriodResult>> =
        ResponseEntity.ok(periodService.findAllResults())

    @GetMapping("/{id}")
    fun getPeriodById(@PathVariable id: UUID): ResponseEntity<AcademicPeriodResult> = try {
        ResponseEntity.ok(periodService.findResultById(id))
    } catch (e: IllegalArgumentException) {
        ResponseEntity.notFound().build()
    }

    @GetMapping("/name/{name}")
    fun getPeriodByName(@PathVariable name: String): ResponseEntity<AcademicPeriodResult> =
        periodService.findByName(name)
            .map { ResponseEntity.ok(periodMapper.toResult(it)) }
            .orElse(ResponseEntity.notFound().build())

    @PostMapping
    fun createPeriod(@Valid @RequestBody input: AcademicPeriodInput): ResponseEntity<Any> =
        try {
            val created = periodService.create(input)
            ResponseEntity.status(HttpStatus.CREATED).body(created)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to (e.message ?: "Invalid data")))
        }

    @PutMapping("/{id}")
    fun updatePeriod(
        @PathVariable id: UUID,
        @Valid @RequestBody input: AcademicPeriodInput
    ): ResponseEntity<Any> =
        try {
            val updated = periodService.updateDto(id, input)
            ResponseEntity.ok(updated)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to (e.message ?: "Invalid data")))
        }

    @DeleteMapping("/{id}")
    fun deletePeriod(@PathVariable id: UUID): ResponseEntity<Any> =
        try {
            periodService.delete(id)
            ResponseEntity.noContent().build()
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to (e.message ?: "Invalid data")))
        }
}
