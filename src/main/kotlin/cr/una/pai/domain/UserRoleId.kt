package cr.una.pai.domain

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.io.Serializable

@Embeddable
data class UserRoleId(
    @Column(name = "user_id") val userId: Long = 0,
    @Column(name = "role_id") val roleId: Long = 0
) : Serializable
