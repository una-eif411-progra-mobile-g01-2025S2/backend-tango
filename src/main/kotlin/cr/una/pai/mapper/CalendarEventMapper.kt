package cr.una.pai.mapper

import cr.una.pai.domain.CalendarEvent
import cr.una.pai.dto.CalendarEventInput
import cr.una.pai.dto.CalendarEventResult
import org.mapstruct.*
import java.util.*

@Mapper(config = MapperConfig::class)
abstract class CalendarEventMapper {
    @Mappings(
        Mapping(target = "id", ignore = true),
        Mapping(source = "studyBlockId", target = "studyBlock", qualifiedByName = ["refStudyBlock"]),
        Mapping(source = "provider", target = "provider", qualifiedByName = ["toCalendarProvider"]),
        Mapping(source = "externalEventId", target = "externalEventId"),
        Mapping(source = "lastSyncAt", target = "lastSyncAt"),
        Mapping(source = "status", target = "status", qualifiedByName = ["toCalendarEventStatus"])
    )
    abstract fun toEntity(input: CalendarEventInput, @Context ctx: MappingContext): CalendarEvent

    @Mappings(
        Mapping(source = "studyBlock.id", target = "studyBlockId")
    )
    abstract fun toResult(entity: CalendarEvent): CalendarEventResult

    @BeanMapping(ignoreByDefault = true)
    @Mappings(
        Mapping(source = "studyBlockId", target = "studyBlock", qualifiedByName = ["refStudyBlock"]),
        Mapping(source = "provider", target = "provider", qualifiedByName = ["toCalendarProvider"]),
        Mapping(source = "externalEventId", target = "externalEventId"),
        Mapping(source = "lastSyncAt", target = "lastSyncAt"),
        Mapping(source = "status", target = "status", qualifiedByName = ["toCalendarEventStatus"])
    )
    abstract fun update(@MappingTarget target: CalendarEvent, input: CalendarEventInput, @Context ctx: MappingContext)

    @Named("refStudyBlock")
    protected fun mapStudyBlock(id: UUID?, @Context ctx: MappingContext) = ctx.refStudyBlock(id)
}
