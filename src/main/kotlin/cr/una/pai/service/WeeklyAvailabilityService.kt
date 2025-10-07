package cr.una.pai.service

import cr.una.pai.domain.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.DayOfWeek
import java.util.*

@Service
@Transactional
class WeeklyAvailabilityService(
    private val weeklyAvailabilityRepository: WeeklyAvailabilityRepository,
    private val userRepository: UserRepository
) {

    fun findAll(): List<WeeklyAvailability> = weeklyAvailabilityRepository.findAll()

    fun findById(id: UUID): Optional<WeeklyAvailability> = weeklyAvailabilityRepository.findById(id)

    fun findAllByUserId(userId: UUID): List<WeeklyAvailability> =
        weeklyAvailabilityRepository.findAllByUserId(userId)

    fun findAllByUserIdAndDayOfWeek(userId: UUID, dayOfWeek: DayOfWeek): List<WeeklyAvailability> =
        weeklyAvailabilityRepository.findAllByUserIdAndDayOfWeek(userId, dayOfWeek)

    fun create(availability: WeeklyAvailability): WeeklyAvailability {
        // Validar que el usuario existe
        userRepository.findById(availability.user.id!!)
            .orElseThrow { IllegalArgumentException("Usuario no encontrado: ${availability.user.id}") }

        // Validar que no hay solapamiento
        validateNoOverlap(availability)

        return weeklyAvailabilityRepository.save(availability)
    }

    fun update(id: UUID, availability: WeeklyAvailability): WeeklyAvailability {
        val existing = weeklyAvailabilityRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Disponibilidad no encontrada: $id") }

        existing.dayOfWeek = availability.dayOfWeek
        existing.startTime = availability.startTime
        existing.endTime = availability.endTime

        // Validar que no hay solapamiento
        validateNoOverlap(existing)

        return weeklyAvailabilityRepository.save(existing)
    }

    fun delete(id: UUID) {
        if (!weeklyAvailabilityRepository.existsById(id)) {
            throw IllegalArgumentException("Disponibilidad no encontrada: $id")
        }
        weeklyAvailabilityRepository.deleteById(id)
    }

    private fun validateNoOverlap(availability: WeeklyAvailability) {
        val overlapping = weeklyAvailabilityRepository.findAllByUserIdAndDayOfWeek(
            availability.user.id!!,
            availability.dayOfWeek
        ).filter { existing ->
            existing.id != availability.id && // Ignorar el mismo registro en actualizaciones
            !(availability.endTime.isBefore(existing.startTime) || availability.endTime == existing.startTime) &&
            !(availability.startTime.isAfter(existing.endTime) || availability.startTime == existing.endTime)
        }

        if (overlapping.isNotEmpty()) {
            throw IllegalArgumentException("La disponibilidad se solapa con otra existente para el mismo día")
        }
    }
}

