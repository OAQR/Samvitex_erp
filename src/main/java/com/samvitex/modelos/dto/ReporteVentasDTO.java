package com.samvitex.modelos.dto;

import java.math.BigDecimal;

/**
 * Data Transfer Object (DTO) de Proyección, diseñado para recibir los resultados
 * agregados de consultas de reportes de ventas.
 *
 * <p>En lugar de que una consulta JPQL devuelva una lista de entidades o un arreglo de objetos,
 * puede construir instancias de este DTO directamente. Esto es altamente eficiente porque: </p>
 * <ol>
 *   <li>Solo se transfieren los datos exactos que necesita el reporte, reduciendo la carga de datos.</li>
 *   <li>Se evita la necesidad de mapear manualmente los resultados de la consulta en la capa de servicio.</li>
 * </ol>
 *
 * <p>Este DTO es utilizado por la consulta {@code findReporteVentasPorPeriodo} en
 * {@link com.samvitex.repositorios.VentaRepositorio}.</p>
 *
 * @param nombreProducto El nombre del producto vendido.
 * @param totalUnidadesVendidas La suma total de unidades vendidas de este producto en el período.
 * @param totalIngresos La suma total de los subtotales de línea para este producto (ingresos brutos).
 * @param gananciaBrutaEstimada Una estimación de la ganancia bruta, calculada como
 *                              (Total Ingresos - (Total Unidades * Costo del Producto)).
 */
public record ReporteVentasDTO(
        String nombreProducto,
        Long totalUnidadesVendidas,
        BigDecimal totalIngresos,
        BigDecimal gananciaBrutaEstimada
) {
}