package cr.una.pai.service

import cr.una.pai.domain.*
import cr.una.pai.dto.SubjectInput
import cr.una.pai.dto.SubjectResult
import cr.una.pai.mapper.SubjectMapper
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
class SubjectService(
    private val subjectRepository: SubjectRepository,
    private val userRepository: UserRepository,
    private val academicPeriodRepository: AcademicPeriodRepository,
    private val subjectMapper: SubjectMapper,
    private val mappingContext: MappingContext
) {

    fun findAll(): List<Subject> = subjectRepository.findAll()
    fun findById(id: UUID): Optional<Subject> = subjectRepository.findById(id)
    fun findAllByUserId(userId: UUID): List<Subject> = subjectRepository.findAllByUserId(userId)
    fun findAllByUserIdAndPeriodId(userId: UUID, periodId: UUID): List<Subject> = subjectRepository.findAllByUserIdAndPeriodId(userId, periodId)

    fun create(subject: Subject): Subject {
        // Validar que el usuario existe
        userRepository.findById(subject.user.id!!)
            .orElseThrow { IllegalArgumentException("Usuario no encontrado: ${subject.user.id}") }

        // Validar que el período existe
        academicPeriodRepository.findById(subject.period.id!!)
            .orElseThrow { IllegalArgumentException("Período académico no encontrado: ${subject.period.id}") }

        // Validar que no exista una materia con el mismo código para el usuario y período
        val existing = subjectRepository.findByUserIdAndPeriodIdAndCode(
            subject.user.id!!,
            subject.period.id!!,
            subject.code
        )
        if (existing.isPresent) {
            throw IllegalArgumentException("Ya existe una materia con código ${subject.code} para este período")
        }

        return subjectRepository.save(subject)
    }

    fun update(id: UUID, subject: Subject): Subject {
        val existing = subjectRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Materia no encontrada: $id") }

        existing.name = subject.name
        existing.code = subject.code
        existing.credits = subject.credits
        existing.professor = subject.professor
        existing.weeklyHours = subject.weeklyHours
        existing.startDate = subject.startDate
        existing.endDate = subject.endDate

        return subjectRepository.save(existing)
    }

    fun delete(id: UUID) {
        if (!subjectRepository.existsById(id)) {
            throw IllegalArgumentException("Materia no encontrada: $id")
        }
        subjectRepository.deleteById(id)
    }

    // ================= DTO + Mapper =================
    fun create(input: SubjectInput): SubjectResult {
        if (input.userId == null || input.periodId == null || input.name == null || input.code == null)
            throw IllegalArgumentException("userId, periodId, name y code son obligatorios")
        subjectRepository.findByUserIdAndPeriodIdAndCode(input.userId!!, input.periodId!!, input.code!!)
            .ifPresent { throw IllegalArgumentException("Ya existe una materia con código ${input.code} para este período") }
        val entity = subjectMapper.toEntity(input, mappingContext)
        return subjectMapper.toResult(subjectRepository.save(entity))
    }

    fun updateDto(id: UUID, input: SubjectInput): SubjectResult {
        val entity = subjectRepository.findById(id).orElseThrow { IllegalArgumentException("Materia no encontrada: $id") }
        subjectMapper.update(entity, input, mappingContext)
        return subjectMapper.toResult(subjectRepository.save(entity))
    }

    fun findResultById(id: UUID): SubjectResult = subjectMapper.toResult(subjectRepository.findById(id).orElseThrow { IllegalArgumentException("Materia no encontrada: $id") })

    fun findAllResults(): List<SubjectResult> = subjectRepository.findAll().toResults(subjectMapper::toResult)

    fun findAllPaged(pageable: Pageable): Page<SubjectResult> = subjectRepository.findAll(pageable).mapWith(subjectMapper::toResult)
}
