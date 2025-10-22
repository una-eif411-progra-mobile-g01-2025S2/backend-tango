package cr.una.pai.domain

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime
import java.util.*

/* =========================
   Seguridad
   ========================= */
interface UserRepository : JpaRepository<User, UUID> {
    fun findByEmail(email: String): Optional<User>
}

interface RoleRepository : JpaRepository<Role, UUID> {
    fun findByName(name: String): Optional<Role>
}

interface PrivilegeRepository : JpaRepository<Privilege, UUID> {
    fun findByName(name: String): Optional<Privilege>
}

// Nuevos repositorios para las entidades intermedias
interface UserRoleRepository : JpaRepository<UserRole, UserRoleId> {
    fun findAllByUserId(userId: UUID): List<UserRole>
    fun findAllByRoleId(roleId: UUID): List<UserRole>

    @Query("SELECT ur FROM UserRole ur JOIN FETCH ur.role WHERE ur.user.id = :userId")
    fun findUserRolesWithRoleByUserId(@Param("userId") userId: UUID): List<UserRole>
}

interface RolePrivilegeRepository : JpaRepository<RolePrivilege, RolePrivilegeId> {
    fun findAllByRoleId(roleId: UUID): List<RolePrivilege>
    fun findAllByPrivilegeId(privilegeId: UUID): List<RolePrivilege>

    @Query("SELECT rp FROM RolePrivilege rp JOIN FETCH rp.privilege WHERE rp.role.id = :roleId")
    fun findRolePrivilegesWithPrivilegeByRoleId(@Param("roleId") roleId: UUID): List<RolePrivilege>
}

/* =========================
   Académico
   ========================= */
interface AcademicPeriodRepository : JpaRepository<AcademicPeriod, UUID> {
    fun findByName(name: String): Optional<AcademicPeriod>
}

interface SubjectRepository : JpaRepository<Subject, UUID> {
    fun findAllByUserId(userId: UUID): List<Subject>
    fun findByUserIdAndPeriodIdAndCode(userId: UUID, periodId: UUID, code: String): Optional<Subject>
    fun findAllByUserIdAndPeriodId(userId: UUID, periodId: UUID): List<Subject>
}

/* =========================
   Planificación
   ========================= */
interface TaskRepository : JpaRepository<Task, UUID> {
    fun findAllByUserIdAndStatus(userId: UUID, status: TaskStatus): List<Task>
    fun findAllByUserId(userId: UUID): List<Task>
    fun findAllBySubjectId(subjectId: UUID): List<Task>
    fun findAllByUserIdAndSubjectId(userId: UUID, subjectId: UUID): List<Task>

    @Query("SELECT t FROM Task t JOIN FETCH t.subject WHERE t.user.id = :userId")
    fun findAllByUserIdWithSubject(userId: UUID): List<Task>
}

interface StudyBlockRepository : JpaRepository<StudyBlock, UUID> {
    fun findAllByUserIdAndStartTimeBetween(
        userId: UUID,
        start: LocalDateTime,
        end: LocalDateTime
    ): List<StudyBlock>

    fun findAllByUserId(userId: UUID): List<StudyBlock>
    fun findAllBySubjectId(subjectId: UUID): List<StudyBlock>
    fun findAllByUserIdAndStatus(userId: UUID, status: StudyBlockStatus): List<StudyBlock>
}

interface WeeklyAvailabilityRepository : JpaRepository<WeeklyAvailability, UUID> {
    fun findAllByUserIdAndDayOfWeek(userId: UUID, dayOfWeek: java.time.DayOfWeek): List<WeeklyAvailability>
    fun findAllByUserId(userId: UUID): List<WeeklyAvailability>
}

interface CalendarEventRepository : JpaRepository<CalendarEvent, Long> {
    fun findByExternalEventId(externalEventId: String): Optional<CalendarEvent>
    fun findByStudyBlockId(studyBlockId: UUID): Optional<CalendarEvent>
    fun findAllByProvider(provider: CalendarProvider): List<CalendarEvent>
}
