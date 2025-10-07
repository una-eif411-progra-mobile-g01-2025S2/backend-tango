package cr.una.pai.web

import cr.una.pai.domain.*
import cr.una.pai.service.AcademicPeriodService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("\${api.endpoints.periods}")
@CrossOrigin(origins = ["\${spring.web.cors.allowed-origins}"])
class AcademicPeriodController(
    private val periodService: AcademicPeriodService
) {

    @GetMapping
    fun getAllPeriods(): ResponseEntity<List<AcademicPeriod>> {
        return ResponseEntity.ok(periodService.findAll())
    }

    @GetMapping("/{id}")
    fun getPeriodById(@PathVariable id: UUID): ResponseEntity<AcademicPeriod> {
        return periodService.findById(id)
            .map { ResponseEntity.ok(it) }
            .orElse(ResponseEntity.notFound().build())
    }

    @GetMapping("/name/{name}")
    fun getPeriodByName(@PathVariable name: String): ResponseEntity<AcademicPeriod> {
        return periodService.findByName(name)
            .map { ResponseEntity.ok(it) }
            .orElse(ResponseEntity.notFound().build())
    }

    @PostMapping
    fun createPeriod(@RequestBody period: AcademicPeriod): ResponseEntity<Any> {
        return try {
            val created = periodService.create(period)
            ResponseEntity.status(HttpStatus.CREATED).body(created)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    @PutMapping("/{id}")
    fun updatePeriod(
        @PathVariable id: UUID,
        @RequestBody period: AcademicPeriod
    ): ResponseEntity<Any> {
        return try {
            val updated = periodService.update(id, period)
            ResponseEntity.ok(updated)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    @DeleteMapping("/{id}")
    fun deletePeriod(@PathVariable id: UUID): ResponseEntity<Any> {
        return try {
            periodService.delete(id)
            ResponseEntity.noContent().build()
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }
}

