package com.samvitex.servicios;

import com.samvitex.modelos.entidades.Almacen;
import com.samvitex.repositorios.AlmacenRepositorio;
import com.samvitex.repositorios.InventarioPorAlmacenRepositorio;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio de negocio para la gestión de Almacenes.
 * Centraliza la lógica para el CRUD y las operaciones relacionadas con las
 * ubicaciones de almacenamiento de productos, adaptado al modelo multi-almacén.
 */
@Service
public class ServicioAlmacen {

    private final AlmacenRepositorio almacenRepositorio;
    /**
     * CAMBIO: Se inyecta el repositorio de inventario para poder realizar validaciones de stock.
     */
    private final InventarioPorAlmacenRepositorio inventarioPorAlmacenRepositorio;

    public ServicioAlmacen(AlmacenRepositorio almacenRepositorio,
                           InventarioPorAlmacenRepositorio inventarioPorAlmacenRepositorio) {
        this.almacenRepositorio = almacenRepositorio;
        this.inventarioPorAlmacenRepositorio = inventarioPorAlmacenRepositorio;
    }

    /**
     * Obtiene todos los almacenes registrados.
     *
     * @return Una lista de todas las entidades {@link Almacen}.
     */
    @Transactional(readOnly = true)
    public List<Almacen> obtenerTodos() {
        return almacenRepositorio.findAll();
    }

    /**
     * Obtiene todos los almacenes que están marcados como activos.
     *
     * @return Una lista de almacenes activos.
     */
    @Transactional(readOnly = true)
    public List<Almacen> obtenerTodosActivos() {
        return almacenRepositorio.findByActivoTrueOrderByIdAsc();
    }

    /**
     * Busca un almacén específico por su ID.
     *
     * @param id El ID del almacén.
     * @return Un {@link Optional} que contendrá el almacén si existe.
     */
    @Transactional(readOnly = true)
    public Optional<Almacen> findById(Integer id) {
        return almacenRepositorio.findById(id);
    }

    /**
     * Guarda un almacén (crea o actualiza).
     * Requiere rol de 'ADMINISTRADOR'.
     *
     * @param almacen El almacén a guardar.
     * @return El almacén guardado.
     */
    @Transactional
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public Almacen guardar(Almacen almacen) {
        return almacenRepositorio.save(almacen);
    }

    /**
     * Desactiva un almacén (borrado lógico).
     * CAMBIO: La regla de negocio ahora consulta la tabla `inventario_por_almacen`
     * para verificar si el almacén tiene stock antes de desactivarlo.
     * Requiere rol de 'ADMINISTRADOR'.
     *
     * @param id El ID del almacén a desactivar.
     * @throws IllegalStateException si el almacén todavía contiene stock.
     */
    @Transactional
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public void desactivar(Integer id) {
        Almacen almacen = almacenRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Almacén no encontrado con ID: " + id));

        boolean tieneStock = inventarioPorAlmacenRepositorio.existsByAlmacenIdAndCantidadGreaterThan(id, 0);

        if (tieneStock) {
            throw new IllegalStateException("No se puede desactivar el almacén '" + almacen.getNombre() + "' porque todavía contiene stock.");
        }

        almacen.setActivo(false);
        almacenRepositorio.save(almacen);
    }
}