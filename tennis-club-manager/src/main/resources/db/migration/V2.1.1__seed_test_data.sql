-- ============================================================================
-- MIGRACIÓN V2.1.1 - DATOS DE PRUEBA PARA FLUJO COMPLETO
-- ============================================================================
-- Este script inserta datos de ejemplo para probar el flujo completo:
-- 1. Ver alumnos
-- 2. Buscar alumnos
-- 3. Crear contratos
-- 4. Ver clases generadas automáticamente
-- 5. Ver calendario
-- ============================================================================

-- ============================================================================
-- 1. INSERTAR USUARIOS (ADMIN y ALUMNOS)
-- ============================================================================

-- ADMIN (para poder crear contratos)
INSERT INTO users (email, first_name, last_name, birth_date, license_number, password_hash, role, phone, created_date, created_by, updated_date, updated_by)
VALUES 
    ('admin@club.com', 'Admin', 'Principal', '1980-01-01', 'ADMIN-001', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN', '+34600123456', NOW(), 'system', NOW(), 'system')
ON CONFLICT (email) DO NOTHING;

-- MONITOR (para asignar a clases)
INSERT INTO users (email, first_name, last_name, birth_date, license_number, password_hash, role, phone, created_date, created_by, updated_date, updated_by)
VALUES 
    ('carlos.monitor@club.com', 'Carlos', 'García', '1985-05-15', 'MON-001', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'MONITOR', '+34600111111', NOW(), 'system', NOW(), 'system'),
    ('ana.monitor@club.com', 'Ana', 'Martínez', '1990-08-20', 'MON-002', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'MONITOR', '+34600222222', NOW(), 'system', NOW(), 'system')
ON CONFLICT (email) DO NOTHING;

-- ALUMNOS (para asignar contratos)
INSERT INTO users (email, first_name, last_name, birth_date, license_number, password_hash, role, phone, created_date, created_by, updated_date, updated_by)
VALUES 
    ('juan.perez@example.com', 'Juan', 'Pérez', '1995-03-10', '12345678A', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ALUMNO', '+34600333333', NOW(), 'system', NOW(), 'system'),
    ('maria.lopez@example.com', 'María', 'López', '1998-07-22', '87654321B', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ALUMNO', '+34600444444', NOW(), 'system', NOW(), 'system'),
    ('pedro.garcia@example.com', 'Pedro', 'García', '1992-11-05', '11223344C', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ALUMNO', '+34600555555', NOW(), 'system', NOW(), 'system'),
    ('laura.sanchez@example.com', 'Laura', 'Sánchez', '2000-01-18', '55667788D', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ALUMNO', '+34600666666', NOW(), 'system', NOW(), 'system'),
    ('carlos.ruiz@example.com', 'Carlos', 'Ruiz', '1996-09-30', '99887766E', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ALUMNO', '+34600777777', NOW(), 'system', NOW(), 'system')
ON CONFLICT (email) DO NOTHING;

-- Nota: password_hash es 'password123' hasheado con BCrypt
-- Puedes usar este hash para todos los usuarios de prueba

-- ============================================================================
-- 2. INSERTAR PERIODOS DEL CLUB (Trimestres)
-- ============================================================================

-- Trimestre 1: Enero - Marzo 2026
INSERT INTO club_periods (id, name, period_type, start_date, end_date, is_active, description, created_date, created_by, updated_date, updated_by)
VALUES 
    (gen_random_uuid(), 'Trimestre 1 - 2026', 'QUARTER', '2026-01-01', '2026-03-31', true, 'Primer trimestre del año 2026', NOW(), 'system', NOW(), 'system')
ON CONFLICT DO NOTHING;

-- Trimestre 2: Abril - Junio 2026
INSERT INTO club_periods (id, name, period_type, start_date, end_date, is_active, description, created_date, created_by, updated_date, updated_by)
VALUES 
    (gen_random_uuid(), 'Trimestre 2 - 2026', 'QUARTER', '2026-04-01', '2026-06-30', true, 'Segundo trimestre del año 2026', NOW(), 'system', NOW(), 'system')
ON CONFLICT DO NOTHING;

-- Trimestre 3: Septiembre - Diciembre 2026
INSERT INTO club_periods (id, name, period_type, start_date, end_date, is_active, description, created_date, created_by, updated_date, updated_by)
VALUES 
    (gen_random_uuid(), 'Trimestre 3 - 2026', 'QUARTER', '2026-09-01', '2026-12-31', true, 'Tercer trimestre del año 2026', NOW(), 'system', NOW(), 'system')
ON CONFLICT DO NOTHING;

-- ============================================================================
-- 3. INSERTAR SERVICIOS (Tipos de Clases)
-- ============================================================================

-- Servicio 1: Clase grupal trimestral - Lunes 18:00
INSERT INTO services (id, code, name, description, service_type, day_of_week, start_time, end_time, base_price, currency, max_capacity, min_capacity, is_active, created_date, created_by, updated_date, updated_by)
VALUES 
    (gen_random_uuid(), 'ESCUELA-LUNES-18H', 'Escuela Lunes 18:00', 'Clase grupal de tenis los lunes a las 18:00 horas', 'QUARTERLY_GROUP_CLASS', 'MONDAY', '18:00:00', '19:30:00', 150.00, 'EUR', 8, 4, true, NOW(), 'system', NOW(), 'system')
ON CONFLICT (code) DO NOTHING;

-- Servicio 2: Clase grupal trimestral - Miércoles 19:00
INSERT INTO services (id, code, name, description, service_type, day_of_week, start_time, end_time, base_price, currency, max_capacity, min_capacity, is_active, created_date, created_by, updated_date, updated_by)
VALUES 
    (gen_random_uuid(), 'ESCUELA-MIERCOLES-19H', 'Escuela Miércoles 19:00', 'Clase grupal de tenis los miércoles a las 19:00 horas', 'QUARTERLY_GROUP_CLASS', 'WEDNESDAY', '19:00:00', '20:30:00', 150.00, 'EUR', 8, 4, true, NOW(), 'system', NOW(), 'system')
ON CONFLICT (code) DO NOTHING;

-- Servicio 3: Clase grupal trimestral - Viernes 17:00
INSERT INTO services (id, code, name, description, service_type, day_of_week, start_time, end_time, base_price, currency, max_capacity, min_capacity, is_active, created_date, created_by, updated_date, updated_by)
VALUES 
    (gen_random_uuid(), 'ESCUELA-VIERNES-17H', 'Escuela Viernes 17:00', 'Clase grupal de tenis los viernes a las 17:00 horas', 'QUARTERLY_GROUP_CLASS', 'FRIDAY', '17:00:00', '18:30:00', 150.00, 'EUR', 8, 4, true, NOW(), 'system', NOW(), 'system')
ON CONFLICT (code) DO NOTHING;

-- Servicio 4: Bono de 10 clases individuales
INSERT INTO services (id, code, name, description, service_type, base_price, currency, classes_in_package, package_validity_days, is_active, created_date, created_by, updated_date, updated_by)
VALUES 
    (gen_random_uuid(), 'BONO-10-CLASES', 'Bono 10 Clases Individuales', 'Bono de 10 clases individuales con validez de 3 meses', 'INDIVIDUAL_CLASS_PACKAGE', 300.00, 'EUR', 10, 90, true, NOW(), 'system', NOW(), 'system')
ON CONFLICT (code) DO NOTHING;

-- Servicio 5: Bono de 20 clases individuales
INSERT INTO services (id, code, name, description, service_type, base_price, currency, classes_in_package, package_validity_days, is_active, created_date, created_by, updated_date, updated_by)
VALUES 
    (gen_random_uuid(), 'BONO-20-CLASES', 'Bono 20 Clases Individuales', 'Bono de 20 clases individuales con validez de 6 meses', 'INDIVIDUAL_CLASS_PACKAGE', 550.00, 'EUR', 20, 180, true, NOW(), 'system', NOW(), 'system')
ON CONFLICT (code) DO NOTHING;

-- ============================================================================
-- NOTAS IMPORTANTES
-- ============================================================================
-- 
-- 1. CONTRATOS: Se crean a través del endpoint POST /api/v1/contracts
--    - Para contratos trimestrales: se generan automáticamente los calendar_events
--    - Para bonos: no se generan eventos automáticamente
--
-- 2. CALENDAR_EVENTS: Se generan automáticamente cuando se crea un contrato
--    trimestral (QUARTERLY_GROUP_CLASS) con un periodo asociado
--
-- 3. ATTENDANCES: Se crean a través del endpoint POST /api/v1/attendances
--    cuando el admin marca la asistencia de un alumno
--
-- 4. PASSWORD: Todos los usuarios de prueba tienen la contraseña: "password123"
--    (hash: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy)
--
-- ============================================================================
-- FLUJO DE PRUEBA RECOMENDADO
-- ============================================================================
--
-- 1. Login como ADMIN:
--    POST /api/v1/users/log-in
--    { "email": "admin@club.com", "password": "password123" }
--
-- 2. Listar alumnos:
--    GET /api/v1/users?role=ALUMNO
--
-- 3. Listar servicios:
--    GET /api/v1/services
--
-- 4. Listar periodos activos:
--    GET /api/v1/club-periods?active=true
--
-- 5. Crear contrato trimestral (genera eventos automáticamente):
--    POST /api/v1/contracts
--    {
--      "userEmail": "juan.perez@example.com",
--      "serviceId": "<id-del-servicio-ESCUELA-LUNES-18H>",
--      "periodId": "<id-del-trimestre-1-2026>",
--      "price": 150.00,
--      "startDate": "2026-01-15",
--      "endDate": "2026-03-31",
--      "daysPerWeek": 1
--    }
--
-- 6. Ver eventos generados:
--    GET /api/v1/calendar-events?month=2026-01
--
-- 7. Marcar asistencia:
--    POST /api/v1/attendances
--    {
--      "eventId": "<id-del-evento>",
--      "userId": "juan.perez@example.com",
--      "contractId": "<id-del-contrato>",
--      "status": "PRESENT"
--    }
--
-- ============================================================================

