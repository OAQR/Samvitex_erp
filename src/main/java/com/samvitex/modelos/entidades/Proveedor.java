package com.samvitex.modelos.entidades;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa a un proveedor de insumos o productos para la empresa.
 * Mapea a la tabla 'proveedores' en la base de datos.
 * Incluye un campo 'activo' para permitir el borrado lógico (soft delete),
 * preservando la integridad de las compras históricas asociadas a este proveedor.
 */
@Entity
@Table(name = "proveedores")
public class Proveedor {

    /**
     * Identificador único del proveedor, generado automáticamente.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * Nombre comercial o razón social del proveedor. Debe ser único.
     */
    @Column(unique = true, nullable = false, length = 255)
    private String nombre;

    /**
     * Registro Único de Contribuyentes (RUC) del proveedor. Debe ser único.
     */
    @Column(unique = true, length = 20)
    private String ruc;

    /**
     * Correo electrónico de contacto principal del proveedor.
     */
    @Column(name = "contacto_email", length = 255)
    private String contactoEmail;

    /**
     * Número de teléfono de contacto principal del proveedor.
     */
    @Column(name = "contacto_telefono", length = 50)
    private String contactoTelefono;

    /**
     * Indica si el proveedor está activo en el sistema.
     * Un proveedor inactivo no aparecerá en las listas para nuevas compras,
     * pero sus registros históricos se mantienen.
     */
    @Column(nullable = false)
    private boolean activo = true;

    /**
     * Lista de productos que son suministrados por este proveedor.
     * La carga es perezosa (LAZY) para optimizar el rendimiento.
     */
    @OneToMany(mappedBy = "proveedor", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Producto> productosSuministrados = new ArrayList<>();

    /**
     * Constructor por defecto requerido por JPA.
     */
    public Proveedor() {}

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

    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    public String getContactoEmail() {
        return contactoEmail;
    }

    public void setContactoEmail(String contactoEmail) {
        this.contactoEmail = contactoEmail;
    }

    public String getContactoTelefono() {
        return contactoTelefono;
    }

    public void setContactoTelefono(String contactoTelefono) {
        this.contactoTelefono = contactoTelefono;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public List<Producto> getProductosSuministrados() {
        return productosSuministrados;
    }

    public void setProductosSuministrados(List<Producto> productosSuministrados) {
        this.productosSuministrados = productosSuministrados;
    }

    /**
     * Representación en String del objeto, útil para logging y debugging.
     * Devuelve el nombre del proveedor.
     *
     * @return El nombre del proveedor.
     */
    @Override
    public String toString() {
        return nombre;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Proveedor proveedor = (Proveedor) o;
        return java.util.Objects.equals(id, proveedor.id);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id);
    }
}