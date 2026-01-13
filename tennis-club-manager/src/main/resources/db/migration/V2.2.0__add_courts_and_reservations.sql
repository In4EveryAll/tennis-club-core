-- ============================================================================
-- MIGRACIÓN: Agregar pistas (courts) y soporte para reservas
-- ============================================================================
-- Esta migración agrega:
-- 1. Tabla courts (pistas)
-- 2. Enum court_surface (tipo de superficie)
-- 3. Columna court_id en calendar_events para relacionar eventos con pistas
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 1. CREAR ENUM PARA TIPO DE SUPERFICIE
-- ----------------------------------------------------------------------------
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'court_surface') THEN
    CREATE TYPE court_surface AS ENUM (
      'CLAY',      -- Tierra batida
      'HARD',      -- Dura
      'GRASS'      -- Césped
    );
  END IF;
END$$;

-- ----------------------------------------------------------------------------
-- 2. CREAR TABLA COURTS (PISTAS)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS courts (
  id                      UUID                PRIMARY KEY DEFAULT gen_random_uuid(),
  name                    VARCHAR(200)        NOT NULL,          -- Nombre de la pista (ej: "Pista Central")
  image_url               VARCHAR(500),                          -- URL de imagen de la pista
  surface                 court_surface       NOT NULL,          -- Tipo de superficie
  is_active               BOOLEAN             NOT NULL DEFAULT TRUE,
  
  created_date            TIMESTAMP           NOT NULL DEFAULT NOW(),
  created_by              VARCHAR(100)        NOT NULL DEFAULT 'system',
  updated_date            TIMESTAMP           NOT NULL DEFAULT NOW(),
  updated_by              VARCHAR(100)        NOT NULL DEFAULT 'system',
  
  CONSTRAINT uq_court_name UNIQUE (name)
);

CREATE INDEX IF NOT EXISTS ix_courts_active ON courts(is_active) WHERE is_active = TRUE;
CREATE INDEX IF NOT EXISTS ix_courts_surface ON courts(surface);

-- ----------------------------------------------------------------------------
-- 3. AGREGAR COLUMNAS A calendar_events (court_id y user_email para reservas)
-- ----------------------------------------------------------------------------
DO $$
BEGIN
  -- Agregar columna court_id si no existe
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns 
    WHERE table_name = 'calendar_events' 
    AND column_name = 'court_id'
  ) THEN
    ALTER TABLE calendar_events 
    ADD COLUMN court_id UUID;
    
    -- Agregar foreign key
    ALTER TABLE calendar_events
    ADD CONSTRAINT fk_event_court
      FOREIGN KEY (court_id)
      REFERENCES courts(id)
      ON UPDATE CASCADE ON DELETE SET NULL;
    
    -- Crear índice para mejorar consultas de disponibilidad
    CREATE INDEX IF NOT EXISTS ix_events_court ON calendar_events(court_id) WHERE court_id IS NOT NULL;
    
    -- Índice compuesto para consultas de disponibilidad por pista y fecha
    CREATE INDEX IF NOT EXISTS ix_events_court_datetime 
      ON calendar_events(court_id, start_datetime, end_datetime) 
      WHERE court_id IS NOT NULL;
  END IF;
  
  -- Agregar columna user_email si no existe (para reservas)
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns 
    WHERE table_name = 'calendar_events' 
    AND column_name = 'user_email'
  ) THEN
    ALTER TABLE calendar_events 
    ADD COLUMN user_email VARCHAR(150);
    
    -- Agregar foreign key
    ALTER TABLE calendar_events
    ADD CONSTRAINT fk_event_user
      FOREIGN KEY (user_email)
      REFERENCES users(email)
      ON UPDATE CASCADE ON DELETE SET NULL;
    
    -- Crear índice para consultas de reservas por usuario
    CREATE INDEX IF NOT EXISTS ix_events_user ON calendar_events(user_email) WHERE user_email IS NOT NULL;
  END IF;
END$$;

-- ----------------------------------------------------------------------------
-- 4. COMENTARIOS PARA DOCUMENTACIÓN
-- ----------------------------------------------------------------------------
COMMENT ON TABLE courts IS 'Pistas del club de tenis. Cada pista puede tener reservas y clases asociadas.';
COMMENT ON COLUMN calendar_events.court_id IS 'Pista asociada al evento (clase o reserva). NULL si el evento no requiere pista.';

