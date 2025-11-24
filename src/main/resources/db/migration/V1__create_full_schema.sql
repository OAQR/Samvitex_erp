/**
 * V1: Creación del Esquema Multi-Almacén y Mejorado (Versión Final)
 * -----------------------------------------------------------------
 * Estructura de base de datos completa para SamVitex, incluyendo:
 * 1. Gestión de inventario multi-almacén con la tabla 'inventario_por_almacen'.
 * 2. Uso de ENUMs de PostgreSQL para consistencia de datos en estados y tipos.
 * 3. Campos de auditoría con claves foráneas a la tabla 'usuarios'.
 * 4. Políticas ON DELETE para proteger la integridad referencial.
 */

-- ===================================================================
-- SECCIÓN 1: TIPOS ENUMERADOS
-- ===================================================================
CREATE TYPE TIPO_MOVIMIENTO AS ENUM (
    'ENTRADA_COMPRA', 'SALIDA_VENTA', 'AJUSTE_POSITIVO', 'AJUSTE_NEGATIVO',
    'ENTRADA_DEVOLUCION_CLIENTE', 'SALIDA_DEVOLUCION_PROVEEDOR',
    'SALIDA_A_PRODUCCION', 'ENTRADA_POR_PRODUCCION', 'TRANSFERENCIA_SALIDA', 'TRANSFERENCIA_ENTRADA'
);
CREATE TYPE TIPO_TALLER AS ENUM ('INTERNO', 'EXTERNO');
CREATE TYPE ESTADO_TRANSACCION AS ENUM ('COMPLETADA', 'PENDIENTE', 'ANULADA');
CREATE TYPE TIPO_COMPROBANTE AS ENUM ('NOTA_DE_VENTA', 'BOLETA', 'FACTURA');

