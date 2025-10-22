-- Flyway Migration: V3__seed_all.sql (fixed)
-- Inserts demo data across all tables without conflicting with V2.
-- Uses WHERE NOT EXISTS to avoid duplicates when V2 already seeded data.

-- ===== Ensure base roles & privileges =====
INSERT INTO role (id, name, description)
SELECT uuid_generate_v4(), 'ADMIN', 'Administrador del sistema'
WHERE NOT EXISTS (SELECT 1 FROM role WHERE name='ADMIN');

INSERT INTO role (id, name, description)
SELECT uuid_generate_v4(), 'USER', 'Usuario estándar'
WHERE NOT EXISTS (SELECT 1 FROM role WHERE name='USER');

INSERT INTO privilege (id, name, description)
SELECT uuid_generate_v4(), 'USER_READ', 'Leer usuarios'
WHERE NOT EXISTS (SELECT 1 FROM privilege WHERE name='USER_READ');

INSERT INTO privilege (id, name, description)
SELECT uuid_generate_v4(), 'USER_WRITE', 'Crear/editar usuarios'
WHERE NOT EXISTS (SELECT 1 FROM privilege WHERE name='USER_WRITE');

INSERT INTO privilege (id, name, description)
SELECT uuid_generate_v4(), 'TASK_READ', 'Leer tareas'
WHERE NOT EXISTS (SELECT 1 FROM privilege WHERE name='TASK_READ');

INSERT INTO privilege (id, name, description)
SELECT uuid_generate_v4(), 'TASK_WRITE', 'Crear/editar tareas'
WHERE NOT EXISTS (SELECT 1 FROM privilege WHERE name='TASK_WRITE');

INSERT INTO privilege (id, name, description)
SELECT uuid_generate_v4(), 'CALENDAR_SYNC', 'Sincronizar calendario'
WHERE NOT EXISTS (SELECT 1 FROM privilege WHERE name='CALENDAR_SYNC');

-- Role-privilege mappings
INSERT INTO role_privilege (role_id, privilege_id)
SELECT r.id, p.id
FROM role r CROSS JOIN privilege p
WHERE r.name='ADMIN'
  AND NOT EXISTS (SELECT 1 FROM role_privilege rp WHERE rp.role_id=r.id AND rp.privilege_id=p.id);

INSERT INTO role_privilege (role_id, privilege_id)
SELECT r.id, p.id
FROM role r JOIN privilege p ON p.name IN ('TASK_READ','CALENDAR_SYNC')
WHERE r.name='USER'
  AND NOT EXISTS (SELECT 1 FROM role_privilege rp WHERE rp.role_id=r.id AND rp.privilege_id=p.id);

-- ===== Users =====
INSERT INTO app_user (id, email, password, full_name)
SELECT uuid_generate_v4(), 'admin@pai.local', 'admin123', 'Admin PAI'
WHERE NOT EXISTS (SELECT 1 FROM app_user WHERE email='admin@pai.local');

INSERT INTO app_user (id, email, password, full_name, degree, year_of_study, university)
SELECT uuid_generate_v4(), 'user@pai.local', 'user123', 'Usuario PAI', 'Ingeniería', 2, 'TEC'
WHERE NOT EXISTS (SELECT 1 FROM app_user WHERE email='user@pai.local');

INSERT INTO app_user (id, email, password, full_name, degree, year_of_study, university)
SELECT uuid_generate_v4(), 'user2@pai.local', 'user2123', 'Usuario 2 PAI', 'Computación', 1, 'UCR'
WHERE NOT EXISTS (SELECT 1 FROM app_user WHERE email='user2@pai.local');

INSERT INTO app_user (id, email, password, full_name, degree, year_of_study, university)
SELECT uuid_generate_v4(), 'mentor@pai.local', 'mentor123', 'Mentor Académico', 'Docencia', 0, 'UNA'
WHERE NOT EXISTS (SELECT 1 FROM app_user WHERE email='mentor@pai.local');

-- Assign roles
INSERT INTO user_role (user_id, role_id)
SELECT u.id, r.id FROM app_user u JOIN role r ON u.email='admin@pai.local' AND r.name='ADMIN'
WHERE NOT EXISTS (SELECT 1 FROM user_role ur WHERE ur.user_id=u.id AND ur.role_id=r.id);

