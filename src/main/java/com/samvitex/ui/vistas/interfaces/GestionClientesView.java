package com.samvitex.ui.vistas.interfaces;

import com.samvitex.modelos.entidades.Cliente;

import java.util.List;

/**
 * Define el contrato para la vista de gestión de clientes.
 * Implementa el lado de la Vista del patrón Model-View-Presenter para la
 * entidad {@link Cliente}.
 */
public interface GestionClientesView {

    /**
     * Actualiza la tabla de la vista con una lista de clientes.
     *
     * @param clientes La lista de entidades {@link Cliente} a mostrar.
     */
    void mostrarClientes(List<Cliente> clientes);

    /**
     * Despliega un diálogo con un mensaje de error.
     *
     * @param mensaje El mensaje de error.
     */
    void mostrarError(String mensaje);

    /**
     * Despliega un diálogo con un mensaje de éxito.
     *
     * @param mensaje El mensaje de éxito.
     */
    void mostrarMensajeExito(String mensaje);

    /**
     * Lanza el formulario modal para crear o editar un cliente.
     *
     * @param cliente El cliente a ser editado. Si se pasa {@code null}, el formulario se abrirá en modo de creación.
     */
    void mostrarDialogoCliente(Cliente cliente);

    /**
     * Indica a la vista que debe recargar sus datos.
     */
    void refrescarVista();

    /**
     * Muestra un diálogo de confirmación y retorna la elección del usuario.
     *
     * @param mensaje La pregunta a mostrar en el diálogo.
     * @param titulo El título para la ventana del diálogo.
     * @return {@code true} si el usuario confirma, {@code false} si cancela.
     */
    boolean confirmarAccion(String mensaje, String titulo);

    /**
     * Solicita a la vista que encuentre, muestre y seleccione un cliente específico por su ID.
     * @param clienteId El ID del cliente a seleccionar.
     */
    void seleccionarYMostrarCliente(Integer clienteId);

    /**
     * Establece el texto en el campo de búsqueda de la vista de clientes.
     *
     * @param termino El texto a mostrar en el campo de búsqueda.
     */
    void setTerminoBusqueda(String termino);
}