package cr.una.pai.dto

import java.time.LocalDateTime
import java.util.*
import cr.una.pai.domain.CalendarProvider
import cr.una.pai.domain.CalendarEventStatus

data class CalendarEventInput(
    var id: UUID? = null,
    var studyBlockId: UUID? = null,
    var provider: CalendarProvider? = null,
    var externalEventId: String? = null,
    var lastSyncAt: LocalDateTime? = null,
    var status: CalendarEventStatus? = null
)

data class CalendarEventCreateInput(
    var studyBlockId: UUID? = null,
    var provider: CalendarProvider = CalendarProvider.GOOGLE,
    var externalEventId: String? = null,
    var lastSyncAt: LocalDateTime? = null,
    var status: CalendarEventStatus = CalendarEventStatus.CREATED
)

data class CalendarEventUpdateInput(
    var externalEventId: String? = null,
    var lastSyncAt: LocalDateTime? = null,
    var status: CalendarEventStatus? = null
)

data class CalendarEventResult(
    var id: UUID,
    var studyBlockId: UUID?,
    var provider: CalendarProvider,
    var externalEventId: String?,
    var lastSyncAt: LocalDateTime?,
    var status: CalendarEventStatus
)