-- ===================================================================
-- SECCIÓN 2: AUTENTICACIÓN Y USUARIOS
-- ===================================================================
CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE usuarios (
    id SERIAL PRIMARY KEY,
    nombre_usuario VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    nombre_completo VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    rol_id INT NOT NULL REFERENCES roles(id),
    fecha_creacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ===================================================================
-- SECCIÓN 3: CATÁLOGOS DE NEGOCIO
-- ===================================================================
CREATE TABLE categorias (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(100) UNIQUE NOT NULL,
    descripcion TEXT,
    fecha_creacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usuario_creacion_id INT REFERENCES usuarios(id) ON DELETE SET NULL,
    fecha_modificacion TIMESTAMP WITH TIME ZONE,
    usuario_modificacion_id INT REFERENCES usuarios(id) ON DELETE SET NULL
);

CREATE TABLE proveedores (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(255) UNIQUE NOT NULL,
    ruc VARCHAR(20) UNIQUE,
    contacto_email VARCHAR(255),
    contacto_telefono VARCHAR(50),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usuario_creacion_id INT REFERENCES usuarios(id) ON DELETE SET NULL,
    fecha_modificacion TIMESTAMP WITH TIME ZONE,
    usuario_modificacion_id INT REFERENCES usuarios(id) ON DELETE SET NULL
);

CREATE TABLE almacenes (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(150) UNIQUE NOT NULL,
    ubicacion_descripcion TEXT,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usuario_creacion_id INT REFERENCES usuarios(id) ON DELETE SET NULL,
    fecha_modificacion TIMESTAMP WITH TIME ZONE,
    usuario_modificacion_id INT REFERENCES usuarios(id) ON DELETE SET NULL
);

CREATE TABLE clientes (
    id SERIAL PRIMARY KEY,
    nombre_completo VARCHAR(255) NOT NULL,
    dni_ruc VARCHAR(20) UNIQUE,
    email VARCHAR(255),
    telefono VARCHAR(50),
    direccion TEXT,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usuario_creacion_id INT REFERENCES usuarios(id) ON DELETE SET NULL,
    fecha_modificacion TIMESTAMP WITH TIME ZONE,
    usuario_modificacion_id INT REFERENCES usuarios(id) ON DELETE SET NULL
);

CREATE TABLE talleres (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(200) UNIQUE NOT NULL,
    direccion TEXT,
    persona_contacto VARCHAR(200),
    tipo TIPO_TALLER NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usuario_creacion_id INT REFERENCES usuarios(id) ON DELETE SET NULL,
    fecha_modificacion TIMESTAMP WITH TIME ZONE,
    usuario_modificacion_id INT REFERENCES usuarios(id) ON DELETE SET NULL
);

-- ===================================================================
-- SECCIÓN 4: NÚCLEO DEL INVENTARIO (PRODUCTOS Y STOCK)
-- ===================================================================
CREATE TABLE productos (
    id SERIAL PRIMARY KEY,
    sku VARCHAR(50) UNIQUE NOT NULL,
    nombre VARCHAR(255) NOT NULL,
    descripcion TEXT,
    precio_costo NUMERIC(10, 2) NOT NULL,
    precio_venta NUMERIC(10, 2) NOT NULL,
    stock_minimo INT NOT NULL DEFAULT 0,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    categoria_id INT REFERENCES categorias(id) ON DELETE SET NULL,
    proveedor_id INT REFERENCES proveedores(id) ON DELETE SET NULL,
    fecha_creacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usuario_creacion_id INT REFERENCES usuarios(id) ON DELETE SET NULL,
    fecha_modificacion TIMESTAMP WITH TIME ZONE,
    usuario_modificacion_id INT REFERENCES usuarios(id) ON DELETE SET NULL
);

CREATE TABLE inventario_por_almacen (
    id BIGSERIAL PRIMARY KEY,
    producto_id INT NOT NULL REFERENCES productos(id) ON DELETE CASCADE,
    almacen_id INT NOT NULL REFERENCES almacenes(id) ON DELETE CASCADE,
    cantidad INT NOT NULL DEFAULT 0 CHECK (cantidad >= 0),
    fecha_modificacion TIMESTAMP WITH TIME ZONE,
    usuario_modificacion_id INT REFERENCES usuarios(id) ON DELETE SET NULL,
    CONSTRAINT ux_producto_almacen UNIQUE (producto_id, almacen_id)
);

-- ===================================================================
-- SECCIÓN 5: TABLAS TRANSACCIONALES
-- ===================================================================
CREATE TABLE ventas (
    id BIGSERIAL PRIMARY KEY,
    cliente_id INT NOT NULL REFERENCES clientes(id) ON DELETE RESTRICT,
    usuario_id INT NOT NULL REFERENCES usuarios(id) ON DELETE RESTRICT,
    almacen_origen_id INT NOT NULL REFERENCES almacenes(id) ON DELETE RESTRICT, -- CORRECCIÓN: Columna añadida
    fecha_venta TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    subtotal NUMERIC(12, 2) NOT NULL,
    impuestos NUMERIC(12, 2) NOT NULL,
    total NUMERIC(12, 2) NOT NULL,
    estado ESTADO_TRANSACCION NOT NULL DEFAULT 'COMPLETADA',
    metodo_pago VARCHAR(50),
    tipo_comprobante TIPO_COMPROBANTE
);

CREATE TABLE ventas_detalle (
    id BIGSERIAL PRIMARY KEY,
    venta_id BIGINT NOT NULL REFERENCES ventas(id) ON DELETE CASCADE,
    producto_id INT NOT NULL REFERENCES productos(id) ON DELETE RESTRICT,
    cantidad INT NOT NULL CHECK (cantidad > 0),
    precio_unitario NUMERIC(10, 2) NOT NULL,
    subtotal_linea NUMERIC(12, 2) NOT NULL
);

CREATE TABLE compras (
    id BIGSERIAL PRIMARY KEY,
    proveedor_id INT NOT NULL REFERENCES proveedores(id) ON DELETE RESTRICT,
    usuario_id INT NOT NULL REFERENCES usuarios(id) ON DELETE RESTRICT,
    almacen_destino_id INT NOT NULL REFERENCES almacenes(id) ON DELETE RESTRICT,
    fecha_compra TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    referencia_factura VARCHAR(100),
    total NUMERIC(12, 2) NOT NULL,
    estado ESTADO_TRANSACCION NOT NULL DEFAULT 'COMPLETADA'
);

CREATE TABLE compras_detalle (
    id BIGSERIAL PRIMARY KEY,
    compra_id BIGINT NOT NULL REFERENCES compras(id) ON DELETE CASCADE,
    producto_id INT NOT NULL REFERENCES productos(id) ON DELETE RESTRICT,
    cantidad INT NOT NULL CHECK (cantidad > 0),
    costo_unitario_compra NUMERIC(10, 2) NOT NULL,
    subtotal_linea NUMERIC(12, 2) NOT NULL
);

CREATE TABLE ordenes_produccion (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(50) UNIQUE NOT NULL,
    estado VARCHAR(50) NOT NULL,
    taller_id INT NOT NULL REFERENCES talleres(id) ON DELETE RESTRICT,
    usuario_responsable_id INT NOT NULL REFERENCES usuarios(id) ON DELETE RESTRICT,
    almacen_insumos_id INT NOT NULL REFERENCES almacenes(id), -- CORRECCIÓN: Columna añadida
    almacen_destino_id INT NOT NULL REFERENCES almacenes(id), -- CORRECCIÓN: Columna añadida
    fecha_creacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_inicio_produccion TIMESTAMP WITH TIME ZONE,
    fecha_finalizacion TIMESTAMP WITH TIME ZONE
);

CREATE TABLE ordenes_produccion_detalle (
    id BIGSERIAL PRIMARY KEY,
    orden_produccion_id BIGINT NOT NULL REFERENCES ordenes_produccion(id) ON DELETE CASCADE,
    producto_id INT NOT NULL REFERENCES productos(id) ON DELETE RESTRICT,
    tipo_detalle VARCHAR(50) NOT NULL,
    cantidad INT NOT NULL
);

-- ===================================================================
-- SECCIÓN 6: KARDEX (MOVIMIENTOS DE INVENTARIO)
-- ===================================================================
CREATE TABLE movimientos_inventario (
    id BIGSERIAL PRIMARY KEY,
    producto_id INT NOT NULL REFERENCES productos(id) ON DELETE RESTRICT,
    almacen_id INT NOT NULL REFERENCES almacenes(id) ON DELETE RESTRICT,
    usuario_id INT NOT NULL REFERENCES usuarios(id) ON DELETE RESTRICT,
    tipo TIPO_MOVIMIENTO NOT NULL,
    cantidad_movida INT NOT NULL,
    stock_anterior INT NOT NULL CHECK (stock_anterior >= 0),
    stock_nuevo INT NOT NULL CHECK (stock_nuevo >= 0),
    fecha_movimiento TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    notas TEXT,
    venta_id BIGINT REFERENCES ventas(id) ON DELETE SET NULL,
    compra_id BIGINT REFERENCES compras(id) ON DELETE SET NULL,
    orden_produccion_id BIGINT REFERENCES ordenes_produccion(id) ON DELETE SET NULL,
    CONSTRAINT chk_movimiento_origen
        CHECK (
            (CASE WHEN venta_id IS NOT NULL THEN 1 ELSE 0 END) +
            (CASE WHEN compra_id IS NOT NULL THEN 1 ELSE 0 END) +
            (CASE WHEN orden_produccion_id IS NOT NULL THEN 1 ELSE 0 END)
            <= 1
        )
);

-- ===================================================================
-- SECCIÓN 7: ÍNDICES Y EXTENSIONES
-- ===================================================================
CREATE INDEX idx_productos_sku ON productos(sku);
CREATE INDEX idx_productos_nombre ON productos(nombre);
CREATE INDEX idx_inventario_producto_almacen ON inventario_por_almacen(producto_id, almacen_id);
CREATE INDEX idx_movimientos_producto_almacen_fecha ON movimientos_inventario(producto_id, almacen_id, fecha_movimiento);
CREATE INDEX idx_ventas_fecha ON ventas(fecha_venta);

CREATE EXTENSION IF NOT EXISTS unaccent;