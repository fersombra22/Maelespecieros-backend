-- =====================================================
-- V4 MAEL ESPECIEROS
-- AUDITORIA AUTOMATICA
-- =====================================================

PRAGMA foreign_keys = ON;

-- -----------------------------------------------------
-- AUDITORIA DE VENTAS
-- -----------------------------------------------------
CREATE TRIGGER trg_auditoria_venta_creada
AFTER INSERT ON ventas
BEGIN
    INSERT INTO auditoria (
        fecha,
        usuario,
        accion,
        descripcion
    )
    SELECT
        CURRENT_TIMESTAMP,
        u.username,
        'VENTA_CREADA',
        'Se creo la venta ' || NEW.numero_venta ||
        ' por un total de ' || printf('%.2f', NEW.total)
    FROM usuarios u
    WHERE u.id = NEW.usuario_id;
END;

CREATE TRIGGER trg_auditoria_venta_anulada
AFTER UPDATE OF estado ON ventas
WHEN OLD.estado = 'COMPLETADA' AND NEW.estado = 'ANULADA'
BEGIN
    INSERT INTO auditoria (
        fecha,
        usuario,
        accion,
        descripcion
    )
    SELECT
        CURRENT_TIMESTAMP,
        u.username,
        'VENTA_ANULADA',
        'Se anulo la venta ' || NEW.numero_venta ||
        '. Total original: ' || printf('%.2f', NEW.total)
    FROM usuarios u
    WHERE u.id = NEW.usuario_id;
END;

-- -----------------------------------------------------
-- AUDITORIA DE MOVIMIENTOS DE STOCK
-- -----------------------------------------------------
CREATE TRIGGER trg_auditoria_movimiento_stock
AFTER INSERT ON movimientos_stock
BEGIN
    INSERT INTO auditoria (
        fecha,
        usuario,
        accion,
        descripcion
    )
    SELECT
        CURRENT_TIMESTAMP,
        'SISTEMA_DB',
        'MOVIMIENTO_STOCK',
        'Producto ' || p.codigo_producto ||
        ' (' || p.nombre || '): ' || NEW.tipo_movimiento ||
        ', cantidad=' || NEW.cantidad ||
        ', stock ' || NEW.stock_anterior || ' -> ' || NEW.stock_nuevo ||
        CASE
            WHEN NEW.motivo IS NOT NULL AND NEW.motivo <> ''
                THEN ', motivo=' || NEW.motivo
            ELSE ''
        END
    FROM productos p
    WHERE p.id = NEW.producto_id;
END;

-- -----------------------------------------------------
-- AUDITORIA DE PRODUCTOS
-- Sin un usuario de contexto dentro de SQLite, estos eventos
-- quedan marcados como SISTEMA_DB. El actor real sigue estando
-- disponible en los logs de Spring Boot.
-- -----------------------------------------------------
CREATE TRIGGER trg_auditoria_producto_creado
AFTER INSERT ON productos
BEGIN
    INSERT INTO auditoria (
        fecha,
        usuario,
        accion,
        descripcion
    )
    VALUES (
        CURRENT_TIMESTAMP,
        'SISTEMA_DB',
        'PRODUCTO_CREADO',
        'Producto creado: ' || NEW.codigo_producto || ' - ' || NEW.nombre
    );
END;

CREATE TRIGGER trg_auditoria_producto_actualizado
AFTER UPDATE OF codigo_producto, nombre, descripcion, modelo,
               precio_efectivo, costo, stock_minimo, activo,
               categoria_id, hash_integridad ON productos
BEGIN
    INSERT INTO auditoria (
        fecha,
        usuario,
        accion,
        descripcion
    )
    VALUES (
        CURRENT_TIMESTAMP,
        'SISTEMA_DB',
        'PRODUCTO_ACTUALIZADO',
        'Producto actualizado: ' || NEW.codigo_producto || ' - ' || NEW.nombre
    );
END;

-- -----------------------------------------------------
-- AUDITORIA DE USUARIOS
-- Nunca se registra la contraseña.
-- -----------------------------------------------------
CREATE TRIGGER trg_auditoria_usuario_creado
AFTER INSERT ON usuarios
BEGIN
    INSERT INTO auditoria (
        fecha,
        usuario,
        accion,
        descripcion
    )
    VALUES (
        CURRENT_TIMESTAMP,
        'SISTEMA_DB',
        'USUARIO_CREADO',
        'Usuario creado: ' || NEW.username || ', rol=' || NEW.rol
    );
END;

CREATE TRIGGER trg_auditoria_usuario_actualizado
AFTER UPDATE ON usuarios
BEGIN
    INSERT INTO auditoria (
        fecha,
        usuario,
        accion,
        descripcion
    )
    VALUES (
        CURRENT_TIMESTAMP,
        'SISTEMA_DB',
        'USUARIO_ACTUALIZADO',
        'Usuario actualizado: ' || NEW.username ||
        ', rol=' || NEW.rol ||
        ', activo=' || NEW.activo ||
        ', bloqueado=' || NEW.bloqueado
    );
END;

-- -----------------------------------------------------
-- AUDITORIA DE CATEGORIAS
-- -----------------------------------------------------
CREATE TRIGGER trg_auditoria_categoria_creada
AFTER INSERT ON categorias
BEGIN
    INSERT INTO auditoria (
        fecha,
        usuario,
        accion,
        descripcion
    )
    VALUES (
        CURRENT_TIMESTAMP,
        'SISTEMA_DB',
        'CATEGORIA_CREADA',
        'Categoria creada: ' || NEW.nombre || ' (' || NEW.prefijo || ')'
    );
END;

CREATE TRIGGER trg_auditoria_categoria_actualizada
AFTER UPDATE ON categorias
BEGIN
    INSERT INTO auditoria (
        fecha,
        usuario,
        accion,
        descripcion
    )
    VALUES (
        CURRENT_TIMESTAMP,
        'SISTEMA_DB',
        'CATEGORIA_ACTUALIZADA',
        'Categoria actualizada: ' || NEW.nombre
    );
END;

-- -----------------------------------------------------
-- Fin V4
-- -----------------------------------------------------