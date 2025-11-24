package com.samvitex.ui.presentadores;

import com.samvitex.modelos.entidades.Categoria;
import com.samvitex.servicios.ServicioCategoria;
import com.samvitex.ui.vistas.interfaces.GestionCategoriasView;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.swing.SwingWorker;
import java.util.List;

/**
 * Presenter para la vista de Gestión de Categorías.
 * Contiene la lógica para cargar, crear, editar y eliminar categorías.
 */
public class GestionCategoriasPresenter {

    private final GestionCategoriasView view;
    private final ServicioCategoria servicioCategoria;

    /**
     * Construye un nuevo presenter.
     *
     * @param view La instancia de la vista que este presenter controlará.
     * @param servicioCategoria El servicio de negocio para las operaciones de categoría.
     */
    public GestionCategoriasPresenter(GestionCategoriasView view, ServicioCategoria servicioCategoria) {
        this.view = view;
        this.servicioCategoria = servicioCategoria;
    }

    /**
     * Carga la lista de todas las categorías de forma asíncrona y le indica a la vista que las muestre.
     */
    public void cargarCategorias() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        new SwingWorker<List<Categoria>, Void>() {
            @Override
            protected List<Categoria> doInBackground() throws Exception {
                SecurityContextHolder.getContext().setAuthentication(authentication);
                try {
                    return servicioCategoria.obtenerTodas();
                } finally {
                    SecurityContextHolder.clearContext();
                }
            }
            @Override
            protected void done() {
                try {
                    view.mostrarCategorias(get());
                } catch (Exception e) {
                    view.mostrarError("Error al cargar categorías: " + e.getMessage());
                }
            }
        }.execute();
    }

    /**
     * Maneja el evento de clic en "Nueva Categoría", indicando a la vista que abra el diálogo.
     */
    public void onNuevaCategoriaClicked() {
        view.mostrarDialogoCategoria(null);
    }

    /**
     * Maneja el evento de clic en "Editar Categoría".
     *
     * @param categoria La categoría seleccionada en la vista para editar.
     */
    public void onEditarCategoriaClicked(Categoria categoria) {
        if (categoria == null) {
            view.mostrarError("Por favor, seleccione una categoría para editar.");
            return;
        }
        view.mostrarDialogoCategoria(categoria);
    }

    /**
     * Maneja el evento de clic en "Eliminar Categoría". Pide confirmación antes de proceder.
     *
     * @param categoria La categoría seleccionada para eliminar.
     */
    public void onEliminarCategoriaClicked(Categoria categoria) {
        if (categoria == null) {
            view.mostrarError("Por favor, seleccione una categoría para eliminar.");
            return;
        }
        String mensaje = String.format("¿Está seguro de que desea eliminar la categoría '%s'?", categoria.getNombre());
        if (view.confirmarAccion(mensaje, "Confirmar Eliminación")) {
            final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    try {
                        servicioCategoria.eliminar(categoria.getId());
                        return null;
                    } finally {
                        SecurityContextHolder.clearContext();
                    }
                }
                @Override
                protected void done() {
                    try {
                        get();
                        view.mostrarMensajeExito("Categoría eliminada correctamente.");
                        view.refrescarVista();
                    } catch (Exception e) {
                        view.mostrarError("Error al eliminar la categoría: " + e.getMessage());
                    }
                }
            }.execute();
        }
    }
}