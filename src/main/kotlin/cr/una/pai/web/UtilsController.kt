package cr.una.pai.web

import cr.una.pai.domain.*
import cr.una.pai.dto.AcademicPeriodInput
import cr.una.pai.service.AcademicPeriodService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/api/v1/utils")
@CrossOrigin(origins = ["\${spring.web.cors.allowed-origins}"])
@Tag(name = "Utilidades", description = "Endpoints de utilidad para desarrollo")
class UtilsController(
    private val academicPeriodService: AcademicPeriodService,
    private val userRepository: UserRepository,
    private val academicPeriodRepository: AcademicPeriodRepository,
    private val subjectRepository: SubjectRepository,
    private val taskRepository: TaskRepository,
    private val passwordEncoder: PasswordEncoder
) {

    @Operation(summary = "Inicializa TODOS los datos de prueba (usuarios, períodos, materias, tareas)")
    @PostMapping("/init-all-data")
    fun initializeAllTestData(): ResponseEntity<Map<String, Any>> {
        val results = mutableMapOf<String, Any>()

        try {
            // 1. Crear usuarios de prueba
            val usersCreated = createTestUsers()
            results["users"] = usersCreated

            // 2. Crear períodos
            val periodsCreated = createTestPeriods()
            results["periods"] = periodsCreated

            // 3. Crear materias (si hay usuarios y períodos)
            val subjectsCreated = createTestSubjects()
            results["subjects"] = subjectsCreated

            // 4. Crear tareas (si hay materias)
            val tasksCreated = createTestTasks()
            results["tasks"] = tasksCreated

            results["status"] = "success"
            results["message"] = "Datos de prueba creados exitosamente"

            return ResponseEntity.ok(results)
        } catch (e: Exception) {
            results["status"] = "error"
            results["error"] = e.message ?: "Error desconocido"
            return ResponseEntity.status(500).body(results)
        }
    }

    @Operation(summary = "Crea solo usuarios de prueba")
    @PostMapping("/init-users")
    fun initializeUsers(): ResponseEntity<Map<String, Any>> {
        val usersCreated = createTestUsers()
        return ResponseEntity.ok(mapOf(
            "status" to "success",
            "users" to usersCreated
        ))
    }

    @Operation(summary = "Inicializa períodos académicos de prueba si no existen")
    @PostMapping("/init-periods")
    fun initializePeriods(): ResponseEntity<Map<String, Any>> {
        val existingPeriods = academicPeriodService.findAll()

        if (existingPeriods.isNotEmpty()) {
            return ResponseEntity.ok(mapOf(
                "message" to "Los períodos ya existen. No se crearon nuevos.",
                "existing" to existingPeriods.size
            ))
        }

        val periodsCreated = createTestPeriods()

        return ResponseEntity.ok(mapOf(
            "message" to "Períodos académicos inicializados correctamente",
            "created" to periodsCreated.size,
            "periods" to periodsCreated
        ))
    }

    @Operation(summary = "Verifica el estado del sistema y datos existentes")
    @GetMapping("/status")
    fun systemStatus(): ResponseEntity<Map<String, Any>> {
        val usersCount = userRepository.count()
        val periodsCount = academicPeriodRepository.count()
        val subjectsCount = subjectRepository.count()
        val tasksCount = taskRepository.count()

        val suggestions = mutableListOf<String>()

        if (usersCount == 0L) {
            suggestions.add("No hay usuarios. Ejecuta POST /api/v1/utils/init-users")
        }

        if (periodsCount == 0L) {
            suggestions.add("No hay períodos. Ejecuta POST /api/v1/utils/init-periods")
        }

        if (usersCount > 0 && periodsCount > 0 && subjectsCount == 0L) {
            suggestions.add("Hay usuarios y períodos pero no hay materias. Ejecuta POST /api/v1/utils/init-all-data")
        }

        return ResponseEntity.ok(mapOf(
            "status" to "OK",
            "database" to "PostgreSQL",
            "counts" to mapOf(
                "users" to usersCount,
                "periods" to periodsCount,
                "subjects" to subjectsCount,
                "tasks" to tasksCount
            ),
            "suggestions" to suggestions,
            "message" to if (suggestions.isEmpty())
                "Sistema funcionando correctamente con datos"
                else
                "Sistema funcionando pero necesita datos de prueba"
        ))
    }

    // ====== MÉTODOS PRIVADOS ======

    private fun createTestUsers(): List<Map<String, Any>> {
        val users = listOf(
            Triple("admin@test.com", "Admin Usuario", "Ingeniería en Sistemas"),
            Triple("estudiante1@test.com", "María García", "Ingeniería en Computación"),
            Triple("estudiante2@test.com", "Carlos Rodríguez", "Ingeniería de Software"),
            Triple("test@test.com", "Usuario Test", "Ingeniería en Sistemas")
        )

        val created = mutableListOf<Map<String, Any>>()

        users.forEach { (email, fullName, degree) ->
            try {
                // Verificar si el usuario ya existe
                if (userRepository.findByEmail(email).isEmpty) {
                    val user = User(
                        email = email,
                        password = passwordEncoder.encode("password123"),
                        fullName = fullName
                    ).apply {
                        this.degree = degree
                        this.university = "Universidad de Costa Rica"
                        this.yearOfStudy = 3
                    }

                    val saved = userRepository.save(user)
                    created.add(mapOf(
                        "id" to saved.id.toString(),
                        "email" to saved.email,
                        "fullName" to saved.fullName,
                        "password" to "password123" // Para info del usuario
                    ))
                }
            } catch (e: Exception) {
                // Continuar con el siguiente usuario
            }
        }

        return created
    }

    private fun createTestPeriods(): List<Map<String, Any>> {
        val currentYear = LocalDate.now().year

        val periods = listOf(
            AcademicPeriodInput(
                name = "I Ciclo $currentYear",
                startDate = LocalDate.of(currentYear, 2, 1),
                endDate = LocalDate.of(currentYear, 6, 30)
            ),
            AcademicPeriodInput(
                name = "II Ciclo $currentYear",
                startDate = LocalDate.of(currentYear, 8, 1),
                endDate = LocalDate.of(currentYear, 12, 15)
            ),
            AcademicPeriodInput(
                name = "III Ciclo $currentYear",
                startDate = LocalDate.of(currentYear, 1, 1),
                endDate = LocalDate.of(currentYear, 1, 31)
            )
        )

        val created = mutableListOf<Map<String, Any>>()

        periods.forEach { period ->
            try {
                val result = academicPeriodService.create(period)
                created.add(mapOf(
                    "id" to result.id,
                    "name" to result.name,
                    "startDate" to result.startDate,
                    "endDate" to result.endDate
                ))
            } catch (e: Exception) {
                // Si ya existe, continuar
            }
        }

        return created
    }

    private fun createTestSubjects(): List<Map<String, Any>> {
        val users = userRepository.findAll()
        val periods = academicPeriodRepository.findAll()

        if (users.isEmpty() || periods.isEmpty()) {
            return emptyList()
        }

        val firstUser = users.first()
        val firstPeriod = periods.first()

        val subjects = listOf(
            Quad("Programación I", "CI0101", "Dr. Juan Pérez", 4),
            Quad("Cálculo I", "MA0101", "Dra. Ana López", 4),
            Quad("Física General", "FS0101", "Dr. Pedro Martínez", 4),
            Quad("Estructuras de Datos", "CI0102", "Dra. Laura Gómez", 4)
        )

        val created = mutableListOf<Map<String, Any>>()

        subjects.forEach { (name, code, professor, credits) ->
            try {
                // Verificar que no exista
                val existing = subjectRepository.findByUserIdAndPeriodIdAndCode(
                    firstUser.id!!,
                    firstPeriod.id!!,
                    code
                )

                if (existing.isEmpty) {
                    val subject = Subject(
                        user = firstUser,
                        period = firstPeriod,
                        name = name,
                        code = code,
                        professor = professor,
                        credits = credits,
                        weeklyHours = 4,
                        startDate = firstPeriod.startDate,
                        endDate = firstPeriod.endDate
                    )

                    val saved = subjectRepository.save(subject)
                    created.add(mapOf(
                        "id" to saved.id.toString(),
                        "name" to saved.name,
                        "code" to saved.code,
                        "professor" to (saved.professor ?: "Sin profesor")
                    ))
                }
            } catch (e: Exception) {
                // Continuar
            }
        }

        return created
    }

    private fun createTestTasks(): List<Map<String, Any>> {
        val subjects = subjectRepository.findAll()

        if (subjects.isEmpty()) {
            return emptyList()
        }

        val firstSubject = subjects.first()
        val user = firstSubject.user

        val tasks = listOf(
            Triple("Tarea 1 - Algoritmos", "Implementar algoritmo de ordenamiento", 4),
            Triple("Proyecto Final", "Desarrollo de sistema completo", 5),
            Triple("Quiz 1", "Repaso de conceptos básicos", 3),
            Triple("Laboratorio 2", "Práctica de estructuras de datos", 4)
        )

        val created = mutableListOf<Map<String, Any>>()

        tasks.forEach { (title, description, priority) ->
            try {
                val task = Task(
                    user = user,
                    subject = firstSubject,
                    title = title,
                    description = description,
                    priority = priority,
                    deadline = LocalDate.now().plusDays(7),
                    status = TaskStatus.PENDING
                )

                val saved = taskRepository.save(task)
                created.add(mapOf(
                    "id" to saved.id.toString(),
                    "title" to saved.title,
                    "priority" to saved.priority,
                    "deadline" to saved.deadline.toString()
                ))
            } catch (e: Exception) {
                // Continuar
            }
        }

        return created
    }
}

// Clase auxiliar para tuplas de 4 elementos
private data class Quad<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

