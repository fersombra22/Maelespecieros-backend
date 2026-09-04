-- =====================================================
-- V1 MAEL ESPECIEROS
-- ESTRUCTURA BASE DE PRODUCCION
-- SQLite + Flyway
-- =====================================================

-- =====================================================
-- CATEGORIAS
-- =====================================================

CREATE TABLE categorias (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    descripcion VARCHAR(250),
    prefijo VARCHAR(5) NOT NULL UNIQUE,
    ultimo_numero INTEGER NOT NULL DEFAULT 0,
    activo BOOLEAN NOT NULL DEFAULT 1
);

CREATE INDEX idx_categoria_nombre
    ON categorias(nombre);


-- =====================================================
-- USUARIOS
-- =====================================================

CREATE TABLE usuarios (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre VARCHAR(80) NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    rol VARCHAR(20) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT 1,
    cambio_password_pendiente BOOLEAN NOT NULL DEFAULT 1,
    intentos_fallidos INTEGER NOT NULL DEFAULT 0,
    bloqueado BOOLEAN NOT NULL DEFAULT 0,
    fecha_bloqueo TIMESTAMP,
    ultimo_login TIMESTAMP,
    ultimo_cambio_password TIMESTAMP,
    fecha_alta TIMESTAMP NOT NULL,
    fecha_actualizacion TIMESTAMP,

    CHECK (
        rol IN (
            'SUPER_ADMIN',
            'ADMIN',
            'EMPLEADO'
        )
    )
);

CREATE INDEX idx_usuario_username
    ON usuarios(username);

CREATE INDEX idx_usuario_rol
    ON usuarios(rol);

CREATE INDEX idx_usuario_activo
    ON usuarios(activo);

CREATE INDEX idx_usuario_bloqueado
    ON usuarios(bloqueado);


-- =====================================================
-- NUMERADORES
-- =====================================================

CREATE TABLE numeradores (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    tipo VARCHAR(30) NOT NULL UNIQUE,
    ultimo_numero INTEGER NOT NULL DEFAULT 0
);


-- =====================================================
-- PRODUCTOS
-- =====================================================

CREATE TABLE productos (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    codigo_producto VARCHAR(20) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(300),
    modelo VARCHAR(100) NOT NULL,
    precio_efectivo DECIMAL(10,2) NOT NULL,
    costo DECIMAL(10,2) NOT NULL,
    stock INTEGER NOT NULL DEFAULT 0,
    stock_minimo INTEGER NOT NULL DEFAULT 0,
    activo BOOLEAN NOT NULL DEFAULT 1,
    fecha_alta TIMESTAMP NOT NULL,
    fecha_actualizacion TIMESTAMP,
    hash_integridad VARCHAR(64),
    categoria_id INTEGER NOT NULL,

    FOREIGN KEY (categoria_id)
        REFERENCES categorias(id)
);

CREATE INDEX idx_producto_codigo
    ON productos(codigo_producto);

CREATE INDEX idx_producto_nombre
    ON productos(nombre);

CREATE INDEX idx_producto_categoria
    ON productos(categoria_id);

CREATE INDEX idx_producto_activo
    ON productos(activo);

CREATE INDEX idx_producto_stock
    ON productos(stock);


-- =====================================================
-- MOVIMIENTOS STOCK
-- HISTORIAL
-- =====================================================

CREATE TABLE movimientos_stock (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    producto_id INTEGER NOT NULL,
    tipo_movimiento VARCHAR(30) NOT NULL,
    cantidad INTEGER NOT NULL,
    stock_anterior INTEGER NOT NULL,
    stock_nuevo INTEGER NOT NULL,
    motivo VARCHAR(250),
    fecha TIMESTAMP NOT NULL,

    FOREIGN KEY (producto_id)
        REFERENCES productos(id)
);

CREATE INDEX idx_movimiento_producto
    ON movimientos_stock(producto_id);

CREATE INDEX idx_movimiento_fecha
    ON movimientos_stock(fecha);

CREATE INDEX idx_movimiento_tipo
    ON movimientos_stock(tipo_movimiento);

CREATE INDEX idx_movimiento_producto_fecha
    ON movimientos_stock(producto_id, fecha);


-- =====================================================
-- VENTAS
-- =====================================================

