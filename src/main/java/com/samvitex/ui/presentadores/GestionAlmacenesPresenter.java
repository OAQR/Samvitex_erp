package com.samvitex.ui.presentadores;

import com.samvitex.modelos.entidades.Almacen;
import com.samvitex.servicios.ServicioAlmacen;
import com.samvitex.ui.vistas.interfaces.GestionAlmacenesView;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.swing.SwingWorker;
import java.util.List;

/**
 * Presenter para la vista de Gestión de Almacenes.
 */
public class GestionAlmacenesPresenter {

    private final GestionAlmacenesView view;
    private final ServicioAlmacen servicioAlmacen;

    public GestionAlmacenesPresenter(GestionAlmacenesView view, ServicioAlmacen servicioAlmacen) {
        this.view = view;
        this.servicioAlmacen = servicioAlmacen;
    }

    /**
     * Carga todos los almacenes desde la capa de servicio y actualiza la vista.
     */
    public void cargarAlmacenes() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        new SwingWorker<List<Almacen>, Void>() {
            @Override
            protected List<Almacen> doInBackground() throws Exception {
                SecurityContextHolder.getContext().setAuthentication(authentication);
                try {
                    return servicioAlmacen.obtenerTodos();
                } finally {
                    SecurityContextHolder.clearContext();
                }
            }
            @Override
            protected void done() {
                try {
                    view.mostrarAlmacenes(get());
                } catch (Exception e) {
                    view.mostrarError("Error al cargar los almacenes: " + e.getMessage());
                }
            }
        }.execute();
    }

    /**
     * Inicia el flujo para crear un nuevo almacén.
     */
    public void onNuevoAlmacenClicked() {
        view.mostrarDialogoAlmacen(null);
    }
    /**
     * Inicia el flujo para editar un almacén existente.
     *
     * @param almacen El almacén seleccionado en la vista.
     */
    public void onEditarAlmacenClicked(Almacen almacen) {
        if (almacen == null) {
            view.mostrarError("Seleccione un almacén para editar.");
            return;
        }
        view.mostrarDialogoAlmacen(almacen);
    }

    /**
     * Inicia el flujo para desactivar un almacén.
     *
     * @param almacen El almacén seleccionado en la vista.
     */
    public void onDesactivarAlmacenClicked(Almacen almacen) {
        if (almacen == null) {
            view.mostrarError("Seleccione un almacén para desactivar.");
            return;
        }
        String mensaje = String.format("¿Está seguro de que desea desactivar el almacén '%s'?", almacen.getNombre());
        if (view.confirmarAccion(mensaje, "Confirmar Desactivación")) {
            final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    try {
                        servicioAlmacen.desactivar(almacen.getId());
                        return null;
                    } finally {
                        SecurityContextHolder.clearContext();
                    }
                }
                @Override
                protected void done() {
                    try {
                        get();
                        view.mostrarMensajeExito("Almacén desactivado correctamente.");
                        view.refrescarVista();
                    } catch (Exception e) {
                        view.mostrarError("Error al desactivar almacén: " + e.getMessage());
                    }
                }
            }.execute();
        }
    }
}