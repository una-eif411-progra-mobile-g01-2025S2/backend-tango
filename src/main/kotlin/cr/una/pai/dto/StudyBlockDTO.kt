package cr.una.pai.dto

import java.time.LocalDateTime
import java.util.*
import cr.una.pai.domain.StudyBlockStatus

data class StudyBlockInput(
    var id: UUID? = null,
    var startTime: LocalDateTime? = null,
    var endTime: LocalDateTime? = null,
    var priority: Int? = null,
    var status: StudyBlockStatus? = null,
    var userId: UUID? = null,
    var subjectId: UUID? = null,
    var taskId: UUID? = null
)

data class StudyBlockCreateInput(
    var startTime: LocalDateTime,
    var endTime: LocalDateTime,
    var priority: Int,
    var status: StudyBlockStatus,
    var userId: UUID,
    var subjectId: UUID,
    var taskId: UUID? = null
)

data class StudyBlockUpdateInput(
    var startTime: LocalDateTime? = null,
    var endTime: LocalDateTime? = null,
    var priority: Int? = null,
    var status: StudyBlockStatus? = null
)

data class StudyBlockResult(
    var id: UUID,
    var startTime: LocalDateTime,
    var endTime: LocalDateTime,
    var priority: Int,
    var status: StudyBlockStatus,
    var userId: UUID,
    var subjectId: UUID,
    var taskId: UUID?
)