-- Agrega la columna si no existe
ALTER TABLE calendar_event
  ADD COLUMN IF NOT EXISTS external_event_id TEXT;

DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_name = 'calendar_event' AND column_name = 'event_id'
  ) THEN
    UPDATE calendar_event
      SET external_event_id = COALESCE(external_event_id, event_id);
  END IF;
END$$;
