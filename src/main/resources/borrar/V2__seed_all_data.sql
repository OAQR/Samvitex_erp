/**
 * V2: Siembra Masiva y Realista de Datos de Prueba
 * ------------------------------------------------
 * Este script puebla todas las tablas con un conjunto de datos significativo
 * para simular una operación a escala de una empresa mediana con varios años
 * de operación.
 */

-- ===================================================================
-- SECCIÓN 1: SIEMBRA DE USUARIOS
-- ===================================================================
-- Contraseñas: 'admin', 'vendedor123', 'almacen123'
INSERT INTO usuarios (nombre_usuario, password_hash, nombre_completo, email, rol_id) VALUES
('admin', '$2a$10$uzySj8.fbBEHubaDUatEROxRalGUV/g0j6wXMrjw2xlilqtgoP0.K', 'Administrador General', 'admin@samvitex.com', (SELECT id FROM roles WHERE nombre = 'ADMINISTRADOR')),
('lrodriguez', '$2a$10$3H7df6XU48nKaLsnvLzHiOlkKs5eprnb.1dtS7l7tUPQZFPlRpaae', 'Laura Rodríguez', 'lrodriguez@samvitex.com', (SELECT id FROM roles WHERE nombre = 'VENDEDOR')),
('mgarcia', '$2a$10$ZH7TegylQ9jqIucQfcoPV.kMleXxTVOn/CFI5QkUXjldXsCo4zAwe', 'Miguel García', 'mgarcia@samvitex.com', (SELECT id FROM roles WHERE nombre = 'VENDEDOR')),
('dsilva', '$2a$10$TeYRR2oh/fTBTpgzAA1OYuqXE6TwR.byK9KbvKl8Xxi8xhjwJN2Qm', 'Daniel Silva', 'dsilva@samvitex.com', (SELECT id FROM roles WHERE nombre = 'ALMACENISTA')),
('pflores', '$2a$10$pnFPgkMLHLdegf8guiLWQuo0HIIFTI6Ol1Xw4cZIWd1F8IkTmlNs6', 'Patricia Flores', 'pflores@samvitex.com', (SELECT id FROM roles WHERE nombre = 'ALMACENISTA'))
ON CONFLICT (nombre_usuario) DO NOTHING;


-- ===================================================================
-- SECCIÓN 2: SIEMBRA DE CATÁLOGOS
-- ===================================================================
INSERT INTO categorias (nombre, descripcion) VALUES
('Telas de Algodón', 'Telas naturales, transpirables y versátiles.'),
('Telas de Poliéster', 'Telas sintéticas duraderas y resistentes a las arrugas.'),
('Mezclas (Poly-Algodón)', 'Combinan la suavidad del algodón con la durabilidad del poliéster.'),
('Denim (Mezclilla)', 'Tela robusta de algodón, ideal para jeans y chaquetas.'),
('Lino y Viscosas', 'Telas naturales ligeras y muy transpirables.'),
('Forros y Entretelas', 'Materiales para el interior y estructura de las prendas.'),
('Hilos', 'Hilos de poliéster, algodón y mezclas para todo tipo de costura.'),
('Botones y Accesorios', 'Botones, cierres, remaches y otros avíos de confección.')
ON CONFLICT (nombre) DO NOTHING;

INSERT INTO proveedores (nombre, ruc, contacto_email, contacto_telefono, activo) VALUES
('Textiles San Jacinto S.A.C.', '20501234567', 'ventas@sanjacinto.com.pe', '987654321', TRUE),
('Importaciones El Hilo Dorado E.I.R.L.', '20409876543', 'contacto@hilosdeoro.net', '912345678', TRUE),
('Fábrica de Tejidos La Colonial S.A.', '20301122334', 'pedidos@lacolonial.com', '998877665', TRUE),
('Inversiones Algodoneras del Perú', '20601231234', 'info@algodonperu.com', '955443322', TRUE),
('Sintéticos Modernos S.R.L.', '20555666777', 'gerencia@sinmod.pe', '944332211', TRUE),
('Avíos y Accesorios Andinos S.A.', '20508877665', 'ventas@aviosandinos.pe', '933221100', TRUE),
('Textil del Norte (INACTIVO)', '20401112223', 'contacto@textilnorte.com', '911223344', FALSE)
ON CONFLICT (nombre) DO NOTHING;

INSERT INTO almacenes (nombre, ubicacion_descripcion, activo) VALUES
('Almacén Principal - Gamarra', 'Galería El Rey, Sótano 1, La Victoria, Lima', TRUE),
('Almacén de Despacho - VMT', 'Av. Pachacútec 2345, Villa María del Triunfo, Lima', TRUE),
('Depósito de Insumos', 'Jr. Hipólito Unanue 1520, Interior 3, La Victoria, Lima', TRUE),
('Almacén Antiguo (INACTIVO)', 'Av. 28 de Julio 556, La Victoria', FALSE)
ON CONFLICT (nombre) DO NOTHING;

