package com.samvitex.modelos.entidades;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;

/**
 * Representa a un cliente de la empresa.
 * Mapea a la tabla 'clientes' en la base de datos.
 * Incluye un campo 'activo' para permitir el borrado lógico.
 */
@Entity
@Table(name = "clientes")
public class Cliente {

    /**
     * Identificador único del cliente.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * Nombre completo o razón social del cliente.
     */
    @Column(name = "nombre_completo", nullable = false)
    private String nombreCompleto;

    /**
     * Documento de identidad (DNI) o RUC del cliente. Debe ser único.
     */
    @Column(name = "dni_ruc", unique = true, length = 20)
    private String dniRuc;

    /**
     * Correo electrónico del cliente.
     */
    @Column(length = 255)
    private String email;

    /**
     * Número de teléfono del cliente.
     */
    @Column(length = 50)
    private String telefono;

    /**
     * Dirección física del cliente.
     */
    @Column(columnDefinition = "TEXT")
    private String direccion;

    /**
     * Indica si el cliente está activo. Un cliente inactivo no puede ser seleccionado
     * para nuevas ventas.
     */
    @Column(nullable = false)
    private boolean activo = true;

    /**
     * Marca de tiempo de la creación del registro del cliente, gestionada automáticamente.
     */
    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false) // Nombre de columna corregido
    private Instant fechaCreacion;

    /**
     * Constructor por defecto.
     */
    public Cliente() {}

    // --- Getters y Setters ---

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getDniRuc() {
        return dniRuc;
    }

    public void setDniRuc(String dniRuc) {
        this.dniRuc = dniRuc;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public Instant getFechaCreacion() {
        return fechaCreacion;
    }
    public void setFechaCreacion(Instant fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    /**
     * Representación en String, útil para mostrar en ComboBoxes.
     * @return El nombre completo del cliente.
     */
    @Override
    public String toString() {
        return nombreCompleto;
    }
}