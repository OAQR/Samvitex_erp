package com.samvitex.modelos.dto;

import com.samvitex.modelos.enums.TipoDetalleProduccion;
import java.util.List;

/**
 * DTO para transportar los datos necesarios para crear una nueva Orden de Producción
 * desde la capa de presentación (UI) a la capa de servicio.
 *
 * @param tallerId ID del taller asignado.
 * @param codigo El código único para la nueva orden.
 * @param detalles La lista de ítems (insumos y productos finales) de la orden.
 */
public record OrdenProduccionDTO(
        Integer tallerId,
        String codigo,
        Integer almacenInsumosId,
        Integer almacenDestinoId,
        List<DetalleDTO> detalles
) {
    /**
     * DTO anidado para representar una línea de detalle dentro de la orden de producción.
     * @param productoId ID del producto.
     * @param tipoDetalle Si es INSUMO o PRODUCTO_FINAL.
     * @param cantidad La cantidad requerida o a producir.
     */
    public record DetalleDTO(
            Integer productoId,
            TipoDetalleProduccion tipoDetalle,
            int cantidad
    ) {}
}