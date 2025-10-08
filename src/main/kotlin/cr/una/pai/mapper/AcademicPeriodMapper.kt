package cr.una.pai.mapper

import cr.una.pai.domain.AcademicPeriod
import cr.una.pai.dto.AcademicPeriodInput
import cr.una.pai.dto.AcademicPeriodResult
import org.mapstruct.*

@Mapper(config = MapperConfig::class)
abstract class AcademicPeriodMapper {
    @Mappings(
        Mapping(target = "id", ignore = true),
        Mapping(source = "name", target = "name"),
        Mapping(source = "startDate", target = "startDate"),
        Mapping(source = "endDate", target = "endDate")
    )
    abstract fun toEntity(input: AcademicPeriodInput): AcademicPeriod

    abstract fun toResult(entity: AcademicPeriod): AcademicPeriodResult

    @BeanMapping(ignoreByDefault = true)
    @Mappings(
        Mapping(source = "name", target = "name"),
        Mapping(source = "startDate", target = "startDate"),
        Mapping(source = "endDate", target = "endDate")
    )
    abstract fun update(@MappingTarget target: AcademicPeriod, input: AcademicPeriodInput)
}
