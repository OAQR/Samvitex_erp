package com.samvitex.repositorios;

import com.samvitex.modelos.entidades.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de Spring Data JPA para la entidad {@link Cliente}.
 * Proporciona métodos CRUD y consultas personalizadas para la gestión de clientes.
 */
@Repository
public interface ClienteRepositorio extends JpaRepository<Cliente, Integer> {

    /**
     * Busca clientes cuyo nombre completo contenga el texto proporcionado, sin
     * distinguir entre mayúsculas y minúsculas.
     *
     * @param nombre El texto a buscar dentro del nombre completo de los clientes.
     * @return Una lista de clientes que coinciden con el criterio de búsqueda.
     */
    List<Cliente> findByNombreCompletoContainingIgnoreCase(String nombre);

    /**
     * Busca todos los clientes que se encuentran activos en el sistema.
     * Útil para las vistas de ventas, para asegurar que solo se puedan realizar
     * transacciones con clientes habilitados.
     *
     * @return Una lista de clientes activos, ordenados por su ID.
     */
    List<Cliente> findByActivoTrueOrderByIdAsc();

    /**
     * Busca clientes por un término en múltiples campos: ID, Nombre, DNI/RUC, Email, Teléfono.
     * También permite buscar por estado ("activo" o "inactivo").
     * La búsqueda es insensible a mayúsculas/minúsculas y acentos.
     *
     * @param termino El texto a buscar.
     * @return Una lista de clientes que coinciden con el criterio.
     */
    @Query("""
        SELECT c FROM Cliente c
        WHERE CAST(c.id AS string) LIKE CONCAT('%', :termino, '%')
        OR unaccent(LOWER(c.nombreCompleto)) LIKE unaccent(LOWER(CONCAT('%', :termino, '%')))
        OR unaccent(LOWER(c.dniRuc)) LIKE unaccent(LOWER(CONCAT('%', :termino, '%')))
        OR unaccent(LOWER(c.email)) LIKE unaccent(LOWER(CONCAT('%', :termino, '%')))
        OR unaccent(LOWER(c.telefono)) LIKE unaccent(LOWER(CONCAT('%', :termino, '%')))
        OR (:termino ILIKE 'activo' AND c.activo = true)
        OR (:termino ILIKE 'inactivo' AND c.activo = false)
        ORDER BY c.nombreCompleto ASC
    """)
    List<Cliente> buscarPorTermino(@Param("termino") String termino);
}