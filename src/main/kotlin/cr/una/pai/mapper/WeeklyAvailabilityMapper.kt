package cr.una.pai.mapper

import cr.una.pai.domain.WeeklyAvailability
import cr.una.pai.dto.WeeklyAvailabilityInput
import cr.una.pai.dto.WeeklyAvailabilityResult
import org.mapstruct.*
import java.util.*

@Mapper(config = MapperConfig::class, uses = [TypeConverters::class])
interface WeeklyAvailabilityMapper {
    @Mappings(
        Mapping(target = "id", ignore = true),
        Mapping(source = "userId", target = "user", qualifiedByName = ["refUser"]),
        Mapping(source = "dayOfWeek", target = "dayOfWeek"),
        Mapping(source = "startTime", target = "startTime"),
        Mapping(source = "endTime", target = "endTime")
    )
    fun toEntity(input: WeeklyAvailabilityInput, @Context ctx: MappingContext): WeeklyAvailability

    @Mappings(
        Mapping(source = "user.id", target = "userId"),
        Mapping(source = "dayOfWeek", target = "dayOfWeek"),
        Mapping(source = "startTime", target = "startTime"),
        Mapping(source = "endTime", target = "endTime")
    )
    fun toResult(entity: WeeklyAvailability): WeeklyAvailabilityResult

    @BeanMapping(ignoreByDefault = true)
    @Mappings(
        Mapping(source = "userId", target = "user", qualifiedByName = ["refUser"]),
        Mapping(source = "dayOfWeek", target = "dayOfWeek"),
        Mapping(source = "startTime", target = "startTime"),
        Mapping(source = "endTime", target = "endTime")
    )
    fun update(@MappingTarget target: WeeklyAvailability, input: WeeklyAvailabilityInput, @Context ctx: MappingContext)

    @AfterMapping
    fun validate(@MappingTarget target: WeeklyAvailability) {
        require(target.endTime.isAfter(target.startTime)) { "startTime must be < endTime" }
    }

    @Named("refUser")
    fun mapUser(id: UUID?, @Context ctx: MappingContext) = ctx.refUser(id)
}
