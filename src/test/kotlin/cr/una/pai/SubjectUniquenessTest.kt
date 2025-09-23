package cr.una.pai

import cr.una.pai.domain.*
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.test.context.jdbc.Sql
import java.time.LocalDate

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(
    scripts = [
        "classpath:sql/clean.sql",
        "classpath:sql/import-users.sql",
        "classpath:sql/import-periods.sql",
        "classpath:sql/import-subjects.sql",
    ]
)
class SubjectUniquenessTest @Autowired constructor(
    val users: UserRepository,
    val periods: AcademicPeriodRepository,
    val subjects: SubjectRepository,
) {

    @Test
    fun falla_por_UK_user_period_code() {
        val ana = users.findByEmail("ana@example.com").get()
        val period = periods.findByName("2025-I").get()

        val duplicado = Subject(
            user = ana,
            period = period,
            name = "Programación 1 DUP",
            code = "EIF-101",
            professor = "X",
            credits = 4,
            weeklyHours = 6,
            startDate = LocalDate.parse("2025-02-10"),
            endDate   = LocalDate.parse("2025-06-30")
        )

        // (user, period, code)
        assertThatThrownBy { subjects.saveAndFlush(duplicado) }
            .isInstanceOf(DataIntegrityViolationException::class.java)
    }
}
