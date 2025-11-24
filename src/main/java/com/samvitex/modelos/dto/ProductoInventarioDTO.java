package com.samvitex.modelos.dto;

import com.samvitex.modelos.entidades.Producto;

/**
 * DTO para mostrar un producto en la tabla de inventario, incluyendo su stock total.
 */
public record ProductoInventarioDTO(
        Producto producto,
        long stockTotal,
        String resumenUbicacion
) {}