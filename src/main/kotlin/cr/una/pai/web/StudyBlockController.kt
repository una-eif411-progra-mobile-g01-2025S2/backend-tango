package cr.una.pai.web

import cr.una.pai.domain.*
import cr.una.pai.service.StudyBlockService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import java.util.*

@RestController
@RequestMapping("\${api.endpoints.study-blocks}")
@CrossOrigin(origins = ["\${spring.web.cors.allowed-origins}"])
class StudyBlockController(
    private val studyBlockService: StudyBlockService
) {

    @GetMapping
    fun getAllStudyBlocks(): ResponseEntity<List<StudyBlock>> {
        return ResponseEntity.ok(studyBlockService.findAll())
    }

    @GetMapping("/{id}")
    fun getStudyBlockById(@PathVariable id: UUID): ResponseEntity<StudyBlock> {
        return studyBlockService.findById(id)
            .map { ResponseEntity.ok(it) }
            .orElse(ResponseEntity.notFound().build())
    }

    @GetMapping("/user/{userId}")
    fun getStudyBlocksByUserId(@PathVariable userId: UUID): ResponseEntity<List<StudyBlock>> {
        return ResponseEntity.ok(studyBlockService.findAllByUserId(userId))
    }

    @GetMapping("/user/{userId}/status/{status}")
    fun getStudyBlocksByUserIdAndStatus(
        @PathVariable userId: UUID,
        @PathVariable status: StudyBlockStatus
    ): ResponseEntity<List<StudyBlock>> {
        return ResponseEntity.ok(studyBlockService.findAllByUserIdAndStatus(userId, status))
    }

    @GetMapping("/subject/{subjectId}")
    fun getStudyBlocksBySubjectId(@PathVariable subjectId: UUID): ResponseEntity<List<StudyBlock>> {
        return ResponseEntity.ok(studyBlockService.findAllBySubjectId(subjectId))
    }

    @GetMapping("/user/{userId}/range")
    fun getStudyBlocksByUserIdAndDateRange(
        @PathVariable userId: UUID,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) start: LocalDateTime,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) end: LocalDateTime
    ): ResponseEntity<List<StudyBlock>> {
        return ResponseEntity.ok(studyBlockService.findAllByUserIdAndDateRange(userId, start, end))
    }

    @PostMapping
    fun createStudyBlock(@RequestBody studyBlock: StudyBlock): ResponseEntity<Any> {
        return try {
            val created = studyBlockService.create(studyBlock)
            ResponseEntity.status(HttpStatus.CREATED).body(created)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    @PutMapping("/{id}")
    fun updateStudyBlock(
        @PathVariable id: UUID,
        @RequestBody studyBlock: StudyBlock
    ): ResponseEntity<Any> {
        return try {
            val updated = studyBlockService.update(id, studyBlock)
            ResponseEntity.ok(updated)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    @PatchMapping("/{id}/status")
    fun updateStudyBlockStatus(
        @PathVariable id: UUID,
        @RequestBody statusUpdate: Map<String, String>
    ): ResponseEntity<Any> {
        return try {
            val status = StudyBlockStatus.valueOf(statusUpdate["status"] ?: throw IllegalArgumentException("Status is required"))
            val updated = studyBlockService.updateStatus(id, status)
            ResponseEntity.ok(updated)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    @DeleteMapping("/{id}")
    fun deleteStudyBlock(@PathVariable id: UUID): ResponseEntity<Any> {
        return try {
            studyBlockService.delete(id)
            ResponseEntity.noContent().build()
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }
}

