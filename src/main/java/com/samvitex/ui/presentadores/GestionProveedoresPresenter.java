package com.samvitex.ui.presentadores;

import com.samvitex.modelos.entidades.Proveedor;
import com.samvitex.servicios.ServicioProveedor;
import com.samvitex.ui.vistas.interfaces.GestionProveedoresView;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.swing.SwingWorker;
import java.util.List;

/**
 * Presenter para la vista de Gestión de Proveedores.
 * Orquesta la interacción entre la vista y el servicio de proveedores.
 */
public class GestionProveedoresPresenter {

    private final GestionProveedoresView view;
    private final ServicioProveedor servicioProveedor;

    public GestionProveedoresPresenter(GestionProveedoresView view, ServicioProveedor servicioProveedor) {
        this.view = view;
        this.servicioProveedor = servicioProveedor;
    }

    /**
     * Carga la lista de todos los proveedores y actualiza la vista.
     */
    public void cargarProveedores() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        new SwingWorker<List<Proveedor>, Void>() {
            @Override
            protected List<Proveedor> doInBackground() throws Exception {
                SecurityContextHolder.getContext().setAuthentication(authentication);
                try {
                    return servicioProveedor.obtenerTodos();
                } finally {
                    SecurityContextHolder.clearContext();
                }
            }
            @Override
            protected void done() {
                try {
                    view.mostrarProveedores(get());
                } catch (Exception e) {
                    view.mostrarError("Error al cargar proveedores: " + e.getMessage());
                }
            }
        }.execute();
    }

    /**
     * Inicia el flujo para crear un nuevo proveedor.
     */
    public void onNuevoProveedorClicked() {
        view.mostrarDialogoProveedor(null);
    }

    /**
     * Inicia el flujo para editar un proveedor existente.
     *
     * @param proveedor El proveedor seleccionado que se desea editar.
     */
    public void onEditarProveedorClicked(Proveedor proveedor) {
        if (proveedor == null) {
            view.mostrarError("Seleccione un proveedor para editar.");
            return;
        }
        view.mostrarDialogoProveedor(proveedor);
    }

    /**
     * Inicia el flujo para desactivar un proveedor (borrado lógico).
     *
     * @param proveedor El proveedor seleccionado que se desea desactivar.
     */
    public void onDesactivarProveedorClicked(Proveedor proveedor) {
        if (proveedor == null) {
            view.mostrarError("Seleccione un proveedor para desactivar.");
            return;
        }
        String mensaje = String.format("¿Está seguro de que desea desactivar al proveedor '%s'?", proveedor.getNombre());
        if (view.confirmarAccion(mensaje, "Confirmar Desactivación")) {
            final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    try {
                        servicioProveedor.desactivar(proveedor.getId());
                        return null;
                    } finally {
                        SecurityContextHolder.clearContext();
                    }
                }
                @Override
                protected void done() {
                    try {
                        get();
                        view.mostrarMensajeExito("Proveedor desactivado correctamente.");
                        view.refrescarVista();
                    } catch (Exception e) {
                        view.mostrarError("Error al desactivar proveedor: " + e.getMessage());
                    }
                }
            }.execute();
        }
    }
}