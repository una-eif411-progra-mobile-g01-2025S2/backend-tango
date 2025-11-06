package cr.una.pai.web

import cr.una.pai.domain.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/api/v1/data")
@CrossOrigin(origins = ["\${spring.web.cors.allowed-origins}"])
@Tag(name = "Data", description = "Endpoint para obtener todos los datos del sistema")
class DataController(
    private val userRepository: UserRepository,
    private val academicPeriodRepository: AcademicPeriodRepository,
    private val subjectRepository: SubjectRepository,
    private val taskRepository: TaskRepository
) {

    @Operation(summary = "Obtiene todos los datos del sistema")
    @GetMapping
    fun getAllData(): ResponseEntity<Map<String, Any>> {
        return try {
            val users = userRepository.findAll()
            val periods = academicPeriodRepository.findAll()
            val subjects = subjectRepository.findAll()
            val tasks = taskRepository.findAll()

            val data = mapOf(
                "status" to "success",
                "timestamp" to LocalDate.now().toString(),
                "data" to mapOf(
                    "users" to users.map { user ->
                        mapOf(
                            "id" to user.id.toString(),
                            "email" to user.email,
                            "fullName" to user.fullName,
                            "degree" to (user.degree ?: "Sin especificar"),
                            "university" to (user.university ?: "Sin especificar"),
                            "yearOfStudy" to (user.yearOfStudy ?: 0)
                        )
                    },
                    "periods" to periods.map { period ->
                        mapOf(
                            "id" to period.id.toString(),
                            "name" to period.name,
                            "startDate" to period.startDate.toString(),
                            "endDate" to period.endDate.toString()
                        )
                    },
                    "subjects" to subjects.map { subject ->
                        mapOf(
                            "id" to subject.id.toString(),
                            "name" to subject.name,
                            "code" to subject.code,
                            "credits" to subject.credits,
                            "professor" to (subject.professor ?: "Sin asignar"),
                            "weeklyHours" to subject.weeklyHours,
                            "userId" to subject.user.id.toString(),
                            "periodId" to subject.period.id.toString(),
                            "startDate" to subject.startDate.toString(),
                            "endDate" to subject.endDate.toString()
                        )
                    },
                    "tasks" to tasks.map { task ->
                        mapOf(
                            "id" to task.id.toString(),
                            "title" to task.title,
                            "description" to (task.description ?: ""),
                            "priority" to task.priority,
                            "status" to task.status.name,
                            "deadline" to task.deadline?.toString(),
                            "userId" to task.user.id.toString(),
                            "subjectId" to task.subject?.id?.toString(),
                            "subjectName" to task.subject?.name
                        )
                    }
                ),
                "counts" to mapOf(
                    "users" to users.size,
                    "periods" to periods.size,
                    "subjects" to subjects.size,
                    "tasks" to tasks.size
                ),
                "message" to if (users.isEmpty() || periods.isEmpty()) {
                    "Sistema activo pero sin datos. Use POST /api/v1/utils/init-all-data para crear datos de prueba"
                } else {
                    "Datos cargados exitosamente"
                }
            )

            ResponseEntity.ok(data)
        } catch (e: Exception) {
            e.printStackTrace()
            val errorData = mapOf(
                "status" to "error",
                "error" to (e.message ?: "Error desconocido al obtener datos"),
                "errorType" to e.javaClass.simpleName,
                "timestamp" to LocalDate.now().toString(),
                "suggestion" to "Verifique que la base de datos esté funcionando correctamente y que las tablas existan"
            )
            ResponseEntity.status(500).body(errorData)
        }
    }

    @Operation(summary = "Obtiene solo los conteos de datos")
    @GetMapping("/counts")
    fun getDataCounts(): ResponseEntity<Map<String, Any>> {
        return try {
            val counts = mapOf(
                "status" to "success",
                "timestamp" to LocalDate.now().toString(),
                "counts" to mapOf(
                    "users" to userRepository.count(),
                    "periods" to academicPeriodRepository.count(),
                    "subjects" to subjectRepository.count(),
                    "tasks" to taskRepository.count()
                )
            )
            ResponseEntity.ok(counts)
        } catch (e: Exception) {
            ResponseEntity.status(500).body(mapOf(
                "status" to "error",
                "error" to (e.message ?: "Error al obtener conteos")
            ))
        }
    }
}

