package com.samvitex.modelos.excepciones;

/**
 * Excepción personalizada para errores que ocurren durante el proceso de una venta.
 */
public class VentaException extends RuntimeException {

    /**
     * Construye una nueva VentaException con el mensaje de detalle especificado.
     * @param message el mensaje de detalle.
     */
    public VentaException(String message) {
        super(message);
    }
}