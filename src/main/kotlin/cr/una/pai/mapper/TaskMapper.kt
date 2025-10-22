package cr.una.pai.mapper

import cr.una.pai.domain.Task
import cr.una.pai.dto.TaskInput
import cr.una.pai.dto.TaskResult
import org.mapstruct.BeanMapping
import org.mapstruct.Context
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Mappings
import org.mapstruct.Named
import org.mapstruct.MappingTarget
import java.util.*

@Mapper(componentModel = "spring", config = MapperConfig::class)
abstract class TaskMapper {
    @Mappings(
        Mapping(target = "id", ignore = true),
        Mapping(source = "userId", target = "user", qualifiedByName = ["refUser"]),
        Mapping(source = "subjectId", target = "subject", qualifiedByName = ["refSubject"]),
        Mapping(source = "title", target = "title"),
        Mapping(source = "description", target = "description"),
        Mapping(source = "priority", target = "priority"),
        Mapping(source = "deadline", target = "deadline"),
        Mapping(source = "status", target = "status", qualifiedByName = ["toTaskStatus"])
    )
    abstract fun toEntity(input: TaskInput, @Context ctx: MappingContext): Task

    @Mappings(
        Mapping(source = "user.id", target = "userId"),
        Mapping(source = "subject.id", target = "subjectId"),
        Mapping(source = "status", target = "status")
    )
    abstract fun toResult(entity: Task): TaskResult

    @BeanMapping(ignoreByDefault = true)
    @Mappings(
        Mapping(source = "userId", target = "user", qualifiedByName = ["refUser"]),
        Mapping(source = "subjectId", target = "subject", qualifiedByName = ["refSubject"]),
        Mapping(source = "title", target = "title"),
        Mapping(source = "description", target = "description"),
        Mapping(source = "priority", target = "priority"),
        Mapping(source = "deadline", target = "deadline"),
        Mapping(source = "status", target = "status", qualifiedByName = ["toTaskStatus"])
    )
    abstract fun update(@MappingTarget target: Task, input: TaskInput, @Context ctx: MappingContext)

    @Named("refUser")
    protected fun mapUser(id: UUID?, @Context ctx: MappingContext) = ctx.refUser(id)

    @Named("refSubject")
    protected fun mapSubject(id: UUID?, @Context ctx: MappingContext) = ctx.refSubject(id)
}
