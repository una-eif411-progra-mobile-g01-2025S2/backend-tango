package cr.una.pai.dto

import java.time.LocalDate
import java.util.*

data class SubjectInput(
    var id: UUID? = null,
    var name: String? = null,
    var code: String? = null,
    var professor: String? = null,
    var credits: Int? = null,
    var weeklyHours: Int? = null,
    var startDate: LocalDate? = null,
    var endDate: LocalDate? = null,
    var userId: UUID? = null,
    var periodId: UUID? = null
)

data class SubjectCreateInput(
    var name: String,
    var code: String,
    var professor: String? = null,
    var credits: Int? = null,
    var weeklyHours: Int,
    var startDate: LocalDate,
    var endDate: LocalDate,
    var userId: UUID,
    var periodId: UUID
)

data class SubjectUpdateInput(
    var name: String? = null,
    var code: String? = null,
    var professor: String? = null,
    var credits: Int? = null,
    var weeklyHours: Int? = null,
    var startDate: LocalDate? = null,
    var endDate: LocalDate? = null
)

data class SubjectResult(
    var id: UUID,
    var name: String,
    var code: String,
    var professor: String?,
    var credits: Int?,
    var weeklyHours: Int,
    var startDate: LocalDate,
    var endDate: LocalDate,
    var userId: UUID,
    var periodId: UUID
)