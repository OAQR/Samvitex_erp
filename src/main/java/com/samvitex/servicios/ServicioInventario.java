package com.samvitex.servicios;

import com.samvitex.modelos.dto.ProductoInventarioDTO;
import com.samvitex.modelos.entidades.InventarioPorAlmacen;
import com.samvitex.modelos.entidades.Producto;
import com.samvitex.modelos.excepciones.InventarioException;
import com.samvitex.repositorios.InventarioPorAlmacenRepositorio;
import com.samvitex.repositorios.ProductoRepositorio;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;

/**
 * Servicio de negocio para la gestión del inventario de {@link Producto}.
 * <p>
 * Centraliza la lógica de negocio para operaciones CRUD de productos, búsquedas,
 * y consultas de stock en un entorno multi-almacén.
 */
@Service
public class ServicioInventario {

    private final ProductoRepositorio productoRepositorio;
    private final InventarioPorAlmacenRepositorio inventarioPorAlmacenRepositorio;

    public ServicioInventario(ProductoRepositorio productoRepositorio, InventarioPorAlmacenRepositorio inventarioPorAlmacenRepositorio) {
        this.productoRepositorio = productoRepositorio;
        this.inventarioPorAlmacenRepositorio = inventarioPorAlmacenRepositorio;
    }

    /**
     * Obtiene el stock de un producto específico en un almacén determinado.
     *
     * @param productoId El ID del producto.
     * @param almacenId El ID del almacén.
     * @return La cantidad de stock como un entero. Devuelve 0 si no se encuentra registro.
     */
    @Transactional(readOnly = true)
    public int obtenerStockDeProductoEnAlmacen(Integer productoId, Integer almacenId) {
        return inventarioPorAlmacenRepositorio
                .findByProductoIdAndAlmacenId(productoId, almacenId)
                .map(InventarioPorAlmacen::getCantidad)
                .orElse(0);
    }

    /**
     * Busca un producto por su identificador único (ID).
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VENDEDOR', 'ALMACENISTA')")
    public Optional<Producto> findById(Integer id) {
        return productoRepositorio.findById(id);
    }

    /**
     * Obtiene una lista con el desglose del stock de un producto específico
     * a través de todos los almacenes.
     *
     * @param productoId El ID del producto a consultar.
     * @return Una lista de entidades InventarioPorAlmacen.
     */
    @Transactional(readOnly = true)
    public List<InventarioPorAlmacen> obtenerDesgloseStockPorProducto(Integer productoId) {
        return inventarioPorAlmacenRepositorio.findByProductoIdWithAlmacen(productoId);
    }

    /**
     * Guarda un producto (lo crea o actualiza). El stock no se gestiona aquí.
     */
    @Transactional
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public Producto guardarProducto(Producto producto) {
        if (producto.getId() == null && productoRepositorio.existsBySku(producto.getSku())) {
            throw new InventarioException("El SKU '" + producto.getSku() + "' ya existe y debe ser único.");
        }
        return productoRepositorio.save(producto);
    }

    /**
     * Realiza un borrado lógico (soft delete) de un producto.
     */
    @Transactional
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public void eliminarProducto(Integer id) {
        Producto producto = productoRepositorio.findById(id)
                .orElseThrow(() -> new InventarioException("Producto no encontrado con ID: " + id));
        producto.setActivo(false);
        productoRepositorio.save(producto);
    }

    /**
     * Busca productos de forma paginada para la vista principal de inventario.
     */
    @Transactional(readOnly = true)
    public Page<Producto> buscarProductosPaginado(String texto, int pagina, int tamano) {
        Pageable pageable = PageRequest.of(pagina, tamano, Sort.by("nombre").ascending());

        if (texto == null || texto.isBlank()) {
            return productoRepositorio.findAllWithDetails(pageable);
        }
        return productoRepositorio.findByNombreOrSkuWithDetails(texto, pageable);
    }

    /**
     * Busca productos sin paginación, sin considerar el stock.
     * Utilizado en el módulo de compras.
     */
    @Transactional(readOnly = true)
    public List<Producto> buscarProductos(String textoBusqueda) {
        if (textoBusqueda == null || textoBusqueda.isBlank()) {
            return productoRepositorio.findAll();
        }
        return productoRepositorio.findByNombreOrSkuWithDetails(textoBusqueda, Pageable.unpaged()).getContent();
    }

    /**
     * Obtiene una lista completa de todos los productos registrados, con sus relaciones principales.
     */
    @Transactional(readOnly = true)
    public List<Producto> obtenerTodosLosProductos() {
        return productoRepositorio.findAllWithDetails();
    }

    /**
     * Busca productos vendibles (activos y con stock > 0) en un almacén específico.
     */
    @Transactional(readOnly = true)
    public List<Producto> buscarProductosActivosConStock(String textoBusqueda, Integer almacenId) {
        if (almacenId == null) {
            throw new IllegalArgumentException("Se debe especificar un almacén para la búsqueda.");
        }
        String busqueda = (textoBusqueda == null || textoBusqueda.isBlank()) ? "" : textoBusqueda;
        return productoRepositorio.findActivosConStockPorAlmacenYNombreOSku(almacenId, busqueda);
    }

