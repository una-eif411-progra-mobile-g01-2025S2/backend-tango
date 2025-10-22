ALTER TABLE calendar_event
    ADD COLUMN IF NOT EXISTS last_sync_at TIMESTAMP;