package cr.una.pai

import cr.una.pai.domain.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.jdbc.Sql

/**
 * Smoke test del Data Layer, inspirado en el ejemplo del profesor:
 * Carga datos de prueba vía scripts SQL (clean + import-*.sql)
 * Verifica que los repositorios JPA funcionan y que las relaciones básicas están operativas
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(
    scripts = [
        "classpath:sql/clean.sql",
        "classpath:sql/import-privileges.sql",
        "classpath:sql/import-roles.sql",
        "classpath:sql/import-role-privilege.sql",
        "classpath:sql/import-users.sql",
        "classpath:sql/import-user-role.sql",
        "classpath:sql/import-periods.sql",
        "classpath:sql/import-subjects.sql",
        "classpath:sql/import-tasks.sql",
        "classpath:sql/import-study-blocks.sql",
        "classpath:sql/import-weekly-availability.sql",
        "classpath:sql/import-calendar-events.sql",
    ]
)
class DataLayerSmokeTest @Autowired constructor(
    private val users: UserRepository,
    private val roles: RoleRepository,
    private val privileges: PrivilegeRepository,
    private val periods: AcademicPeriodRepository,
    private val subjects: SubjectRepository,
    private val tasks: TaskRepository,
    private val blocks: StudyBlockRepository,
    private val availability: WeeklyAvailabilityRepository,
    private val events: CalendarEventRepository,
) {

    @Test
    fun seed_cargado_y_repos_operativos() {
        // Usuarios base
        val anaOpt = users.findByEmail("ana@example.com")
        val luisOpt = users.findByEmail("luis@example.com")
        assertThat(anaOpt).isPresent
        assertThat(luisOpt).isPresent
        val ana = anaOpt.get()

        // Roles & Privilegios
        assertThat(roles.findAll()).isNotEmpty
        assertThat(privileges.findAll()).isNotEmpty

        // Periodos académicos
        val period = periods.findByName("2025-I")
        assertThat(period).isPresent

        // Materias del usuario
        val anaSubjects = subjects.findAllByUserId(ana.id!!)
        assertThat(anaSubjects).isNotEmpty

        // Tareas
        assertThat(tasks.findAll()).isNotEmpty

        // Bloques de estudio
        assertThat(blocks.findAll()).isNotEmpty

        // Disponibilidad semanal
        assertThat(availability.findAll()).isNotEmpty

        // Eventos de calendario
        assertThat(events.findAll()).isNotEmpty
    }
}
