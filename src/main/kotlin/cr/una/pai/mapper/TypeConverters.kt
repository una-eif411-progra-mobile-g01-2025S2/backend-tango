package cr.una.pai.mapper

import org.springframework.stereotype.Component
import java.time.*

@Component
class TypeConverters {
    fun toLocalDate(src: String?) = src?.let { LocalDate.parse(it) }
    fun toLocalDateTime(src: String?) = src?.let { LocalDateTime.parse(it) }
    fun toLocalTime(src: String?) = src?.let { LocalTime.parse(it) }

    fun toIso(src: LocalDate?) = src?.toString()
    fun toIso(src: LocalDateTime?) = src?.toString()
    fun toIso(src: LocalTime?) = src?.toString()
}