    /**
     * Busca un producto vendible por su SKU exacto en un almacén específico.
     */
    @Transactional(readOnly = true)
    public Optional<Producto> buscarProductoActivoConStockPorSku(String sku, Integer almacenId) {
        if (almacenId == null) {
            throw new IllegalArgumentException("Se debe especificar un almacén para la búsqueda.");
        }
        return productoRepositorio.findActivoConStockPorAlmacenYSku(almacenId, sku);
    }

    /**
     * Búsqueda inteligente y carga de datos para tooltip.
     */
    /**
     * Busca productos de forma paginada y enriquecida con stock total y resumen.
     * Soporta filtro por texto (búsqueda inteligente) y por ID de almacén.
     *
     * @param texto Término de búsqueda (nombre o SKU).
     * @param almacenId ID del almacén para filtrar el conteo de stock (null para todos).
     * @param pagina Número de página.
     * @param tamano Tamaño de página.
     * @return Página de DTOs listos para la UI.
     */
    @Transactional(readOnly = true)
    public Page<ProductoInventarioDTO> buscarProductosPaginadoConStockTotal(String texto, Integer almacenId, int pagina, int tamano) {
        Pageable pageable = PageRequest.of(pagina, tamano, Sort.by("nombre").ascending());

        // 1. ESPECIFICACIÓN DINÁMICA (FILTROS)
        org.springframework.data.jpa.domain.Specification<Producto> spec = (root, query, cb) -> {

            // --- CORRECCIÓN CLAVE: Cargar relaciones (JOIN FETCH) ---
            // Verificamos el tipo de resultado para no romper la query de conteo (count)
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("categoria", JoinType.LEFT);
                root.fetch("proveedor", JoinType.LEFT);
            }
            // ---------------------------------------------------------

            List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();

            // Siempre filtrar solo productos activos
            predicates.add(cb.isTrue(root.get("activo")));

            // Filtro de texto inteligente (busca por palabras separadas)
            if (texto != null && !texto.isBlank()) {
                String[] palabras = texto.trim().split("\\s+"); // Separar por espacios
                for (String palabra : palabras) {
                    // Usar unaccent() si está configurado en DB, sino lower()
                    String patron = "%" + palabra.toLowerCase() + "%";

                    // Busca en Nombre O en SKU
                    jakarta.persistence.criteria.Predicate nombreLike = cb.like(cb.lower(root.get("nombre")), patron);
                    jakarta.persistence.criteria.Predicate skuLike = cb.like(cb.lower(root.get("sku")), patron);

                    // Agrega condición: (nombre LIKE %palabra% OR sku LIKE %palabra%)
                    predicates.add(cb.or(nombreLike, skuLike));
                }
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        // Ejecutar consulta principal de productos
        Page<Producto> paginaProductos = productoRepositorio.findAll(spec, pageable);

        // 2. ENRIQUECIMIENTO DE DATOS (DTOs)
        List<ProductoInventarioDTO> dtos = paginaProductos.getContent().stream()
                .map(producto -> {
                    // Obtenemos el detalle de inventario para este producto
                    List<InventarioPorAlmacen> inventarios = inventarioPorAlmacenRepositorio.findByProductoIdWithAlmacen(producto.getId());

                    long stockCalculado = 0;
                    StringBuilder tooltipBuilder = new StringBuilder("<html><b>Detalle de Stock:</b><br/>");
                    boolean hayStockEnAlgunLado = false;

                    for (InventarioPorAlmacen inv : inventarios) {
                        // Lógica de filtrado de visualización
                        boolean coincideAlmacen = (almacenId == null || almacenId <= 0 || inv.getAlmacen().getId().equals(almacenId));

                        if (coincideAlmacen) {
                            // Sumamos al total visible en la tabla
                            stockCalculado += inv.getCantidad();
                        }

                        // Para el tooltip
                        if (inv.getCantidad() > 0) {
                            hayStockEnAlgunLado = true;
                            String estiloAlmacen = coincideAlmacen ? "color:black;" : "color:gray;";

                            tooltipBuilder.append("<span style='").append(estiloAlmacen).append("'>• ")
                                    .append(inv.getAlmacen().getNombre())
                                    .append(": <b>").append(inv.getCantidad()).append("</b></span><br/>");
                        }
                    }

                    if (!hayStockEnAlgunLado) {
                        tooltipBuilder.append("<span style='color:gray;'>Sin stock registrado</span>");
                    }
                    tooltipBuilder.append("</html>");

                    return new ProductoInventarioDTO(producto, stockCalculado, tooltipBuilder.toString());
                })
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, paginaProductos.getTotalElements());
    }

    /**
     * Busca un producto por su ID, asegurando que sus relaciones (Categoría, Proveedor)
     * sean cargadas para su uso en contextos desconectados como la UI de edición.
     *
     * @param id El ID del producto.
     * @return un Optional conteniendo al Producto con sus detalles cargados, si se encuentra.
     */
    @Transactional(readOnly = true)
    public Optional<Producto> findByIdForEditing(Integer id) {
        return productoRepositorio.findByIdWithDetails(id);
    }

}