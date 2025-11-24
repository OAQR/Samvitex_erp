package com.samvitex.modelos.excepciones;

/**
 * Excepción personalizada para errores de lógica de negocio dentro del carrito de compras.
 * Se utiliza para comunicar condiciones de error a la capa de presentación, como
 * intentar añadir un producto sin stock suficiente.
 */
public class CarritoException extends Exception {

    public CarritoException(String message) {
        super(message);
    }
}