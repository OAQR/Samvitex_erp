package com.samvitex.repositorios;

import com.samvitex.modelos.entidades.Taller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de Spring Data JPA para la entidad {@link Taller}.
 * Proporciona los métodos CRUD básicos y permite la definición de
 * consultas personalizadas para la gestión de talleres.
 */
@Repository
public interface TallerRepositorio extends JpaRepository<Taller, Integer> {

    /**
     * Busca todos los talleres que se encuentran activos.
     * Es útil para poblar listas de selección en la UI donde solo deben
     * aparecer los talleres operativos.
     *
     * @return Una lista de talleres activos, ordenados por nombre.
     */
    List<Taller> findByActivoTrueOrderByNombreAsc();
}