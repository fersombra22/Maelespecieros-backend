-- =====================================================
-- V2 MAEL ESPECIEROS
-- INTEGRIDAD Y REGLAS DE BASE DE DATOS
-- SQLite + Flyway
-- =====================================================


-- =====================================================
-- UNICIDAD DE PRODUCTO DENTRO DE UNA VENTA
-- =====================================================
-- Un mismo producto no puede aparecer dos veces
-- en el detalle de una misma venta.
-- =====================================================

CREATE UNIQUE INDEX ux_detalle_venta_producto
ON detalle_venta(venta_id, producto_id);


-- =====================================================
-- VALIDACIONES DE PRODUCTO
-- =====================================================

CREATE TRIGGER trg_integridad_producto_insert
BEFORE INSERT ON productos
BEGIN

    SELECT RAISE(
        ABORT,
        'El stock no puede ser negativo'
    )
    WHERE NEW.stock < 0;

    SELECT RAISE(
        ABORT,
        'El stock minimo no puede ser negativo'
    )
    WHERE NEW.stock_minimo < 0;

    SELECT RAISE(
        ABORT,
        'El precio efectivo no puede ser negativo'
    )
    WHERE NEW.precio_efectivo < 0;

    SELECT RAISE(
        ABORT,
        'El costo no puede ser negativo'
    )
    WHERE NEW.costo < 0;

END;


CREATE TRIGGER trg_integridad_producto_update
BEFORE UPDATE OF stock, stock_minimo, precio_efectivo, costo
ON productos
BEGIN

    SELECT RAISE(
        ABORT,
        'El stock no puede ser negativo'
    )
    WHERE NEW.stock < 0;

    SELECT RAISE(
        ABORT,
        'El stock minimo no puede ser negativo'
    )
    WHERE NEW.stock_minimo < 0;

    SELECT RAISE(
        ABORT,
        'El precio efectivo no puede ser negativo'
    )
    WHERE NEW.precio_efectivo < 0;

    SELECT RAISE(
        ABORT,
        'El costo no puede ser negativo'
    )
    WHERE NEW.costo < 0;

END;


-- =====================================================
-- VALIDACIONES DEL DETALLE DE VENTA
-- =====================================================

CREATE TRIGGER trg_integridad_detalle_insert
BEFORE INSERT ON detalle_venta
BEGIN

    SELECT RAISE(
        ABORT,
        'La cantidad vendida debe ser mayor que cero'
    )
    WHERE NEW.cantidad <= 0;

    SELECT RAISE(
        ABORT,
        'El precio unitario no puede ser negativo'
    )
    WHERE NEW.precio_unitario < 0;

    SELECT RAISE(
        ABORT,
        'El subtotal no puede ser negativo'
    )
    WHERE NEW.subtotal < 0;

    SELECT RAISE(
        ABORT,
        'El subtotal del detalle no coincide con cantidad por precio'
    )
    WHERE ROUND(NEW.subtotal, 2)
          <> ROUND(NEW.cantidad * NEW.precio_unitario, 2);

END;


-- =====================================================
-- INMUTABILIDAD DEL DETALLE
-- =====================================================

CREATE TRIGGER trg_integridad_detalle_update
BEFORE UPDATE ON detalle_venta
BEGIN

    SELECT RAISE(
        ABORT,
        'Los detalles de una venta no pueden modificarse'
    )
    WHERE EXISTS (
        SELECT 1
        FROM ventas v
        WHERE v.id = OLD.venta_id
          AND v.estado IN ('COMPLETADA', 'ANULADA')
    );

END;


CREATE TRIGGER trg_integridad_detalle_delete
BEFORE DELETE ON detalle_venta
BEGIN

    SELECT RAISE(
        ABORT,
        'Los detalles de una venta no pueden eliminarse'
    )
    WHERE EXISTS (
        SELECT 1
        FROM ventas v
        WHERE v.id = OLD.venta_id
          AND v.estado IN ('COMPLETADA', 'ANULADA')
    );

END;


-- =====================================================
-- VALIDACIONES FINANCIERAS DE VENTAS
-- =====================================================

