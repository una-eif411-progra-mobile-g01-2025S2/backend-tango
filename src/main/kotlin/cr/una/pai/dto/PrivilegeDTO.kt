package cr.una.pai.dto

import java.util.*

data class PrivilegeInput(
    var id: UUID? = null,
    var name: String? = null,
    var description: String? = null,
)

data class PrivilegeCreateInput(
    var name: String,
    var description: String? = null,
)

data class PrivilegeUpdateInput(
    var description: String? = null,
)

data class PrivilegeResult(
    var id: UUID,
    var name: String,
    var description: String?
)
