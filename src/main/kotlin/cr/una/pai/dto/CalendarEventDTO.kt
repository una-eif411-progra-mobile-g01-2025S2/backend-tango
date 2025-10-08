package cr.una.pai.dto

import java.time.LocalDateTime
import java.util.*
import cr.una.pai.domain.CalendarProvider
import cr.una.pai.domain.CalendarEventStatus

data class CalendarEventInput(
    var id: UUID? = null,
    var studyBlockId: UUID? = null,
    var provider: String? = null, // changed to String
    var externalEventId: String? = null,
    var lastSyncAt: LocalDateTime? = null,
    var status: String? = null // changed to String
)

data class CalendarEventCreateInput(
    var studyBlockId: UUID? = null,
    var provider: String? = null,
    var externalEventId: String? = null,
    var lastSyncAt: LocalDateTime? = null,
    var status: String? = null
)

data class CalendarEventUpdateInput(
    var externalEventId: String? = null,
    var lastSyncAt: LocalDateTime? = null,
    var status: String? = null
)

data class CalendarEventResult(
    var id: UUID,
    var studyBlockId: UUID?,
    var provider: CalendarProvider,
    var externalEventId: String?,
    var lastSyncAt: LocalDateTime?,
    var status: CalendarEventStatus
)
