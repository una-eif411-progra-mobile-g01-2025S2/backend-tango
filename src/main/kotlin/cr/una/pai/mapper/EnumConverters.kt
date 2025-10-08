package cr.una.pai.mapper

import cr.una.pai.domain.*
import org.mapstruct.Named

object EnumConverters {
    @Named("toTaskStatus")
    @JvmStatic
    fun toTaskStatus(value: String?): TaskStatus = value.safeEnum(TaskStatus.PENDING)

    @Named("toStudyBlockStatus")
    @JvmStatic
    fun toStudyBlockStatus(value: String?): StudyBlockStatus = value.safeEnum(StudyBlockStatus.PLANNED)

    @Named("toCalendarEventStatus")
    @JvmStatic
    fun toCalendarEventStatus(value: String?): CalendarEventStatus = value.safeEnum(CalendarEventStatus.CREATED)

    @Named("toCalendarProvider")
    @JvmStatic
    fun toCalendarProvider(value: String?): CalendarProvider = value.safeEnum(CalendarProvider.GOOGLE)

    private inline fun <reified E: Enum<E>> String?.safeEnum(default: E): E =
        try { if (this.isNullOrBlank()) default else java.lang.Enum.valueOf(E::class.java, this.trim().uppercase()) } catch (_: Exception) { default }
}
