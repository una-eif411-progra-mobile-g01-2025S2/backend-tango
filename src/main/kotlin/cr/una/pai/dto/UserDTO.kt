package cr.una.pai.dto

import java.util.UUID

data class UserInput(
    var id: UUID? = null,
    var fullName: String? = null,
    var email: String? = null,
    var password: String? = null,
    var degree: String? = null,
    var yearOfStudy: Int? = null,
    var university: String? = null,
)

data class UserCreateInput(
    var fullName: String,
    var email: String,
    var password: String,
    var degree: String? = null,
    var yearOfStudy: Int? = null,
    var university: String? = null,
)

data class UserUpdateInput(
    var fullName: String? = null,
    var password: String? = null,
    var degree: String? = null,
    var yearOfStudy: Int? = null,
    var university: String? = null,
)

data class UserResult(
    var id: UUID,
    var fullName: String,
    var email: String,
    var degree: String?,
    var yearOfStudy: Int?,
    var university: String?,
    var roles: List<RoleResult> = emptyList()
)

