ALTER TABLE ventas ADD COLUMN cliente_id INTEGER REFERENCES clientes(id);
