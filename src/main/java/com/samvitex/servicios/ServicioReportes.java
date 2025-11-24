package com.samvitex.servicios;

import com.samvitex.modelos.dto.ReporteVentasDTO;
import com.samvitex.modelos.entidades.MovimientoInventario;
import com.samvitex.repositorios.MovimientoInventarioRepositorio;
import com.samvitex.repositorios.VentaRepositorio;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Servicio de negocio para la generación de reportes históricos y de auditoría.
 *
 * <p>Este servicio centraliza la lógica para consultar y procesar datos con el fin de
 * crear reportes complejos que proporcionen una visión detallada de las operaciones
 * del negocio a lo largo del tiempo. Todas las operaciones son de solo lectura y
 * están protegidas para que solo usuarios autorizados (administradores) puedan acceder
 * a esta información sensible.</p>
 */
@Service
public class ServicioReportes {

    private final VentaRepositorio ventaRepositorio;
    private final MovimientoInventarioRepositorio movimientoInventarioRepositorio;

    public ServicioReportes(VentaRepositorio ventaRepositorio, MovimientoInventarioRepositorio movimientoInventarioRepositorio) {
        this.ventaRepositorio = ventaRepositorio;
        this.movimientoInventarioRepositorio = movimientoInventarioRepositorio;
    }

    /**
     * Genera un reporte agregado de ventas por producto en un período de tiempo.
     * Reutiliza la consulta de proyección definida en {@link VentaRepositorio} para
     * obtener los datos de manera eficiente.
     *
     * @param fechaInicio La fecha de inicio del período del reporte.
     * @param fechaFin La fecha de fin del período del reporte.
     * @return Una lista de DTOs {@link ReporteVentasDTO} con los datos del reporte.
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public List<ReporteVentasDTO> generarReporteVentas(Instant fechaInicio, Instant fechaFin) {
        return ventaRepositorio.findReporteVentasPorPeriodo(fechaInicio, fechaFin);
    }

    /**
     * Genera el reporte de trazabilidad de inventario (Kardex) para un producto específico
     * dentro de un rango de fechas.
     *
     * @param productoId El ID del producto a consultar.
     * @param fechaInicio La fecha de inicio del período.
     * @param fechaFin La fecha de fin del período.
     * @return Una lista de todos los {@link MovimientoInventario} para ese producto en el período,
     *         ordenados cronológicamente.
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ALMACENISTA')")
    public List<MovimientoInventario> generarReporteKardex(Integer productoId, Instant fechaInicio, Instant fechaFin) {
        return movimientoInventarioRepositorio.findKardexPorProductoYPeriodo(
                productoId, fechaInicio, fechaFin);
    }
}