INSERT INTO clientes (nombre_completo, dni_ruc, email, telefono, activo) VALUES
('Cliente Genérico', '00000000', 'sincorreo@generico.com', '000000000', TRUE),
('Confecciones Gamarra S.R.L.', '20601234567', 'compras@confgamarra.com', '987123456', TRUE),
('Maria Quispe (Diseñadora)', '10456789', 'maria.quispe.design@email.com', '998234567', TRUE),
('Juan Pérez (Taller)', '10987654', 'jperez.taller@email.com', '976543210', TRUE),
('Moda Juvenil S.A.C.', '20509876543', 'logistica@modajuvenil.pe', '965432109', TRUE),
('Comercial Textil del Centro E.I.R.L', '20501122334', 'gerencia@ctc-textil.com', '954321987', TRUE),
('Ana López (Emprendedora)', '10876543', 'ana.lopez@emprende.com', '943210987', TRUE),
('Cliente Antiguo (INACTIVO)', '10223344', 'old@cliente.com', '954321098', FALSE)
ON CONFLICT (dni_ruc) DO NOTHING;


-- ===================================================================
-- SECCIÓN 3: SIEMBRA MASIVA DE PRODUCTOS
-- ===================================================================
DO $$
DECLARE
    -- IDs de referencia
    cat_algodon INT := (SELECT id FROM categorias WHERE nombre = 'Telas de Algodón');
    cat_poliester INT := (SELECT id FROM categorias WHERE nombre = 'Telas de Poliéster');
    cat_hilos INT := (SELECT id FROM categorias WHERE nombre = 'Hilos');
    cat_botones INT := (SELECT id FROM categorias WHERE nombre = 'Botones y Accesorios');

    prov_sanjacinto INT := (SELECT id FROM proveedores WHERE nombre = 'Textiles San Jacinto S.A.C.');
    prov_hilosdeoro INT := (SELECT id FROM proveedores WHERE nombre = 'Importaciones El Hilo Dorado E.I.R.L.');
    prov_avios INT := (SELECT id FROM proveedores WHERE nombre = 'Avíos y Accesorios Andinos S.A.');

    alm_gamarra INT := (SELECT id FROM almacenes WHERE nombre = 'Almacén Principal - Gamarra');
    alm_insumos INT := (SELECT id FROM almacenes WHERE nombre = 'Depósito de Insumos');

    -- Variables para el bucle
    i INT;
    precio_base NUMERIC;
    stock_base INT;
BEGIN
    -- Generar 100 tipos de telas de algodón
    FOR i IN 1..100 LOOP
        precio_base := 10 + (random() * 20);
        stock_base := 50 + floor(random() * 500);
        INSERT INTO productos (sku, nombre, precio_costo, precio_venta, cantidad, stock_minimo, categoria_id, proveedor_id, almacen_id)
        VALUES ('ALG-' || LPAD(i::text, 4, '0'), 'Tela Jersey 30/1 Peinado, Color ' || i, precio_base, precio_base * 1.8, stock_base, 50, cat_algodon, prov_sanjacinto, alm_gamarra)
        ON CONFLICT (sku) DO NOTHING;
    END LOOP;

    -- Generar 100 tipos de telas de poliéster
    FOR i IN 1..100 LOOP
        precio_base := 8 + (random() * 15);
        stock_base := 100 + floor(random() * 1000);
        INSERT INTO productos (sku, nombre, precio_costo, precio_venta, cantidad, stock_minimo, categoria_id, proveedor_id, almacen_id)
        VALUES ('POL-' || LPAD(i::text, 4, '0'), 'Tela Polystel Liso, Tono ' || i, precio_base, precio_base * 1.9, stock_base, 100, cat_poliester, prov_sanjacinto, alm_gamarra)
        ON CONFLICT (sku) DO NOTHING;
    END LOOP;

    -- Generar 200 tipos de hilos
    FOR i IN 1..200 LOOP
        precio_base := 4 + (random() * 5);
        stock_base := 20 + floor(random() * 200);
        INSERT INTO productos (sku, nombre, precio_costo, precio_venta, cantidad, stock_minimo, categoria_id, proveedor_id, almacen_id)
        VALUES ('HIL-' || LPAD(i::text, 4, '0'), 'Hilo Poliéster 40/2, Cono ' || i, precio_base, precio_base * 2.0, stock_base, 20, cat_hilos, prov_hilosdeoro, alm_insumos)
        ON CONFLICT (sku) DO NOTHING;
    END LOOP;

    -- Generar 50 tipos de botones
    FOR i IN 1..50 LOOP
        precio_base := 0.1 + (random() * 0.4); -- Costo por unidad
        stock_base := 1000 + floor(random() * 10000); -- Se venden en cantidad
        INSERT INTO productos (sku, nombre, precio_costo, precio_venta, cantidad, stock_minimo, categoria_id, proveedor_id, almacen_id)
        VALUES ('BOT-' || LPAD(i::text, 4, '0'), 'Botón de Camisa 11mm, Modelo ' || i, precio_base, precio_base * 2.5, stock_base, 1000, cat_botones, prov_avios, alm_insumos)
        ON CONFLICT (sku) DO NOTHING;
    END LOOP;

END $$;