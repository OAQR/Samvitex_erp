package com.samvitex.ui.vistas.interfaces;

import com.samvitex.modelos.dto.ReporteVentasDTO;
import com.samvitex.modelos.entidades.MovimientoInventario;
import com.samvitex.modelos.entidades.Producto;

import java.util.List;

/**
 * Define el contrato para la vista del panel de Reportes, siguiendo el patrón MVP.
 */
public interface ReportesView {

    /**
     * Muestra los datos del reporte de ventas en el componente de tabla correspondiente.
     *
     * @param datos La lista de DTOs con la información del reporte.
     */
    void mostrarReporteVentas(List<ReporteVentasDTO> datos);

    /**
     * Muestra los datos del reporte de Kardex en el componente de tabla correspondiente.
     *
     * @param datos La lista de movimientos de inventario.
     */
    void mostrarReporteKardex(List<MovimientoInventario> datos);

    /**
     * Muestra una lista de productos en un componente de selección para que el
     * usuario elija uno para el reporte de Kardex.
     *
     * @param productos La lista de productos disponibles.
     */
    void mostrarListaProductosParaSeleccion(List<Producto> productos);

    /**
     * Alterna la visibilidad de los controles de filtro específicos para cada tipo de reporte.
     * Por ejemplo, muestra el selector de productos solo si el reporte es "Kardex".
     *
     * @param tipoReporte El nombre del reporte seleccionado (ej. "Kardex de Producto").
     */
    void cambiarFiltrosVisibles(String tipoReporte);

    /**
     * Muestra un mensaje de error al usuario.
     *
     * @param mensaje El texto del error.
     */
    void mostrarError(String mensaje);
}