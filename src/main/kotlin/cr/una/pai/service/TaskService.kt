package cr.una.pai.service

import cr.una.pai.domain.*
import cr.una.pai.dto.TaskInput
import cr.una.pai.dto.TaskResult
import cr.una.pai.mapper.TaskMapper
import cr.una.pai.mapper.MappingContext
import cr.una.pai.mapper.mapWith
import cr.una.pai.mapper.toResults
import org.slf4j.LoggerFactory
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
    private val logger = LoggerFactory.getLogger(TaskService::class.java)

    fun findAll(): List<Task> = taskRepository.findAll()

    fun findById(id: UUID): Optional<Task> = taskRepository.findById(id)

    fun findAllByUserId(userId: UUID): List<Task> {
        logger.info("========== SERVICE: Buscando tareas del usuario $userId ==========")
        val tasks = taskRepository.findAllByUserId(userId)
        logger.info("Total de tareas encontradas: ${tasks.size}")
        tasks.forEach { task ->
            logger.info("  - Task en BD: ID=${task.id}, Título=${task.title}, Status=${task.status}")
            logger.info("    Usuario: ${task.user.email} (${task.user.id})")
            logger.info("    Materia: ${task.subject?.name ?: "NULL"} (${task.subject?.id})")
        }
        logger.info("=================================================================")
        return tasks
    }

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
        logger.info("========== INICIO CREACIÓN DE TAREA ==========")
        logger.info("Input recibido: $input")
        logger.info("userId: ${input.userId}")
        logger.info("subjectId: ${input.subjectId}")
        logger.info("title: ${input.title}")
        logger.info("priority: ${input.priority}")

        if (input.userId == null || input.subjectId == null || input.title == null || input.priority == null) {
            logger.error("Faltan campos obligatorios")
            throw IllegalArgumentException("userId, subjectId, title y priority son obligatorios")
        }

        // Validar existencia de usuario
        logger.info("Validando usuario con ID: ${input.userId}")
        val userExists = userRepository.existsById(input.userId!!)
        logger.info("¿Usuario existe? $userExists")
        if (!userExists) {
            logger.error("Usuario no encontrado: ${input.userId}")
            throw IllegalArgumentException("Usuario no encontrado: ${input.userId}")
        }
        val user = userRepository.findById(input.userId!!).get()
        logger.info("Usuario encontrado: ${user.email}")

        // Validar existencia de materia CON LOGS DETALLADOS
        logger.info("========== VALIDANDO MATERIA ==========")
        logger.info("Buscando materia con ID: ${input.subjectId}")

        // Primero buscar la materia en las materias del usuario directamente
        val userSubjects = subjectRepository.findAllByUserId(input.userId!!)
        logger.info("Total de materias del usuario: ${userSubjects.size}")

        val subjectForUser = userSubjects.find { it.id == input.subjectId }

        if (subjectForUser == null) {
            // La materia no pertenece al usuario o no existe
            logger.error("MATERIA NO ENCONTRADA O NO PERTENECE AL USUARIO: ${input.subjectId}")
            logger.error("Materias disponibles del usuario ${input.userId}:")
            userSubjects.forEach { subject ->
                logger.error("  - ID=${subject.id}, Nombre=${subject.name}, Código=${subject.code}, Período=${subject.period.name}")
            }
            logger.error("========================================")
            throw IllegalArgumentException("La materia con ID ${input.subjectId} no pertenece al usuario o no existe. El usuario tiene ${userSubjects.size} materias disponibles.")
        }

        logger.info("✓ Materia encontrada y verificada: ${subjectForUser.name} (${subjectForUser.code})")
        logger.info("✓ Materia pertenece al usuario: ${subjectForUser.user.email}")
        logger.info("✓ Período: ${subjectForUser.period.name}")

        logger.info("Validaciones completadas. Creando tarea...")
        val entity = taskMapper.toEntity(input, mappingContext)
        val saved = taskRepository.save(entity)
        logger.info("Tarea creada exitosamente con ID: ${saved.id}")
        logger.info("========== FIN CREACIÓN DE TAREA ==========")

        return taskMapper.toResult(saved)
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
