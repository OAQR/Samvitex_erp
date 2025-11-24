package com.samvitex.servicios;

import com.samvitex.modelos.dto.DashboardStatsDTO;
import com.samvitex.repositorios.ProductoRepositorio;
import com.samvitex.repositorios.VentaRepositorio;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Servicio de negocio para obtener las estadísticas clave (KPIs) del Dashboard.
 *
 * <p>Este servicio es responsable de consultar diferentes repositorios para agregar
 * y consolidar la información más relevante sobre el estado actual del negocio en un
 * único DTO ({@link DashboardStatsDTO}). Las operaciones son de solo lectura y están
 * optimizadas para un rendimiento rápido.</p>
 */
@Service
public class ServicioDashboard {

    private final ProductoRepositorio productoRepositorio;
    private final VentaRepositorio ventaRepositorio;

    public ServicioDashboard(ProductoRepositorio productoRepositorio, VentaRepositorio ventaRepositorio) {
        this.productoRepositorio = productoRepositorio;
        this.ventaRepositorio = ventaRepositorio;
    }

    /**
     * Recopila todas las estadísticas necesarias para el dashboard en una sola operación transaccional.
     * Esta operación es de solo lectura y está protegida para que solo los roles autorizados
     * puedan acceder a esta información consolidada del negocio.
     *
     * @return un {@link DashboardStatsDTO} con todas las estadísticas calculadas.
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VENDEDOR', 'ALMACENISTA')")
    public DashboardStatsDTO getDashboardStats() {
        // Ejecuta las 4 consultas de agregación necesarias para los KPIs.
        long totalProductos = productoRepositorio.count();
        long productosConStockBajo = productoRepositorio.countByStockBajo();
        BigDecimal valorTotalInventario = productoRepositorio.findValorTotalInventario();
        BigDecimal ventasHoy = ventaRepositorio.findTotalVentasEnPeriodo(getInicioDelDia(), Instant.now());

        return new DashboardStatsDTO(totalProductos, productosConStockBajo, valorTotalInventario, ventasHoy);
    }

    /**
     * Metodo de utilidad privado para obtener el {@link Instant} que representa el inicio del día actual
     * (00:00:00) en la zona horaria del sistema.
     *
     * @return un {@code Instant} que marca el comienzo del día.
     */
    private Instant getInicioDelDia() {
        return LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant();
    }
}