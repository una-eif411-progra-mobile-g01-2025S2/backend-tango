package cr.una.pai.dto

import java.time.LocalDate
import java.util.*
import cr.una.pai.domain.TaskStatus

data class TaskInput(
    var id: UUID? = null,
    var title: String? = null,
    var description: String? = null,
    var priority: Int? = null,
    var deadline: LocalDate? = null,
    var status: String? = null, // changed to String for tolerant mapping
    var userId: UUID? = null,
    var subjectId: UUID? = null
)

data class TaskCreateInput(
    var title: String,
    var description: String? = null,
    var priority: Int,
    var deadline: LocalDate? = null,
    var status: String? = null, // allow omitted -> default PENDING
    var userId: UUID,
    var subjectId: UUID
)

data class TaskUpdateInput(
    var title: String? = null,
    var description: String? = null,
    var priority: Int? = null,
    var deadline: LocalDate? = null,
    var status: String? = null
)

data class TaskResult(
    var id: UUID,
    var title: String,
    var description: String?,
    var priority: Int,
    var deadline: LocalDate?,
    var status: TaskStatus,
    var userId: UUID,
    var subjectId: UUID
)