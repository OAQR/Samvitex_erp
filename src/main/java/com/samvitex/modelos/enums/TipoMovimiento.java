package com.samvitex.modelos.enums;

/**
 * Enum que representa los diferentes tipos de movimientos que pueden ocurrir en el inventario.
 * Usar un Enum en lugar de Strings garantiza la consistencia de los datos, previene errores
 * de tipeo y hace el código más legible y autodocumentado.
 *
 * Cada valor del enum corresponde a una operación de negocio específica que afecta el stock.
 */
public enum TipoMovimiento {

    /**
     * Representa una entrada de stock debido a la recepción de una compra a un proveedor.
     * Este movimiento siempre incrementará la cantidad de stock (cantidadMovida > 0).
     */
    ENTRADA_COMPRA,

    /**
     * Representa una salida de stock debido a una venta a un cliente.
     * Este movimiento siempre decrementará la cantidad de stock (cantidadMovida < 0).
     */
    SALIDA_VENTA,

    /**
     * Representa una entrada de stock debido a una devolución de un cliente.
     * Este movimiento incrementará la cantidad de stock (cantidadMovida > 0) y
     * debería estar asociado a una venta original.
     */
    ENTRADA_DEVOLUCION_CLIENTE,

    /**
     * Representa una salida de stock debido a una devolución a un proveedor.
     * Este movimiento decrementará la cantidad de stock (cantidadMovida < 0) y
     * debería estar asociado a una compra original.
     */
    SALIDA_DEVOLUCION_PROVEEDOR,

    /**
     * Representa un ajuste manual que incrementa el stock.
     * Se utiliza para corregir discrepancias encontradas durante un conteo físico,
     * cuando el stock físico es mayor que el stock del sistema. (cantidadMovida > 0).
     */
    AJUSTE_POSITIVO,

    /**
     * Representa un ajuste manual que decrementa el stock.
     * Se utiliza para dar de baja mercancía por daño, pérdida o cuando el stock físico
     * es menor que el del sistema. (cantidadMovida < 0).
     */
    AJUSTE_NEGATIVO,

    /**
     * Representa una salida de stock de materia prima que se envía a un taller
     * para ser utilizada en una orden de producción. (cantidadMovida < 0).
     */
    SALIDA_A_PRODUCCION,

    /**
     * Representa una entrada de stock de un producto terminado que ha sido
     * fabricado y recibido desde un taller. (cantidadMovida > 0).
     */
    ENTRADA_POR_PRODUCCION
}