DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'study_block'
          AND column_name = 'priority'
    ) THEN
        ALTER TABLE study_block
            ADD COLUMN priority INT;
    END IF;
END $$;

UPDATE study_block
SET priority = 3
WHERE priority IS NULL;

ALTER TABLE study_block
    ALTER COLUMN priority SET DEFAULT 3;

ALTER TABLE study_block
    ALTER COLUMN priority SET NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.constraint_column_usage ccu
        JOIN information_schema.table_constraints tc
          ON ccu.constraint_name = tc.constraint_name
         AND ccu.table_schema = tc.table_schema
        WHERE tc.table_schema = 'public'
          AND tc.table_name = 'study_block'
          AND tc.constraint_type = 'CHECK'
          AND tc.constraint_name = 'chk_study_block_priority'
    ) THEN
        ALTER TABLE study_block
            ADD CONSTRAINT chk_study_block_priority CHECK (priority BETWEEN 1 AND 5);
    END IF;
END $$;