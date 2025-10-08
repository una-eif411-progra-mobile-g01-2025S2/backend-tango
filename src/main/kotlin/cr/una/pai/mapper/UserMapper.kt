package cr.una.pai.mapper

import cr.una.pai.domain.*
import cr.una.pai.dto.*
import org.mapstruct.*
import java.util.*

@Mapper(config = MapperConfig::class, uses = [TypeConverters::class])
interface UserMapper {

    // ====== Input -> Entity ======
    @Mappings(
        Mapping(target = "id", ignore = true),
        Mapping(source = "roleIds", target = "userRoles", qualifiedByName = ["refRoles"])
    )
    fun toEntity(input: UserInput, @Context ctx: MappingContext): User

    // ====== Update parcial (UserUpdateInput) ======
    @BeanMapping(ignoreByDefault = true)
    @Mappings(
        Mapping(source = "fullName", target = "fullName"),
        Mapping(source = "password", target = "password"),
        Mapping(source = "degree", target = "degree"),
        Mapping(source = "yearOfStudy", target = "yearOfStudy"),
        Mapping(source = "university", target = "university"),
        Mapping(source = "roleIds", target = "userRoles", qualifiedByName = ["refRoles"]),
        Mapping(target = "email", ignore = true)
    )
    fun update(@MappingTarget entity: User, input: UserUpdateInput, @Context ctx: MappingContext)

    // ====== Update parcial (UserInput genérico) ======
    @BeanMapping(ignoreByDefault = true)
    @Mappings(
        Mapping(source = "fullName", target = "fullName"),
        Mapping(source = "password", target = "password"),
        Mapping(source = "degree", target = "degree"),
        Mapping(source = "yearOfStudy", target = "yearOfStudy"),
        Mapping(source = "university", target = "university"),
        Mapping(source = "roleIds", target = "userRoles", qualifiedByName = ["refRoles"]),
        Mapping(target = "email", ignore = true) // email no se cambia vía input genérico
    )
    fun updateFromInput(@MappingTarget entity: User, input: UserInput, @Context ctx: MappingContext)

    // ====== Entity -> Result ======
    @Mappings(
        Mapping(target = "roles", source = "userRoles", qualifiedByName = ["userRolesToRoleResults"]),
        Mapping(target = "roleIds", source = "userRoles", qualifiedByName = ["extractRoleIds"])
    )
    fun toResult(entity: User): UserResult
    fun toResult(entities: List<User>): List<UserResult>

    // ====== Normalización ======
    @AfterMapping
    fun normalize(@MappingTarget entity: User) {
        entity.fullName = entity.fullName.trim()
        entity.email = entity.email.lowercase().trim()
    }

    // ====== Enlace de referencia inversa ======
    @AfterMapping
    fun linkBackReference(@MappingTarget entity: User) {
        entity.userRoles.forEach { it.user = entity }
    }

    // ====== Conversión roleIds -> userRoles ======
    @Named("refRoles")
    fun mapRoles(ids: List<UUID>?, @Context ctx: MappingContext): MutableSet<UserRole> =
        ids?.mapNotNull { rid ->
            val role = ctx.refRole(rid) ?: return@mapNotNull null
            UserRole(
                id = UserRoleId(userId = null, roleId = role.id), // userId se fija al persistir
                user = null, // será asignado por JPA cuando se asocie al user
                role = role
            )
        }?.toMutableSet() ?: mutableSetOf()

    // ====== Extracción userRoles -> roleIds ======
    @Named("extractRoleIds")
    fun extractRoleIds(userRoles: MutableSet<UserRole>?): List<UUID> =
        userRoles?.mapNotNull { it.role?.id } ?: emptyList()
}
