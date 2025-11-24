package com.samvitex.modelos.dto;

import java.math.BigDecimal;

/**
 * Data Transfer Object (DTO) para encapsular las estadísticas clave (KPIs - Key Performance Indicators)
 * que se mostrarán en el Panel del Dashboard.
 *
 * <p>El uso de este DTO permite que el {@code ServicioDashboard} ejecute múltiples consultas
 * agregadas y devuelva todos los resultados en un único objeto inmutable y cohesivo. Esto
 * simplifica la comunicación con el {@code DashboardPresenter} y asegura que todos los
 * datos del dashboard se obtengan de manera atómica.</p>
 *
 * @param totalProductos Cantidad total de productos distintos registrados en el inventario.
 * @param productosConStockBajo Cantidad de productos cuyo stock actual es menor o igual a su stock mínimo definido.
 * @param valorTotalInventario Suma monetaria total del inventario, calculada como (cantidad * precio_costo) de todos los productos.
 * @param ventasHoy Suma total de las ventas (monto total) realizadas en el día actual (desde las 00:00 hasta ahora).
 */
public record DashboardStatsDTO(
        long totalProductos,
        long productosConStockBajo,
        BigDecimal valorTotalInventario,
        BigDecimal ventasHoy
) {
}