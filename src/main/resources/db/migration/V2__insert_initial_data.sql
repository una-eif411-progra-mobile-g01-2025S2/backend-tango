-- Flyway Migration: V2__insert_initial_data.sql
-- Seeds baseline security and demo academic data for a fresh installation.

/* ===== Roles ===== */
INSERT INTO role (id, name, description) VALUES
    (uuid_generate_v4(), 'ADMIN', 'Administrador del sistema'),
    (uuid_generate_v4(), 'USER',  'Usuario estándar')
ON CONFLICT (name) DO NOTHING;

/* ===== Privileges ===== */
INSERT INTO privilege (id, name, description) VALUES
    (uuid_generate_v4(), 'USER_READ',    'Leer usuarios'),
    (uuid_generate_v4(), 'USER_WRITE',   'Crear/editar usuarios'),
    (uuid_generate_v4(), 'TASK_READ',    'Leer tareas'),
    (uuid_generate_v4(), 'TASK_WRITE',   'Crear/editar tareas'),
    (uuid_generate_v4(), 'CALENDAR_SYNC','Sincronizar calendario')
ON CONFLICT (name) DO NOTHING;

/* ===== Role - Privilege mappings ===== */
INSERT INTO role_privilege (role_id, privilege_id)
SELECT r.id, p.id
FROM role r CROSS JOIN privilege p
WHERE r.name = 'ADMIN'
ON CONFLICT DO NOTHING;

INSERT INTO role_privilege (role_id, privilege_id)
SELECT r.id, p.id
FROM role r
JOIN privilege p ON p.name IN ('TASK_READ', 'CALENDAR_SYNC')
WHERE r.name = 'USER'
ON CONFLICT DO NOTHING;

/* ===== Users ===== */
INSERT INTO app_user (id, email, password, full_name, degree, year_of_study, university) VALUES
    (uuid_generate_v4(), 'admin@pai.local', 'admin123', 'Admin PAI', NULL, NULL, NULL),
    (uuid_generate_v4(), 'user@pai.local',  'user123',  'Usuario PAI', 'Ingeniería', 2, 'TEC'),
    (uuid_generate_v4(), 'user2@pai.local', 'user2123', 'Usuario 2 PAI', 'Computación', 1, 'UCR'),
    (uuid_generate_v4(), 'mentor@pai.local','mentor123','Mentor Académico', 'Docencia', 0, 'UNA')
ON CONFLICT (email) DO NOTHING;

/* ===== User - Role mappings ===== */
INSERT INTO user_role (user_id, role_id)
SELECT u.id, r.id
FROM app_user u
JOIN role r ON r.name = 'ADMIN'
WHERE u.email = 'admin@pai.local'
ON CONFLICT DO NOTHING;

INSERT INTO user_role (user_id, role_id)
SELECT u.id, r.id
FROM app_user u
JOIN role r ON r.name = 'USER'
WHERE u.email IN ('user@pai.local', 'user2@pai.local', 'mentor@pai.local')
ON CONFLICT DO NOTHING;

/* ===== Academic data ===== */
INSERT INTO academic_period (id, name, start_date, end_date)
SELECT uuid_generate_v4(), 'II-2025', DATE '2025-08-01', DATE '2025-12-15'
WHERE NOT EXISTS (SELECT 1 FROM academic_period WHERE name = 'II-2025');

INSERT INTO subject (id, user_id, period_id, name, code, professor, credits, weekly_hours, start_date, end_date)
SELECT uuid_generate_v4(), u.id, ap.id, 'Estructuras de Datos', 'EIF-400', 'Dra. Ramírez', 4, 6, ap.start_date, ap.end_date
FROM app_user u
JOIN academic_period ap ON ap.name = 'II-2025'
WHERE u.email = 'user@pai.local'
ON CONFLICT (user_id, period_id, code) DO NOTHING;

INSERT INTO task (id, user_id, subject_id, title, description, priority, deadline, status)
SELECT uuid_generate_v4(), u.id, s.id,
       'Proyecto Final', 'Implementa una app web', 3, NULL, 'PENDING'
FROM app_user u
JOIN subject s ON s.user_id = u.id
WHERE u.email = 'user@pai.local'
ON CONFLICT DO NOTHING;