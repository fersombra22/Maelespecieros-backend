-- =====================================================
-- V5 MAEL ESPECIEROS
-- BLOCKCHAIN AVANZADA Y CENTRALIZACION DE STOCK
-- =====================================================

-- 1. Eliminar triggers de la tabla detalle_venta y ventas que afectaban al stock (V3)
DROP TRIGGER IF EXISTS trg_stock_venta;
DROP TRIGGER IF EXISTS trg_stock_anulacion_venta;
DROP TRIGGER IF EXISTS trg_stock_entrada_manual;

-- 2. Eliminar triggers de auditoria automatica local (V4)
DROP TRIGGER IF EXISTS trg_auditoria_venta_creada;
DROP TRIGGER IF EXISTS trg_auditoria_venta_anulada;
DROP TRIGGER IF EXISTS trg_auditoria_movimiento_stock;
DROP TRIGGER IF EXISTS trg_auditoria_producto_creado;
DROP TRIGGER IF EXISTS trg_auditoria_producto_actualizado;
DROP TRIGGER IF EXISTS trg_auditoria_categoria_creada;
DROP TRIGGER IF EXISTS trg_auditoria_categoria_actualizada;
DROP TRIGGER IF EXISTS trg_auditoria_usuario_creado;
DROP TRIGGER IF EXISTS trg_auditoria_usuario_actualizado;

-- 3. Agregar campos avanzados a la blockchain
ALTER TABLE blockchain_auditoria ADD COLUMN entidad_tipo VARCHAR(100);
ALTER TABLE blockchain_auditoria ADD COLUMN entidad_id VARCHAR(100);
ALTER TABLE blockchain_auditoria ADD COLUMN payload_json TEXT;

-- 4. Triggers Anti-Hackeo para asegurar inmutabilidad total a nivel de motor SQL
CREATE TRIGGER trg_blockchain_no_update
BEFORE UPDATE ON blockchain_auditoria
BEGIN
    SELECT RAISE(ABORT, 'ALERTA DE SEGURIDAD CRITICA: La blockchain es inmutable. Intento de UPDATE detectado y bloqueado.');
END;

CREATE TRIGGER trg_blockchain_no_delete
BEFORE DELETE ON blockchain_auditoria
BEGIN
    SELECT RAISE(ABORT, 'ALERTA DE SEGURIDAD CRITICA: La blockchain es inmutable. Intento de DELETE detectado y bloqueado.');
END;
