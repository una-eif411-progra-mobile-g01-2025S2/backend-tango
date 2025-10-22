package cr.una.pai.domain

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.io.Serializable

@Embeddable
data class RolePrivilegeId(
    @Column(name = "role_id") val roleId: Long = 0,
    @Column(name = "privilege_id") val privilegeId: Long = 0
) : Serializable
