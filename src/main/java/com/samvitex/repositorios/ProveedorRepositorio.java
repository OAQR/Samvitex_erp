package com.samvitex.repositorios;

import com.samvitex.modelos.entidades.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de Spring Data JPA para la entidad {@link Proveedor}.
 * Proporciona métodos CRUD y consultas personalizadas para la gestión de proveedores.
 */
@Repository
public interface ProveedorRepositorio extends JpaRepository<Proveedor, Integer> {

    /**
     * Busca todos los proveedores que se encuentran activos.
     * Este metodo es útil para poblar listas de selección donde solo deben
     * aparecer los proveedores con los que se puede operar actualmente.
     *
     * @return Una lista de proveedores activos, ordenados por su ID.
     */
    List<Proveedor> findByActivoTrueOrderByIdAsc();

    /**
     * Busca proveedores cuyo nombre contenga el texto proporcionado, sin distinguir
     * entre mayúsculas y minúsculas.
     *
     * @param nombre El texto a buscar dentro del nombre de los proveedores.
     * @return Una lista de proveedores que coinciden con el criterio de búsqueda.
     */
    List<Proveedor> findByNombreContainingIgnoreCase(String nombre);
}