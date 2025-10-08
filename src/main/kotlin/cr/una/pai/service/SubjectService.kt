package cr.una.pai.service

import cr.una.pai.domain.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class SubjectService(
    private val subjectRepository: SubjectRepository,
    private val userRepository: UserRepository,
    private val academicPeriodRepository: AcademicPeriodRepository
) {

    fun findAll(): List<Subject> = subjectRepository.findAll()

    fun findById(id: UUID): Optional<Subject> = subjectRepository.findById(id)

    fun findAllByUserId(userId: UUID): List<Subject> =
        subjectRepository.findAllByUserId(userId)

    fun findAllByUserIdAndPeriodId(userId: UUID, periodId: UUID): List<Subject> =
        subjectRepository.findAllByUserIdAndPeriodId(userId, periodId)

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
}
