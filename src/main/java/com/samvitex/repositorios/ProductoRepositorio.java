package com.samvitex.repositorios;

import com.samvitex.modelos.entidades.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio de Spring Data JPA para la entidad {@link Producto}.
 *
 * <p>Proporciona una abstracción de la capa de acceso a datos para operaciones sobre
 * productos, incluyendo CRUD, búsquedas, paginación y consultas optimizadas para el dashboard,
 * adaptado a la arquitectura de inventario multi-almacén.</p>
 */
@Repository
public interface ProductoRepositorio extends JpaRepository<Producto, Integer>, JpaSpecificationExecutor<Producto> {

    // --- Consultas para el Dashboard ---

    /**
     * Cuenta el número de productos activos cuyo stock total (sumado de todos los almacenes)
     * es menor o igual a su stock mínimo definido.
     *
     * @return el número total de productos con bajo stock.
     */
    @Query("""
        SELECT COUNT(p) FROM Producto p WHERE p.activo = true AND
        (SELECT SUM(ipa.cantidad) FROM InventarioPorAlmacen ipa WHERE ipa.producto = p) <= p.stockMinimo
    """)
    long countByStockBajo();

    /**
     * Calcula el valor monetario total del inventario.
     * Multiplica el stock total de cada producto (suma de todos los almacenes) por su precio de costo
     * y luego suma todos estos valores.
     *
     * @return un {@link BigDecimal} con el valor total del inventario.
     */
    @Query("""
        SELECT COALESCE(SUM(p.precioCosto * (SELECT SUM(ipa.cantidad) FROM InventarioPorAlmacen ipa WHERE ipa.producto = p)), 0)
        FROM Producto p
        WHERE p.activo = true
    """)
    BigDecimal findValorTotalInventario();


    /**
     * Verifica si existe un producto con un SKU específico.
     *
     * @param sku el SKU a verificar.
     * @return {@code true} si el SKU ya existe, {@code false} de lo contrario.
     */
    boolean existsBySku(String sku);


    // --- Consultas Genéricas de Búsqueda (sin paginación) ---

    /**
     * Busca productos cuyo nombre o SKU contenga el texto de búsqueda, sin paginación.
     * La búsqueda es insensible a mayúsculas/minúsculas y acentos.
     *
     * @param textoBusqueda el texto a buscar.
     * @return una lista de productos que coinciden.
     */
    @Query("""
        SELECT p FROM Producto p WHERE
        unaccent(LOWER(p.nombre)) LIKE unaccent(LOWER(CONCAT('%', :textoBusqueda, '%'))) OR
        unaccent(LOWER(p.sku)) LIKE unaccent(LOWER(CONCAT('%', :textoBusqueda, '%')))
    """)
    List<Producto> findByNombreContainingIgnoreCaseOrSkuContainingIgnoreCase(@Param("textoBusqueda") String textoBusqueda);


    // --- Consultas Específicas para el Módulo de Ventas (Multi-Almacén) ---

    /**
     * Busca productos que están activos, tienen stock disponible (> 0) en un almacén específico
     * y cuyo nombre o SKU coincida con el texto de búsqueda.
     *
     * @param almacenId El ID del almacén en el que se debe verificar el stock.
     * @param textoBusqueda el texto a buscar en el nombre o SKU.
     * @return una lista de productos vendibles que coinciden con la búsqueda en el almacén especificado.
     */
    @Query("""
        SELECT p FROM Producto p JOIN InventarioPorAlmacen ipa ON p.id = ipa.producto.id
        WHERE p.activo = true
        AND ipa.almacen.id = :almacenId
        AND ipa.cantidad > 0
        AND (unaccent(LOWER(p.nombre)) LIKE unaccent(LOWER(CONCAT('%', :textoBusqueda, '%')))
             OR unaccent(LOWER(p.sku)) LIKE unaccent(LOWER(CONCAT('%', :textoBusqueda, '%'))))
    """)
    List<Producto> findActivosConStockPorAlmacenYNombreOSku(@Param("almacenId") Integer almacenId, @Param("textoBusqueda") String textoBusqueda);

