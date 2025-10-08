package cr.una.pai.mapper

import cr.una.pai.domain.AcademicPeriodRepository
import cr.una.pai.dto.AcademicPeriodInput
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.InvalidDataAccessApiUsageException
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@SpringBootTest
@Transactional
class AcademicPeriodMapperTest @Autowired constructor(
    private val mapper: AcademicPeriodMapper,
    private val repo: AcademicPeriodRepository
) {
    @Test
    fun `maps input to entity and to result`() {
        val input = AcademicPeriodInput(
            name = "Q1 2025",
            startDate = LocalDate.of(2025,1,1),
            endDate = LocalDate.of(2025,3,31)
        )
        val entity = mapper.toEntity(input)
        val saved = repo.save(entity)
        val result = mapper.toResult(saved)
        assertThat(result.name).isEqualTo(input.name)
        assertThat(result.startDate).isEqualTo(input.startDate)
        assertThat(result.endDate).isEqualTo(input.endDate)
    }

    @Test
    fun `invalid date range triggers validation on persist`() {
        val bad = AcademicPeriodInput(
            name = "Bad",
            startDate = LocalDate.of(2025,3,31),
            endDate = LocalDate.of(2025,1,1)
        )
        val entity = mapper.toEntity(bad)
        val ex = assertThrows(InvalidDataAccessApiUsageException::class.java) {
            repo.saveAndFlush(entity) // triggers @PrePersist validation
        }
        assertThat(ex.cause).isInstanceOf(IllegalArgumentException::class.java)
    }
}
