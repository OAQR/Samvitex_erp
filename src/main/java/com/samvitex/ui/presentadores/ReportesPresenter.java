package com.samvitex.ui.presentadores;

import com.samvitex.modelos.dto.ReporteVentasDTO;
import com.samvitex.modelos.entidades.MovimientoInventario;
import com.samvitex.modelos.entidades.Producto;
import com.samvitex.servicios.ServicioInventario;
import com.samvitex.servicios.ServicioReportes;
import com.samvitex.ui.vistas.interfaces.ReportesView;

import javax.swing.SwingWorker;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Presenter para la vista de Reportes (Patrón MVP).
 * Orquesta la lógica para generar reportes, invocando al servicio de forma asíncrona
 * y pasando los resultados a la vista para su renderización.
 */
public class ReportesPresenter {

    private final ReportesView view;
    private final ServicioReportes servicioReportes;
    private final ServicioInventario servicioInventario;

    public ReportesPresenter(ReportesView view, ServicioReportes servicioReportes, ServicioInventario servicioInventario) {
        this.view = view;
        this.servicioReportes = servicioReportes;
        this.servicioInventario = servicioInventario;
    }

    /**
     * Carga los datos iniciales para los filtros (lista de productos para Kardex).
     */
    public void cargarDatosIniciales() {
        new SwingWorker<List<Producto>, Void>() {
            @Override
            protected List<Producto> doInBackground() throws Exception {
                return servicioInventario.obtenerTodosLosProductos();
            }
            @Override
            protected void done() {
                try {
                    view.mostrarListaProductosParaSeleccion(get());
                } catch (Exception e) {
                    handleError(e, "Error al cargar lista de productos");
                }
            }
        }.execute();
    }

    /**
     * Notifica a la vista que debe ajustar los filtros visibles según el tipo de reporte.
     * @param tipoReporte El nombre del reporte seleccionado.
     */
    public void onTipoReporteCambiado(String tipoReporte) {
        view.cambiarFiltrosVisibles(tipoReporte);
    }

    /**
     * Inicia la generación de un reporte validando los parámetros de la vista.
     */
    public void generarReporte(String tipoReporte, Instant fechaInicio, Instant fechaFin, Producto productoSeleccionado) {
        if (fechaInicio.isAfter(fechaFin)) {
            view.mostrarError("La fecha de inicio no puede ser posterior a la fecha de fin.");
            return;
        }

        switch (tipoReporte) {
            case "Ventas por Producto":
                generarReporteVentasAsync(fechaInicio, fechaFin);
                break;
            case "Kardex de Producto":
                if (productoSeleccionado == null) {
                    view.mostrarError("Debe seleccionar un producto para generar el reporte de Kardex.");
                    return;
                }
                generarReporteKardexAsync(productoSeleccionado.getId(), fechaInicio, fechaFin);
                break;
            default:
                view.mostrarError("Tipo de reporte no reconocido.");
        }
    }

    private void generarReporteVentasAsync(Instant inicio, Instant fin) {
        final org.springframework.security.core.Authentication authentication =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();

        new SwingWorker<List<ReporteVentasDTO>, Void>() {
            @Override
            protected List<ReporteVentasDTO> doInBackground() throws Exception {
                org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(authentication);
                try {
                    return servicioReportes.generarReporteVentas(inicio, fin);
                } finally {
                    org.springframework.security.core.context.SecurityContextHolder.clearContext();
                }
            }
            @Override
            protected void done() {
                try {
                    view.mostrarReporteVentas(get());
                } catch (Exception e) {
                    handleError(e, "Error al generar reporte de ventas");
                }
            }
        }.execute();
    }

    private void generarReporteKardexAsync(Integer productoId, Instant inicio, Instant fin) {
        final org.springframework.security.core.Authentication authentication =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();

        new SwingWorker<List<MovimientoInventario>, Void>() {
            @Override
            protected List<MovimientoInventario> doInBackground() throws Exception {
                org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(authentication);
                try {
                    return servicioReportes.generarReporteKardex(productoId, inicio, fin);
                } finally {
                    org.springframework.security.core.context.SecurityContextHolder.clearContext();
                }
            }
            @Override
            protected void done() {
                try {
                    view.mostrarReporteKardex(get());
                } catch (Exception e) {
                    handleError(e, "Error al generar reporte de Kardex");
                }
            }
        }.execute();
    }

    private void handleError(Exception e, String context) {
        String message = (e instanceof ExecutionException && e.getCause() != null) ? e.getCause().getMessage() : e.getMessage();
        view.mostrarError(String.format("%s: %s", context, message));
    }
}