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