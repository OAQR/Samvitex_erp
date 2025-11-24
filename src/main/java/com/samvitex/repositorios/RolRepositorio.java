package com.samvitex.repositorios;

import com.samvitex.modelos.entidades.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio de Spring Data JPA para la entidad {@link Rol}.
 * Proporciona los métodos CRUD básicos para interactuar con la tabla de roles.
 */
@Repository
public interface RolRepositorio extends JpaRepository<Rol, Integer> {

    /**
     * Busca un rol específico por su nombre.
     * Este metodo es útil para asignar roles a los usuarios de forma programática
     * sin necesidad de conocer su ID.
     *
     * @param nombre El nombre del rol a buscar (ej. "ADMINISTRADOR").
     * @return Un {@link Optional} que contendrá la entidad {@link Rol} si se encuentra.
     */
    Optional<Rol> findByNombre(String nombre);
}