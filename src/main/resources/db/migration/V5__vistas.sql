-- =====================================================
-- V5 MAEL ESPECIEROS
-- VISTAS DE CONSULTA Y REPORTES
-- =====================================================

PRAGMA foreign_keys = ON;

-- -----------------------------------------------------
-- Productos con stock bajo
-- -----------------------------------------------------
CREATE VIEW v_productos_stock_bajo AS
SELECT
    p.id,
    p.codigo_producto,
    p.nombre,
    p.modelo,
    c.nombre AS categoria,
    p.stock,
    p.stock_minimo,
    (p.stock_minimo - p.stock) AS faltante,
    p.precio_efectivo,
    p.activo
FROM productos p
JOIN categorias c ON c.id = p.categoria_id
WHERE p.activo = 1
  AND p.stock <= p.stock_minimo;

-- -----------------------------------------------------
-- Stock actual con categoria
-- -----------------------------------------------------
CREATE VIEW v_stock_actual AS
SELECT
    p.id,
    p.codigo_producto,
    p.nombre,
    p.modelo,
    c.id AS categoria_id,
    c.nombre AS categoria,
    p.stock,
    p.stock_minimo,
    CASE
        WHEN p.stock = 0 THEN 'SIN_STOCK'
        WHEN p.stock <= p.stock_minimo THEN 'STOCK_BAJO'
        ELSE 'NORMAL'
    END AS estado_stock,
    p.precio_efectivo,
    p.costo,
    p.activo
FROM productos p
JOIN categorias c ON c.id = p.categoria_id;

-- -----------------------------------------------------
-- Resumen de ventas
-- -----------------------------------------------------
CREATE VIEW v_ventas_resumen AS
SELECT
    v.id,
    v.numero_venta,
    v.fecha,
    v.subtotal,
    v.descuento,
    v.total,
    v.forma_pago,
    v.estado,
    u.id AS usuario_id,
    u.username,
    COUNT(d.id) AS cantidad_lineas,
    COALESCE(SUM(d.cantidad), 0) AS unidades_vendidas
FROM ventas v
JOIN usuarios u ON u.id = v.usuario_id
LEFT JOIN detalle_venta d ON d.venta_id = v.id
GROUP BY
    v.id,
    v.numero_venta,
    v.fecha,
    v.subtotal,
    v.descuento,
    v.total,
    v.forma_pago,
    v.estado,
    u.id,
    u.username;

-- -----------------------------------------------------
-- Productos mas vendidos
-- -----------------------------------------------------
CREATE VIEW v_productos_mas_vendidos AS
SELECT
    p.id AS producto_id,
    p.codigo_producto,
    p.nombre,
    p.modelo,
    c.nombre AS categoria,
    COALESCE(SUM(CASE WHEN v.estado = 'COMPLETADA' THEN d.cantidad ELSE 0 END), 0)
        AS unidades_vendidas,
    COALESCE(SUM(CASE WHEN v.estado = 'COMPLETADA' THEN d.subtotal ELSE 0 END), 0)
        AS importe_vendido
FROM productos p
JOIN categorias c ON c.id = p.categoria_id
LEFT JOIN detalle_venta d ON d.producto_id = p.id
LEFT JOIN ventas v ON v.id = d.venta_id
GROUP BY
    p.id,
    p.codigo_producto,
    p.nombre,
    p.modelo,
    c.nombre;

-- -----------------------------------------------------
-- Movimientos de stock enriquecidos
-- -----------------------------------------------------
CREATE VIEW v_movimientos_stock_detalle AS
SELECT
    m.id,
    m.fecha,
    m.producto_id,
    p.codigo_producto,
    p.nombre AS producto,
    c.nombre AS categoria,
    m.tipo_movimiento,
    m.cantidad,
    m.stock_anterior,
    m.stock_nuevo,
    m.motivo
FROM movimientos_stock m
JOIN productos p ON p.id = m.producto_id
JOIN categorias c ON c.id = p.categoria_id;

-- -----------------------------------------------------
-- Ventas por dia
-- -----------------------------------------------------
CREATE VIEW v_ventas_diarias AS
SELECT
    DATE(v.fecha) AS fecha,
    COUNT(*) AS cantidad_ventas,
    SUM(CASE WHEN v.estado = 'COMPLETADA' THEN 1 ELSE 0 END)
        AS ventas_completadas,
    SUM(CASE WHEN v.estado = 'ANULADA' THEN 1 ELSE 0 END)
        AS ventas_anuladas,
    COALESCE(SUM(CASE WHEN v.estado = 'COMPLETADA' THEN v.total ELSE 0 END), 0)
        AS total_vendido
FROM ventas v
GROUP BY DATE(v.fecha);

-- -----------------------------------------------------
-- Ventas por forma de pago
-- -----------------------------------------------------
CREATE VIEW v_ventas_por_forma_pago AS
SELECT
    v.forma_pago,
    COUNT(*) AS cantidad,
    COALESCE(SUM(CASE WHEN v.estado = 'COMPLETADA' THEN v.total ELSE 0 END), 0)
        AS total_completado,
    COALESCE(SUM(CASE WHEN v.estado = 'ANULADA' THEN v.total ELSE 0 END), 0)
        AS total_anulado
FROM ventas v
GROUP BY v.forma_pago;

-- -----------------------------------------------------
-- Auditoria visible para consultas del sistema
-- -----------------------------------------------------
CREATE VIEW v_auditoria_reciente AS
SELECT
    a.id,
    a.fecha,
    a.usuario,
    a.accion,
    a.descripcion
FROM auditoria a;

-- -----------------------------------------------------
-- Fin V5
-- -----------------------------------------------------