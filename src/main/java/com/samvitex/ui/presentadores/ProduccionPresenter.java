package com.samvitex.ui.presentadores;

import com.samvitex.modelos.entidades.OrdenProduccion;
import com.samvitex.servicios.ServicioProduccion;
import com.samvitex.ui.vistas.interfaces.ProduccionView;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.swing.SwingWorker;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Presenter para la vista de Gestión de Producción.
 * Orquesta la interacción entre la vista y el servicio, manejando el ciclo de vida de las órdenes.
 */
public class ProduccionPresenter {

    private final ProduccionView view;
    private final ServicioProduccion servicioProduccion;

    public ProduccionPresenter(ProduccionView view, ServicioProduccion servicioProduccion) {
        this.view = view;
        this.servicioProduccion = servicioProduccion;
    }

    public void cargarOrdenes() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        new SwingWorker<List<OrdenProduccion>, Void>() {
            @Override
            protected List<OrdenProduccion> doInBackground() throws Exception {
                SecurityContextHolder.getContext().setAuthentication(authentication);
                try {
                    return servicioProduccion.obtenerTodas();
                } finally {
                    SecurityContextHolder.clearContext();
                }
            }
            @Override
            protected void done() {
                try {
                    view.mostrarOrdenes(get());
                } catch (Exception e) { handleError(e, "Error al cargar órdenes de producción"); }
            }
        }.execute();
    }

    public void onNuevaOrdenClicked() {
        view.abrirDialogoCrearOrden();
    }

    public void onIniciarProduccionClicked(OrdenProduccion orden) {
        if (view.confirmarAccion("¿Desea iniciar la producción para la orden '" + orden.getCodigo() + "'?\nEsto descontará los insumos del inventario.", "Confirmar Inicio")) {
            final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    try {
                        servicioProduccion.iniciarProduccion(orden.getId());
                        return null;
                    } finally {
                        SecurityContextHolder.clearContext();
                    }
                }
                @Override
                protected void done() {
                    try {
                        get();
                        view.mostrarMensajeExito("Producción iniciada. Stock de insumos actualizado.");
                        view.refrescarVista();
                    } catch (Exception e) {
                        handleError(e, "Error al iniciar producción");
                    }
                }
            }.execute();
        }
    }

    public void onFinalizarProduccionClicked(OrdenProduccion orden) {
        if (view.confirmarAccion("¿Desea finalizar la producción para la orden '" + orden.getCodigo() + "'?\nEsto ingresará los productos terminados al inventario.", "Confirmar Finalización")) {
            final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    try {
                        servicioProduccion.finalizarProduccion(orden.getId());                        return null;
                    } finally {
                        SecurityContextHolder.clearContext();
                    }
                }
                @Override
                protected void done() {
                    try {
                        get();
                        view.mostrarMensajeExito("Producción finalizada. Stock de productos actualizado.");
                        view.refrescarVista();
                    } catch (Exception e) {
                        handleError(e, "Error al finalizar producción");
                    }
                }
            }.execute();
        }
    }

    private void handleError(Exception e, String context) {
        String message = (e instanceof ExecutionException && e.getCause() != null) ? e.getCause().getMessage() : e.getMessage();
        view.mostrarError(String.format("%s: %s", context, message));
    }
}