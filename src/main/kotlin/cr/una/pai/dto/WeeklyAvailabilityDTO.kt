package cr.una.pai.dto

import java.time.DayOfWeek
import java.time.LocalTime
import java.util.*

data class WeeklyAvailabilityInput(
    var id: UUID? = null,
    var dayOfWeek: DayOfWeek? = null,
    var startTime: LocalTime? = null,
    var endTime: LocalTime? = null,
    var userId: UUID? = null
)

data class WeeklyAvailabilityCreateInput(
    var dayOfWeek: DayOfWeek,
    var startTime: LocalTime,
    var endTime: LocalTime,
    var userId: UUID
)

data class WeeklyAvailabilityUpdateInput(
    var dayOfWeek: DayOfWeek? = null,
    var startTime: LocalTime? = null,
    var endTime: LocalTime? = null
)

data class WeeklyAvailabilityResult(
    var id: UUID,
    var dayOfWeek: DayOfWeek,
    var startTime: LocalTime,
    var endTime: LocalTime,
    var userId: UUID
)