CREATE TABLE ventas (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    numero_venta VARCHAR(20) NOT NULL UNIQUE,
    fecha TIMESTAMP NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    descuento DECIMAL(10,2) NOT NULL DEFAULT 0,
    total DECIMAL(10,2) NOT NULL,
    forma_pago VARCHAR(20) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    hash_integridad VARCHAR(64),
    usuario_id INTEGER NOT NULL,

    FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id),

    CHECK (
        forma_pago IN (
            'EFECTIVO',
            'TRANSFERENCIA',
            'TARJETA',
            'CREDITO',
            'DEBITO'
        )
    ),

    CHECK (
        estado IN (
            'COMPLETADA',
            'ANULADA'
        )
    )
);

CREATE INDEX idx_venta_numero
    ON ventas(numero_venta);

CREATE INDEX idx_venta_fecha
    ON ventas(fecha);

CREATE INDEX idx_venta_usuario
    ON ventas(usuario_id);

CREATE INDEX idx_venta_estado
    ON ventas(estado);

CREATE INDEX idx_venta_forma_pago
    ON ventas(forma_pago);


-- =====================================================
-- DETALLE DE VENTA
-- =====================================================

CREATE TABLE detalle_venta (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    venta_id INTEGER NOT NULL,
    producto_id INTEGER NOT NULL,
    cantidad INTEGER NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,

    FOREIGN KEY (venta_id)
        REFERENCES ventas(id)
        ON DELETE CASCADE,

    FOREIGN KEY (producto_id)
        REFERENCES productos(id)
);

CREATE INDEX idx_detalle_venta
    ON detalle_venta(venta_id);

CREATE INDEX idx_detalle_producto
    ON detalle_venta(producto_id);

CREATE INDEX idx_detalle_venta_producto
    ON detalle_venta(venta_id, producto_id);


-- =====================================================
-- AUDITORIA
-- EVENTOS DEL SISTEMA
-- =====================================================

CREATE TABLE auditoria (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    fecha TIMESTAMP NOT NULL,
    usuario VARCHAR(50) NOT NULL,
    accion VARCHAR(100) NOT NULL,
    descripcion VARCHAR(300)
);

CREATE INDEX idx_auditoria_fecha
    ON auditoria(fecha);

CREATE INDEX idx_auditoria_usuario
    ON auditoria(usuario);

CREATE INDEX idx_auditoria_accion
    ON auditoria(accion);

CREATE INDEX idx_auditoria_usuario_fecha
    ON auditoria(usuario, fecha);


-- =====================================================
-- BLOCKCHAIN AUDITORIA SEGURA
-- =====================================================

CREATE TABLE blockchain_auditoria (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    uuid VARCHAR(36) NOT NULL UNIQUE,
    fecha TIMESTAMP NOT NULL,
    usuario VARCHAR(100) NOT NULL,
    accion VARCHAR(100) NOT NULL,
    descripcion TEXT NOT NULL,
    hash_actual VARCHAR(64) NOT NULL UNIQUE,
    hash_anterior VARCHAR(64) NOT NULL,
    version VARCHAR(20) NOT NULL,
    algoritmo VARCHAR(30) NOT NULL,
    nonce BIGINT NOT NULL,
    firma_hmac VARCHAR(64) NOT NULL
);

CREATE INDEX idx_blockchain_uuid
    ON blockchain_auditoria(uuid);

CREATE INDEX idx_blockchain_fecha
    ON blockchain_auditoria(fecha);

CREATE INDEX idx_blockchain_usuario
    ON blockchain_auditoria(usuario);

CREATE INDEX idx_blockchain_accion
    ON blockchain_auditoria(accion);

CREATE INDEX idx_blockchain_hash_actual
    ON blockchain_auditoria(hash_actual);

CREATE INDEX idx_blockchain_hash_anterior
    ON blockchain_auditoria(hash_anterior);

CREATE INDEX idx_blockchain_usuario_fecha
    ON blockchain_auditoria(usuario, fecha);


-- =====================================================
-- NUMERADOR INICIAL
-- =====================================================

INSERT INTO numeradores (
    tipo,
    ultimo_numero
)
VALUES (
    'VENTA',
    0
);

-- =====================================================
-- FIN V1
-- =====================================================