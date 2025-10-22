package cr.una.pai.mapper

import cr.una.pai.domain.WeeklyAvailability
import cr.una.pai.dto.WeeklyAvailabilityInput
import cr.una.pai.dto.WeeklyAvailabilityResult
import org.mapstruct.*
import java.util.*
import org.mapstruct.Mapper

@Mapper(componentModel = "spring", config = MapperConfig::class)
abstract class WeeklyAvailabilityMapper {
    @Mappings(
        Mapping(target = "id", ignore = true),
        Mapping(source = "userId", target = "user", qualifiedByName = ["refUser"]),
        Mapping(source = "dayOfWeek", target = "dayOfWeek"),
        Mapping(source = "startTime", target = "startTime"),
        Mapping(source = "endTime", target = "endTime")
    )
    abstract fun toEntity(input: WeeklyAvailabilityInput, @Context ctx: MappingContext): WeeklyAvailability

    @Mappings(
        Mapping(source = "user.id", target = "userId"),
        Mapping(source = "dayOfWeek", target = "dayOfWeek"),
        Mapping(source = "startTime", target = "startTime"),
        Mapping(source = "endTime", target = "endTime")
    )
    abstract fun toResult(entity: WeeklyAvailability): WeeklyAvailabilityResult

    @BeanMapping(ignoreByDefault = true)
    @Mappings(
        Mapping(source = "userId", target = "user", qualifiedByName = ["refUser"]),
        Mapping(source = "dayOfWeek", target = "dayOfWeek"),
        Mapping(source = "startTime", target = "startTime"),
        Mapping(source = "endTime", target = "endTime")
    )
    abstract fun update(@MappingTarget target: WeeklyAvailability, input: WeeklyAvailabilityInput, @Context ctx: MappingContext)

    @AfterMapping
    protected fun validate(@MappingTarget target: WeeklyAvailability) {
        require(target.endTime.isAfter(target.startTime)) { "startTime must be < endTime" }
    }

    @Named("refUser")
    protected fun mapUser(id: UUID?, @Context ctx: MappingContext) = ctx.refUser(id)
}