INSERT INTO user_role (user_id, role_id)
SELECT u.id, r.id FROM app_user u JOIN role r ON u.email='user@pai.local' AND r.name='USER'
WHERE NOT EXISTS (SELECT 1 FROM user_role ur WHERE ur.user_id=u.id AND ur.role_id=r.id);

INSERT INTO user_role (user_id, role_id)
SELECT u.id, r.id FROM app_user u JOIN role r ON u.email='user2@pai.local' AND r.name='USER'
WHERE NOT EXISTS (SELECT 1 FROM user_role ur WHERE ur.user_id=u.id AND ur.role_id=r.id);

INSERT INTO user_role (user_id, role_id)
SELECT u.id, r.id FROM app_user u JOIN role r ON u.email='mentor@pai.local' AND r.name='ADMIN'
WHERE NOT EXISTS (SELECT 1 FROM user_role ur WHERE ur.user_id=u.id AND ur.role_id=r.id);

-- ===== Academic periods =====
INSERT INTO academic_period (id, name, start_date, end_date)
SELECT uuid_generate_v4(), 'II-2025', DATE '2025-08-01', DATE '2025-12-15'
WHERE NOT EXISTS (SELECT 1 FROM academic_period WHERE name='II-2025');

INSERT INTO academic_period (id, name, start_date, end_date)
SELECT uuid_generate_v4(), 'I-2026', DATE '2026-02-01', DATE '2026-06-30'
WHERE NOT EXISTS (SELECT 1 FROM academic_period WHERE name='I-2026');

-- ===== Subjects =====
INSERT INTO subject (id, user_id, period_id, name, code, professor, credits, weekly_hours)
SELECT uuid_generate_v4(), u.id, ap.id, 'Estructuras de Datos', 'EIF-400', 'Dra. Ramírez', 4, 6
FROM app_user u, academic_period ap
WHERE u.email='user@pai.local' AND ap.name='II-2025'
  AND NOT EXISTS (SELECT 1 FROM subject s WHERE s.user_id=u.id AND s.code='EIF-400');

INSERT INTO subject (id, user_id, period_id, name, code, professor, credits, weekly_hours)
SELECT uuid_generate_v4(), u.id, ap.id, 'Bases de Datos', 'EIF-401', 'Ing. Cordero', 4, 5
FROM app_user u, academic_period ap
WHERE u.email='user@pai.local' AND ap.name='II-2025'
  AND NOT EXISTS (SELECT 1 FROM subject s WHERE s.user_id=u.id AND s.code='EIF-401');

INSERT INTO subject (id, user_id, period_id, name, code, professor, credits, weekly_hours)
SELECT uuid_generate_v4(), u.id, ap.id, 'Programación Funcional', 'EIF-402', 'M.Sc. Solano', 3, 4
FROM app_user u, academic_period ap
WHERE u.email='user2@pai.local' AND ap.name='I-2026'
  AND NOT EXISTS (SELECT 1 FROM subject s WHERE s.user_id=u.id AND s.code='EIF-402');

INSERT INTO subject (id, user_id, period_id, name, code, professor, credits, weekly_hours)
SELECT uuid_generate_v4(), u.id, ap.id, 'Ingeniería de Software', 'EIF-403', 'PhD. Araya', 4, 5
FROM app_user u, academic_period ap
WHERE u.email='user2@pai.local' AND ap.name='I-2026'
  AND NOT EXISTS (SELECT 1 FROM subject s WHERE s.user_id=u.id AND s.code='EIF-403');

-- ===== Tasks =====
-- For each subject above, insert three tasks if not present
INSERT INTO task (id, user_id, subject_id, title, description, priority, status)
SELECT uuid_generate_v4(), s.user_id, s.id, 'Tarea 1', 'Primer entregable', 3, 'PENDING'
FROM subject s
WHERE s.code IN ('EIF-400','EIF-401','EIF-402','EIF-403')
  AND NOT EXISTS (
    SELECT 1 FROM task t WHERE t.subject_id=s.id AND t.title='Tarea 1'
  );

