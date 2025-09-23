package cr.una.pai.domain

import jakarta.persistence.*
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import java.time.*
import java.util.*

/* ========= Enums ========= */
enum class TaskStatus { PENDING, IN_PROGRESS, COMPLETED }
enum class StudyBlockStatus { PLANNED, IN_PROGRESS, COMPLETED }
enum class CalendarProvider { GOOGLE }
enum class CalendarEventStatus { CREATED, UPDATED, DELETED }

/* ========= Seguridad ========= */

@Entity
@Table(
    name = "app_user",
    indexes = [Index(name = "idx_user_email", columnList = "email", unique = true)]
)
class User(
    @Id @GeneratedValue var id: UUID? = null,
    @Column(nullable = false, unique = true) @NotBlank var email: String,
    @Column(nullable = false) @NotBlank var password: String,
    @Column(nullable = false) @NotBlank var fullName: String,
    var degree: String? = null,
    var yearOfStudy: Int? = null,
    var university: String? = null,

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_role",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "role_id")]
    )
    var roles: MutableSet<Role> = mutableSetOf(),
)

@Entity
@Table(name = "role", indexes = [Index(name = "idx_role_name", columnList = "name", unique = true)])
class Role(
    @Id @GeneratedValue var id: UUID? = null,
    @Column(nullable = false, unique = true) var name: String,
    var description: String? = null,

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "role_privilege",
        joinColumns = [JoinColumn(name = "role_id")],
        inverseJoinColumns = [JoinColumn(name = "privilege_id")]
    )
    var privileges: MutableSet<Privilege> = mutableSetOf(),
)

@Entity
@Table(name = "privilege", indexes = [Index(name = "idx_priv_name", columnList = "name", unique = true)])
class Privilege(
    @Id @GeneratedValue var id: UUID? = null,
    @Column(nullable = false, unique = true) var name: String,
    var description: String? = null,
)

/* ========= Académico ========= */

@Entity
@Table(name = "academic_period")
class AcademicPeriod(
    @Id @GeneratedValue var id: UUID? = null,
    @Column(nullable = false) var name: String,
    @Column(nullable = false) var startDate: LocalDate,
    @Column(nullable = false) var endDate: LocalDate,
) {
    @PrePersist @PreUpdate
    fun validateDates() {
        require(!endDate.isBefore(startDate)) { "endDate must be >= startDate" }
    }
}

@Entity
@Table(
    name = "subject",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_subject_user_period_code",
            columnNames = ["user_id", "period_id", "code"]
        )
    ]
)
class Subject(
    @Id @GeneratedValue var id: UUID? = null,

    @ManyToOne(optional = false) @JoinColumn(name = "user_id")
    var user: User,

    @ManyToOne(optional = false) @JoinColumn(name = "period_id")
    var period: AcademicPeriod,

    @Column(nullable = false) var name: String,
    @Column(nullable = false) var code: String,
    var professor: String? = null,
    var credits: Int? = null,

    @field:Min(1) @field:Max(20)
    @Column(nullable = false)
    var weeklyHours: Int = 4,

    @Column(nullable = false) var startDate: LocalDate,
    @Column(nullable = false) var endDate: LocalDate,
) {
    @PrePersist @PreUpdate
    fun validateDates() {
        require(!endDate.isBefore(startDate)) { "endDate must be >= startDate" }
    }
}

/* ========= Planificación ========= */

@Entity
@Table(name = "task")
class Task(
    @Id @GeneratedValue var id: UUID? = null,

    @ManyToOne(optional = false) @JoinColumn(name = "user_id")
    var user: User,

    @ManyToOne(optional = false) @JoinColumn(name = "subject_id")
    var subject: Subject,

    @Column(nullable = false) var title: String,
    @Column(columnDefinition = "text") var description: String? = null,

    @field:Min(1) @field:Max(5)
    @Column(nullable = false)
    var priority: Int = 3,

    var deadline: LocalDate? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: TaskStatus = TaskStatus.PENDING,
)

@Entity
@Table(name = "study_block")
class StudyBlock(
    @Id @GeneratedValue var id: UUID? = null,

    @ManyToOne(optional = false) @JoinColumn(name = "user_id")
    var user: User,

    @ManyToOne(optional = false) @JoinColumn(name = "subject_id")
    var subject: Subject,

    @ManyToOne(optional = true) @JoinColumn(name = "task_id")
    var task: Task? = null,

    @Column(nullable = false) var startTime: LocalDateTime,
    @Column(nullable = false) var endTime: LocalDateTime,

    @field:Min(1) @field:Max(5)
    @Column(nullable = false)
    var priority: Int = 3,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: StudyBlockStatus = StudyBlockStatus.PLANNED,
) {
    @PrePersist @PreUpdate
    fun validateTimes() {
        require(endTime.isAfter(startTime)) { "startTime must be < endTime" }
    }
}

@Entity
@Table(name = "weekly_availability")
class WeeklyAvailability(
    @Id @GeneratedValue var id: UUID? = null,

    @ManyToOne(optional = false) @JoinColumn(name = "user_id")
    var user: User,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var dayOfWeek: DayOfWeek,

    @Column(nullable = false) var startTime: LocalTime,
    @Column(nullable = false) var endTime: LocalTime,
) {
    @PrePersist @PreUpdate
    fun validateTimes() {
        require(endTime.isAfter(startTime)) { "startTime must be < endTime" }
    }
}

@Entity
@Table(name = "calendar_event")
class CalendarEvent(
    @Id @GeneratedValue var id: UUID? = null,

    @OneToOne(optional = true) @JoinColumn(name = "study_block_id", unique = true)
    var studyBlock: StudyBlock? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var provider: CalendarProvider = CalendarProvider.GOOGLE,

    var externalEventId: String? = null,
    var lastSyncAt: LocalDateTime? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: CalendarEventStatus = CalendarEventStatus.CREATED,
)
