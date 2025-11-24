package com.samvitex.repositorios;

import com.samvitex.modelos.entidades.OrdenProduccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de Spring Data JPA para la entidad {@link OrdenProduccion}.
 * Proporciona la funcionalidad CRUD para gestionar las órdenes de producción.
 */
@Repository
public interface OrdenProduccionRepositorio extends JpaRepository<OrdenProduccion, Long> {

    /**
     * Verifica si ya existe una orden de producción con un código específico.
     * @param codigo El código a verificar.
     * @return {@code true} si el código ya existe, {@code false} de lo contrario.
     */
    boolean existsByCodigo(String codigo);

    /**
     * Obtiene todas las órdenes de producción, precargando las relaciones Taller y Usuario.
     *
     * @return Una lista de todas las órdenes de producción.
     */
    @Query("SELECT op FROM OrdenProduccion op JOIN FETCH op.taller JOIN FETCH op.usuarioResponsable JOIN FETCH op.almacenInsumos JOIN FETCH op.almacenDestino ORDER BY op.fechaCreacion DESC")
    List<OrdenProduccion> findAllWithDetails();
}