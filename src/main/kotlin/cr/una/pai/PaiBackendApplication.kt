package cr.una.pai

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PaiBackendApplication

fun main(args: Array<String>) {
	runApplication<PaiBackendApplication>(*args)
}
