package com.samvitex.modelos.entidades;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa una categoría a la que puede pertenecer un producto.
 * Mapea a la tabla 'categorias' en la base de datos.
 * Sirve para organizar y clasificar el inventario de productos.
 * Por decisión de diseño, esta entidad no utiliza borrado lógico ('activo'),
 * ya que se considera una entidad de catálogo muy estable. La lógica de negocio
 * en la capa de servicio previene su eliminación si tiene productos asociados.
 */
@Entity
@Table(name = "categorias")
public class Categoria {

    /**
     * Identificador único de la categoría, generado automáticamente.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * Nombre de la categoría. Debe ser único para evitar duplicados.
     */
    @Column(unique = true, nullable = false, length = 100)
    private String nombre;

    /**
     * Descripción opcional y detallada de la categoría.
     */
    @Column(columnDefinition = "TEXT")
    private String descripcion;

    /**
     * Lista de productos que pertenecen a esta categoría.
     * La relación es gestionada por el campo 'categoria' en la entidad {@link Producto}.
     * La carga es perezosa (LAZY) para optimizar el rendimiento.
     */
    @OneToMany(mappedBy = "categoria", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Producto> productos = new ArrayList<>();

    /**
     * Constructor por defecto requerido por JPA.
     */
    public Categoria() {}

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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public List<Producto> getProductos() {
        return productos;
    }

    public void setProductos(List<Producto> productos) {
        this.productos = productos;
    }

    /**
     * Representación en String del objeto, principalmente para su uso en
     * componentes de UI como JComboBox.
     *
     * @return El nombre de la categoría.
     */
    @Override
    public String toString() {
        return nombre;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Categoria categoria = (Categoria) o;
        return java.util.Objects.equals(id, categoria.id);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id);
    }
}