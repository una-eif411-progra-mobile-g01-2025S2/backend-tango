DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'calendar_event'
          AND column_name = 'study_block_id'
    ) THEN
        ALTER TABLE calendar_event
            ADD COLUMN study_block_id UUID;
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints tc
        JOIN information_schema.key_column_usage kcu
          ON tc.constraint_name = kcu.constraint_name
         AND tc.table_schema = kcu.table_schema
        WHERE tc.table_schema = 'public'
          AND tc.table_name = 'calendar_event'
          AND tc.constraint_type = 'FOREIGN KEY'
          AND kcu.column_name = 'study_block_id'
    ) THEN
        ALTER TABLE calendar_event
            ADD CONSTRAINT fk_calendar_event_study_block
            FOREIGN KEY (study_block_id)
            REFERENCES study_block(id)
            ON DELETE SET NULL;
    END IF;
END $$;

CREATE UNIQUE INDEX IF NOT EXISTS uk_calendar_event_study_block
    ON calendar_event(study_block_id)
    WHERE study_block_id IS NOT NULL;