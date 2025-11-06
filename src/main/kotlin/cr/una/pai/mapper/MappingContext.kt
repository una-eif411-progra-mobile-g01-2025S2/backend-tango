package cr.una.pai.mapper

import cr.una.pai.domain.*
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.stereotype.Component
import java.util.*

@Component
class MappingContext(@PersistenceContext private val em: EntityManager) {
    // Usar find en lugar de getReference para evitar EntityNotFoundException
    private fun <T> ref(type: Class<T>, id: UUID?): T? = id?.let {
        try {
            em.find(type, it)
        } catch (e: Exception) {
            null
        }
    }

    // Métodos usados actualmente por los mappers
    fun user(id: UUID?): User? = ref(User::class.java, id)
    fun role(id: UUID?): Role? = ref(Role::class.java, id)
    fun privilege(id: UUID?): Privilege? = ref(Privilege::class.java, id)
    fun academicPeriod(id: UUID?): AcademicPeriod? = ref(AcademicPeriod::class.java, id)
    fun subject(id: UUID?): Subject? = ref(Subject::class.java, id)
    fun task(id: UUID?): Task? = ref(Task::class.java, id)
    fun studyBlock(id: UUID?): StudyBlock? = ref(StudyBlock::class.java, id)
    fun weeklyAvailability(id: UUID?): WeeklyAvailability? = ref(WeeklyAvailability::class.java, id)
    fun calendarEvent(id: UUID?): CalendarEvent? = ref(CalendarEvent::class.java, id)

    // API alternativa estilo solicitado (refXxx) adaptada a UUID (el dominio usa UUID, no Long)
    fun refUser(id: UUID?) = user(id)
    fun refRole(id: UUID?) = role(id)
    fun refPrivilege(id: UUID?) = privilege(id)
    fun refAcademicPeriod(id: UUID?) = academicPeriod(id)
    fun refSubject(id: UUID?) = subject(id)
    fun refTask(id: UUID?) = task(id)
    fun refStudyBlock(id: UUID?) = studyBlock(id)
    fun refWeeklyAvailability(id: UUID?) = weeklyAvailability(id)
    fun refCalendarEvent(id: UUID?) = calendarEvent(id)
}
