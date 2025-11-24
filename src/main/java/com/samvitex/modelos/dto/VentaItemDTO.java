package com.samvitex.modelos.dto;

/**
 * Data Transfer Object (DTO) para transportar la información de un ítem individual
 * desde el carrito de ventas (UI) hacia la capa de servicio ({@code ServicioVentas}).
 *
 * <p>Este DTO simplifica la comunicación entre capas. En lugar de pasar objetos complejos
 * de la UI (como un {@code ItemCarrito}) o entidades JPA completas, se envía solo la
 * información estrictamente necesaria para procesar la venta de un producto: su
 * identificador y la cantidad a vender.</p>
 *
 * <p>Al ser un {@code record}, es inmutable, garantizando que los datos no se modifiquen
 * durante su transporte entre capas.</p>
 *
 * @param productoId El identificador único (ID) del {@link com.samvitex.modelos.entidades.Producto} que se está vendiendo.
 * @param cantidad La cantidad de unidades de ese producto que se van a vender.
 */
public record VentaItemDTO(
        Integer productoId,
        int cantidad
) {
}