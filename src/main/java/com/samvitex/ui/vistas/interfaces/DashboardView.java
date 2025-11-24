package com.samvitex.ui.vistas.interfaces;

import com.samvitex.modelos.dto.DashboardStatsDTO;

/**
 * Define el contrato para la vista del Dashboard, siguiendo el patrón MVP.
 *
 * <p>Esta interfaz abstrae las operaciones de la UI del dashboard, permitiendo al
 * {@code DashboardPresenter} actualizar la vista sin conocer los detalles de
 * los componentes de Swing.</p>
 */
public interface DashboardView {

    /**
     * Instruye a la vista para que muestre las estadísticas proporcionadas.
     * La implementación se encargará de actualizar cada tarjeta de KPI con los
     * datos del DTO.
     *
     * @param stats El DTO que contiene todas las estadísticas del dashboard.
     */
    void mostrarEstadisticas(DashboardStatsDTO stats);

    /**
     * Instruye a la vista para que muestre un estado de error.
     * Esto podría implicar mostrar un mensaje en las tarjetas de KPI o un diálogo.
     *
     * @param mensaje El mensaje de error a mostrar.
     */
    void mostrarError(String mensaje);

    /**
     * Solicita a la vista que inicie un proceso de actualización de sus datos.
     * Típicamente, esto resultará en una llamada de vuelta al Presenter para
     * recargar las estadísticas.
     */
    void refrescarVista();
}