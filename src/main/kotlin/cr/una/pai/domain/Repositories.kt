package cr.una.pai.domain

import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime
import java.util.*

/* =========================
   Seguridad
   ========================= */
interface UserRepository : JpaRepository<User, UUID> {
    fun findByEmail(email: String): Optional<User>
}

interface RoleRepository : JpaRepository<Role, UUID>

interface PrivilegeRepository : JpaRepository<Privilege, UUID>

/* =========================
   Académico
   ========================= */
interface AcademicPeriodRepository : JpaRepository<AcademicPeriod, UUID> {
    fun findByName(name: String): Optional<AcademicPeriod>
}

interface SubjectRepository : JpaRepository<Subject, UUID> {
    fun findAllByUserId(userId: UUID): List<Subject>
    fun findByUserIdAndPeriodIdAndCode(userId: UUID, periodId: UUID, code: String): Optional<Subject>
}

/* =========================
   Planificación
   ========================= */
interface TaskRepository : JpaRepository<Task, UUID> {
    fun findAllByUserIdAndStatus(userId: UUID, status: TaskStatus): List<Task>
}

interface StudyBlockRepository : JpaRepository<StudyBlock, UUID> {
    fun findAllByUserIdAndStartTimeBetween(
        userId: UUID,
        start: LocalDateTime,
        end: LocalDateTime
    ): List<StudyBlock>
}

interface WeeklyAvailabilityRepository : JpaRepository<WeeklyAvailability, UUID> {
    fun findAllByUserIdAndDayOfWeek(userId: UUID, dayOfWeek: java.time.DayOfWeek): List<WeeklyAvailability>
}

interface CalendarEventRepository : JpaRepository<CalendarEvent, UUID> {
    fun findByExternalEventId(externalEventId: String): Optional<CalendarEvent>
}
