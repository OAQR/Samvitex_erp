package com.samvitex.ui.presentadores;

import com.samvitex.modelos.dto.DashboardStatsDTO;
import com.samvitex.servicios.ServicioDashboard;
import com.samvitex.ui.vistas.interfaces.DashboardView;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.swing.SwingWorker;

/**
 * Presenter para la vista del Dashboard.
 *
 * <p>Esta clase orquesta la carga de datos para el dashboard. Utiliza un {@link SwingWorker}
 * para invocar al {@link ServicioDashboard} de forma asíncrona, asegurando que la
 * interfaz de usuario permanezca responsiva durante la carga de datos. Una vez que los
 * datos son obtenidos, se los pasa a la {@link DashboardView} para su renderización.</p>
 */
@Component
public class DashboardPresenter {

    private DashboardView view;
    private final ServicioDashboard servicioDashboard;

    public DashboardPresenter(ServicioDashboard servicioDashboard) {
        this.servicioDashboard = servicioDashboard;
    }

    /**
     * Establece la vista que este presentador controlará.
     * Este metodo se utiliza para la inyección de la vista y romper dependencias circulares.
     * @param view La instancia de la DashboardView.
     */
    public void setView(DashboardView view) {
        this.view = view;
    }

    /**
     * Inicia la carga asíncrona de las estadísticas del dashboard.
     */
    public void cargarEstadisticas() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        new SwingWorker<DashboardStatsDTO, Void>() {
            @Override
            protected DashboardStatsDTO doInBackground() throws Exception {
                SecurityContextHolder.getContext().setAuthentication(authentication);
                try {
                    return servicioDashboard.getDashboardStats();
                } finally {
                    SecurityContextHolder.clearContext();
                }
            }

            @Override
            protected void done() {
                try {
                    DashboardStatsDTO stats = get();
                    view.mostrarEstadisticas(stats);
                } catch (Exception e) {
                    view.mostrarError("No se pudieron cargar las estadísticas: " + e.getMessage());
                }
            }
        }.execute();
    }
}