INSERT INTO task (id, user_id, subject_id, title, description, priority, status)
SELECT uuid_generate_v4(), s.user_id, s.id, 'Proyecto Parcial', 'Proyecto intermedio', 4, 'IN_PROGRESS'
FROM subject s
WHERE s.code IN ('EIF-400','EIF-401','EIF-402','EIF-403')
  AND NOT EXISTS (
    SELECT 1 FROM task t WHERE t.subject_id=s.id AND t.title='Proyecto Parcial'
  );

INSERT INTO task (id, user_id, subject_id, title, description, priority, status)
SELECT uuid_generate_v4(), s.user_id, s.id, 'Laboratorio', 'Ejercicios guiados', 2, 'COMPLETED'
FROM subject s
WHERE s.code IN ('EIF-400','EIF-401','EIF-402','EIF-403')
  AND NOT EXISTS (
    SELECT 1 FROM task t WHERE t.subject_id=s.id AND t.title='Laboratorio'
  );

-- ===== Study blocks =====
-- Create blocks tied to each of the three tasks, if none exist yet
INSERT INTO study_block (id, user_id, subject_id, task_id, start_time, end_time, status)
SELECT uuid_generate_v4(), t.user_id, t.subject_id, t.id,
       NOW() + INTERVAL '1 day', NOW() + INTERVAL '1 day 2 hours', 'PLANNED'
FROM task t
WHERE t.title='Tarea 1'
  AND NOT EXISTS (SELECT 1 FROM study_block b WHERE b.task_id=t.id);

INSERT INTO study_block (id, user_id, subject_id, task_id, start_time, end_time, status)
SELECT uuid_generate_v4(), t.user_id, t.subject_id, t.id,
       NOW() - INTERVAL '1 day', NOW(), 'COMPLETED'
FROM task t
WHERE t.title='Laboratorio'
  AND NOT EXISTS (SELECT 1 FROM study_block b WHERE b.task_id=t.id);

INSERT INTO study_block (id, user_id, subject_id, task_id, start_time, end_time, status)
SELECT uuid_generate_v4(), t.user_id, t.subject_id, t.id,
       NOW() + INTERVAL '3 days', NOW() + INTERVAL '3 days 2 hours', 'IN_PROGRESS'
FROM task t
WHERE t.title='Proyecto Parcial'
  AND NOT EXISTS (SELECT 1 FROM study_block b WHERE b.task_id=t.id);

-- ===== Weekly availability =====
INSERT INTO weekly_availability (id, user_id, day_of_week, start_time, end_time)
SELECT uuid_generate_v4(), u.id, 'MONDAY', TIME '18:00', TIME '20:00'
FROM app_user u
WHERE u.email='user@pai.local'
  AND NOT EXISTS (SELECT 1 FROM weekly_availability w WHERE w.user_id=u.id AND w.day_of_week='MONDAY' AND w.start_time=TIME '18:00');

INSERT INTO weekly_availability (id, user_id, day_of_week, start_time, end_time)
SELECT uuid_generate_v4(), u.id, 'WEDNESDAY', TIME '19:00', TIME '21:00'
FROM app_user u
WHERE u.email='user@pai.local'
  AND NOT EXISTS (SELECT 1 FROM weekly_availability w WHERE w.user_id=u.id AND w.day_of_week='WEDNESDAY' AND w.start_time=TIME '19:00');

INSERT INTO weekly_availability (id, user_id, day_of_week, start_time, end_time)
SELECT uuid_generate_v4(), u.id, 'FRIDAY', TIME '17:00', TIME '19:00'
FROM app_user u
WHERE u.email='user2@pai.local'
  AND NOT EXISTS (SELECT 1 FROM weekly_availability w WHERE w.user_id=u.id AND w.day_of_week='FRIDAY' AND w.start_time=TIME '17:00');

-- ===== Calendar events =====
INSERT INTO calendar_event (id, user_id, study_block_id, provider, external_event_id, last_sync_at, status)
SELECT uuid_generate_v4(), b.user_id, b.id, 'GOOGLE', 'evt-' || substring(b.id::text, 1, 8), NOW(), 'CREATED'
FROM study_block b
WHERE NOT EXISTS (SELECT 1 FROM calendar_event ce WHERE ce.study_block_id=b.id);
