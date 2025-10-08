package cr.una.pai.mapper

import cr.una.pai.domain.Privilege
import cr.una.pai.dto.PrivilegeInput
import cr.una.pai.dto.PrivilegeResult
import org.mapstruct.*

@Mapper(config = MapperConfig::class, uses = [TypeConverters::class])
interface PrivilegeMapper {
    @Mappings(
        Mapping(source = "name", target = "name"),
        Mapping(source = "description", target = "description"),
        Mapping(target = "rolePrivileges", ignore = true)
    )
    fun toEntity(input: PrivilegeInput): Privilege

    fun toResult(entity: Privilege): PrivilegeResult

    @BeanMapping(ignoreByDefault = true)
    @Mappings(
        Mapping(source = "name", target = "name"),
        Mapping(source = "description", target = "description")
    )
    fun update(@MappingTarget target: Privilege, input: PrivilegeInput)
}
