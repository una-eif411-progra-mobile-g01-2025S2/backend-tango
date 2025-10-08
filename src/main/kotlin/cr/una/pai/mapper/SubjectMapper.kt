package cr.una.pai.mapper

import cr.una.pai.domain.Subject
import cr.una.pai.dto.SubjectInput
import cr.una.pai.dto.SubjectResult
import org.mapstruct.*
import java.util.*

@Mapper(config = MapperConfig::class)
abstract class SubjectMapper {

    // Crear entidad desde SubjectInput. Se mapean relaciones vía context.
    @Mappings(
        Mapping(target = "id", ignore = true),
        Mapping(source = "userId", target = "user", qualifiedByName = ["refUser"]),
        Mapping(source = "periodId", target = "period", qualifiedByName = ["refAcademicPeriod"]),
        Mapping(source = "name", target = "name"),
        Mapping(source = "code", target = "code"),
        Mapping(source = "professor", target = "professor"),
        Mapping(source = "credits", target = "credits"),
        Mapping(source = "weeklyHours", target = "weeklyHours"),
        Mapping(source = "startDate", target = "startDate"),
        Mapping(source = "endDate", target = "endDate")
    )
    abstract fun toEntity(input: SubjectInput, @Context ctx: MappingContext): Subject

    // Entidad a DTO de resultado
    @Mappings(
        Mapping(source = "user.id", target = "userId"),
        Mapping(source = "period.id", target = "periodId")
    )
    abstract fun toResult(entity: Subject): SubjectResult
    abstract fun toResult(entities: List<Subject>): List<SubjectResult>

    // Actualización parcial: sólo mapear campos presentes; config IGNORE evita sobrescribir con nulls.
    @BeanMapping(ignoreByDefault = true)
    @Mappings(
        Mapping(source = "name", target = "name"),
        Mapping(source = "code", target = "code"),
        Mapping(source = "professor", target = "professor"),
        Mapping(source = "credits", target = "credits"),
        Mapping(source = "weeklyHours", target = "weeklyHours"),
        Mapping(source = "startDate", target = "startDate"),
        Mapping(source = "endDate", target = "endDate"),
        Mapping(source = "userId", target = "user", qualifiedByName = ["refUser"]),
        Mapping(source = "periodId", target = "period", qualifiedByName = ["refAcademicPeriod"])
    )
    abstract fun update(@MappingTarget target: Subject, input: SubjectInput, @Context ctx: MappingContext)

    // ======= Métodos de referencia para IDs (UUID) =======
    @Named("refAcademicPeriod")
    protected fun mapAcademicPeriod(id: UUID?, @Context ctx: MappingContext) = ctx.refAcademicPeriod(id)

    @Named("refUser")
    protected fun mapUser(id: UUID?, @Context ctx: MappingContext) = ctx.refUser(id)

    // Normalización después de crear o actualizar
    @AfterMapping
    protected fun normalize(@MappingTarget target: Subject) {
        target.name = target.name.trim()
        target.code = target.code.trim()
    }
}
