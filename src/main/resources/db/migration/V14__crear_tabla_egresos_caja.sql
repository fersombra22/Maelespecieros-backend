-- =====================================================
-- V14: CREACIÓN DE TABLA DE EGRESOS Y RETIROS DE CAJA
-- Permite registrar extracciones en efectivo y virtuales
-- =====================================================

CREATE TABLE IF NOT EXISTS egresos_caja (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    caja_id INTEGER NOT NULL,
    usuario_id INTEGER NOT NULL,
    monto NUMERIC(10, 2) NOT NULL,
    tipo_medio VARCHAR(30) NOT NULL, -- 'EFECTIVO', 'TRANSFERENCIA'
    motivo VARCHAR(255) NOT NULL,
    comprobante VARCHAR(100),
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    hash_integridad VARCHAR(64),
    FOREIGN KEY (caja_id) REFERENCES cajas(id),
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

CREATE INDEX IF NOT EXISTS idx_egresos_caja_id ON egresos_caja(caja_id);
CREATE INDEX IF NOT EXISTS idx_egresos_fecha ON egresos_caja(fecha);
