package com.samvitex.servicios;

import com.samvitex.modelos.entidades.Cliente;
import com.samvitex.modelos.entidades.Producto;
import com.samvitex.repositorios.ClienteRepositorio;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio de negocio para la gestión de Clientes.
 * Proporciona métodos para las operaciones CRUD y la lógica de negocio
 * relacionada con los clientes de la empresa.
 */
@Service
public class ServicioCliente {

    private final ClienteRepositorio clienteRepositorio;

    public ServicioCliente(ClienteRepositorio clienteRepositorio) {
        this.clienteRepositorio = clienteRepositorio;
    }

    /**
     * Obtiene una lista de todos los clientes registrados en el sistema.
     *
     * @return Lista de entidades {@link Cliente}.
     */
    @Transactional(readOnly = true)
    public List<Cliente> obtenerTodos() {
        return clienteRepositorio.findAll();
    }

    /**
     * Obtiene una lista de todos los clientes marcados como activos.
     * Ideal para poblar las vistas donde se seleccionan clientes para nuevas transacciones.
     *
     * @return Lista de clientes activos.
     */
    @Transactional(readOnly = true)
    public List<Cliente> obtenerTodosActivos() {
        return clienteRepositorio.findByActivoTrueOrderByIdAsc();
    }

    /**
     * Busca un cliente específico por su ID.
     *
     * @param id El ID del cliente.
     * @return Un {@link Optional} que puede contener al cliente si fue encontrado.
     */
    @Transactional(readOnly = true)
    public Optional<Cliente> findById(Integer id) {
        return clienteRepositorio.findById(id);
    }

    /**
     * Busca clientes por un término de búsqueda en múltiples campos.
     *
     * @param termino El texto a buscar.
     * @return Una lista de clientes coincidentes.
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VENDEDOR')") // Aseguramos que solo roles autorizados puedan buscar
    public List<Cliente> buscarClientes(String termino) {
        return clienteRepositorio.buscarPorTermino(termino);
    }

    /**
     * Guarda los datos de un cliente (creación o actualización).
     * Esta operación requiere permisos de 'ADMINISTRADOR'.
     *
     * @param cliente El objeto cliente a persistir.
     * @return El cliente persistido.
     */
    @Transactional
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public Cliente guardar(Cliente cliente) {
        return clienteRepositorio.save(cliente);
    }

    /**
     * Desactiva un cliente mediante un borrado lógico.
     * Esta operación requiere permisos de 'ADMINISTRADOR'.
     *
     * @param id El ID del cliente a desactivar.
     */
    @Transactional
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public void desactivar(Integer id) {
        Cliente cliente = clienteRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + id));
        cliente.setActivo(false);
        clienteRepositorio.save(cliente);
    }
}