package com.samvitex.modelos.excepciones;

/**
 * Excepción personalizada para errores relacionados con la lógica del módulo de Producción.
 * Se utiliza para señalar condiciones de error específicas del negocio, como la validación
 * de una orden de producción o problemas durante el cambio de estado.
 */
public class ProduccionException extends RuntimeException {

    /**
     * Construye una nueva ProduccionException con el mensaje de detalle especificado.
     * @param message el mensaje de detalle que explica la razón de la excepción.
     */
    public ProduccionException(String message) {
        super(message);
    }
}