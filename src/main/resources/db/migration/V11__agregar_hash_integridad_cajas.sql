-- =====================================================
-- V11 MAEL ESPECIEROS
-- AGREGAR SELLO DE INTEGRIDAD CRIPTOGRAFICA A CAJAS
-- SQLite + Flyway
-- =====================================================

ALTER TABLE cajas ADD COLUMN hash_integridad VARCHAR(64);
