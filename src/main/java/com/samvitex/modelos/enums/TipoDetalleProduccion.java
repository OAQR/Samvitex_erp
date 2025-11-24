package com.samvitex.modelos.enums;

/**
 * Distingue el propósito de una línea de detalle dentro de una Orden de Producción.
 * Permite que una misma tabla de detalles pueda registrar tanto los insumos
 * que se consumen como los productos finales que se generan.
 */
public enum TipoDetalleProduccion {
    /**
     * Indica que la línea de detalle se refiere a una materia prima o insumo
     * que será consumido durante el proceso de producción.
     */
    INSUMO,

    /**
     * Indica que la línea de detalle se refiere a un producto terminado
     * que resultará del proceso de producción.
     */
    PRODUCTO_FINAL
}