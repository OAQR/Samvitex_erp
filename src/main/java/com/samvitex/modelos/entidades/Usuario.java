package com.samvitex.modelos.entidades;

import com.samvitex.servicios.ServicioUsuario;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;
import java.util.Objects;

/**
 * Representa a un usuario del sistema, con sus credenciales, información personal y rol.
 * <p>
 * Esta entidad es fundamental para la seguridad y auditoría de la aplicación. Mapea a la
 * tabla {@code usuarios} y almacena la información necesaria para los procesos de
 * autenticación (inicio de sesión) y autorización (permisos).
 *
 * @see Rol
 * @see ServicioUsuario
 */
@Entity
@Table(name = "usuarios")
public class Usuario {

    /**
     * Identificador único del usuario, generado automáticamente por la base de datos.
     * Es la clave primaria de la tabla.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * Nombre de usuario único utilizado para el inicio de sesión.
     * Este campo tiene una restricción de unicidad en la base de datos para prevenir duplicados.
     */
    @Column(name = "nombre_usuario", unique = true, nullable = false, length = 100)
    private String nombreUsuario;

    /**
     * Hash de la contraseña del usuario.
     * <b>Nunca</b> se almacena la contraseña en texto plano. Este campo guarda el resultado
     * de aplicar un algoritmo de hashing fuerte (ej. BCrypt) a la contraseña del usuario.
     */
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    /**
     * Nombre completo del usuario, utilizado para fines de visualización en la interfaz
     * y en reportes.
     */
    @Column(name = "nombre_completo", nullable = false)
    private String nombreCompleto;

    /**
     * Dirección de correo electrónico del usuario. Debe ser única en el sistema.
     */
    @Column(unique = true, nullable = false)
    private String email;

    /**
     * Indicador de estado para borrado lógico (soft delete).
     * Un usuario con {@code activo = false} no podrá iniciar sesión en el sistema,
     * pero su registro se mantiene para preservar la integridad de las transacciones históricas.
     */
    @Column(nullable = false)
    private boolean activo = true;

    /**
     * El rol asignado al usuario, que define su nivel de permisos en el sistema.
     * <p>
     * La relación es {@code ManyToOne} porque muchos usuarios pueden tener el mismo rol.
     * Se carga de forma perezosa (LAZY) para optimizar el rendimiento, cargando el rol
     * solo cuando es explícitamente necesario.
     * Es una relación obligatoria ({@code optional = false}).
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rol_id", nullable = false)
    private Rol rol;

    /**
     * Marca de tiempo que registra el momento exacto en que el registro del usuario fue creado.
     * Este valor es asignado automáticamente por Hibernate al momento de la inserción
     * y no puede ser modificado posteriormente ({@code updatable = false}).
     */
    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private Instant fechaCreacion;

    /**
     * Constructor por defecto requerido por el framework JPA.
     */
    public Usuario() {
    }

    // --- Getters y Setters ---

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public Instant getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Instant fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    /**
     * Compara este objeto Usuario con otro para determinar si son iguales.
     * Dos usuarios se consideran iguales si tienen el mismo ID y este no es nulo.
     * Esto es fundamental para el correcto funcionamiento de colecciones y componentes de UI.
     *
     * @param o el objeto a comparar.
     * @return {@code true} si los objetos son el mismo usuario, {@code false} de lo contrario.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return id != null && Objects.equals(id, usuario.id);
    }

    /**
     * Devuelve un código hash para el objeto, basado en su ID.
     * Es consistente con el método {@code equals}.
     *
     * @return el código hash del usuario.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Proporciona una representación en formato de cadena del objeto Usuario,
     * útil para propósitos de depuración y registro (logging).
     * No incluye información sensible como el hash de la contraseña.
     *
     * @return Una cadena que representa el estado del objeto Usuario.
     */
    @Override
    public String toString() {
        return "Usuario{" +
                "id=" + id +
                ", nombreUsuario='" + nombreUsuario + '\'' +
                ", nombreCompleto='" + nombreCompleto + '\'' +
                ", email='" + email + '\'' +
                ", activo=" + activo +
                ", rol=" + (rol != null ? rol.getNombre() : "null") +
                '}';
    }
}