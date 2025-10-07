package cr.una.pai.service

import cr.una.pai.domain.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class AcademicPeriodService(
    private val periodRepository: AcademicPeriodRepository
) {

    fun findAll(): List<AcademicPeriod> = periodRepository.findAll()

    fun findById(id: UUID): Optional<AcademicPeriod> = periodRepository.findById(id)

    fun findByName(name: String): Optional<AcademicPeriod> = periodRepository.findByName(name)

    fun create(period: AcademicPeriod): AcademicPeriod {
        // Validar que no existe otro período con el mismo nombre
        val existing = periodRepository.findByName(period.name)
        if (existing.isPresent) {
            throw IllegalArgumentException("Ya existe un período con el nombre: ${period.name}")
        }

        return periodRepository.save(period)
    }

    fun update(id: UUID, period: AcademicPeriod): AcademicPeriod {
        val existing = periodRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Período académico no encontrado: $id") }

        existing.name = period.name
        existing.startDate = period.startDate
        existing.endDate = period.endDate

        return periodRepository.save(existing)
    }

    fun delete(id: UUID) {
        if (!periodRepository.existsById(id)) {
            throw IllegalArgumentException("Período académico no encontrado: $id")
        }
        periodRepository.deleteById(id)
    }
}

