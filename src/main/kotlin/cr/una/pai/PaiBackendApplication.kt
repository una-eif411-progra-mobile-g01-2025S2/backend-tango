package cr.una.pai

import cr.una.pai.security.JwtProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class PaiBackendApplication

fun main(args: Array<String>) {
	runApplication<PaiBackendApplication>(*args)
}
