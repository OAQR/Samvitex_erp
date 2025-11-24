package com.samvitex.repositorios;

import com.samvitex.modelos.dto.ReporteVentasDTO;
import com.samvitex.modelos.entidades.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Repositorio de Spring Data JPA para la entidad {@link Venta}.
 *
 * <p>Además de los métodos CRUD estándar heredados de {@link JpaRepository}, este repositorio
 * incluye consultas JPQL personalizadas y optimizadas para la generación de reportes
 * y estadísticas de ventas, que son cruciales para la inteligencia de negocio de la aplicación.</p>
 */
@Repository
public interface VentaRepositorio extends JpaRepository<Venta, Long> {

    /**
     * Genera un reporte agregado de ventas por producto dentro de un rango de fechas especificado.
     * <p>La consulta realiza las siguientes operaciones:</p>
     * <ol>
     *     <li>Une los detalles de venta ({@code VentaDetalle}) con los productos ({@code Producto}).</li>
     *     <li>Filtra las transacciones para incluir solo aquellas dentro del período de tiempo dado.</li>
     *     <li>Agrupa los resultados por el nombre del producto.</li>
     *     <li>Calcula la suma de cantidades vendidas, el total de ingresos y una estimación de la ganancia bruta.</li>
     *     <li>Proyecta estos resultados directamente en un {@link ReporteVentasDTO} para eficiencia.</li>
     *     <li>Ordena los resultados de mayor a menor ingreso.</li>
     * </ol>
     *
     * @param fechaInicio El {@link Instant} que marca el inicio del período del reporte (inclusivo).
     * @param fechaFin El {@link Instant} que marca el final del período del reporte (inclusivo).
     * @return Una lista de {@link ReporteVentasDTO} con los datos agregados para el reporte.
     */
    @Query("""
        SELECT new com.samvitex.modelos.dto.ReporteVentasDTO(
            p.nombre,
            SUM(vd.cantidad),
            SUM(vd.subtotalLinea),
            SUM(vd.subtotalLinea - (p.precioCosto * vd.cantidad))
        )
        FROM VentaDetalle vd JOIN vd.producto p
        WHERE vd.venta.fechaVenta BETWEEN :fechaInicio AND :fechaFin
        GROUP BY p.id, p.nombre
        ORDER BY SUM(vd.subtotalLinea) DESC
    """)
    List<ReporteVentasDTO> findReporteVentasPorPeriodo(
            @Param("fechaInicio") Instant fechaInicio,
            @Param("fechaFin") Instant fechaFin
    );

    /**
     * Calcula la suma total de los montos de venta (campo {@code total}) dentro de un período de tiempo.
     * Utiliza {@code COALESCE} para garantizar que devuelva {@code 0} en lugar de {@code NULL} si no
     * se encuentran ventas en el período especificado, evitando errores de {@code NullPointerException}.
     *
     * @param fechaInicio El inicio del período.
     * @param fechaFin El fin del período.
     * @return Un {@link BigDecimal} con la suma total de las ventas, o {@code 0} si no hubo ventas.
     */
    @Query("SELECT COALESCE(SUM(v.total), 0) FROM Venta v WHERE v.fechaVenta BETWEEN :fechaInicio AND :fechaFin")
    BigDecimal findTotalVentasEnPeriodo(
            @Param("fechaInicio") Instant fechaInicio,
            @Param("fechaFin") Instant fechaFin
    );

    /**
     * Busca todas las ventas realizadas dentro de un rango de fechas, ordenadas de la más reciente a la más antigua.
     * Esta consulta utiliza 'JOIN FETCH' para cargar de forma proactiva las entidades asociadas
     * (Cliente y Usuario) en una sola consulta, evitando así problemas de N+1 consultas y
     * {@code LazyInitializationException} en la capa de presentación.
     *
     * @param fechaInicio El inicio del período.
     * @param fechaFin El fin del período.
     * @return Una lista de entidades {@link Venta} con sus relaciones clave ya inicializadas.
     */
    @Query("SELECT v FROM Venta v JOIN FETCH v.cliente JOIN FETCH v.usuario WHERE v.fechaVenta BETWEEN :fechaInicio AND :fechaFin ORDER BY v.fechaVenta DESC")
    List<Venta> findVentasConDetallesEnPeriodo(
            @Param("fechaInicio") Instant fechaInicio,
            @Param("fechaFin") Instant fechaFin
    );
}