package com.samvitex.ui.vistas.interfaces;

import com.samvitex.modelos.entidades.Almacen;

import java.util.List;

/**
 * Define el contrato para la vista de gestión de almacenes.
 * Sigue el patrón MVP para separar la lógica de la presentación
 * en la gestión de la entidad {@link Almacen}.
 */
public interface GestionAlmacenesView {

    /**
     * Pobla el componente principal de la vista con una lista de almacenes.
     *
     * @param almacenes La lista de entidades {@link Almacen} a mostrar.
     */
    void mostrarAlmacenes(List<Almacen> almacenes);

    /**
     * Muestra un mensaje de error al usuario en un componente apropiado.
     *
     * @param mensaje El contenido del mensaje de error.
     */
    void mostrarError(String mensaje);

    /**
     * Muestra un mensaje de éxito o informativo al usuario.
     *
     * @param mensaje El contenido del mensaje de éxito.
     */
    void mostrarMensajeExito(String mensaje);

    /**
     * Invoca la apertura del diálogo para crear un nuevo almacén o editar uno existente.
     *
     * @param almacen El almacén que se va a editar. Si es {@code null}, el diálogo se prepara para una nueva entrada.
     */
    void mostrarDialogoAlmacen(Almacen almacen);

    /**
     * Solicita a la vista que actualice sus datos desde la fuente.
     */
    void refrescarVista();

    /**
     * Muestra un diálogo de confirmación para operaciones que requieren validación del usuario.
     *
     * @param mensaje La pregunta de confirmación.
     * @param titulo El título de la ventana de diálogo.
     * @return {@code true} si el usuario confirma la operación, de lo contrario {@code false}.
     */
    boolean confirmarAccion(String mensaje, String titulo);
}