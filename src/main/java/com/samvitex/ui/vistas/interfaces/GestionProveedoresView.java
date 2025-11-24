package com.samvitex.ui.vistas.interfaces;

import com.samvitex.modelos.entidades.Proveedor;

import java.util.List;

/**
 * Define el contrato para la vista de gestión de proveedores.
 * Esta interfaz desacopla al Presenter de la implementación concreta de la UI (Swing).
 */
public interface GestionProveedoresView {

    /**
     * Muestra una lista de proveedores en el componente de tabla.
     *
     * @param proveedores La lista de entidades {@link Proveedor} para mostrar.
     */
    void mostrarProveedores(List<Proveedor> proveedores);

    /**
     * Muestra un mensaje de error en un diálogo.
     *
     * @param mensaje El mensaje de error a mostrar.
     */
    void mostrarError(String mensaje);

    /**
     * Muestra un mensaje de éxito en un diálogo.
     *
     * @param mensaje El mensaje de éxito a mostrar.
     */
    void mostrarMensajeExito(String mensaje);

    /**
     * Abre el diálogo modal para la creación o edición de un proveedor.
     *
     * @param proveedor El proveedor a editar. Si es {@code null}, se abre en modo de creación.
     */
    void mostrarDialogoProveedor(Proveedor proveedor);

    /**
     * Dispara una recarga de los datos mostrados en la vista.
     */
    void refrescarVista();

    /**
     * Presenta un diálogo de confirmación al usuario para acciones críticas.
     *
     * @param mensaje La pregunta que se le hará al usuario.
     * @param titulo El título del diálogo de confirmación.
     * @return {@code true} si el usuario presiona 'Sí' o 'Aceptar', {@code false} de lo contrario.
     */
    boolean confirmarAccion(String mensaje, String titulo);
}