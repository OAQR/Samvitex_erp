package com.samvitex.servicios;

import com.samvitex.modelos.entidades.Proveedor;
import com.samvitex.repositorios.ProveedorRepositorio;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio de negocio para la gestión de Proveedores.
 * Encapsula la lógica para crear, leer, actualizar y desactivar proveedores,
 * aplicando las reglas de negocio y la seguridad correspondientes.
 */
@Service
public class ServicioProveedor {

    private final ProveedorRepositorio proveedorRepositorio;

    public ServicioProveedor(ProveedorRepositorio proveedorRepositorio) {
        this.proveedorRepositorio = proveedorRepositorio;
    }

    /**
     * Obtiene todos los proveedores del sistema.
     *
     * @return Una lista de todas las entidades {@link Proveedor}.
     */
    @Transactional(readOnly = true)
    public List<Proveedor> obtenerTodos() {
        return proveedorRepositorio.findAll();
    }

    /**
     * Obtiene únicamente los proveedores que están marcados como activos.
     * Es el método preferido para poblar listas de selección en la UI.
     *
     * @return Una lista de proveedores activos.
     */
    @Transactional(readOnly = true)
    public List<Proveedor> obtenerTodosActivos() {
        return proveedorRepositorio.findByActivoTrueOrderByIdAsc();
    }

    /**
     * Busca un proveedor por su ID.
     *
     * @param id El ID del proveedor a buscar.
     * @return Un {@link Optional} que contiene al proveedor si se encuentra.
     */
    @Transactional(readOnly = true)
    public Optional<Proveedor> findById(Integer id) {
        return proveedorRepositorio.findById(id);
    }

    /**
     * Guarda un proveedor (crea uno nuevo o actualiza uno existente).
     * Requiere que el usuario tenga el rol 'ADMINISTRADOR'.
     *
     * @param proveedor El proveedor a guardar.
     * @return El proveedor guardado con su ID actualizado.
     */
    @Transactional
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public Proveedor guardar(Proveedor proveedor) {
        // Aquí se podrían añadir validaciones de negocio, como verificar la unicidad del RUC.
        return proveedorRepositorio.save(proveedor);
    }

    /**
     * Desactiva un proveedor (borrado lógico). No lo elimina de la base de datos.
     * Requiere que el usuario tenga el rol 'ADMINISTRADOR'.
     *
     * @param id El ID del proveedor a desactivar.
     */
    @Transactional
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public void desactivar(Integer id) {
        Proveedor proveedor = proveedorRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado con ID: " + id));
        proveedor.setActivo(false);
        proveedorRepositorio.save(proveedor);
    }
}