package com.samvitex.ui.vistas.interfaces;

import com.samvitex.modelos.entidades.OrdenProduccion;
import java.util.List;

/**
 * Define el contrato para la vista de gestión de Órdenes de Producción, siguiendo el patrón MVP.
 * <p>
 * Esta interfaz desacopla al {@code ProduccionPresenter} de la implementación concreta de la UI,
 * permitiendo que la lógica de presentación controle la vista a través de estos métodos definidos.
 */
public interface ProduccionView {

    /**
     * Instruye a la vista para que muestre una lista de órdenes de producción.
     *
     * @param ordenes La lista de entidades {@link OrdenProduccion} a renderizar.
     */
    void mostrarOrdenes(List<OrdenProduccion> ordenes);

    /**
     * Muestra un diálogo modal con un mensaje de error.
     *
     * @param mensaje El texto del error a presentar al usuario.
     */
    void mostrarError(String mensaje);

    /**
     * Muestra un diálogo modal con un mensaje de éxito o informativo.
     *
     * @param mensaje El texto del mensaje a presentar.
     */
    void mostrarMensajeExito(String mensaje);

    /**
     * Solicita a la vista que inicie el flujo para crear una nueva orden de producción,
     * típicamente abriendo un diálogo especializado.
     */
    void abrirDialogoCrearOrden();

    /**
     * Indica a la vista que sus datos deben ser recargados. Esto usualmente resulta
     * en una llamada de vuelta al presenter para obtener los datos más recientes.
     */
    void refrescarVista();

    /**
     * Presenta al usuario un diálogo de confirmación para validar una acción crítica,
     * como iniciar o finalizar una orden de producción.
     *
     * @param mensaje La pregunta de confirmación que se le mostrará al usuario (ej. "¿Está seguro?").
     * @param titulo El título para la ventana del diálogo de confirmación.
     * @return {@code true} si el usuario confirma la acción (ej. presiona "Sí"),
     *         {@code false} en caso contrario.
     */
    boolean confirmarAccion(String mensaje, String titulo);
}