    /**
     * Busca un único producto por su SKU que esté activo y tenga stock disponible (> 0) en un almacén específico.
     * Optimizado para la funcionalidad de "escanear código de barras".
     *
     * @param almacenId El ID del almacén donde buscar.
     * @param sku El SKU exacto del producto.
     * @return un {@link Optional} que contiene el producto si se encuentra.
     */
    @Query("""
        SELECT p FROM Producto p JOIN InventarioPorAlmacen ipa ON p.id = ipa.producto.id
        WHERE p.activo = true
        AND ipa.almacen.id = :almacenId
        AND ipa.cantidad > 0
        AND p.sku = :sku
    """)
    Optional<Producto> findActivoConStockPorAlmacenYSku(@Param("almacenId") Integer almacenId, @Param("sku") String sku);


    // --- Consultas Paginadas para el Módulo de Inventario ---

    /**
     * Carga una página de productos e inicializa (hace FETCH) sus relaciones principales
     * (Categoria y Proveedor) para evitar LazyInitializationException.
     *
     * @param pageable el objeto que contiene la información de paginación.
     * @return una 'Página' (Page) de productos con sus relaciones clave ya cargadas.
     */
    @Query(value = "SELECT p FROM Producto p LEFT JOIN FETCH p.categoria LEFT JOIN FETCH p.proveedor",
            countQuery = "SELECT count(p) FROM Producto p")
    Page<Producto> findAllWithDetails(Pageable pageable);

    /**
     * Obtiene una lista de todos los productos del sistema, precargando sus relaciones
     * principales para evitar LazyInitializationException.
     *
     * @return una lista de todas las entidades {@link Producto} con sus detalles ya cargados.
     */
    @Query("SELECT p FROM Producto p LEFT JOIN FETCH p.categoria LEFT JOIN FETCH p.proveedor ORDER BY p.nombre ASC")
    List<Producto> findAllWithDetails();

    /**
     * Busca productos por nombre o SKU de forma paginada, e inicializa (FETCH) las relaciones
     * Categoria y Proveedor en la misma consulta.
     *
     * @param texto el texto a buscar en el nombre o SKU.
     * @param pageable el objeto que contiene la información de paginación.
     * @return una 'Página' (Page) de productos filtrados con sus relaciones clave ya cargadas.
     */
    /**
     * Busca productos por nombre o SKU de forma paginada, e inicializa (FETCH) las relaciones
     * Categoria y Proveedor en la misma consulta.
     *
     * @param texto el texto a buscar en el nombre o SKU.
     * @param pageable el objeto que contiene la información de paginación.
     * @return una 'Página' (Page) de productos filtrados con sus relaciones clave ya cargadas.
     */
    @Query(value = """
        SELECT p FROM Producto p LEFT JOIN FETCH p.categoria LEFT JOIN FETCH p.proveedor WHERE
        unaccent(LOWER(p.nombre)) LIKE unaccent(LOWER(CONCAT('%', :texto, '%'))) OR
        unaccent(LOWER(p.sku)) LIKE unaccent(LOWER(CONCAT('%', :texto, '%')))
    """,
            countQuery = """
        SELECT count(p) FROM Producto p WHERE
        unaccent(LOWER(p.nombre)) LIKE unaccent(LOWER(CONCAT('%', :texto, '%'))) OR
        unaccent(LOWER(p.sku)) LIKE unaccent(LOWER(CONCAT('%', :texto, '%')))
    """)
    Page<Producto> findByNombreOrSkuWithDetails(@Param("texto") String texto, Pageable pageable);

    /**
     * Busca un producto por su ID y carga proactivamente sus relaciones principales
     * (Categoria y Proveedor) para evitar LazyInitializationException en la UI.
     * Utiliza LEFT JOIN FETCH para manejar correctamente productos que no tengan
     * una categoría o proveedor asignado.
     *
     * @param id El ID del producto a buscar.
     * @return Un Optional que contiene el Producto con sus relaciones ya inicializadas.
     */
    @Query("SELECT p FROM Producto p LEFT JOIN FETCH p.categoria LEFT JOIN FETCH p.proveedor WHERE p.id = :id")
    Optional<Producto> findByIdWithDetails(@Param("id") Integer id);
}