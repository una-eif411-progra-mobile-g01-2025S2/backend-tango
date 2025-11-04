-- Flyway Migration: V1__init.sql
-- Schema initialization consolidated for PAI backend.
-- Ensures tables reflect current JPA/Kotlin entities and use UUID primary keys.

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

/* ========= Seguridad ========= */
CREATE TABLE IF NOT EXISTS app_user (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email TEXT NOT NULL,
    password TEXT NOT NULL,
    full_name TEXT NOT NULL,
    degree TEXT,
    year_of_study INT,
    university TEXT,
    CONSTRAINT idx_user_email UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS role (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL,
    description TEXT,
    CONSTRAINT idx_role_name UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS privilege (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL,
    description TEXT,
    CONSTRAINT idx_priv_name UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS user_role (
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    CONSTRAINT pk_user_role PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES role(id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_user_role ON user_role(user_id, role_id);

CREATE TABLE IF NOT EXISTS role_privilege (
    role_id UUID NOT NULL,
    privilege_id UUID NOT NULL,
    CONSTRAINT pk_role_privilege PRIMARY KEY (role_id, privilege_id),
    CONSTRAINT fk_role_privilege_role FOREIGN KEY (role_id) REFERENCES role(id) ON DELETE CASCADE,
    CONSTRAINT fk_role_privilege_priv FOREIGN KEY (privilege_id) REFERENCES privilege(id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_role_privilege ON role_privilege(role_id, privilege_id);

CREATE TABLE IF NOT EXISTS refresh_token (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    issued_at TIMESTAMPTZ NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    revoked_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE,
    CONSTRAINT idx_refresh_token_hash UNIQUE (token_hash)
);

CREATE INDEX IF NOT EXISTS idx_refresh_token_user ON refresh_token(user_id);

/* ========= Académico ========= */
CREATE TABLE IF NOT EXISTS academic_period (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    CONSTRAINT chk_period_dates CHECK (end_date >= start_date)
);

CREATE TABLE IF NOT EXISTS subject (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    period_id UUID NOT NULL,
    name TEXT NOT NULL,
    code TEXT NOT NULL,
    professor TEXT,
    credits INT,
    weekly_hours INT NOT NULL DEFAULT 4,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    CONSTRAINT fk_subject_user FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_subject_period FOREIGN KEY (period_id) REFERENCES academic_period(id) ON DELETE CASCADE,
    CONSTRAINT chk_subject_weekly_hours CHECK (weekly_hours BETWEEN 1 AND 20),
    CONSTRAINT chk_subject_dates CHECK (end_date >= start_date),
    CONSTRAINT uk_subject_user_period_code UNIQUE (user_id, period_id, code)
);

CREATE INDEX IF NOT EXISTS idx_subject_user ON subject(user_id);
CREATE INDEX IF NOT EXISTS idx_subject_period ON subject(period_id);

/* ========= Planificación ========= */
CREATE TABLE IF NOT EXISTS task (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    subject_id UUID NOT NULL,
    title TEXT NOT NULL,
    description TEXT,
    priority INT NOT NULL DEFAULT 3,
    deadline DATE,
    status TEXT NOT NULL DEFAULT 'PENDING',
    CONSTRAINT fk_task_user FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_task_subject FOREIGN KEY (subject_id) REFERENCES subject(id) ON DELETE CASCADE,
    CONSTRAINT chk_task_priority CHECK (priority BETWEEN 1 AND 5),
    CONSTRAINT chk_task_status CHECK (status IN ('PENDING','IN_PROGRESS','COMPLETED'))
);

CREATE INDEX IF NOT EXISTS idx_task_user ON task(user_id);
CREATE INDEX IF NOT EXISTS idx_task_subject ON task(subject_id);

CREATE TABLE IF NOT EXISTS study_block (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    subject_id UUID NOT NULL,
    task_id UUID,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    priority INT NOT NULL DEFAULT 3,
    status TEXT NOT NULL DEFAULT 'PLANNED',
    CONSTRAINT fk_study_block_user FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_study_block_subject FOREIGN KEY (subject_id) REFERENCES subject(id) ON DELETE CASCADE,
    CONSTRAINT fk_study_block_task FOREIGN KEY (task_id) REFERENCES task(id) ON DELETE SET NULL,
    CONSTRAINT chk_study_block_priority CHECK (priority BETWEEN 1 AND 5),
    CONSTRAINT chk_study_block_status CHECK (status IN ('PLANNED','IN_PROGRESS','COMPLETED')),
    CONSTRAINT chk_study_block_times CHECK (end_time > start_time)
);

CREATE INDEX IF NOT EXISTS idx_study_block_user ON study_block(user_id);
CREATE INDEX IF NOT EXISTS idx_study_block_subject ON study_block(subject_id);
CREATE INDEX IF NOT EXISTS idx_study_block_task ON study_block(task_id);

CREATE TABLE IF NOT EXISTS weekly_availability (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    day_of_week TEXT NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    CONSTRAINT fk_weekly_availability_user FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE,
    CONSTRAINT chk_weekly_availability_day CHECK (day_of_week IN ('MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY','SUNDAY')),
    CONSTRAINT chk_weekly_availability_times CHECK (end_time > start_time)
);

CREATE INDEX IF NOT EXISTS idx_weekly_availability_user ON weekly_availability(user_id);

CREATE TABLE IF NOT EXISTS calendar_event (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    study_block_id UUID,
    provider TEXT NOT NULL,
    external_event_id TEXT,
    last_sync_at TIMESTAMP,
    status TEXT NOT NULL DEFAULT 'CREATED',
    CONSTRAINT fk_calendar_event_study_block FOREIGN KEY (study_block_id) REFERENCES study_block(id) ON DELETE SET NULL,
    CONSTRAINT chk_calendar_event_provider CHECK (provider IN ('GOOGLE')),
    CONSTRAINT chk_calendar_event_status CHECK (status IN ('CREATED','UPDATED','DELETED'))
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_calendar_event_study_block ON calendar_event(study_block_id) WHERE study_block_id IS NOT NULL;

