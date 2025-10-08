package cr.una.pai.service

import cr.una.pai.domain.*
import cr.una.pai.dto.StudyBlockInput
import cr.una.pai.dto.StudyBlockResult
import cr.una.pai.mapper.StudyBlockMapper
import cr.una.pai.mapper.MappingContext
import cr.una.pai.mapper.mapWith
import cr.una.pai.mapper.toResults
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
@Transactional
class StudyBlockService(
    private val studyBlockRepository: StudyBlockRepository,
    private val userRepository: UserRepository,
    private val subjectRepository: SubjectRepository,
    private val taskRepository: TaskRepository,
    private val studyBlockMapper: StudyBlockMapper,
    private val mappingContext: MappingContext
) {

    fun findAll(): List<StudyBlock> = studyBlockRepository.findAll()
    fun findById(id: UUID): Optional<StudyBlock> = studyBlockRepository.findById(id)
    fun findAllByUserId(userId: UUID): List<StudyBlock> = studyBlockRepository.findAllByUserId(userId)
    fun findAllByUserIdAndStatus(userId: UUID, status: StudyBlockStatus): List<StudyBlock> = studyBlockRepository.findAllByUserIdAndStatus(userId, status)
    fun findAllBySubjectId(subjectId: UUID): List<StudyBlock> = studyBlockRepository.findAllBySubjectId(subjectId)
    fun findAllByUserIdAndDateRange(userId: UUID, start: LocalDateTime, end: LocalDateTime): List<StudyBlock> = studyBlockRepository.findAllByUserIdAndStartTimeBetween(userId, start, end)

    fun create(studyBlock: StudyBlock): StudyBlock {
        // Validar que el usuario existe
        userRepository.findById(studyBlock.user.id!!)
            .orElseThrow { IllegalArgumentException("Usuario no encontrado: ${studyBlock.user.id}") }

        // Validar que la materia existe
        subjectRepository.findById(studyBlock.subject.id!!)
            .orElseThrow { IllegalArgumentException("Materia no encontrada: ${studyBlock.subject.id}") }

        // Validar que la tarea existe si se proporcionó
        studyBlock.task?.let { task ->
            taskRepository.findById(task.id!!)
                .orElseThrow { IllegalArgumentException("Tarea no encontrada: ${task.id}") }
        }

        // Validar que no hay solapamiento de bloques
        validateNoOverlap(studyBlock)

        return studyBlockRepository.save(studyBlock)
    }

    fun update(id: UUID, studyBlock: StudyBlock): StudyBlock {
        val existing = studyBlockRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Bloque de estudio no encontrado: $id") }

        existing.startTime = studyBlock.startTime
        existing.endTime = studyBlock.endTime
        existing.priority = studyBlock.priority
        existing.status = studyBlock.status

        // Validar que no hay solapamiento
        validateNoOverlap(existing)

        return studyBlockRepository.save(existing)
    }

    fun updateStatus(id: UUID, status: StudyBlockStatus): StudyBlock {
        val existing = studyBlockRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Bloque de estudio no encontrado: $id") }

        existing.status = status
        return studyBlockRepository.save(existing)
    }

    fun delete(id: UUID) {
        if (!studyBlockRepository.existsById(id)) {
            throw IllegalArgumentException("Bloque de estudio no encontrado: $id")
        }
        studyBlockRepository.deleteById(id)
    }

    private fun validateNoOverlap(studyBlock: StudyBlock) {
        val overlapping = studyBlockRepository.findAllByUserIdAndStartTimeBetween(
            studyBlock.user.id!!,
            studyBlock.startTime.minusHours(24),
            studyBlock.startTime.plusHours(24)
        ).filter { existing ->
            existing.id != studyBlock.id && // Ignorar el mismo bloque en actualizaciones
            !(studyBlock.endTime.isBefore(existing.startTime) || studyBlock.endTime.isEqual(existing.startTime)) &&
            !(studyBlock.startTime.isAfter(existing.endTime) || studyBlock.startTime.isEqual(existing.endTime))
        }

        if (overlapping.isNotEmpty()) {
            throw IllegalArgumentException("El bloque de estudio se solapa con otro bloque existente")
        }
    }

    // ================= DTO + Mapper =================
    fun create(input: StudyBlockInput): StudyBlockResult {
        if (input.userId == null || input.subjectId == null || input.startTime == null || input.endTime == null || input.priority == null)
            throw IllegalArgumentException("userId, subjectId, startTime, endTime y priority son obligatorios")
        userRepository.findById(input.userId!!).orElseThrow { IllegalArgumentException("Usuario no encontrado: ${input.userId}") }
        subjectRepository.findById(input.subjectId!!).orElseThrow { IllegalArgumentException("Materia no encontrada: ${input.subjectId}") }
        input.taskId?.let { taskRepository.findById(it).orElseThrow { IllegalArgumentException("Tarea no encontrada: $it") } }
        val entity = studyBlockMapper.toEntity(input, mappingContext)
        validateNoOverlap(entity)
        return studyBlockMapper.toResult(studyBlockRepository.save(entity))
    }

    fun updateDto(id: UUID, input: StudyBlockInput): StudyBlockResult {
        val entity = studyBlockRepository.findById(id).orElseThrow { IllegalArgumentException("Bloque de estudio no encontrado: $id") }
        studyBlockMapper.update(entity, input, mappingContext)
        validateNoOverlap(entity)
        return studyBlockMapper.toResult(studyBlockRepository.save(entity))
    }

    fun findResultById(id: UUID): StudyBlockResult = studyBlockMapper.toResult(studyBlockRepository.findById(id).orElseThrow { IllegalArgumentException("Bloque de estudio no encontrado: $id") })

    fun findAllResults(): List<StudyBlockResult> = studyBlockRepository.findAll().toResults(studyBlockMapper::toResult)

    fun findAllPaged(pageable: Pageable): Page<StudyBlockResult> = studyBlockRepository.findAll(pageable).mapWith(studyBlockMapper::toResult)
}
