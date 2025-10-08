package cr.una.pai.mapper

import cr.una.pai.domain.Role
import cr.una.pai.dto.RoleInput
import cr.una.pai.dto.RoleResult
import org.mapstruct.*

@Mapper(config = MapperConfig::class, uses = [TypeConverters::class])
interface RoleMapper {
    @Mappings(
        Mapping(target = "id", ignore = true),
        Mapping(source = "name", target = "name"),
        Mapping(source = "description", target = "description"),
        Mapping(target = "rolePrivileges", ignore = true)
    )
    fun toEntity(input: RoleInput): Role

    @Mappings(
        Mapping(target = "privileges", source = "rolePrivileges", qualifiedByName = ["rolePrivilegesToPrivilegeResults"])
    )
    fun toResult(entity: Role): RoleResult

    @BeanMapping(ignoreByDefault = true)
    @Mappings(
        Mapping(source = "name", target = "name"),
        Mapping(source = "description", target = "description")
    )
    fun update(@MappingTarget target: Role, input: RoleInput)
}
