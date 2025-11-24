package com.samvitex.modelos.entidades;

import jakarta.persistence.*;

/**
 * Entidad que representa un Rol dentro del sistema (ej. ADMINISTRADOR, VENDEDOR).
 * Un rol define un conjunto de permisos y accesos que un usuario puede tener.
 * Está mapeado a la tabla 'roles' en la base de datos y actúa como una tabla de catálogo.
 */
@Entity
@Table(name = "roles")
public class Rol {

    /**
     * Identificador único del rol, generado automáticamente por la base de datos.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * Nombre único del rol. Este campo es crucial para la lógica de autorización
     * basada en roles de Spring Security (ej. "ROLE_ADMINISTRADOR").
     */
    @Column(unique = true, nullable = false, length = 50)
    private String nombre;

    /**
     * Constructor por defecto requerido por JPA.
     */
    public Rol() {
    }

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

    /**
     * Proporciona una representación en formato de cadena del objeto Rol.
     * Es particularmente útil para mostrar el nombre del rol en componentes de UI
     * como JComboBox.
     *
     * @return El nombre del rol.
     */
    @Override
    public String toString() {
        return nombre;
    }
}