DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'task'
          AND column_name = 'deadline'
    ) THEN
        ALTER TABLE task
            ADD COLUMN deadline DATE;
    END IF;
END $$;