package com.samvitex.modelos.dto;

/**
 * Data Transfer Object (DTO) para representar un resultado de búsqueda universal.
 * <p>
 * Este DTO inmutable se utiliza para encapsular resultados de diferentes entidades (como
 * {@link com.samvitex.modelos.entidades.Producto} y {@link com.samvitex.modelos.entidades.Cliente})
 * en una única lista, facilitando su manejo en la interfaz de usuario.
 *
 * @param id El identificador único (ID) de la entidad encontrada.
 * @param tipo El tipo de entidad (ej. "PRODUCTO", "CLIENTE").
 * @param textoPrincipal El texto principal a mostrar en la lista de resultados (ej. nombre del producto).
 * @param textoSecundario El texto secundario o descriptivo (ej. SKU del producto o DNI del cliente).
 */
public record SearchResultDTO(
        Integer id,
        String tipo,
        String textoPrincipal,
        String textoSecundario
) {
}