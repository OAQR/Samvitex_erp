-- V3__add_transaction_links_to_movements.sql
/**
 * Modifica la tabla de movimientos de inventario para vincular explícitamente
 * cada movimiento a la transacción que lo originó (una venta o una compra).
 * Esto mejora drásticamente la integridad de los datos y la trazabilidad.
 */

-- Eliminar la columna de referencia genérica que ya no se usará.
ALTER TABLE movimientos_inventario DROP COLUMN IF EXISTS referencia;

-- Añadir columnas para las claves foráneas. Se permiten nulos porque un
-- movimiento puede ser por una venta, por una compra, o por un ajuste (ninguno).
ALTER TABLE movimientos_inventario ADD COLUMN venta_id BIGINT;
ALTER TABLE movimientos_inventario ADD COLUMN compra_id BIGINT;

-- Añadir las restricciones de clave foránea.
ALTER TABLE movimientos_inventario
    ADD CONSTRAINT fk_movimiento_venta FOREIGN KEY (venta_id) REFERENCES ventas(id),
    ADD CONSTRAINT fk_movimiento_compra FOREIGN KEY (compra_id) REFERENCES compras(id);

-- Opcional: Añadir una restricción CHECK para asegurar que un movimiento no puede
-- estar vinculado a una venta Y a una compra al mismo tiempo.
ALTER TABLE movimientos_inventario
    ADD CONSTRAINT chk_movimiento_origen EXCLUDE (venta_id WITH =, compra_id WITH =) WHERE (venta_id IS NOT NULL AND compra_id IS NOT NULL);