CREATE TRIGGER trg_integridad_venta_insert
BEFORE INSERT ON ventas
BEGIN

    SELECT RAISE(
        ABORT,
        'El subtotal no puede ser negativo'
    )
    WHERE NEW.subtotal < 0;

    SELECT RAISE(
        ABORT,
        'El descuento no puede ser negativo'
    )
    WHERE NEW.descuento < 0;

    SELECT RAISE(
        ABORT,
        'El descuento no puede superar el subtotal'
    )
    WHERE NEW.descuento > NEW.subtotal;

    SELECT RAISE(
        ABORT,
        'El total no puede ser negativo'
    )
    WHERE NEW.total < 0;

    SELECT RAISE(
        ABORT,
        'El total no coincide con subtotal menos descuento'
    )
    WHERE ROUND(NEW.total, 2)
          <> ROUND(NEW.subtotal - NEW.descuento, 2);

END;


CREATE TRIGGER trg_integridad_venta_update
BEFORE UPDATE OF subtotal, descuento, total
ON ventas
BEGIN

    SELECT RAISE(
        ABORT,
        'El subtotal no puede ser negativo'
    )
    WHERE NEW.subtotal < 0;

    SELECT RAISE(
        ABORT,
        'El descuento no puede ser negativo'
    )
    WHERE NEW.descuento < 0;

    SELECT RAISE(
        ABORT,
        'El descuento no puede superar el subtotal'
    )
    WHERE NEW.descuento > NEW.subtotal;

    SELECT RAISE(
        ABORT,
        'El total no puede ser negativo'
    )
    WHERE NEW.total < 0;

    SELECT RAISE(
        ABORT,
        'El total no coincide con subtotal menos descuento'
    )
    WHERE ROUND(NEW.total, 2)
          <> ROUND(NEW.subtotal - NEW.descuento, 2);

END;


-- =====================================================
-- CONTROL DE ESTADO DE LA VENTA
-- =====================================================

CREATE TRIGGER trg_integridad_estado_venta
BEFORE UPDATE OF estado ON ventas
WHEN OLD.estado = 'ANULADA'
 AND NEW.estado <> 'ANULADA'
BEGIN

    SELECT RAISE(
        ABORT,
        'Una venta anulada no puede volver a completarse'
    );

END;


CREATE TRIGGER trg_integridad_transicion_venta
BEFORE UPDATE OF estado ON ventas
WHEN OLD.estado = 'COMPLETADA'
 AND NEW.estado NOT IN ('COMPLETADA', 'ANULADA')
BEGIN

    SELECT RAISE(
        ABORT,
        'Estado de venta no permitido'
    );

END;


-- =====================================================
-- VALIDACIONES DE MOVIMIENTOS DE STOCK
-- =====================================================

CREATE TRIGGER trg_integridad_movimiento_insert
BEFORE INSERT ON movimientos_stock
BEGIN

    SELECT RAISE(
        ABORT,
        'La cantidad del movimiento debe ser mayor que cero'
    )
    WHERE NEW.cantidad <= 0;

    SELECT RAISE(
        ABORT,
        'El stock anterior no puede ser negativo'
    )
    WHERE NEW.stock_anterior < 0;

    SELECT RAISE(
        ABORT,
        'El stock nuevo no puede ser negativo'
    )
    WHERE NEW.stock_nuevo < 0;

    SELECT RAISE(
        ABORT,
        'Tipo de movimiento no permitido'
    )
    WHERE NEW.tipo_movimiento NOT IN (
        'ENTRADA',
        'VENTA',
        'ANULACION_VENTA',
        'AJUSTE',
        'DEVOLUCION'
    );

END;


-- =====================================================
-- INMUTABILIDAD DEL HISTORIAL DE STOCK
-- =====================================================

CREATE TRIGGER trg_inmutabilidad_movimiento_update
BEFORE UPDATE ON movimientos_stock
BEGIN

    SELECT RAISE(
        ABORT,
        'Los movimientos de stock son inmutables'
    );

END;


CREATE TRIGGER trg_inmutabilidad_movimiento_delete
BEFORE DELETE ON movimientos_stock
BEGIN

    SELECT RAISE(
        ABORT,
        'Los movimientos de stock son inmutables'
    );

END;


-- =====================================================
-- FIN V2
-- =====================================================