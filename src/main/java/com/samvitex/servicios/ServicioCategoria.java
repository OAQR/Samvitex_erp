package com.samvitex.servicios;

import com.samvitex.modelos.entidades.Categoria;
import com.samvitex.repositorios.CategoriaRepositorio;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio de negocio para la gestión de Categorías de productos.
 */
@Service
public class ServicioCategoria {

    private final CategoriaRepositorio categoriaRepositorio;

    public ServicioCategoria(CategoriaRepositorio categoriaRepositorio) {
        this.categoriaRepositorio = categoriaRepositorio;
    }

    /**
     * Obtiene todas las categorías.
     * @return Una lista de todas las categorías.
     */
    @Transactional(readOnly = true)
    public List<Categoria> obtenerTodas() {
        return categoriaRepositorio.findAll();
    }

    /**
     * Busca una categoría específica por su ID.
     *
     * @param id El ID de la categoría.
     * @return Un {@link Optional} que puede contener la categoría si fue encontrada.
     */
    @Transactional(readOnly = true)
    public Optional<Categoria> findById(Integer id) {
        return categoriaRepositorio.findById(id);
    }

    /**
     * Guarda una categoría (la crea si no tiene ID, o la actualiza si lo tiene).
     * Requiere rol de ADMINISTRADOR.
     * @param categoria La categoría a guardar.
     * @return La categoría guardada.
     */
    @Transactional
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public Categoria guardar(Categoria categoria) {
        return categoriaRepositorio.save(categoria);
    }

    /**
     * Elimina una categoría por su ID.
     * Regla de negocio: No permite eliminar si la categoría tiene productos asociados.
     * Requiere rol de ADMINISTRADOR.
     * @param id El ID de la categoría a eliminar.
     */
    @Transactional
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public void eliminar(Integer id) {
        Categoria categoria = categoriaRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + id));
        if (!categoria.getProductos().isEmpty()) {
            throw new IllegalStateException("No se puede eliminar la categoría '" + categoria.getNombre() + "' porque tiene productos asociados.");
        }
        categoriaRepositorio.deleteById(id);
    }
}