package cr.una.pai.dto

import java.util.*

data class RoleInput(
    var id: UUID? = null,
    var name: String? = null,
    var description: String? = null,
)

data class RoleCreateInput(
    var name: String,
    var description: String? = null,
)

data class RoleUpdateInput(
    var description: String? = null,
)

data class RoleResult(
    var id: UUID,
    var name: String,
    var description: String?,
    var privileges: List<PrivilegeResult> = emptyList()
)