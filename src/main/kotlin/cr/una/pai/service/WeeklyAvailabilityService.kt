package cr.una.pai.service

import cr.una.pai.domain.*
import cr.una.pai.dto.WeeklyAvailabilityInput
import cr.una.pai.dto.WeeklyAvailabilityResult
import cr.una.pai.mapper.WeeklyAvailabilityMapper
import cr.una.pai.mapper.MappingContext
import cr.una.pai.mapper.mapWith
import cr.una.pai.mapper.toResults
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.DayOfWeek
import java.util.*

@Service
@Transactional
class WeeklyAvailabilityService(
    private val weeklyAvailabilityRepository: WeeklyAvailabilityRepository,
    private val userRepository: UserRepository,
    private val weeklyAvailabilityMapper: WeeklyAvailabilityMapper,
    private val mappingContext: MappingContext
) {

    fun findAll(): List<WeeklyAvailability> = weeklyAvailabilityRepository.findAll()
    fun findById(id: UUID): Optional<WeeklyAvailability> = weeklyAvailabilityRepository.findById(id)
    fun findAllByUserId(userId: UUID): List<WeeklyAvailability> = weeklyAvailabilityRepository.findAllByUserId(userId)
    fun findAllByUserIdAndDayOfWeek(userId: UUID, dayOfWeek: DayOfWeek): List<WeeklyAvailability> = weeklyAvailabilityRepository.findAllByUserIdAndDayOfWeek(userId, dayOfWeek)

    fun create(availability: WeeklyAvailability): WeeklyAvailability {
        userRepository.findById(availability.user.id!!).orElseThrow { IllegalArgumentException("Usuario no encontrado: ${availability.user.id}") }
        validateNoOverlap(availability)
        return weeklyAvailabilityRepository.save(availability)
    }

    fun update(id: UUID, availability: WeeklyAvailability): WeeklyAvailability {
        val existing = weeklyAvailabilityRepository.findById(id).orElseThrow { IllegalArgumentException("Disponibilidad no encontrada: $id") }
        existing.dayOfWeek = availability.dayOfWeek
        existing.startTime = availability.startTime
        existing.endTime = availability.endTime
        validateNoOverlap(existing)
        return weeklyAvailabilityRepository.save(existing)
    }

    fun delete(id: UUID) {
        if (!weeklyAvailabilityRepository.existsById(id)) throw IllegalArgumentException("Disponibilidad no encontrada: $id")
        weeklyAvailabilityRepository.deleteById(id)
    }

    private fun validateNoOverlap(availability: WeeklyAvailability) {
        val overlapping = weeklyAvailabilityRepository.findAllByUserIdAndDayOfWeek(
            availability.user.id!!,
            availability.dayOfWeek
        ).filter { existing ->
            existing.id != availability.id &&
            !(availability.endTime.isBefore(existing.startTime) || availability.endTime == existing.startTime) &&
            !(availability.startTime.isAfter(existing.endTime) || availability.startTime == existing.endTime)
        }
        if (overlapping.isNotEmpty()) throw IllegalArgumentException("La disponibilidad se solapa con otra existente para el mismo día")
    }

    // ================= DTO + Mapper =================
    fun create(input: WeeklyAvailabilityInput): WeeklyAvailabilityResult {
        if (input.userId == null || input.dayOfWeek == null || input.startTime == null || input.endTime == null)
            throw IllegalArgumentException("userId, dayOfWeek, startTime y endTime son obligatorios")
        userRepository.findById(input.userId!!).orElseThrow { IllegalArgumentException("Usuario no encontrado: ${input.userId}") }
        val entity = weeklyAvailabilityMapper.toEntity(input, mappingContext)
        validateNoOverlap(entity)
        return weeklyAvailabilityMapper.toResult(weeklyAvailabilityRepository.save(entity))
    }

    fun updateDto(id: UUID, input: WeeklyAvailabilityInput): WeeklyAvailabilityResult {
        val entity = weeklyAvailabilityRepository.findById(id).orElseThrow { IllegalArgumentException("Disponibilidad no encontrada: $id") }
        weeklyAvailabilityMapper.update(entity, input, mappingContext)
        validateNoOverlap(entity)
        return weeklyAvailabilityMapper.toResult(weeklyAvailabilityRepository.save(entity))
    }

    fun findResultById(id: UUID): WeeklyAvailabilityResult = weeklyAvailabilityMapper.toResult(weeklyAvailabilityRepository.findById(id).orElseThrow { IllegalArgumentException("Disponibilidad no encontrada: $id") })

    fun findAllResults(): List<WeeklyAvailabilityResult> = weeklyAvailabilityRepository.findAll().toResults(weeklyAvailabilityMapper::toResult)

    fun findAllPaged(pageable: Pageable): Page<WeeklyAvailabilityResult> = weeklyAvailabilityRepository.findAll(pageable).mapWith(weeklyAvailabilityMapper::toResult)
}
