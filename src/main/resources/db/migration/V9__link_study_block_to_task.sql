DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'study_block'
          AND column_name = 'task_id'
    ) THEN
        ALTER TABLE study_block
            ADD COLUMN task_id UUID;
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
          AND tc.table_name = 'study_block'
          AND tc.constraint_type = 'FOREIGN KEY'
          AND tc.constraint_name = 'fk_study_block_task'
    ) THEN
        ALTER TABLE study_block
            ADD CONSTRAINT fk_study_block_task
            FOREIGN KEY (task_id)
            REFERENCES task(id)
            ON DELETE SET NULL;
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_study_block_task
    ON study_block(task_id);