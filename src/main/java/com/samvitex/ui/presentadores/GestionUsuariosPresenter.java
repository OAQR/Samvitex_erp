package com.samvitex.ui.presentadores;

import com.samvitex.modelos.entidades.Rol;
import com.samvitex.modelos.entidades.Usuario;
import com.samvitex.repositorios.RolRepositorio;
import com.samvitex.servicios.ServicioUsuario;
import com.samvitex.ui.vistas.interfaces.GestionUsuariosView;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.swing.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Presenter para la vista de Gestión de Usuarios, implementando la lógica de presentación del patrón MVP.
 * <p>
 * Este Presenter actúa como intermediario entre la {@link GestionUsuariosView} (la UI) y el
 * {@link ServicioUsuario} (la lógica de negocio). Orquesta el flujo de datos y eventos:
 * <ul>
 *     <li>Responde a las acciones del usuario (ej. clics en botones) notificadas por la Vista.</li>
 *     <li>Solicita datos a la capa de servicio.</li>
 *     <li>Procesa y prepara los datos para su visualización.</li>
 *     <li>Instruye a la Vista para que muestre los datos o cambie su estado.</li>
 * </ul>
 * Todas las operaciones de larga duración se ejecutan en hilos de fondo mediante {@link SwingWorker}.
 */
public class GestionUsuariosPresenter {

    private final GestionUsuariosView view;
    private final ServicioUsuario servicioUsuario;
    private final RolRepositorio rolRepositorio; // Necesario para poblar el diálogo

    /**
     * Construye un nuevo presenter.
     *
     * @param view La instancia de la vista que este presenter controlará.
     * @param servicioUsuario El servicio de negocio para las operaciones de usuario.
     * @param rolRepositorio El repositorio para obtener la lista de roles.
     */
    public GestionUsuariosPresenter(GestionUsuariosView view, ServicioUsuario servicioUsuario, RolRepositorio rolRepositorio) {
        this.view = view;
        this.servicioUsuario = servicioUsuario;
        this.rolRepositorio = rolRepositorio;
    }

    /**
     * Inicia la carga asíncrona de la lista de todos los usuarios.
     * Al finalizar, instruye a la vista para que los muestre.
     */
    public void cargarUsuarios() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        new SwingWorker<List<Usuario>, Void>() {
            @Override
            protected List<Usuario> doInBackground() throws Exception {
                SecurityContextHolder.getContext().setAuthentication(authentication);
                try {
                    return servicioUsuario.obtenerTodosLosUsuarios();
                } finally {
                    SecurityContextHolder.clearContext();
                }
            }
            @Override
            protected void done() {
                try {
                    view.mostrarUsuarios(get());
                } catch (Exception e) {
                    view.mostrarError("Error al cargar usuarios: " + e.getMessage());
                }
            }
        }.execute();
    }

    /**
     * Orquesta el flujo para crear un nuevo usuario. Carga primero los roles disponibles
     * y luego le pide a la vista que muestre el diálogo en modo de creación.
     */
    public void onNuevoUsuarioClicked() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        new SwingWorker<List<Rol>, Void>() {
            @Override
            protected List<Rol> doInBackground() throws Exception {
                SecurityContextHolder.getContext().setAuthentication(authentication);
                try {
                    return rolRepositorio.findAll();
                } finally {
                    SecurityContextHolder.clearContext();
                }
            }
            @Override
            protected void done() {
                try {
                    view.mostrarDialogoUsuario(null, get());
                } catch (Exception e) {
                    view.mostrarError("Error al preparar formulario: " + e.getMessage());
                }
            }
        }.execute();
    }

    /**
     * Orquesta el flujo para editar un usuario. Carga los datos completos del usuario
     * y la lista de roles disponibles, y luego pide a la vista que muestre el diálogo
     * con los datos precargados.
     *
     * @param usuarioId El ID del usuario seleccionado en la vista.
     */
    public void onEditarUsuarioClicked(Integer usuarioId) {
        if (usuarioId == null) {
            view.mostrarError("No se ha seleccionado un usuario válido.");
            return;
        }
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        new SwingWorker<Usuario, Void>() {
            @Override
            protected Usuario doInBackground() throws Exception {
                SecurityContextHolder.getContext().setAuthentication(authentication);
                try {
                    return servicioUsuario.findByIdForEditing(usuarioId)
                            .orElseThrow(() -> new RuntimeException("El usuario ya no existe."));
                } finally {
                    SecurityContextHolder.clearContext();
                }
            }
            @Override
            protected void done() {
                try {
                    cargarRolesYMostrarDialogo(get());
                } catch (Exception e) {
                    view.mostrarError("Error al obtener datos del usuario: " + e.getMessage());
                }
            }
        }.execute();
    }


    /**
     * Metodo de ayuda que carga la lista de roles y luego instruye a la vista para abrir el diálogo.
     * @param usuario El usuario a editar, o null si es para creación.
     */
    private void cargarRolesYMostrarDialogo(Usuario usuario) {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        new SwingWorker<List<Rol>, Void>() {
            @Override
            protected List<Rol> doInBackground() throws Exception {
                SecurityContextHolder.getContext().setAuthentication(authentication);
                try {
                    return rolRepositorio.findAll();
                } finally {
                    SecurityContextHolder.clearContext();
                }
            }
            @Override
            protected void done() {
                try {
                    view.mostrarDialogoUsuario(usuario, get());
                } catch (Exception e) {
                    view.mostrarError("Error al cargar roles: " + e.getMessage());
                }
            }
        }.execute();
    }


    /**
     * Maneja la lógica para cambiar el estado (activo/inactivo) de un usuario.
     *
     * @param usuarioId El ID del usuario a modificar.
     * @param nombreUsuario El nombre del usuario para mostrarlo en el mensaje de confirmación.
     * @param estadoActual El estado actual del usuario.
     */
    public void onToggleEstadoUsuarioClicked(Integer usuarioId, String nombreUsuario, boolean estadoActual) {
        if (usuarioId == null) {
            view.mostrarError("No se ha seleccionado un usuario válido.");
            return;
        }

        String accion = estadoActual ? "desactivar" : "activar";
        String mensaje = String.format("¿Está seguro de que desea %s al usuario '%s'?", accion, nombreUsuario);
        String titulo = "Confirmar " + (estadoActual ? "Desactivación" : "Activación");

        if (view.confirmarAccion(mensaje, titulo)) {
            final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    try {
                        servicioUsuario.toggleEstadoUsuario(usuarioId);
                        return null;
                    } finally {
                        SecurityContextHolder.clearContext();
                    }
                }
                @Override
                protected void done() {
                    try {
                        get();
                        view.mostrarMensajeExito("El estado del usuario ha sido actualizado.");
                        view.refrescarVista();
                    } catch (Exception e) {
                        handleError(e, "Error al actualizar estado");
                    }
                }
            }.execute();
        }
    }

    /**
     * Centraliza el manejo de excepciones ocurridas en los SwingWorkers.
     * Extrae el mensaje de la causa raíz para ofrecer una retroalimentación más clara al usuario.
     *
     * @param e La excepción capturada del método {@code done()}.
     * @param context El prefijo para el mensaje de error, indicando qué operación falló.
     */
    private void handleError(Exception e, String context) {
        String message = e.getMessage();
        if (e instanceof ExecutionException && e.getCause() != null) {
            message = e.getCause().getMessage();
        }
        view.mostrarError(String.format("%s: %s", context, message));
    }
}