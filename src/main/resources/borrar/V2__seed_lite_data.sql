/**
 * V2: Siembra de Datos Ligera para Desarrollo y Pruebas
 * ------------------------------------------------------
 * Esta es una versión reducida del script de siembra masiva, diseñada para
 * ejecutarse rápidamente durante el desarrollo.
 * - Crea un número limitado de productos.
 * - Simula transacciones solo para los últimos 90 días.
 */

-- ===================================================================
-- SECCIÓN 1: DATOS ESTRUCTURALES
-- ===================================================================
INSERT INTO roles (nombre) VALUES ('ADMINISTRADOR'), ('VENDEDOR'), ('ALMACENISTA') ON CONFLICT (nombre) DO NOTHING;

INSERT INTO usuarios (nombre_usuario, password_hash, nombre_completo, email, rol_id) VALUES
('admin', '$2a$10$uzySj8.fbBEHubaDUatEROxRalGUV/g0j6wXMrjw2xlilqtgoP0.K', 'Usuario Administrador', 'admin@samvitex.com', (SELECT id FROM roles WHERE nombre = 'ADMINISTRADOR')),
('vendedor', '$2a$10$3H7df6XU48nKaLsnvLzHiOlkKs5eprnb.1dtS7l7tUPQZFPlRpaae', 'Usuario Vendedor', 'vendedor@samvitex.com', (SELECT id FROM roles WHERE nombre = 'VENDEDOR')),
('almacenero', '$2a$10$TeYRR2oh/fTBTpgzAA1OYuqXE6TwR.byK9KbvKl8Xxi8xhjwJN2Qm', 'Usuario Almacenista', 'almacen@samvitex.com', (SELECT id FROM roles WHERE nombre = 'ALMACENISTA'))
ON CONFLICT (nombre_usuario) DO NOTHING;

-- ===================================================================
-- SECCIÓN 2: SIEMBRA DE CATÁLOGOS
-- ===================================================================
DO $$
DECLARE
    admin_user_id INT := (SELECT id FROM usuarios WHERE nombre_usuario = 'admin');
BEGIN
    INSERT INTO categorias (nombre, descripcion, usuario_creacion_id) VALUES
    ('Telas de Algodón', 'Telas naturales, transpirables y versátiles.', admin_user_id),
    ('Telas de Poliéster', 'Telas sintéticas duraderas y resistentes a las arrugas.', admin_user_id),
    ('Hilos', 'Hilos de poliéster, algodón y mezclas para todo tipo de costura.', admin_user_id)
    ON CONFLICT (nombre) DO NOTHING;

    INSERT INTO proveedores (nombre, ruc, contacto_email, contacto_telefono, activo, usuario_creacion_id) VALUES
    ('Textiles San Jacinto S.A.C.', '20501234567', 'ventas@sanjacinto.com.pe', '987654321', TRUE, admin_user_id),
    ('Importaciones El Hilo Dorado E.I.R.L.', '20409876543', 'contacto@hilosdeoro.net', '912345678', TRUE, admin_user_id)
    ON CONFLICT (nombre) DO NOTHING;

    INSERT INTO almacenes (nombre, ubicacion_descripcion, activo, usuario_creacion_id) VALUES
    ('Almacén Principal - Gamarra', 'Galería El Rey, Sótano 1, La Victoria, Lima', TRUE, admin_user_id),
    ('Depósito de Insumos', 'Jr. Hipólito Unanue 1520, Interior 3, La Victoria, Lima', TRUE, admin_user_id)
    ON CONFLICT (nombre) DO NOTHING;

    INSERT INTO clientes (nombre_completo, dni_ruc, email, telefono, activo, usuario_creacion_id) VALUES
    ('Cliente Genérico', '00000000', 'sincorreo@generico.com', '000000000', TRUE, admin_user_id),
    ('Confecciones Gamarra S.R.L.', '20601234567', 'compras@confgamarra.com', '987123456', TRUE, admin_user_id)
    ON CONFLICT (dni_ruc) DO NOTHING;

    INSERT INTO talleres (nombre, direccion, tipo, activo, usuario_creacion_id) VALUES
    ('Taller de Corte Central', 'Jr. Gamarra 123, Piso 4, La Victoria', 'INTERNO', TRUE, admin_user_id),
    ('Taller de Confección "Hermanos Solis"', 'Av. Los Proceres 456, S.J.L.', 'EXTERNO', TRUE, admin_user_id)
    ON CONFLICT (nombre) DO NOTHING;
END $$;

