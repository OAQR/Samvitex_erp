package com.samvitex.repositorios;

import com.samvitex.modelos.entidades.Almacen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de Spring Data JPA para la entidad {@link Almacen}.
 * Proporciona métodos CRUD y consultas personalizadas para la gestión de almacenes.
 */
@Repository
public interface AlmacenRepositorio extends JpaRepository<Almacen, Integer> {

    /**
     * Busca todos los almacenes que se encuentran activos u operativos.
     * Este metodo es esencial para las operaciones de inventario, como transferencias
     * o asignación de productos, para asegurar que se utilicen ubicaciones válidas.
     *
     * @return Una lista de almacenes activos, ordenados por su ID.
     */
    List<Almacen> findByActivoTrueOrderByIdAsc();
}