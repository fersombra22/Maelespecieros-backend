-- =====================================================
-- V12 MAEL ESPECIEROS
-- AJUSTE DE TRIGGERS PARA COHERENCIA CON JPA Y HASH CRIPTOGRAFICO
-- =====================================================

-- Eliminamos los triggers que duplican la baja de stock
-- o causan conflictos con los eventos auditados por Spring Boot (InventoryService)
DROP TRIGGER IF EXISTS trg_stock_venta;
DROP TRIGGER IF EXISTS trg_stock_anulacion_venta;
DROP TRIGGER IF EXISTS trg_stock_entrada_manual;

-- Trigger para asegurar que no se inserte un detalle de venta sin stock suficiente
CREATE TRIGGER IF NOT EXISTS trg_stock_validar_venta
BEFORE INSERT ON detalle_venta
BEGIN
    SELECT RAISE(ABORT, 'Stock insuficiente para realizar la venta')
    WHERE NOT EXISTS (
        SELECT 1
        FROM productos p
        WHERE p.id = NEW.producto_id
          AND p.activo = 1
          AND p.stock >= NEW.cantidad
    );
END;
