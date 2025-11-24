package com.samvitex.modelos.enums;

/**
 * Representa los diferentes estados en el ciclo de vida de una Orden de Producción.
 * Permite un seguimiento claro del progreso de cada orden de fabricación.
 */
public enum EstadoProduccion {
    /**
     * La orden ha sido creada y planificada, pero los materiales aún no han sido
     * asignados o enviados al taller.
     */
    PLANIFICADA,

    /**
     * Los materiales han sido descontados del inventario y enviados al taller.
     * La fabricación está en curso.
     */
    EN_PRODUCCION,

    /**
     * La producción ha finalizado y los productos terminados están pendientes de
     * revisión de calidad antes de ser ingresados al inventario.
     */
    CONTROL_CALIDAD,

    /**
     * La orden ha sido completada exitosamente y los productos terminados
     * han sido añadidos al stock del inventario.
     */
    COMPLETADA,

    /**
     * La orden ha sido cancelada antes de su finalización. Cualquier material
     * asignado debería ser revertido al inventario.
     */
    CANCELADA
}