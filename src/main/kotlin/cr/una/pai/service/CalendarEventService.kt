package cr.una.pai.service

import cr.una.pai.domain.*
import cr.una.pai.dto.CalendarEventInput
import cr.una.pai.dto.CalendarEventResult
import cr.una.pai.mapper.CalendarEventMapper
import cr.una.pai.mapper.MappingContext
import cr.una.pai.mapper.mapWith
import cr.una.pai.mapper.toResults
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
@Transactional
class CalendarEventService(
    private val calendarEventRepository: CalendarEventRepository,
    private val studyBlockRepository: StudyBlockRepository,
    private val calendarEventMapper: CalendarEventMapper,
    private val mappingContext: MappingContext
) {

    fun findAll(): List<CalendarEvent> = calendarEventRepository.findAll()

    fun findById(id: UUID): Optional<CalendarEvent> = calendarEventRepository.findById(id)

    fun findByExternalEventId(externalEventId: String): Optional<CalendarEvent> =
        calendarEventRepository.findByExternalEventId(externalEventId)

    fun findByStudyBlockId(studyBlockId: UUID): Optional<CalendarEvent> =
        calendarEventRepository.findByStudyBlockId(studyBlockId)

    fun findAllByProvider(provider: CalendarProvider): List<CalendarEvent> =
        calendarEventRepository.findAllByProvider(provider)

    fun create(calendarEvent: CalendarEvent): CalendarEvent {
        // Validar que el bloque de estudio existe si se proporcionó
        calendarEvent.studyBlock?.let { block ->
            studyBlockRepository.findById(block.id!!)
                .orElseThrow { IllegalArgumentException("Bloque de estudio no encontrado: ${block.id}") }

            // Validar que el bloque no tiene ya un evento de calendario
            val existing = calendarEventRepository.findByStudyBlockId(block.id!!)
            if (existing.isPresent && existing.get().id != calendarEvent.id) {
                throw IllegalArgumentException("El bloque de estudio ya tiene un evento de calendario asociado")
            }
        }

        return calendarEventRepository.save(calendarEvent)
    }

    fun update(id: UUID, calendarEvent: CalendarEvent): CalendarEvent {
        val existing = calendarEventRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Evento de calendario no encontrado: $id") }

        existing.externalEventId = calendarEvent.externalEventId
        existing.lastSyncAt = calendarEvent.lastSyncAt
        existing.status = calendarEvent.status
        existing.provider = calendarEvent.provider

        return calendarEventRepository.save(existing)
    }

    fun syncEvent(id: UUID): CalendarEvent {
        val existing = calendarEventRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Evento de calendario no encontrado: $id") }

        existing.lastSyncAt = LocalDateTime.now()
        existing.status = CalendarEventStatus.UPDATED

        return calendarEventRepository.save(existing)
    }

    fun delete(id: UUID) {
        if (!calendarEventRepository.existsById(id)) {
            throw IllegalArgumentException("Evento de calendario no encontrado: $id")
        }
        calendarEventRepository.deleteById(id)
    }

    // ================= DTO + Mapper =================
    fun create(input: CalendarEventInput): CalendarEventResult {
        // Validar estudio si se provee
        input.studyBlockId?.let { sbId ->
            studyBlockRepository.findById(sbId)
                .orElseThrow { IllegalArgumentException("Bloque de estudio no encontrado: $sbId") }
            calendarEventRepository.findByStudyBlockId(sbId).ifPresent { throw IllegalArgumentException("El bloque ya tiene evento asociado") }
        }
        val entity = calendarEventMapper.toEntity(input, mappingContext)
        return calendarEventMapper.toResult(calendarEventRepository.save(entity))
    }

    fun updateDto(id: UUID, input: CalendarEventInput): CalendarEventResult {
        val entity = calendarEventRepository.findById(id).orElseThrow { IllegalArgumentException("Evento de calendario no encontrado: $id") }
        calendarEventMapper.update(entity, input, mappingContext)
        // Revalidar potencial conflicto de studyBlock
        entity.studyBlock?.id?.let { sbId ->
            calendarEventRepository.findByStudyBlockId(sbId).ifPresent { other ->
                if (other.id != entity.id) throw IllegalArgumentException("El bloque de estudio ya tiene un evento de calendario asociado")
            }
        }
        return calendarEventMapper.toResult(calendarEventRepository.save(entity))
    }

    fun findResultById(id: UUID): CalendarEventResult = calendarEventMapper.toResult(calendarEventRepository.findById(id).orElseThrow { IllegalArgumentException("Evento de calendario no encontrado: $id") })

    fun findAllResults(): List<CalendarEventResult> = calendarEventRepository.findAll().toResults(calendarEventMapper::toResult)

    fun findAllPaged(pageable: Pageable): Page<CalendarEventResult> = calendarEventRepository.findAll(pageable).mapWith(calendarEventMapper::toResult)
}
