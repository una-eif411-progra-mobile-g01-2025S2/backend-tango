package cr.una.pai.mapper

import cr.una.pai.domain.StudyBlock
import cr.una.pai.dto.StudyBlockInput
import cr.una.pai.dto.StudyBlockResult
import org.mapstruct.*
import java.util.*
import org.mapstruct.Mapper

@Mapper(componentModel = "spring", config = MapperConfig::class)
abstract class StudyBlockMapper {
    @Mappings(
        Mapping(target = "id", ignore = true),
        Mapping(source = "userId", target = "user", qualifiedByName = ["refUser"]),
        Mapping(source = "subjectId", target = "subject", qualifiedByName = ["refSubject"]),
        Mapping(source = "taskId", target = "task", qualifiedByName = ["refTask"]),
        Mapping(source = "startTime", target = "startTime"),
        Mapping(source = "endTime", target = "endTime"),
        Mapping(source = "priority", target = "priority"),
        Mapping(source = "status", target = "status", qualifiedByName = ["toStudyBlockStatus"])
    )
    abstract fun toEntity(input: StudyBlockInput, @Context ctx: MappingContext): StudyBlock

    @Mappings(
        Mapping(source = "user.id", target = "userId"),
        Mapping(source = "subject.id", target = "subjectId"),
        Mapping(source = "task.id", target = "taskId")
    )
    abstract fun toResult(entity: StudyBlock): StudyBlockResult

    @BeanMapping(ignoreByDefault = true)
    @Mappings(
        Mapping(source = "userId", target = "user", qualifiedByName = ["refUser"]),
        Mapping(source = "subjectId", target = "subject", qualifiedByName = ["refSubject"]),
        Mapping(source = "taskId", target = "task", qualifiedByName = ["refTask"]),
        Mapping(source = "startTime", target = "startTime"),
        Mapping(source = "endTime", target = "endTime"),
        Mapping(source = "priority", target = "priority"),
        Mapping(source = "status", target = "status", qualifiedByName = ["toStudyBlockStatus"])
    )
    abstract fun update(@MappingTarget target: StudyBlock, input: StudyBlockInput, @Context ctx: MappingContext)

    @AfterMapping
    protected fun validate(@MappingTarget target: StudyBlock) {
        require(target.endTime.isAfter(target.startTime)) { "startTime must be < endTime" }
    }

    @Named("refUser")
    protected fun mapUser(id: UUID?, @Context ctx: MappingContext) = ctx.refUser(id)

    @Named("refSubject")
    protected fun mapSubject(id: UUID?, @Context ctx: MappingContext) = ctx.refSubject(id)

    @Named("refTask")
    protected fun mapTask(id: UUID?, @Context ctx: MappingContext) = ctx.refTask(id)
}
