package cr.una.pai.service

import cr.una.pai.domain.*
import cr.una.pai.dto.AcademicPeriodInput
import cr.una.pai.dto.AcademicPeriodResult
import cr.una.pai.mapper.AcademicPeriodMapper
import cr.una.pai.mapper.mapWith
import cr.una.pai.mapper.toResults
import cr.una.pai.mapper.MappingContext
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class AcademicPeriodService(
    private val periodRepository: AcademicPeriodRepository,
    private val periodMapper: AcademicPeriodMapper,
    private val mappingContext: MappingContext
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

    // ================= DTO + Mapper =================
    fun create(input: AcademicPeriodInput): AcademicPeriodResult {
        if (input.name == null || input.startDate == null || input.endDate == null)
            throw IllegalArgumentException("name, startDate y endDate son obligatorios")
        periodRepository.findByName(input.name!!).ifPresent { throw IllegalArgumentException("Ya existe un período con el nombre: ${input.name}") }
        val entity = periodMapper.toEntity(input)
        return periodMapper.toResult(periodRepository.save(entity))
    }

    fun updateDto(id: UUID, input: AcademicPeriodInput): AcademicPeriodResult {
        val entity = periodRepository.findById(id).orElseThrow { IllegalArgumentException("Período académico no encontrado: $id") }
        periodMapper.update(entity, input) // copia solo no nulos
        return periodMapper.toResult(periodRepository.save(entity))
    }

    fun findResultById(id: UUID): AcademicPeriodResult = periodMapper.toResult(periodRepository.findById(id).orElseThrow { IllegalArgumentException("Período académico no encontrado: $id") })

    fun findAllResults(): List<AcademicPeriodResult> = periodRepository.findAll().toResults(periodMapper::toResult)

    fun findAllPaged(pageable: Pageable): Page<AcademicPeriodResult> = periodRepository.findAll(pageable).mapWith(periodMapper::toResult)
}
