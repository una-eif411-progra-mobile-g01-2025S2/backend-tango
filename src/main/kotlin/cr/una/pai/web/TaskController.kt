package cr.una.pai.web

import cr.una.pai.dto.TaskInput
import cr.una.pai.dto.TaskResult
import cr.una.pai.mapper.TaskMapper
import cr.una.pai.service.TaskService
import cr.una.pai.domain.TaskStatus
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("\${api.endpoints.tasks}")
@CrossOrigin(origins = ["\${spring.web.cors.allowed-origins}"])
@Validated
class TaskController(
    private val taskService: TaskService,
    private val taskMapper: TaskMapper
) {

    @Operation(summary = "Obtiene todas las tareas")
    @GetMapping
    fun getAllTasks(): ResponseEntity<List<TaskResult>> =
        ResponseEntity.ok(taskService.findAllResults())

    @Operation(summary = "Obtiene una tarea por su ID")
    @GetMapping("/{id}")
    fun getTaskById(@PathVariable id: UUID): ResponseEntity<TaskResult> = try {
        ResponseEntity.ok(taskService.findResultById(id))
    } catch (e: IllegalArgumentException) { ResponseEntity.notFound().build() }

    @Operation(summary = "Obtiene todas las tareas de un usuario")
    @GetMapping("/user/{userId}")
    fun getTasksByUserId(@PathVariable userId: UUID): ResponseEntity<List<TaskResult>> =
        ResponseEntity.ok(taskService.findAllByUserId(userId).map(taskMapper::toResult))

    @Operation(summary = "Obtiene las tareas de un usuario filtradas por estado")
    @GetMapping("/user/{userId}/status/{status}")
    fun getTasksByUserIdAndStatus(
        @PathVariable userId: UUID,
        @PathVariable status: TaskStatus
    ): ResponseEntity<List<TaskResult>> =
        ResponseEntity.ok(taskService.findAllByUserIdAndStatus(userId, status).map(taskMapper::toResult))

    @Operation(summary = "Obtiene las tareas de una materia")
    @GetMapping("/subject/{subjectId}")
    fun getTasksBySubjectId(@PathVariable subjectId: UUID): ResponseEntity<List<TaskResult>> =
        ResponseEntity.ok(taskService.findAllBySubjectId(subjectId).map(taskMapper::toResult))

    @Operation(summary = "Obtiene las tareas de un usuario para una materia específica")
    @GetMapping("/user/{userId}/subject/{subjectId}")
    fun getTasksByUserIdAndSubjectId(
        @PathVariable userId: UUID,
        @PathVariable subjectId: UUID
    ): ResponseEntity<List<TaskResult>> =
        ResponseEntity.ok(taskService.findAllByUserIdAndSubjectId(userId, subjectId).map(taskMapper::toResult))

    @Operation(summary = "Crea una nueva tarea")
    @PostMapping
    fun createTask(@Valid @RequestBody input: TaskInput): ResponseEntity<Any> =
        try {
            val created = taskService.create(input)
            ResponseEntity.status(HttpStatus.CREATED).body(created)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to (e.message ?: "Invalid data")))
        }

    @Operation(summary = "Actualiza una tarea por su ID")
    @PutMapping("/{id}")
    fun updateTask(
        @PathVariable id: UUID,
        @Valid @RequestBody input: TaskInput
    ): ResponseEntity<Any> =
        try {
            val updated = taskService.updateDto(id, input)
            ResponseEntity.ok(updated)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to (e.message ?: "Invalid data")))
        }

    @Operation(summary = "Actualiza el estado de una tarea por su ID")
    @PatchMapping("/{id}/status")
    fun updateTaskStatus(
        @PathVariable id: UUID,
        @RequestBody statusUpdate: Map<String, String>
    ): ResponseEntity<Any> =
        try {
            val raw = statusUpdate["status"]
            val status = try { raw?.let { cr.una.pai.domain.TaskStatus.valueOf(it.trim().uppercase()) } ?: cr.una.pai.domain.TaskStatus.PENDING } catch (_: Exception) { cr.una.pai.domain.TaskStatus.PENDING }
            val updated = taskMapper.toResult(taskService.updateStatus(id, status))
            ResponseEntity.ok(updated)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to (e.message ?: "Invalid data")))
        }

    @Operation(summary = "Elimina una tarea por su ID")
    @DeleteMapping("/{id}")
    fun deleteTask(@PathVariable id: UUID): ResponseEntity<Any> =
        try {
            taskService.delete(id)
            ResponseEntity.noContent().build()
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to (e.message ?: "Invalid data")))
        }
}
