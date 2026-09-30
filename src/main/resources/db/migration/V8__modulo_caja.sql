-- =====================================================
-- V8 MAEL ESPECIEROS
-- MODULO DE APERTURA Y CIERRE DE CAJA
-- SQLite + Flyway
-- =====================================================

CREATE TABLE IF NOT EXISTS cajas (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    monto_inicial NUMERIC(10, 2) NOT NULL,
    monto_final NUMERIC(10, 2),
    monto_ventas NUMERIC(10, 2),
    diferencia NUMERIC(10, 2),
    fecha_apertura TIMESTAMP NOT NULL,
    fecha_cierre TIMESTAMP,
    estado VARCHAR(20) NOT NULL,
    observaciones VARCHAR(500),
    usuario_id INTEGER NOT NULL REFERENCES usuarios(id)
);

CREATE INDEX IF NOT EXISTS idx_caja_estado
    ON cajas(estado);

CREATE INDEX IF NOT EXISTS idx_caja_fecha_apertura
    ON cajas(fecha_apertura);

CREATE INDEX IF NOT EXISTS idx_caja_usuario
    ON cajas(usuario_id);
