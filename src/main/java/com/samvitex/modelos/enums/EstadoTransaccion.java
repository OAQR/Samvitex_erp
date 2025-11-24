package com.samvitex.modelos.enums;

/**
 * Representa los estados posibles de una transacción como una Venta o Compra.
 */
public enum EstadoTransaccion {
    /**
     * La transacción se ha completado exitosamente.
     */
    COMPLETADA,

    /**
     * La transacción está en un estado intermedio, pendiente de alguna acción.
     */
    PENDIENTE,

    /**
     * La transacción ha sido anulada y no tiene efecto contable o de inventario.
     */
    ANULADA
}