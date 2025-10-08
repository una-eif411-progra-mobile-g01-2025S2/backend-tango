package cr.una.pai.web

import cr.una.pai.domain.StudyBlockStatus
import cr.una.pai.dto.StudyBlockInput
import cr.una.pai.dto.StudyBlockResult
import cr.una.pai.mapper.StudyBlockMapper
import cr.una.pai.service.StudyBlockService
import jakarta.validation.Valid
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import java.util.*

@RestController
@RequestMapping("\${api.endpoints.study-blocks}")
@CrossOrigin(origins = ["\${spring.web.cors.allowed-origins}"])
@Validated
class StudyBlockController(
    private val studyBlockService: StudyBlockService,
    private val studyBlockMapper: StudyBlockMapper
) {

    @GetMapping
    fun getAllStudyBlocks(): ResponseEntity<List<StudyBlockResult>> =
        ResponseEntity.ok(studyBlockService.findAllResults())

    @GetMapping("/{id}")
    fun getStudyBlockById(@PathVariable id: UUID): ResponseEntity<StudyBlockResult> = try {
        ResponseEntity.ok(studyBlockService.findResultById(id))
    } catch (e: IllegalArgumentException) {
        ResponseEntity.notFound().build()
    }

    @GetMapping("/user/{userId}")
    fun getStudyBlocksByUserId(@PathVariable userId: UUID): ResponseEntity<List<StudyBlockResult>> =
        ResponseEntity.ok(studyBlockService.findAllByUserId(userId).map(studyBlockMapper::toResult))

    @GetMapping("/user/{userId}/status/{status}")
    fun getStudyBlocksByUserIdAndStatus(
        @PathVariable userId: UUID,
        @PathVariable status: StudyBlockStatus
    ): ResponseEntity<List<StudyBlockResult>> =
        ResponseEntity.ok(studyBlockService.findAllByUserIdAndStatus(userId, status).map(studyBlockMapper::toResult))

    @GetMapping("/subject/{subjectId}")
    fun getStudyBlocksBySubjectId(@PathVariable subjectId: UUID): ResponseEntity<List<StudyBlockResult>> =
        ResponseEntity.ok(studyBlockService.findAllBySubjectId(subjectId).map(studyBlockMapper::toResult))

    @GetMapping("/user/{userId}/range")
    fun getStudyBlocksByUserIdAndDateRange(
        @PathVariable userId: UUID,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) start: LocalDateTime,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) end: LocalDateTime
    ): ResponseEntity<List<StudyBlockResult>> =
        ResponseEntity.ok(studyBlockService.findAllByUserIdAndDateRange(userId, start, end).map(studyBlockMapper::toResult))

    @PostMapping
    fun createStudyBlock(@Valid @RequestBody input: StudyBlockInput): ResponseEntity<Any> = try {
        val created = studyBlockService.create(input)
        ResponseEntity.status(HttpStatus.CREATED).body(created)
    } catch (e: IllegalArgumentException) {
        ResponseEntity.badRequest().body(mapOf("error" to (e.message ?: "Invalid data")))
    }

    @PutMapping("/{id}")
    fun updateStudyBlock(
        @PathVariable id: UUID,
        @Valid @RequestBody input: StudyBlockInput
    ): ResponseEntity<Any> = try {
        val updated = studyBlockService.updateDto(id, input)
        ResponseEntity.ok(updated)
    } catch (e: IllegalArgumentException) {
        ResponseEntity.badRequest().body(mapOf("error" to (e.message ?: "Invalid data")))
    }

    @PatchMapping("/{id}/status")
    fun updateStudyBlockStatus(
        @PathVariable id: UUID,
        @RequestBody statusUpdate: Map<String, String>
    ): ResponseEntity<Any> = try {
        val status = StudyBlockStatus.valueOf(statusUpdate["status"] ?: throw IllegalArgumentException("status requerido"))
        val updated = studyBlockMapper.toResult(studyBlockService.updateStatus(id, status))
        ResponseEntity.ok(updated)
    } catch (e: IllegalArgumentException) {
        ResponseEntity.badRequest().body(mapOf("error" to (e.message ?: "Invalid data")))
    }

    @DeleteMapping("/{id}")
    fun deleteStudyBlock(@PathVariable id: UUID): ResponseEntity<Any> = try {
        studyBlockService.delete(id)
        ResponseEntity.noContent().build()
    } catch (e: IllegalArgumentException) {
        ResponseEntity.badRequest().body(mapOf("error" to (e.message ?: "Invalid data")))
    }
}
