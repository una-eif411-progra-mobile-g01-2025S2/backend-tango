-- V4 - external_event_id para integraciones externas
ALTER TABLE calendar_event
    ADD COLUMN IF NOT EXISTS external_event_id VARCHAR(255);

CREATE UNIQUE INDEX IF NOT EXISTS uq_calendar_event_external_event_id
    ON calendar_event(external_event_id);