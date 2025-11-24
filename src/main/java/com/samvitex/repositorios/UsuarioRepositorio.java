package com.samvitex.repositorios;

import com.samvitex.modelos.entidades.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de Spring Data JPA para la entidad {@link Usuario}.
 * Esta interfaz proporciona una abstracción sobre el acceso a datos para los usuarios,
 * permitiendo realizar operaciones CRUD (Crear, Leer, Actualizar, Borrar) y consultas
 * personalizadas sin necesidad de escribir código de implementación explícito.
 */
@Repository
public interface UsuarioRepositorio extends JpaRepository<Usuario, Integer> {

    /**
     * Busca un usuario por su nombre de usuario.
     * Este metodo es fundamental para el proceso de autenticación. Spring Data JPA
     * generará automáticamente la consulta SQL correspondiente basada en el nombre del método.
     *
     * @param nombreUsuario El nombre de usuario a buscar, que es único en el sistema.
     * @return Un {@link Optional} que contendrá la entidad {@link Usuario} si se encuentra,
     *         o estará vacío si no existe un usuario con ese nombre.
     */
    Optional<Usuario> findByNombreUsuario(String nombreUsuario);

    /**
     * Verifica si ya existe un usuario con el nombre de usuario proporcionado.
     * Es un metodo optimizado que generalmente se traduce en una consulta
     * {@code SELECT COUNT(*)} o similar, siendo más eficiente que obtener la entidad completa.
     *
     * @param nombreUsuario El nombre de usuario a verificar.
     * @return {@code true} si un usuario con ese nombre ya existe, {@code false} en caso contrario.
     */
    boolean existsByNombreUsuario(String nombreUsuario);

    /**
     * Verifica si ya existe un usuario con la dirección de correo electrónico proporcionada.
     * Útil para garantizar la unicidad del email durante el registro o la actualización de usuarios.
     *
     * @param email La dirección de correo electrónico a verificar.
     * @return {@code true} si un usuario con ese email ya existe, {@code false} en caso contrario.
     */
    boolean existsByEmail(String email);

    /**
     * Sobrescribe el metodo `findAll` por defecto para solucionar el problema de carga perezosa (LazyInitializationException).
     * La cláusula `JOIN FETCH u.rol` le indica a Hibernate que debe cargar la entidad `Rol` asociada
     * en la misma consulta SQL que carga los usuarios. Esto previene el error "could not initialize proxy - no Session"
     * cuando se intenta acceder a la información del rol fuera de una sesión transaccional.
     *
     * @return Una lista de todas las entidades {@link Usuario}, con sus roles ya inicializados.
     */
    @Override
    @Query("SELECT u FROM Usuario u JOIN FETCH u.rol")
    List<Usuario> findAll();

    /**
     * Busca un usuario por su ID y carga proactivamente su relación 'rol'
     * en la misma consulta para evitar LazyInitializationException.
     *
     * @param id El ID del usuario a buscar.
     * @return Un Optional que contiene el Usuario con su Rol ya inicializado.
     */
    @Query("SELECT u FROM Usuario u JOIN FETCH u.rol WHERE u.id = :id")
    Optional<Usuario> findByIdWithRol(@Param("id") Integer id);
}