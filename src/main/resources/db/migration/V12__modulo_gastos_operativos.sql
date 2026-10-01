-- =====================================================
-- V12 MAEL ESPECIEROS
-- MODULO DE GASTOS OPERATIVOS (EGRESOS DE CAJA)
-- SQLite + Flyway
-- =====================================================

CREATE TABLE IF NOT EXISTS gastos_operativos (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    monto NUMERIC(10, 2) NOT NULL,
    concepto VARCHAR(250) NOT NULL,
    categoria_gasto VARCHAR(50) NOT NULL,
    forma_pago VARCHAR(20) NOT NULL,
    comprobante_nro VARCHAR(50),
    fecha TIMESTAMP NOT NULL,
    anulado BOOLEAN NOT NULL DEFAULT 0,
    caja_id INTEGER NOT NULL REFERENCES cajas(id),
    usuario_id INTEGER NOT NULL REFERENCES usuarios(id),
    hash_integridad VARCHAR(64)
);

CREATE INDEX IF NOT EXISTS idx_gasto_caja
    ON gastos_operativos(caja_id);

CREATE INDEX IF NOT EXISTS idx_gasto_usuario
    ON gastos_operativos(usuario_id);

CREATE INDEX IF NOT EXISTS idx_gasto_fecha
    ON gastos_operativos(fecha);

CREATE INDEX IF NOT EXISTS idx_gasto_categoria
    ON gastos_operativos(categoria_gasto);