-- ===================================================================
-- SECCIÓN 3: GENERACIÓN DE PRODUCTOS DE PRUEBA Y STOCK INICIAL
-- ===================================================================
DO $$
DECLARE
    admin_user_id INT := (SELECT id FROM usuarios WHERE nombre_usuario = 'admin');
    cat_algodon INT := (SELECT id FROM categorias WHERE nombre = 'Telas de Algodón');
    cat_poliester INT := (SELECT id FROM categorias WHERE nombre = 'Telas de Poliéster');
    cat_hilos INT := (SELECT id FROM categorias WHERE nombre = 'Hilos');
    prov_sanjacinto INT := (SELECT id FROM proveedores WHERE nombre = 'Textiles San Jacinto S.A.C.');
    prov_hilosdeoro INT := (SELECT id FROM proveedores WHERE nombre = 'Importaciones El Hilo Dorado E.I.R.L.');
    alm_gamarra INT := (SELECT id FROM almacenes WHERE nombre = 'Almacén Principal - Gamarra');
    alm_insumos INT := (SELECT id FROM almacenes WHERE nombre = 'Depósito de Insumos');
    i INT; precio_base NUMERIC; new_product_id INT;
BEGIN
    -- Crear 10 productos de algodón
    FOR i IN 1..10 LOOP
        precio_base := 10 + (random() * 20);
        INSERT INTO productos (sku, nombre, precio_costo, precio_venta, stock_minimo, categoria_id, proveedor_id, usuario_creacion_id)
        VALUES ('ALG-' || LPAD(i::text, 4, '0'), 'Tela Jersey 30/1, Color ' || i, precio_base, precio_base * 1.8, 50, cat_algodon, prov_sanjacinto, admin_user_id) ON CONFLICT (sku) DO NOTHING;
    END LOOP;
    -- Crear 10 productos de poliéster
    FOR i IN 1..10 LOOP
        precio_base := 8 + (random() * 15);
        INSERT INTO productos (sku, nombre, precio_costo, precio_venta, stock_minimo, categoria_id, proveedor_id, usuario_creacion_id)
        VALUES ('POL-' || LPAD(i::text, 4, '0'), 'Tela Polystel Liso, Tono ' || i, precio_base, precio_base * 1.9, 100, cat_poliester, prov_sanjacinto, admin_user_id) ON CONFLICT (sku) DO NOTHING;
    END LOOP;

    -- Asignar Stock Inicial a todos los productos en el Almacén Principal
    FOR new_product_id IN SELECT id FROM productos LOOP
        INSERT INTO inventario_por_almacen(producto_id, almacen_id, cantidad, usuario_modificacion_id, fecha_modificacion)
        VALUES (new_product_id, alm_gamarra, floor(random() * 500 + 100), admin_user_id, NOW())
        ON CONFLICT (producto_id, almacen_id) DO NOTHING;
    END LOOP;
END $$;

-- ===================================================================
-- SECCIÓN 4: SIMULACIÓN DE TRANSACCIONES (ÚLTIMOS 90 DÍAS)
-- ===================================================================
DO $$
DECLARE
    i INT; j INT; k INT; num_items INT;
    random_customer_id INT; random_seller_id INT; new_venta_id BIGINT;
    random_product RECORD; qty_sold INT; stock_anterior INT;
    venta_subtotal NUMERIC; venta_total NUMERIC; venta_impuestos NUMERIC; subtotal_linea NUMERIC;
    random_supplier_id INT; random_warehouseman_id INT; new_compra_id BIGINT;
    qty_bought INT; compra_total NUMERIC;
    alm_principal_id INT := (SELECT id FROM almacenes WHERE nombre = 'Almacén Principal - Gamarra');
