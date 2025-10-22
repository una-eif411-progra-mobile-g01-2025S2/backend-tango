-- Flyway Migration: V1__init.sql
-- Schema initialization for PAI backend (Spring Boot + JPA)
-- Uses UUID primary keys. Enable uuid-ossp for uuid_generate_v4().

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ========= Enums (stored as text with CHECK) =========
-- TaskStatus: PENDING, IN_PROGRESS, COMPLETED
-- StudyBlockStatus: PLANNED, IN_PROGRESS, COMPLETED
-- CalendarProvider: GOOGLE
-- CalendarEventStatus: CREATED, UPDATED, DELETED

-- ========= Seguridad =========
CREATE TABLE IF NOT EXISTS app_user (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  email TEXT NOT NULL UNIQUE,
  password TEXT NOT NULL,
  full_name TEXT NOT NULL,
  degree TEXT,
  year_of_study INT,
  university TEXT
);
CREATE INDEX IF NOT EXISTS idx_user_email ON app_user(email);

CREATE TABLE IF NOT EXISTS role (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  name TEXT NOT NULL UNIQUE,
  description TEXT
);
CREATE INDEX IF NOT EXISTS idx_role_name ON role(name);

CREATE TABLE IF NOT EXISTS privilege (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  name TEXT NOT NULL UNIQUE,
  description TEXT
);
CREATE INDEX IF NOT EXISTS idx_priv_name ON privilege(name);

-- Join: user_role (unique per pair)
CREATE TABLE IF NOT EXISTS user_role (
  user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
  role_id UUID NOT NULL REFERENCES role(id) ON DELETE CASCADE,
  PRIMARY KEY (user_id, role_id)
);
CREATE INDEX IF NOT EXISTS idx_user_role ON user_role(user_id, role_id);

-- Join: role_privilege (unique per pair)
CREATE TABLE IF NOT EXISTS role_privilege (
  role_id UUID NOT NULL REFERENCES role(id) ON DELETE CASCADE,
  privilege_id UUID NOT NULL REFERENCES privilege(id) ON DELETE CASCADE,
  PRIMARY KEY (role_id, privilege_id)
);
CREATE INDEX IF NOT EXISTS idx_role_priv ON role_privilege(role_id, privilege_id);

-- ========= Académico =========
CREATE TABLE IF NOT EXISTS academic_period (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  name TEXT NOT NULL,
  start_date DATE NOT NULL,
  end_date DATE NOT NULL,
  CONSTRAINT chk_period_dates CHECK (end_date >= start_date)
);

CREATE TABLE IF NOT EXISTS subject (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
  period_id UUID NOT NULL REFERENCES academic_period(id) ON DELETE CASCADE,
  name TEXT NOT NULL,
  code TEXT NOT NULL,
  professor TEXT,
  credits INT,
  weekly_hours INT NOT NULL,
  -- Optional constraints hinted by annotations:
  CONSTRAINT chk_subject_weekly_hours CHECK (weekly_hours BETWEEN 1 AND 20)
);
CREATE INDEX IF NOT EXISTS idx_subject_user ON subject(user_id);
CREATE INDEX IF NOT EXISTS idx_subject_period ON subject(period_id);

CREATE TABLE IF NOT EXISTS task (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
  subject_id UUID NOT NULL REFERENCES subject(id) ON DELETE CASCADE,
  title TEXT NOT NULL,
  description TEXT,
  priority INT NOT NULL,
  status TEXT NOT NULL,
  CONSTRAINT chk_task_priority CHECK (priority BETWEEN 1 AND 5),
  CONSTRAINT chk_task_status CHECK (status IN ('PENDING','IN_PROGRESS','COMPLETED'))
);
CREATE INDEX IF NOT EXISTS idx_task_user ON task(user_id);
CREATE INDEX IF NOT EXISTS idx_task_subject ON task(subject_id);

CREATE TABLE IF NOT EXISTS study_block (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
  subject_id UUID NOT NULL REFERENCES subject(id) ON DELETE CASCADE,
  task_id UUID REFERENCES task(id) ON DELETE SET NULL,
  start_time TIMESTAMP NOT NULL,
  end_time TIMESTAMP NOT NULL,
  status TEXT NOT NULL,
  CONSTRAINT chk_block_times CHECK (end_time >= start_time),
  CONSTRAINT chk_block_status CHECK (status IN ('PLANNED','IN_PROGRESS','COMPLETED'))
);
CREATE INDEX IF NOT EXISTS idx_block_user ON study_block(user_id);
CREATE INDEX IF NOT EXISTS idx_block_subject ON study_block(subject_id);
CREATE INDEX IF NOT EXISTS idx_block_task ON study_block(task_id);

CREATE TABLE IF NOT EXISTS weekly_availability (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
  day_of_week TEXT NOT NULL,
  start_time TIME NOT NULL,
  end_time TIME NOT NULL,
  CONSTRAINT chk_availability_day CHECK (day_of_week IN ('MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY','SUNDAY')),
  CONSTRAINT chk_availability_times CHECK (end_time > start_time)
);
CREATE INDEX IF NOT EXISTS idx_availability_user ON weekly_availability(user_id);

-- ========= Calendar sync =========
CREATE TABLE IF NOT EXISTS calendar_event (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
  study_block_id UUID UNIQUE REFERENCES study_block(id) ON DELETE CASCADE,
  provider TEXT NOT NULL,
  external_event_id TEXT,
  last_sync_at TIMESTAMP,
  status TEXT NOT NULL,
  CONSTRAINT chk_calendar_provider CHECK (provider IN ('GOOGLE')),
  CONSTRAINT chk_calendar_status CHECK (status IN ('CREATED','UPDATED','DELETED'))
);
CREATE INDEX IF NOT EXISTS idx_calendar_user ON calendar_event(user_id);
