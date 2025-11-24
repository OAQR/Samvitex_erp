package com.samvitex.eventos;

/**
 * Interfaz para definir un evento que se dispara cuando el índice de la ubicación
 * en la superposición principal ha cambiado.
 */
public interface EventoSuperposicionPrincipal {
    /**
     * Se invoca cuando el índice de la ubicación seleccionada en la superposición cambia.
     *
     * @param nuevoIndice El nuevo índice de la ubicación seleccionada.
     */
    void alCambiar(int nuevoIndice);
}