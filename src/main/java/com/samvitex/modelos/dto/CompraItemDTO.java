package com.samvitex.modelos.dto;

import java.math.BigDecimal;

/**
 * Data Transfer Object (DTO) para transportar la información de un ítem individual
 * desde el formulario de compras (UI) hacia la capa de servicio ({@code ServicioCompras}).
 *
 * <p>Este DTO encapsula los datos necesarios para registrar la compra de un producto.
 * A diferencia del {@link VentaItemDTO}, este incluye el {@code costoUnitario}, ya que
 * el precio de compra de un producto puede variar en cada transacción con el proveedor,
 * y es un dato que el usuario introduce o confirma en la UI.</p>
 *
 * <p>El uso de este DTO desacopla la capa de servicio de los detalles de implementación
 * de la tabla de compras en la UI.</p>
 *
 * @param productoId El identificador único (ID) del {@link com.samvitex.modelos.entidades.Producto} que se está comprando.
 * @param cantidad La cantidad de unidades de ese producto que se están comprando.
 * @param costoUnitario El costo por unidad del producto en esta compra específica.
 */
public record CompraItemDTO(
        Integer productoId,
        int cantidad,
        BigDecimal costoUnitario
) {
}