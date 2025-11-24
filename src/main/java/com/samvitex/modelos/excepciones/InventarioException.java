package com.samvitex.modelos.excepciones;

/**
 * Excepción personalizada para errores relacionados con la lógica del inventario.
 * Se utiliza para señalar condiciones de error específicas del negocio, como stock insuficiente.
 */
public class InventarioException extends RuntimeException {

    /**
     * Construye una nueva InventarioException con el mensaje de detalle especificado.
     * @param message el mensaje de detalle.
     */
    public InventarioException(String message) {
        super(message);
    }
}