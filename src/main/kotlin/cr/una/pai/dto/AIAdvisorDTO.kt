package cr.una.pai.dto

import java.util.*

data class AIAdvisorRequest(
    val userId: UUID,
    val customMessage: String? = null
)

data class AIAdvisorResponse(
    val advice: String,
    val timestamp: String = java.time.LocalDateTime.now().toString()
)

data class AIContextData(
    val pendingTasks: List<TaskContextInfo>,
    val completedTasksCount: Int,
    val upcomingDeadlines: List<TaskContextInfo>,
    val customMessage: String? = null
)

data class TaskContextInfo(
    val title: String,
    val deadline: String?,
    val priority: Int,
    val subject: String?,
    val status: String
)

