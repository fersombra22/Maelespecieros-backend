-- =====================================================
-- V13 MAEL ESPECIEROS
-- OPTIMIZACION DE INDICES COMPUESTOS Y CONCURRENCIA WAL
-- =====================================================

-- 1. Activar modo WAL (Write-Ahead Logging) para lecturas concurrentes sin bloqueo
PRAGMA journal_mode = WAL;

-- 2. Indice compuesto para acelerar el cálculo de ventas por método de pago entre fechas
CREATE INDEX IF NOT EXISTS idx_ventas_estado_fecha_forma_pago
    ON ventas(estado, fecha, forma_pago, total);

-- 3. Indice compuesto para acelerar la búsqueda de cajas abiertas y ordenación cronológica
CREATE INDEX IF NOT EXISTS idx_cajas_estado_fecha_apertura
    ON cajas(estado, fecha_apertura DESC);
