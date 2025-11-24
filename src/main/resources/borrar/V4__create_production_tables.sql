-- V4__create_production_tables.sql

/**
 * Añade las nuevas tablas y modificaciones necesarias para el Módulo de Producción.
 */

-- Primero, se agregan los nuevos tipos al ENUM de movimientos de inventario.
-- Se usa IF NOT EXISTS para evitar errores si el script se ejecuta más de una vez.
ALTER TYPE TIPO_MOVIMIENTO ADD VALUE IF NOT EXISTS 'SALIDA_A_PRODUCCION';
ALTER TYPE TIPO_MOVIMIENTO ADD VALUE IF NOT EXISTS 'ENTRADA_POR_PRODUCCION';

-- Tabla de catálogo para los talleres
CREATE TABLE talleres (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(200) UNIQUE NOT NULL,
    direccion TEXT,
    persona_contacto VARCHAR(200),
    tipo VARCHAR(50) NOT NULL, -- 'INTERNO' o 'EXTERNO'
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

-- Tabla maestra para las órdenes de producción
CREATE TABLE ordenes_produccion (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(50) UNIQUE NOT NULL,
    estado VARCHAR(50) NOT NULL,
    taller_id INT NOT NULL,
    usuario_responsable_id INT NOT NULL,
    fecha_creacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_inicio_produccion TIMESTAMP WITH TIME ZONE,
    fecha_finalizacion TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_op_taller FOREIGN KEY(taller_id) REFERENCES talleres(id),
    CONSTRAINT fk_op_usuario FOREIGN KEY(usuario_responsable_id) REFERENCES usuarios(id)
);

-- Tabla de detalle para las órdenes de producción
CREATE TABLE ordenes_produccion_detalle (
    id BIGSERIAL PRIMARY KEY,
    orden_produccion_id BIGINT NOT NULL,
    producto_id INT NOT NULL,
    tipo_detalle VARCHAR(50) NOT NULL, -- 'INSUMO' o 'PRODUCTO_FINAL'
    cantidad INT NOT NULL,
    CONSTRAINT fk_opd_orden FOREIGN KEY(orden_produccion_id) REFERENCES ordenes_produccion(id) ON DELETE CASCADE,
    CONSTRAINT fk_opd_producto FOREIGN KEY(producto_id) REFERENCES productos(id)
);

-- Se modifica la tabla de movimientos para vincularlos a una orden de producción
ALTER TABLE movimientos_inventario ADD COLUMN orden_produccion_id BIGINT;
ALTER TABLE movimientos_inventario ADD CONSTRAINT fk_movimiento_op FOREIGN KEY(orden_produccion_id) REFERENCES ordenes_produccion(id);

-- Se actualiza la restricción para evitar solapamientos de claves foráneas
ALTER TABLE movimientos_inventario DROP CONSTRAINT IF EXISTS chk_movimiento_origen;
ALTER TABLE movimientos_inventario
    ADD CONSTRAINT chk_movimiento_origen
    CHECK (
        (CASE WHEN venta_id IS NOT NULL THEN 1 ELSE 0 END) +
        (CASE WHEN compra_id IS NOT NULL THEN 1 ELSE 0 END) +
        (CASE WHEN orden_produccion_id IS NOT NULL THEN 1 ELSE 0 END)
        <= 1
    );