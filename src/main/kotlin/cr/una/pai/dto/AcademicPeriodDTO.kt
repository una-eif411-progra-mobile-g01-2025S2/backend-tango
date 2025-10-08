package cr.una.pai.dto


import java.time.LocalDate
import java.util.*

data class AcademicPeriodInput(
    var id: UUID? = null,
    var name: String? = null,
    var startDate: LocalDate? = null,
    var endDate: LocalDate? = null,
)

data class AcademicPeriodCreateInput(
    var name: String,
    var startDate: LocalDate,
    var endDate: LocalDate,
)

data class AcademicPeriodUpdateInput(
    var name: String? = null,
    var startDate: LocalDate? = null,
    var endDate: LocalDate? = null,
)

data class AcademicPeriodResult(
    var id: UUID,
    var name: String,
    var startDate: LocalDate,
    var endDate: LocalDate
)