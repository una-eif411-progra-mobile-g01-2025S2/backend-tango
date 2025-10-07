package cr.una.pai.web

import cr.una.pai.domain.*
import cr.una.pai.service.TaskService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("\${api.endpoints.tasks}")
@CrossOrigin(origins = ["\${spring.web.cors.allowed-origins}"])
class TaskController(
    private val taskService: TaskService
) {

    @GetMapping
    fun getAllTasks(): ResponseEntity<List<Task>> {
        return ResponseEntity.ok(taskService.findAll())
    }

    @GetMapping("/{id}")
    fun getTaskById(@PathVariable id: UUID): ResponseEntity<Task> {
        return taskService.findById(id)
            .map { ResponseEntity.ok(it) }
            .orElse(ResponseEntity.notFound().build())
    }

    @GetMapping("/user/{userId}")
    fun getTasksByUserId(@PathVariable userId: UUID): ResponseEntity<List<Task>> {
        return ResponseEntity.ok(taskService.findAllByUserId(userId))
    }

    @GetMapping("/user/{userId}/status/{status}")
    fun getTasksByUserIdAndStatus(
        @PathVariable userId: UUID,
        @PathVariable status: TaskStatus
    ): ResponseEntity<List<Task>> {
        return ResponseEntity.ok(taskService.findAllByUserIdAndStatus(userId, status))
    }

    @GetMapping("/subject/{subjectId}")
    fun getTasksBySubjectId(@PathVariable subjectId: UUID): ResponseEntity<List<Task>> {
        return ResponseEntity.ok(taskService.findAllBySubjectId(subjectId))
    }

    @GetMapping("/user/{userId}/subject/{subjectId}")
    fun getTasksByUserIdAndSubjectId(
        @PathVariable userId: UUID,
        @PathVariable subjectId: UUID
    ): ResponseEntity<List<Task>> {
        return ResponseEntity.ok(taskService.findAllByUserIdAndSubjectId(userId, subjectId))
    }

    @PostMapping
    fun createTask(@RequestBody task: Task): ResponseEntity<Any> {
        return try {
            val created = taskService.create(task)
            ResponseEntity.status(HttpStatus.CREATED).body(created)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    @PutMapping("/{id}")
    fun updateTask(
        @PathVariable id: UUID,
        @RequestBody task: Task
    ): ResponseEntity<Any> {
        return try {
            val updated = taskService.update(id, task)
            ResponseEntity.ok(updated)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    @PatchMapping("/{id}/status")
    fun updateTaskStatus(
        @PathVariable id: UUID,
        @RequestBody statusUpdate: Map<String, String>
    ): ResponseEntity<Any> {
        return try {
            val status = TaskStatus.valueOf(statusUpdate["status"] ?: throw IllegalArgumentException("Status is required"))
            val updated = taskService.updateStatus(id, status)
            ResponseEntity.ok(updated)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    @DeleteMapping("/{id}")
    fun deleteTask(@PathVariable id: UUID): ResponseEntity<Any> {
        return try {
            taskService.delete(id)
            ResponseEntity.noContent().build()
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }
}

