package cr.una.pai

import cr.una.pai.domain.*
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.jdbc.Sql
import java.time.LocalDateTime

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
class StudyBlockValidationTest @Autowired constructor(
    val users: UserRepository,
    val subjects: SubjectRepository,
    val blocks: StudyBlockRepository,
) {

    @Test
    fun rechaza_endTime_menor_igual_que_startTime_por_callback() {
        val ana = users.findByEmail("ana@example.com").get()
        val subj = subjects.findAllByUserId(ana.id!!).first()

        val invalid = StudyBlock(
            user = ana,
            subject = subj,
            task = null,
            startTime = LocalDateTime.parse("2025-03-10T10:00:00"),
            endTime   = LocalDateTime.parse("2025-03-10T09:59:00"), // <- inválido
            priority = 3,
            status = StudyBlockStatus.PLANNED
        )

        assertThatThrownBy { blocks.saveAndFlush(invalid) }
            .hasRootCauseInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("startTime must be < endTime")
    }
}
