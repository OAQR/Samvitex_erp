/**
 * V1: Creación de Toda la Estructura de la Base de Datos
 * ------------------------------------------------------
 * Este script consolidado crea todas las tablas, tipos y restricciones
 * necesarios para el funcionamiento inicial de la aplicación SamVitex.
 * No contiene datos de negocio (productos, clientes, etc.), los cuales
 * serán insertados en una migración posterior (V2).
 */

-- ===================================================================
-- SECCIÓN 1: AUTENTICACIÓN Y USUARIOS
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
    rol_id INT NOT NULL,
    fecha_creacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_rol FOREIGN KEY(rol_id) REFERENCES roles(id)
);

-- ===================================================================
-- SECCIÓN 2: CATÁLOGOS DE INVENTARIO
-- ===================================================================

CREATE TABLE categorias (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(100) UNIQUE NOT NULL,
    descripcion TEXT
);

CREATE TABLE proveedores (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(255) UNIQUE NOT NULL,
    ruc VARCHAR(20) UNIQUE,
    contacto_email VARCHAR(255),
    contacto_telefono VARCHAR(50),
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE almacenes (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(150) UNIQUE NOT NULL,
    ubicacion_descripcion TEXT,
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE productos (
    id SERIAL PRIMARY KEY,
    sku VARCHAR(50) UNIQUE NOT NULL,
    nombre VARCHAR(255) NOT NULL,
    descripcion TEXT,
    precio_costo NUMERIC(10, 2) NOT NULL,
    precio_venta NUMERIC(10, 2) NOT NULL,
    cantidad INT NOT NULL DEFAULT 0 CHECK (cantidad >= 0),
    stock_minimo INT NOT NULL DEFAULT 0,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    categoria_id INT,
    proveedor_id INT,
    almacen_id INT,
    CONSTRAINT fk_categoria FOREIGN KEY(categoria_id) REFERENCES categorias(id),
    CONSTRAINT fk_proveedor FOREIGN KEY(proveedor_id) REFERENCES proveedores(id),
    CONSTRAINT fk_almacen FOREIGN KEY(almacen_id) REFERENCES almacenes(id)
);

-- ===================================================================
-- SECCIÓN 3: TRANSACCIONES (VENTAS, COMPRAS, MOVIMIENTOS)
-- ===================================================================

CREATE TABLE clientes (
    id SERIAL PRIMARY KEY,
    nombre_completo VARCHAR(255) NOT NULL,
    dni_ruc VARCHAR(20) UNIQUE,
    email VARCHAR(255),
    telefono VARCHAR(50),
    direccion TEXT,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_registro TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE ventas (
    id BIGSERIAL PRIMARY KEY,
    cliente_id INT NOT NULL,
    usuario_id INT NOT NULL,
    fecha_venta TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    subtotal NUMERIC(12, 2) NOT NULL,
    impuestos NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    total NUMERIC(12, 2) NOT NULL,
    estado VARCHAR(50) NOT NULL DEFAULT 'COMPLETADA',
    CONSTRAINT fk_cliente_venta FOREIGN KEY(cliente_id) REFERENCES clientes(id),
    CONSTRAINT fk_usuario_venta FOREIGN KEY(usuario_id) REFERENCES usuarios(id)
);

CREATE TABLE ventas_detalle (
    id BIGSERIAL PRIMARY KEY,
    venta_id BIGINT NOT NULL,
    producto_id INT NOT NULL,
    cantidad INT NOT NULL CHECK (cantidad > 0),
    precio_unitario NUMERIC(10, 2) NOT NULL,
    subtotal_linea NUMERIC(12, 2) NOT NULL,
    CONSTRAINT fk_venta_detalle FOREIGN KEY(venta_id) REFERENCES ventas(id),
    CONSTRAINT fk_producto_detalle FOREIGN KEY(producto_id) REFERENCES productos(id)
);

CREATE TABLE compras (
    id BIGSERIAL PRIMARY KEY,
    proveedor_id INT NOT NULL,
    usuario_id INT NOT NULL,
    fecha_compra TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    referencia_factura VARCHAR(100),
    total NUMERIC(12, 2) NOT NULL CHECK (total >= 0),
    estado VARCHAR(50) NOT NULL DEFAULT 'RECIBIDA',
    CONSTRAINT fk_proveedor_compra FOREIGN KEY(proveedor_id) REFERENCES proveedores(id),
    CONSTRAINT fk_usuario_compra FOREIGN KEY(usuario_id) REFERENCES usuarios(id)
);

CREATE TABLE compras_detalle (
    id BIGSERIAL PRIMARY KEY,
    compra_id BIGINT NOT NULL,
    producto_id INT NOT NULL,
    cantidad INT NOT NULL CHECK (cantidad > 0),
    costo_unitario_compra NUMERIC(10, 2) NOT NULL,
    subtotal_linea NUMERIC(12, 2) NOT NULL,
    CONSTRAINT fk_compra_detalle FOREIGN KEY(compra_id) REFERENCES compras(id) ON DELETE CASCADE,
    CONSTRAINT fk_producto_compra_detalle FOREIGN KEY(producto_id) REFERENCES productos(id)
);

CREATE TYPE TIPO_MOVIMIENTO AS ENUM ('ENTRADA_COMPRA', 'SALIDA_VENTA', 'AJUSTE_POSITIVO', 'AJUSTE_NEGATIVO', 'DEVOLUCION');

CREATE TABLE movimientos_inventario (
    id BIGSERIAL PRIMARY KEY,
    producto_id INT NOT NULL,
    usuario_id INT NOT NULL,
    tipo TIPO_MOVIMIENTO NOT NULL,
    cantidad_movida INT NOT NULL,
    stock_anterior INT NOT NULL,
    stock_nuevo INT NOT NULL,
    fecha_movimiento TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    referencia VARCHAR(255),
    notas TEXT,
    CONSTRAINT fk_producto_movimiento FOREIGN KEY(producto_id) REFERENCES productos(id),
    CONSTRAINT fk_usuario_movimiento FOREIGN KEY(usuario_id) REFERENCES usuarios(id)
);

-- ===================================================================
-- SECCIÓN 4: INSERCIÓN DE DATOS ESTRUCTURALES (ROLES)
-- ===================================================================

INSERT INTO roles (nombre) VALUES
('ADMINISTRADOR'),
('VENDEDOR'),
('ALMACENISTA')
ON CONFLICT (nombre) DO NOTHING;