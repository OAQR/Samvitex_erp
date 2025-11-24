package com.samvitex.ui.vistas.interfaces;

import com.samvitex.modelos.entidades.Rol;
import com.samvitex.modelos.entidades.Usuario;
import java.util.List;

/**
 * Define el contrato para la vista de gestión de usuarios, siguiendo el patrón MVP.
 * <p>
 * Esta interfaz abstrae por completo la implementación de la interfaz de usuario (ej. Swing).
 * El {@code GestionUsuariosPresenter} interactúa exclusivamente con este contrato, ordenando a la
 * vista que realice acciones como mostrar datos o abrir diálogos, sin conocer ningún detalle
 * sobre {@code JTable}, {@code JPanel}, etc.
 */
public interface GestionUsuariosView {

    /**
     * Instruye a la vista para que renderice una lista de usuarios.
     * La implementación se encargará de actualizar su componente de tabla.
     *
     * @param usuarios La lista de entidades {@link Usuario} a mostrar.
     */
    void mostrarUsuarios(List<Usuario> usuarios);

    /**
     * Solicita a la vista que abra un formulario o diálogo para la creación o edición de un usuario.
     *
     * @param usuario El objeto {@link Usuario} a editar. Si es {@code null},
     *                el diálogo se abrirá en modo de creación.
     * @param rolesDisponibles La lista de todos los {@link Rol} que se deben mostrar en el
     *                         selector de roles del diálogo.
     */
    void mostrarDialogoUsuario(Usuario usuario, List<Rol> rolesDisponibles);

    /**
     * Indica a la vista que sus datos pueden estar desactualizados y necesita recargarlos.
     * Típicamente, esto provocará una llamada de vuelta al Presenter para reiniciar el ciclo de carga.
     */
    void refrescarVista();

    /**
     * Muestra un mensaje de error al usuario, generalmente en un diálogo modal.
     *
     * @param mensaje El texto del error a presentar.
     */
    void mostrarError(String mensaje);

    /**
     * Muestra un mensaje de éxito o informativo al usuario.
     *
     * @param mensaje El texto del mensaje a presentar.
     */
    void mostrarMensajeExito(String mensaje);

    /**
     * Presenta un diálogo de confirmación para validar una acción crítica.
     *
     * @param mensaje La pregunta de confirmación para el usuario.
     * @param titulo El título de la ventana del diálogo.
     * @return {@code true} si el usuario confirma la acción, {@code false} en caso contrario.
     */
    boolean confirmarAccion(String mensaje, String titulo);
}