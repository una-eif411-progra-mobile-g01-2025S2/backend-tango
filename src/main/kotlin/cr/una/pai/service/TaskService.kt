package cr.una.pai.service

import cr.una.pai.domain.*
import cr.una.pai.dto.TaskInput
import cr.una.pai.dto.TaskResult
import cr.una.pai.mapper.TaskMapper
import cr.una.pai.mapper.MappingContext
import cr.una.pai.mapper.mapWith
import cr.una.pai.mapper.toResults
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class TaskService(
    private val taskRepository: TaskRepository,
    private val userRepository: UserRepository,
    private val subjectRepository: SubjectRepository,
    private val taskMapper: TaskMapper,
    private val mappingContext: MappingContext
) {

    fun findAll(): List<Task> = taskRepository.findAll()
    fun findById(id: UUID): Optional<Task> = taskRepository.findById(id)
    fun findAllByUserId(userId: UUID): List<Task> = taskRepository.findAllByUserId(userId)
    fun findAllByUserIdAndStatus(userId: UUID, status: TaskStatus): List<Task> = taskRepository.findAllByUserIdAndStatus(userId, status)
    fun findAllBySubjectId(subjectId: UUID): List<Task> = taskRepository.findAllBySubjectId(subjectId)
    fun findAllByUserIdAndSubjectId(userId: UUID, subjectId: UUID): List<Task> = taskRepository.findAllByUserIdAndSubjectId(userId, subjectId)
    fun findAllByUserIdWithSubject(userId: UUID): List<Task> = taskRepository.findAllByUserIdWithSubject(userId)

    fun create(task: Task): Task {
        userRepository.findById(task.user.id!!).orElseThrow { IllegalArgumentException("Usuario no encontrado: ${task.user.id}") }
        subjectRepository.findById(task.subject.id!!).orElseThrow { IllegalArgumentException("Materia no encontrada: ${task.subject.id}") }
        return taskRepository.save(task)
    }

    fun update(id: UUID, task: Task): Task {
        val existing = taskRepository.findById(id).orElseThrow { IllegalArgumentException("Tarea no encontrada: $id") }
        existing.title = task.title
        existing.description = task.description
        existing.priority = task.priority
        existing.deadline = task.deadline
        existing.status = task.status
        return taskRepository.save(existing)
    }

    fun updateStatus(id: UUID, status: TaskStatus): Task {
        val existing = taskRepository.findById(id).orElseThrow { IllegalArgumentException("Tarea no encontrada: $id") }
        existing.status = status
        return taskRepository.save(existing)
    }

    fun delete(id: UUID) {
        if (!taskRepository.existsById(id)) throw IllegalArgumentException("Tarea no encontrada: $id")
        taskRepository.deleteById(id)
    }

    // ================= DTO + Mapper =================
    fun create(input: TaskInput): TaskResult {
        if (input.userId == null || input.subjectId == null || input.title == null || input.priority == null)
            throw IllegalArgumentException("userId, subjectId, title y priority son obligatorios")
        // Validar existencia de user y subject
        userRepository.findById(input.userId!!).orElseThrow { IllegalArgumentException("Usuario no encontrado: ${input.userId}") }
        subjectRepository.findById(input.subjectId!!).orElseThrow { IllegalArgumentException("Materia no encontrada: ${input.subjectId}") }
        val entity = taskMapper.toEntity(input, mappingContext)
        return taskMapper.toResult(taskRepository.save(entity))
    }

    fun updateDto(id: UUID, input: TaskInput): TaskResult {
        val entity = taskRepository.findById(id).orElseThrow { IllegalArgumentException("Tarea no encontrada: $id") }
        taskMapper.update(entity, input, mappingContext)
        return taskMapper.toResult(taskRepository.save(entity))
    }

    fun findResultById(id: UUID): TaskResult = taskMapper.toResult(taskRepository.findById(id).orElseThrow { IllegalArgumentException("Tarea no encontrada: $id") })

    fun findAllResults(): List<TaskResult> = taskRepository.findAll().toResults(taskMapper::toResult)

    fun findAllPaged(pageable: Pageable): Page<TaskResult> = taskRepository.findAll(pageable).mapWith(taskMapper::toResult)
}
