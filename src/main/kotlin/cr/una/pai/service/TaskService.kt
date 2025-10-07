package cr.una.pai.service

import cr.una.pai.domain.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class TaskService(
    private val taskRepository: TaskRepository,
    private val userRepository: UserRepository,
    private val subjectRepository: SubjectRepository
) {

    fun findAll(): List<Task> = taskRepository.findAll()

    fun findById(id: UUID): Optional<Task> = taskRepository.findById(id)

    fun findAllByUserId(userId: UUID): List<Task> = taskRepository.findAllByUserId(userId)

    fun findAllByUserIdAndStatus(userId: UUID, status: TaskStatus): List<Task> =
        taskRepository.findAllByUserIdAndStatus(userId, status)

    fun findAllBySubjectId(subjectId: UUID): List<Task> =
        taskRepository.findAllBySubjectId(subjectId)

    fun findAllByUserIdAndSubjectId(userId: UUID, subjectId: UUID): List<Task> =
        taskRepository.findAllByUserIdAndSubjectId(userId, subjectId)

    fun create(task: Task): Task {
        // Validar que el usuario existe
        userRepository.findById(task.user.id!!)
            .orElseThrow { IllegalArgumentException("Usuario no encontrado: ${task.user.id}") }

        // Validar que la materia existe
        subjectRepository.findById(task.subject.id!!)
            .orElseThrow { IllegalArgumentException("Materia no encontrada: ${task.subject.id}") }

        return taskRepository.save(task)
    }

    fun update(id: UUID, task: Task): Task {
        val existing = taskRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Tarea no encontrada: $id") }

        existing.title = task.title
        existing.description = task.description
        existing.priority = task.priority
        existing.deadline = task.deadline
        existing.status = task.status

        return taskRepository.save(existing)
    }

    fun updateStatus(id: UUID, status: TaskStatus): Task {
        val existing = taskRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Tarea no encontrada: $id") }

        existing.status = status
        return taskRepository.save(existing)
    }

    fun delete(id: UUID) {
        if (!taskRepository.existsById(id)) {
            throw IllegalArgumentException("Tarea no encontrada: $id")
        }
        taskRepository.deleteById(id)
    }
}

