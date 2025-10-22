-- Flyway Migration: V2__insert_users.sql
-- Seed roles, privileges, users and example academic data for PAI

-- ===== Roles =====
INSERT INTO role (id, name, description) VALUES
  (uuid_generate_v4(), 'ADMIN', 'Administrador del sistema'),
  (uuid_generate_v4(), 'USER',  'Usuario estándar')
ON CONFLICT (name) DO NOTHING;

-- ===== Privileges =====
INSERT INTO privilege (id, name, description) VALUES
  (uuid_generate_v4(), 'USER_READ', 'Leer usuarios'),
  (uuid_generate_v4(), 'USER_WRITE','Crear/editar usuarios'),
  (uuid_generate_v4(), 'TASK_READ', 'Leer tareas'),
  (uuid_generate_v4(), 'TASK_WRITE','Crear/editar tareas'),
  (uuid_generate_v4(), 'CALENDAR_SYNC','Sincronizar calendario')
ON CONFLICT (name) DO NOTHING;

-- Map privileges to roles
-- ADMIN: todos los privilegios
INSERT INTO role_privilege (role_id, privilege_id)
SELECT r.id, p.id FROM role r CROSS JOIN privilege p
WHERE r.name = 'ADMIN'
ON CONFLICT DO NOTHING;

-- USER: sólo lectura de tareas y sync de calendario
INSERT INTO role_privilege (role_id, privilege_id)
SELECT r.id, p.id FROM role r
JOIN privilege p ON p.name IN ('TASK_READ','CALENDAR_SYNC')
WHERE r.name = 'USER'
ON CONFLICT DO NOTHING;

-- ===== Users =====
-- Nota: si tu backend usa BCrypt, reemplaza las contraseñas en claro por hashes BCrypt.
INSERT INTO app_user (id, email, password, full_name, degree, year_of_study, university) VALUES
  (uuid_generate_v4(), 'admin@pai.local', 'admin123', 'Admin PAI', NULL, NULL, NULL),
  (uuid_generate_v4(), 'user@pai.local',  'user123',  'Usuario PAI', 'Ingeniería', 2, 'TEC')
ON CONFLICT (email) DO NOTHING;

-- Link users to roles
INSERT INTO user_role (user_id, role_id)
SELECT u.id, r.id
FROM app_user u JOIN role r
  ON (u.email = 'admin@pai.local' AND r.name = 'ADMIN')
ON CONFLICT DO NOTHING;

INSERT INTO user_role (user_id, role_id)
SELECT u.id, r.id
FROM app_user u JOIN role r
  ON (u.email = 'user@pai.local' AND r.name = 'USER')
ON CONFLICT DO NOTHING;

-- ===== Minimal academic data for quick tests =====
-- Create a current academic period
INSERT INTO academic_period (id, name, start_date, end_date)
VALUES (uuid_generate_v4(), 'II-2025', DATE '2025-08-01', DATE '2025-12-15');

-- Assign a subject to the test user
INSERT INTO subject (id, user_id, period_id, name, code, professor, credits, weekly_hours)
SELECT uuid_generate_v4(), u.id, ap.id, 'Estructuras de Datos', 'EIF-400', 'Dra. Ramírez', 4, 6
FROM app_user u, academic_period ap
WHERE u.email = 'user@pai.local'
LIMIT 1;

-- Create a sample task for that subject
INSERT INTO task (id, user_id, subject_id, title, description, priority, status)
SELECT uuid_generate_v4(), u.id, s.id, 'Proyecto Final', 'Implementa una app web', 3, 'PENDING'
FROM app_user u
JOIN subject s ON u.id = s.user_id
WHERE u.email = 'user@pai.local'
LIMIT 1;