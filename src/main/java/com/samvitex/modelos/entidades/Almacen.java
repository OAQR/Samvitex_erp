package com.samvitex.modelos.entidades;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa una ubicación física de almacenamiento de productos (almacén o depósito).
 * Mapea a la tabla 'almacenes' en la base de datos.
 * Incluye un campo 'activo' para permitir el borrado lógico.
 */
@Entity
@Table(name = "almacenes")
public class Almacen {

    /**
     * Identificador único del almacén.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * Nombre del almacén. Debe ser único.
     */
    @Column(unique = true, nullable = false, length = 150)
    private String nombre;

    /**
     * Descripción detallada de la ubicación del almacén.
     */
    @Column(name = "ubicacion_descripcion", columnDefinition = "TEXT")
    private String ubicacionDescripcion;

    /**
     * Indica si el almacén está operativo. Un almacén inactivo no puede
     * ser seleccionado como destino para productos.
     */
    @Column(nullable = false)
    private boolean activo = true;

    /**
     * Constructor por defecto.
     */
    public Almacen() {}

    // --- Getters y Setters ---

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getUbicacionDescripcion() {
        return ubicacionDescripcion;
    }

    public void setUbicacionDescripcion(String ubicacionDescripcion) {
        this.ubicacionDescripcion = ubicacionDescripcion;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    /**
     * Representación en String, útil para mostrar en ComboBoxes.
     * @return El nombre del almacén.
     */
    @Override
    public String toString() {
        return nombre;
    }
}