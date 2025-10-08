package cr.una.pai.mapper

import cr.una.pai.domain.AcademicPeriod
import cr.una.pai.dto.AcademicPeriodInput
import cr.una.pai.dto.AcademicPeriodResult
import org.mapstruct.*

@Mapper(config = MapperConfig::class, uses = [TypeConverters::class])
interface AcademicPeriodMapper {
    @Mappings(
        Mapping(target = "id", ignore = true),
        Mapping(source = "name", target = "name"),
        Mapping(source = "startDate", target = "startDate"),
        Mapping(source = "endDate", target = "endDate")
    )
    fun toEntity(input: AcademicPeriodInput): AcademicPeriod

    fun toResult(entity: AcademicPeriod): AcademicPeriodResult

    @BeanMapping(ignoreByDefault = true)
    @Mappings(
        Mapping(source = "name", target = "name"),
        Mapping(source = "startDate", target = "startDate"),
        Mapping(source = "endDate", target = "endDate")
    )
    fun update(@MappingTarget target: AcademicPeriod, input: AcademicPeriodInput)

    @AfterMapping
    fun validate(@MappingTarget target: AcademicPeriod) {
        require(!target.endDate.isBefore(target.startDate)) { "endDate must be >= startDate" }
    }
}
