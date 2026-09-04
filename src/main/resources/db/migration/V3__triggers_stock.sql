-- =====================================================
-- V3 MAEL ESPECIEROS
-- TRIGGERS DE STOCK
-- =====================================================

PRAGMA foreign_keys = ON;

-- -----------------------------------------------------
-- 1. Validar stock disponible al registrar el detalle.
-- -----------------------------------------------------
CREATE TRIGGER trg_stock_validar_venta
BEFORE INSERT ON detalle_venta
BEGIN
    SELECT RAISE(ABORT, 'Stock insuficiente para realizar la venta')
    WHERE NOT EXISTS (
        SELECT 1
        FROM productos p
        JOIN ventas v ON v.id = NEW.venta_id
        WHERE p.id = NEW.producto_id
          AND p.activo = 1
          AND v.estado = 'COMPLETADA'
          AND p.stock >= NEW.cantidad
    );
END;

-- -----------------------------------------------------
-- 2. Descontar stock y registrar el movimiento.
-- -----------------------------------------------------
CREATE TRIGGER trg_stock_venta
AFTER INSERT ON detalle_venta
BEGIN
    INSERT INTO movimientos_stock (
        producto_id,
        tipo_movimiento,
        cantidad,
        stock_anterior,
        stock_nuevo,
        motivo,
        fecha
    )
    SELECT
        p.id,
        'VENTA',
        NEW.cantidad,
        p.stock,
        p.stock - NEW.cantidad,
        'Venta ' || v.numero_venta,
        CURRENT_TIMESTAMP
    FROM productos p
    JOIN ventas v ON v.id = NEW.venta_id
    WHERE p.id = NEW.producto_id;

    UPDATE productos
    SET stock = stock - NEW.cantidad,
        fecha_actualizacion = CURRENT_TIMESTAMP
    WHERE id = NEW.producto_id;
END;

-- -----------------------------------------------------
-- 3. Al anular una venta, devolver sus cantidades.
-- -----------------------------------------------------
CREATE TRIGGER trg_stock_anulacion_venta
AFTER UPDATE OF estado ON ventas
WHEN OLD.estado = 'COMPLETADA' AND NEW.estado = 'ANULADA'
BEGIN
    INSERT INTO movimientos_stock (
        producto_id,
        tipo_movimiento,
        cantidad,
        stock_anterior,
        stock_nuevo,
        motivo,
        fecha
    )
    SELECT
        d.producto_id,
        'ANULACION_VENTA',
        d.cantidad,
        p.stock,
        p.stock + d.cantidad,
        'Anulacion venta ' || NEW.numero_venta,
        CURRENT_TIMESTAMP
    FROM detalle_venta d
    JOIN productos p ON p.id = d.producto_id
    WHERE d.venta_id = NEW.id;

    UPDATE productos
    SET stock = stock + (
        SELECT COALESCE(SUM(d.cantidad), 0)
        FROM detalle_venta d
        WHERE d.venta_id = NEW.id
          AND d.producto_id = productos.id
    ),
    fecha_actualizacion = CURRENT_TIMESTAMP
    WHERE id IN (
        SELECT DISTINCT d.producto_id
        FROM detalle_venta d
        WHERE d.venta_id = NEW.id
    );
END;

-- -----------------------------------------------------
-- 4. Entrada manual de stock desde SQL/JPA.
-- Se actualiza stock y se deja trazabilidad.
-- IMPORTANTE: Java debe insertar solo el movimiento; el trigger
-- hace el UPDATE del producto.
-- -----------------------------------------------------
CREATE TRIGGER trg_stock_entrada_manual
AFTER INSERT ON movimientos_stock
WHEN NEW.tipo_movimiento IN ('ENTRADA', 'AJUSTE', 'DEVOLUCION')
BEGIN
    UPDATE productos
    SET stock = NEW.stock_nuevo,
        fecha_actualizacion = CURRENT_TIMESTAMP
    WHERE id = NEW.producto_id
      AND stock = NEW.stock_anterior;

    SELECT RAISE(ABORT, 'El stock del producto cambio antes de registrar el movimiento')
    WHERE changes() = 0;
END;

-- -----------------------------------------------------
-- NOTA:
-- Las altas de stock deben registrar un movimiento con:
--   stock_anterior = stock actual
--   stock_nuevo    = stock actual + cantidad
-- o el valor resultante que corresponda para un AJUSTE.
-- Para VENTA/ANULACION_VENTA el control lo realizan los triggers
-- anteriores y Java no debe modificar stock directamente.
-- -----------------------------------------------------

-- -----------------------------------------------------
-- Fin V3
-- -----------------------------------------------------