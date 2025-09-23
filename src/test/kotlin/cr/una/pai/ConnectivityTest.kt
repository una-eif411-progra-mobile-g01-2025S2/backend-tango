package cr.una.pai

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.TestPropertySource

@SpringBootTest
@TestPropertySource(
    properties = [
        "spring.datasource.url=jdbc:postgresql://localhost:5432/paiapp",
        "spring.datasource.username=paiapp",
        "spring.datasource.password=12345",
        "spring.jpa.hibernate.ddl-auto=update"
    ]
)
class ConnectivityTest(
    @Autowired val jdbc: JdbcTemplate
) {
    @Test
    fun select1_contraPostgres() {
        val r = jdbc.queryForObject("SELECT 1") { rs, _ -> rs.getInt(1) }
        assertThat(r).isEqualTo(1)
    }
}