BEGIN
    -- Iterar por los últimos 90 días
    FOR i IN REVERSE 90..1 LOOP
        -- Generar entre 1 y 5 ventas por día
        FOR j IN 1..floor(random() * 5 + 1) LOOP
            SELECT id INTO random_customer_id FROM clientes ORDER BY random() LIMIT 1;
            SELECT id INTO random_seller_id FROM usuarios WHERE rol_id = (SELECT id FROM roles WHERE nombre = 'VENDEDOR') ORDER BY random() LIMIT 1;
            venta_subtotal := 0;

            INSERT INTO ventas (cliente_id, usuario_id, almacen_origen_id, fecha_venta, subtotal, impuestos, total, metodo_pago, tipo_comprobante)
            VALUES (random_customer_id, random_seller_id, alm_principal_id, NOW() - (i || ' days')::INTERVAL, 0, 0, 0, 'Efectivo', 'NOTA_DE_VENTA')
            RETURNING id INTO new_venta_id;

            num_items := floor(random() * 3 + 1);
            FOR k IN 1..num_items LOOP
                SELECT p.id, p.precio_venta, inv.cantidad INTO random_product
                FROM productos p JOIN inventario_por_almacen inv ON p.id = inv.producto_id
                WHERE inv.almacen_id = alm_principal_id AND inv.cantidad > 10 ORDER BY random() LIMIT 1;

                IF FOUND THEN
                    qty_sold := floor(random() * 5 + 1);
                    stock_anterior := random_product.cantidad;
                    qty_sold := LEAST(qty_sold, stock_anterior - 1);

                    IF qty_sold > 0 THEN
                        subtotal_linea := random_product.precio_venta * qty_sold;
                        venta_subtotal := venta_subtotal + subtotal_linea;

                        INSERT INTO ventas_detalle (venta_id, producto_id, cantidad, precio_unitario, subtotal_linea)
                        VALUES (new_venta_id, random_product.id, qty_sold, random_product.precio_venta, subtotal_linea);

                        UPDATE inventario_por_almacen SET cantidad = cantidad - qty_sold WHERE producto_id = random_product.id AND almacen_id = alm_principal_id;

                        INSERT INTO movimientos_inventario (producto_id, almacen_id, usuario_id, tipo, cantidad_movida, stock_anterior, stock_nuevo, fecha_movimiento, venta_id)
                        VALUES (random_product.id, alm_principal_id, random_seller_id, 'SALIDA_VENTA', -qty_sold, stock_anterior, stock_anterior - qty_sold, NOW() - (i || ' days')::INTERVAL, new_venta_id);
                    END IF;
                END IF;
            END LOOP;

            venta_impuestos := venta_subtotal * 0.18;
            venta_total := venta_subtotal + venta_impuestos;
            UPDATE ventas SET subtotal = venta_subtotal, impuestos = venta_impuestos, total = venta_total WHERE id = new_venta_id;
        END LOOP;

        -- Generar una compra cada 7 días para reponer stock
        IF i % 7 = 0 THEN
            SELECT id INTO random_supplier_id FROM proveedores WHERE activo = TRUE ORDER BY random() LIMIT 1;
            SELECT id INTO random_warehouseman_id FROM usuarios WHERE rol_id = (SELECT id FROM roles WHERE nombre = 'ALMACENISTA') ORDER BY random() LIMIT 1;
            compra_total := 0;

            INSERT INTO compras (proveedor_id, usuario_id, almacen_destino_id, fecha_compra, total)
            VALUES (random_supplier_id, random_warehouseman_id, alm_principal_id, NOW() - (i || ' days')::INTERVAL, 0)
            RETURNING id INTO new_compra_id;

            num_items := floor(random() * 3 + 1);
            FOR k IN 1..num_items LOOP
                SELECT p.id, p.precio_costo, inv.cantidad INTO random_product
                FROM productos p LEFT JOIN inventario_por_almacen inv ON p.id = inv.producto_id AND inv.almacen_id = alm_principal_id
                ORDER BY random() LIMIT 1;

                IF FOUND THEN
                    qty_bought := floor(random() * 100 + 20);
                    stock_anterior := COALESCE(random_product.cantidad, 0);
                    subtotal_linea := random_product.precio_costo * qty_bought;
                    compra_total := compra_total + subtotal_linea;

                    INSERT INTO compras_detalle (compra_id, producto_id, cantidad, costo_unitario_compra, subtotal_linea)
                    VALUES (new_compra_id, random_product.id, qty_bought, random_product.precio_costo, subtotal_linea);

                    UPDATE inventario_por_almacen SET cantidad = cantidad + qty_bought WHERE producto_id = random_product.id AND almacen_id = alm_principal_id;

                    INSERT INTO movimientos_inventario (producto_id, almacen_id, usuario_id, tipo, cantidad_movida, stock_anterior, stock_nuevo, fecha_movimiento, compra_id)
                    VALUES (random_product.id, alm_principal_id, random_warehouseman_id, 'ENTRADA_COMPRA', qty_bought, stock_anterior, stock_anterior + qty_bought, NOW() - (i || ' days')::INTERVAL, new_compra_id);
                END IF;
            END LOOP;

            UPDATE compras SET total = compra_total WHERE id = new_compra_id;
        END IF;
    END LOOP;
END $$;