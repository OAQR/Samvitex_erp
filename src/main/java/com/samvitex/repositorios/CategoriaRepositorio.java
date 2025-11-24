package com.samvitex.repositorios;

import com.samvitex.modelos.entidades.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio de Spring Data JPA para la entidad {@link Categoria}.
 * Proporciona métodos CRUD básicos (Crear, Leer, Actualizar, Borrar)
 * para la gestión de las categorías de productos.
 */
@Repository
public interface CategoriaRepositorio extends JpaRepository<Categoria, Integer> {
    // Spring Data JPA proporciona automáticamente métodos como findAll(), findById(), save(), deleteById(), etc.
    // No se necesitan métodos personalizados en este momento.
}