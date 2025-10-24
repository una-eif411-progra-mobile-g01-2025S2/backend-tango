-- Script de datos de prueba para backend-tango (PostgreSQL)
-- Ajusta los nombres de tabla/campo si tu modelo es diferente

-- Usuario de prueba
INSERT INTO app_user (id, email, password, full_name)
VALUES ('11111111-1111-1111-1111-111111111111', 'prueba@ejemplo.com', '1234', 'Usuario Prueba')
ON CONFLICT (id) DO NOTHING;

-- Periodo académico
INSERT INTO academic_period (id, name, start_date, end_date)
VALUES ('22222222-2222-2222-2222-222222222222', '2025-I', '2025-01-01', '2025-06-30')
ON CONFLICT (id) DO NOTHING;

-- Materia (Subject)
INSERT INTO subject (id, user_id, period_id, name, code, weekly_hours, start_date, end_date)
VALUES (
  '33333333-3333-3333-3333-333333333333',
  '11111111-1111-1111-1111-111111111111',
  '22222222-2222-2222-2222-222222222222',
  'Matemáticas',
  'MAT101',
  4,
  '2025-01-01',
  '2025-06-30'
)
ON CONFLICT (id) DO NOTHING;

-- Bloque de estudio (Study Block)
INSERT INTO study_block (id, user_id, subject_id, start_time, end_time, priority, status)
VALUES (
  '44444444-4444-4444-4444-444444444444',
  '11111111-1111-1111-1111-111111111111',
  '33333333-3333-3333-3333-333333333333',
  '2025-01-10T08:00:00',
  '2025-01-10T10:00:00',
  3,
  'PLANNED'
)
ON CONFLICT (id) DO NOTHING;

-- Tarea (Task)
INSERT INTO task (id, user_id, subject_id, title, description, priority, deadline, status)
VALUES (
  '55555555-5555-5555-5555-555555555555',
  '11111111-1111-1111-1111-111111111111',
  '33333333-3333-3333-3333-333333333333',
  'Tarea de ejemplo',
  'Descripción de la tarea',
  3,
  '2025-01-15',
  'PENDING'
)
ON CONFLICT (id) DO NOTHING;

-- Disponibilidad semanal (Weekly Availability)
INSERT INTO weekly_availability (id, user_id, day_of_week, start_time, end_time)
VALUES ('66666666-6666-6666-6666-666666666666', '11111111-1111-1111-1111-111111111111', 'MONDAY', '08:00', '10:00')
ON CONFLICT (id) DO NOTHING;

-- Evento de calendario (Calendar Event)
INSERT INTO calendar_event (id, study_block_id, provider, status)
VALUES (
  '77777777-7777-7777-7777-777777777777',
  '44444444-4444-4444-4444-444444444444',
  'GOOGLE',
  'CREATED'
)
ON CONFLICT (id) DO NOTHING;

-- Roles
INSERT INTO role (id, name, description)
VALUES ('88888888-8888-8888-8888-888888888888', 'ROLE_USER', 'Rol de usuario estándar'),
       ('99999999-9999-9999-9999-999999999999', 'ROLE_ADMIN', 'Rol de administrador')
ON CONFLICT (id) DO NOTHING;

-- Privilegios
INSERT INTO privilege (id, name, description)
VALUES ('aaaaaaa1-aaaa-aaaa-aaaa-aaaaaaaaaaa1', 'READ_PRIVILEGE', 'Permite leer recursos'),
       ('aaaaaaa2-aaaa-aaaa-aaaa-aaaaaaaaaaa2', 'WRITE_PRIVILEGE', 'Permite modificar recursos')
ON CONFLICT (id) DO NOTHING;

-- Relación usuario-rol
INSERT INTO user_role (user_id, role_id)
VALUES ('11111111-1111-1111-1111-111111111111', '88888888-8888-8888-8888-888888888888')
ON CONFLICT (user_id, role_id) DO NOTHING;

-- Relación rol-privilegio
INSERT INTO role_privilege (role_id, privilege_id)
VALUES ('88888888-8888-8888-8888-888888888888', 'aaaaaaa1-aaaa-aaaa-aaaa-aaaaaaaaaaa1'),
       ('88888888-8888-8888-8888-888888888888', 'aaaaaaa2-aaaa-aaaa-aaaa-aaaaaaaaaaa2'),
       ('99999999-9999-9999-9999-999999999999', 'aaaaaaa1-aaaa-aaaa-aaaa-aaaaaaaaaaa1'),
       ('99999999-9999-9999-9999-999999999999', 'aaaaaaa2-aaaa-aaaa-aaaa-aaaaaaaaaaa2')
ON CONFLICT (role_id, privilege_id) DO NOTHING;

-- Puedes agregar más inserts según tus necesidades
