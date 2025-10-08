package cr.una.pai.web

import cr.una.pai.dto.SubjectInput
import cr.una.pai.dto.SubjectResult
import cr.una.pai.mapper.SubjectMapper
import cr.una.pai.service.SubjectService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("\${api.endpoints.subjects}")
@CrossOrigin(origins = ["\${spring.web.cors.allowed-origins}"])
@Validated
class SubjectController(
    private val subjectService: SubjectService,
    private val subjectMapper: SubjectMapper
) {

    @GetMapping
    fun getAll(): ResponseEntity<List<SubjectResult>> =
        ResponseEntity.ok(subjectService.findAllResults())

    @GetMapping("/{id}")
    fun getById(@PathVariable id: UUID): ResponseEntity<SubjectResult> = try {
        ResponseEntity.ok(subjectService.findResultById(id))
    } catch (e: IllegalArgumentException) {
        ResponseEntity.notFound().build()
    }

    @GetMapping("/user/{userId}")
    fun getByUser(@PathVariable userId: UUID): ResponseEntity<List<SubjectResult>> =
        ResponseEntity.ok(subjectService.findAllByUserId(userId).map(subjectMapper::toResult))

    @GetMapping("/user/{userId}/period/{periodId}")
    fun getByUserAndPeriod(
        @PathVariable userId: UUID,
        @PathVariable periodId: UUID
    ): ResponseEntity<List<SubjectResult>> =
        ResponseEntity.ok(subjectService.findAllByUserIdAndPeriodId(userId, periodId).map(subjectMapper::toResult))

    @PostMapping
    fun create(@Valid @RequestBody input: SubjectInput): ResponseEntity<Any> =
        try {
            val created = subjectService.create(input)
            ResponseEntity.status(HttpStatus.CREATED).body(created)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to (e.message ?: "Invalid data")))
        }
    @PutMapping("/{id}")
    fun update(
        @PathVariable id: UUID,
        @Valid @RequestBody input: SubjectInput
    ): ResponseEntity<Any> =
        try {
            val updated = subjectService.updateDto(id, input)
            ResponseEntity.ok(updated)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to (e.message ?: "Invalid data")))
        }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: UUID): ResponseEntity<Any> =
        try {
            subjectService.delete(id)
            ResponseEntity.noContent().build()
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to (e.message ?: "Invalid data")))
        }
}

