-- =====================================================
-- V7 MAEL ESPECIEROS
-- MODULO DE GESTION DE CLIENTES
-- SQLite + Flyway
-- =====================================================

CREATE TABLE IF NOT EXISTS clientes (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre VARCHAR(80) NOT NULL,
    apellido VARCHAR(80) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    telefono VARCHAR(30),
    direccion VARCHAR(200),
    activo BOOLEAN NOT NULL DEFAULT 1,
    fecha_alta TIMESTAMP NOT NULL,
    fecha_actualizacion TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_cliente_email
    ON clientes(email);

CREATE INDEX IF NOT EXISTS idx_cliente_apellido_nombre
    ON clientes(apellido, nombre);

CREATE INDEX IF NOT EXISTS idx_cliente_activo
    ON clientes(activo);
