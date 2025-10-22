DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'subject'
          AND column_name = 'start_date'
    ) THEN
        ALTER TABLE subject
            ADD COLUMN start_date DATE;
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'subject'
          AND column_name = 'end_date'
    ) THEN
        ALTER TABLE subject
            ADD COLUMN end_date DATE;
    END IF;
END $$;

UPDATE subject s
SET start_date = ap.start_date
FROM academic_period ap
WHERE s.period_id = ap.id
  AND s.start_date IS NULL;

UPDATE subject s
SET end_date = ap.end_date
FROM academic_period ap
WHERE s.period_id = ap.id
  AND s.end_date IS NULL;

ALTER TABLE subject
    ALTER COLUMN start_date SET NOT NULL;

ALTER TABLE subject
    ALTER COLUMN end_date SET NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints tc
        WHERE tc.table_schema = 'public'
          AND tc.table_name = 'subject'
          AND tc.constraint_name = 'chk_subject_dates'
    ) THEN
        ALTER TABLE subject
            ADD CONSTRAINT chk_subject_dates CHECK (end_date >= start_date);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints tc
        WHERE tc.table_schema = 'public'
          AND tc.table_name = 'subject'
          AND tc.constraint_name = 'uk_subject_user_period_code'
    ) THEN
        ALTER TABLE subject
            ADD CONSTRAINT uk_subject_user_period_code UNIQUE (user_id, period_id, code);
    END IF;
END $$;