DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = 'public'
          AND table_name = 'weekly_availability'
    ) THEN
        CREATE TABLE weekly_availability (
            id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
            user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
            day_of_week TEXT NOT NULL,
            start_time TIME NOT NULL,
            end_time TIME NOT NULL
        );
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE table_schema = 'public'
          AND table_name = 'weekly_availability'
          AND constraint_name = 'chk_weekly_availability_times'
    ) THEN
        ALTER TABLE weekly_availability
            ADD CONSTRAINT chk_weekly_availability_times
            CHECK (end_time > start_time);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_weekly_availability_user
    ON weekly_availability(user_id);