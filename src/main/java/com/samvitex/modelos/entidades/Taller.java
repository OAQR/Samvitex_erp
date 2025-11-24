package com.samvitex.modelos.entidades;

import com.samvitex.modelos.enums.TipoTaller;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Objects;

/**
 * Representa una unidad de producción (taller), que puede ser interna o externa a la empresa.
 *
 * <p>Esta entidad es una tabla de catálogo utilizada para asignar órdenes de producción
 * a una ubicación o proveedor de servicios de fabricación específico. Incluye información
 * de contacto y un indicador de estado para gestionar el borrado lógico.</p>
 *
 * Mapea a la tabla 'talleres' en la base de datos.
 */
@Entity
@Table(name = "talleres")
public class Taller {

    /**
     * Identificador único del taller.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * Nombre descriptivo o comercial del taller. Debe ser único.
     */
    @Column(unique = true, nullable = false, length = 200)
    private String nombre;

    /**
     * Dirección física del taller.
     */
    @Column(columnDefinition = "TEXT")
    private String direccion;

    /**
     * Nombre de la persona de contacto principal en el taller.
     */
    @Column(name = "persona_contacto", length = 200)
    private String personaContacto;

    /**
     * Clasificación del taller. Generalmente "INTERNO" para talleres propios
     * o "EXTERNO" para proveedores de servicios de confección.
     */
    @Column(nullable = false)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private TipoTaller tipo;

    /**
     * Indicador de borrado lógico. Un taller inactivo no podrá ser asignado
     * a nuevas órdenes de producción.
     */
    @Column(nullable = false)
    private boolean activo = true;

    /**
     * Constructor por defecto requerido por JPA.
     */
    public Taller() {}

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

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getPersonaContacto() {
        return personaContacto;
    }

    public void setPersonaContacto(String personaContacto) {
        this.personaContacto = personaContacto;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public TipoTaller getTipo() {
        return tipo;
    }

    public void setTipo(TipoTaller tipo) {
        this.tipo = tipo;
    }

    /**
     * Devuelve una representación textual del objeto, utilizada por defecto en componentes de UI.
     * @return El nombre del taller.
     */
    @Override
    public String toString() {
        return nombre;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Taller taller = (Taller) o;
        return id != null && Objects.equals(id, taller.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}