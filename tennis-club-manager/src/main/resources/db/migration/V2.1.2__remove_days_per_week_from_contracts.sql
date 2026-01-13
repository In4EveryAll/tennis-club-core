-- Eliminar columna days_per_week de la tabla contracts
-- Esta columna ya no tiene sentido de negocio porque:
-- - Los servicios trimestrales ya definen el día (dayOfWeek)
-- - El alumno asiste todas las semanas de ese día
-- - daysPerWeek no tiene sentido de negocio

DO $$
BEGIN
    -- Eliminar la columna si existe
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'contracts' 
        AND column_name = 'days_per_week'
    ) THEN
        ALTER TABLE contracts DROP COLUMN days_per_week;
    END IF;
END$$;

-- Comentario para documentar el cambio
COMMENT ON TABLE contracts IS 'Contratos entre usuarios y servicios del club. Los servicios trimestrales usan service.dayOfWeek para definir el día de la semana.';






