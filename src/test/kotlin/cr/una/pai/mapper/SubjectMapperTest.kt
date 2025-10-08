package cr.una.pai.mapper

import cr.una.pai.domain.*
import cr.una.pai.dto.SubjectInput
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.InvalidDataAccessApiUsageException
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.*

@SpringBootTest
@Transactional
class SubjectMapperTest @Autowired constructor(
    private val subjectMapper: SubjectMapper,
    private val subjectRepository: SubjectRepository,
    private val userRepository: UserRepository,
    private val academicPeriodRepository: AcademicPeriodRepository,
    private val mappingContext: MappingContext
) {
    private fun seedUser(): User = userRepository.save(
        User(email = "student@test.com", password = "Secret123", fullName = " John Doe ")
    )
    private fun seedPeriod(): AcademicPeriod = academicPeriodRepository.save(
        AcademicPeriod(name = "S1 2025", startDate = LocalDate.of(2025,1,1), endDate = LocalDate.of(2025,6,30))
    )

    @Test
    fun `maps input trimming name and code`() {
        val user = seedUser()
        val period = seedPeriod()
        val input = SubjectInput(
            userId = user.id,
            periodId = period.id,
            name = "  Algebra  ",
            code = "  MATH101  ",
            weeklyHours = 4,
            startDate = LocalDate.of(2025,1,10),
            endDate = LocalDate.of(2025,5,30)
        )
        val entity = subjectMapper.toEntity(input, mappingContext)
        val saved = subjectRepository.save(entity)
        val result = subjectMapper.toResult(saved)
        assertThat(saved.name).isEqualTo("Algebra")
        assertThat(saved.code).isEqualTo("MATH101")
        assertThat(result.name).isEqualTo("Algebra")
        assertThat(result.periodId).isEqualTo(period.id)
        assertThat(result.userId).isEqualTo(user.id)
    }

    @Test
    fun `invalid date range on subject throws`() {
        val user = seedUser()
        val period = seedPeriod()
        val bad = SubjectInput(
            userId = user.id,
            periodId = period.id,
            name = "Bad",
            code = "BAD1",
            weeklyHours = 3,
            startDate = LocalDate.of(2025,6,1),
            endDate = LocalDate.of(2025,1,1)
        )
        val entity = subjectMapper.toEntity(bad, mappingContext)
        val ex = assertThrows(InvalidDataAccessApiUsageException::class.java) { subjectRepository.saveAndFlush(entity) }
        assertThat(ex.cause).isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `update ignores nulls`() {
        val user = seedUser()
        val period = seedPeriod()
        val input = SubjectInput(
            userId = user.id,
            periodId = period.id,
            name = "Data Structures",
            code = "CS201",
            weeklyHours = 5,
            startDate = LocalDate.of(2025,2,1),
            endDate = LocalDate.of(2025,5,15)
        )
        val entity = subjectRepository.save(subjectMapper.toEntity(input, mappingContext))
        val patch = SubjectInput(name = null, code = "CS202")
        subjectMapper.update(entity, patch, mappingContext)
        assertThat(entity.name).isEqualTo("Data Structures") // unchanged
        assertThat(entity.code).isEqualTo("CS202") // updated
    }
}
