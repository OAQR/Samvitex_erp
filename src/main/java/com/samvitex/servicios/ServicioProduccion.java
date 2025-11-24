package com.samvitex.servicios;

import com.samvitex.modelos.dto.OrdenProduccionDTO;
import com.samvitex.modelos.entidades.*;
import com.samvitex.modelos.enums.EstadoProduccion;
import com.samvitex.modelos.enums.TipoDetalleProduccion;
import com.samvitex.modelos.enums.TipoMovimiento;
import com.samvitex.modelos.excepciones.InventarioException;
import com.samvitex.modelos.excepciones.ProduccionException;
import com.samvitex.repositorios.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Servicio de negocio para gestionar el ciclo de vida de las Órdenes de Producción
 * en un entorno multi-almacén.
 */
@Service
public class ServicioProduccion {

    private final OrdenProduccionRepositorio ordenProduccionRepositorio;
    private final TallerRepositorio tallerRepositorio;
    private final ProductoRepositorio productoRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;
    private final AlmacenRepositorio almacenRepositorio;
    private final InventarioPorAlmacenRepositorio inventarioPorAlmacenRepositorio;

    public ServicioProduccion(OrdenProduccionRepositorio ordenProduccionRepositorio,
                              TallerRepositorio tallerRepositorio,
                              ProductoRepositorio productoRepositorio,
                              UsuarioRepositorio usuarioRepositorio,
                              AlmacenRepositorio almacenRepositorio,
                              InventarioPorAlmacenRepositorio inventarioPorAlmacenRepositorio) {
        this.ordenProduccionRepositorio = ordenProduccionRepositorio;
        this.tallerRepositorio = tallerRepositorio;
        this.productoRepositorio = productoRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
        this.almacenRepositorio = almacenRepositorio;
        this.inventarioPorAlmacenRepositorio = inventarioPorAlmacenRepositorio;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ALMACENISTA')")
    public List<OrdenProduccion> obtenerTodas() {
        return ordenProduccionRepositorio.findAllWithDetails();
    }

    /**
     * Crea una nueva orden de producción en estado 'PLANIFICADA'.
     * Esta operación no afecta el stock del inventario. Valida que todos los
     * componentes (taller, almacenes) existan antes de crear la orden.
     *
     * @param dto El DTO con la información de la nueva orden.
     * @return La entidad {@link OrdenProduccion} creada y persistida.
     */
    @Transactional
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ALMACENISTA')")
    public OrdenProduccion crearOrdenProduccion(OrdenProduccionDTO dto) {
        if (ordenProduccionRepositorio.existsByCodigo(dto.codigo())) {
            throw new ProduccionException("El código de orden '" + dto.codigo() + "' ya existe.");
        }
        Taller taller = tallerRepositorio.findById(dto.tallerId())
                .orElseThrow(() -> new ProduccionException("El taller seleccionado no existe."));
        Almacen almacenInsumos = almacenRepositorio.findById(dto.almacenInsumosId())
                .orElseThrow(() -> new ProduccionException("El almacén de insumos seleccionado no existe."));
        Almacen almacenDestino = almacenRepositorio.findById(dto.almacenDestinoId())
                .orElseThrow(() -> new ProduccionException("El almacén de destino seleccionado no existe."));
        Usuario usuario = getCurrentUser();

        OrdenProduccion orden = new OrdenProduccion();
        orden.setCodigo(dto.codigo());
        orden.setTaller(taller);
        orden.setUsuarioResponsable(usuario);
        orden.setEstado(EstadoProduccion.PLANIFICADA);
        orden.setAlmacenInsumos(almacenInsumos);
        orden.setAlmacenDestino(almacenDestino);

        for (OrdenProduccionDTO.DetalleDTO detalleDTO : dto.detalles()) {
            Producto producto = productoRepositorio.findById(detalleDTO.productoId())
                    .orElseThrow(() -> new ProduccionException("El producto con ID " + detalleDTO.productoId() + " no existe."));
            OrdenProduccionDetalle detalle = new OrdenProduccionDetalle();
            detalle.setProducto(producto);
            detalle.setTipoDetalle(detalleDTO.tipoDetalle());
            detalle.setCantidad(detalleDTO.cantidad());
            orden.addDetalle(detalle);
        }
        return ordenProduccionRepositorio.save(orden);
    }

    /**
     * Inicia la producción de una orden.
     * Cambia el estado a EN_PRODUCCION, descuenta los INSUMOS del almacén de origen
     * especificado en la orden y genera los movimientos de inventario correspondientes.
     *
     * @param ordenId El ID de la orden de producción a iniciar.
     * @return La orden de producción actualizada.
     */
    @Transactional
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ALMACENISTA')")
    public OrdenProduccion iniciarProduccion(Long ordenId) {
        OrdenProduccion orden = ordenProduccionRepositorio.findById(ordenId)
                .orElseThrow(() -> new ProduccionException("La orden de producción no existe."));

        if (orden.getEstado() != EstadoProduccion.PLANIFICADA) {
            throw new ProduccionException("Solo se puede iniciar la producción de una orden en estado 'Planificada'.");
        }

        Usuario usuario = getCurrentUser();
        Almacen almacenInsumos = orden.getAlmacenInsumos();

        for (OrdenProduccionDetalle detalle : orden.getDetalles()) {
            if (detalle.getTipoDetalle() == TipoDetalleProduccion.INSUMO) {
                Producto insumo = detalle.getProducto();
                int cantidadRequerida = detalle.getCantidad();

                InventarioPorAlmacen inventario = inventarioPorAlmacenRepositorio
                        .findByProductoIdAndAlmacenId(insumo.getId(), almacenInsumos.getId())
                        .orElseThrow(() -> new InventarioException(String.format("No hay registro de stock para el insumo '%s' en el almacén '%s'.",
                                insumo.getNombre(), almacenInsumos.getNombre())));

                if (inventario.getCantidad() < cantidadRequerida) {
                    throw new InventarioException(String.format("Stock insuficiente para '%s' en '%s'. Requerido: %d, Disponible: %d",
                            insumo.getNombre(), almacenInsumos.getNombre(), cantidadRequerida, inventario.getCantidad()));
                }

                int stockAnterior = inventario.getCantidad();
                int stockNuevo = stockAnterior - cantidadRequerida;
                inventario.setCantidad(stockNuevo);
                inventario.setFechaModificacion(Instant.now());
                inventario.setUsuarioModificacion(usuario);

                MovimientoInventario movimiento = new MovimientoInventario();
                movimiento.setProducto(insumo);
                movimiento.setAlmacen(almacenInsumos);
                movimiento.setUsuario(usuario);
                movimiento.setTipo(TipoMovimiento.SALIDA_A_PRODUCCION);
                movimiento.setCantidadMovida(-cantidadRequerida);
                movimiento.setStockAnterior(stockAnterior);
                movimiento.setStockNuevo(stockNuevo);
                orden.addMovimiento(movimiento);
            }
        }

        orden.setEstado(EstadoProduccion.EN_PRODUCCION);
        orden.setFechaInicioProduccion(Instant.now());
        return ordenProduccionRepositorio.save(orden);
    }

    /**
     * Finaliza la producción de una orden.
     * Cambia el estado a COMPLETADA, ingresa los PRODUCTOS FINALES al almacén de destino
     * especificado en la orden y genera los movimientos de inventario correspondientes.
     *
     * @param ordenId El ID de la orden de producción a finalizar.
     * @return La orden de producción actualizada.
     */
    @Transactional
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ALMACENISTA')")
    public OrdenProduccion finalizarProduccion(Long ordenId) {
        OrdenProduccion orden = ordenProduccionRepositorio.findById(ordenId)
                .orElseThrow(() -> new ProduccionException("La orden de producción no existe."));

        if (orden.getEstado() != EstadoProduccion.EN_PRODUCCION) {
            throw new ProduccionException("Solo se puede finalizar una orden en estado 'En Producción'.");
        }

        Usuario usuario = getCurrentUser();
        Almacen almacenDestino = orden.getAlmacenDestino();

        for (OrdenProduccionDetalle detalle : orden.getDetalles()) {
            if (detalle.getTipoDetalle() == TipoDetalleProduccion.PRODUCTO_FINAL) {
                Producto productoFinal = detalle.getProducto();
                int cantidadProducida = detalle.getCantidad();

                InventarioPorAlmacen inventario = inventarioPorAlmacenRepositorio
                        .findByProductoIdAndAlmacenId(productoFinal.getId(), almacenDestino.getId())
                        .orElseGet(() -> {
                            InventarioPorAlmacen nuevoInventario = new InventarioPorAlmacen();
                            nuevoInventario.setProducto(productoFinal);
                            nuevoInventario.setAlmacen(almacenDestino);
                            nuevoInventario.setCantidad(0);
                            return nuevoInventario;
                        });

                int stockAnterior = inventario.getCantidad();
                int stockNuevo = stockAnterior + cantidadProducida;
                inventario.setCantidad(stockNuevo);
                inventario.setFechaModificacion(Instant.now());
                inventario.setUsuarioModificacion(usuario);
                inventarioPorAlmacenRepositorio.save(inventario);

                MovimientoInventario movimiento = new MovimientoInventario();
                movimiento.setProducto(productoFinal);
                movimiento.setAlmacen(almacenDestino);
                movimiento.setUsuario(usuario);
                movimiento.setTipo(TipoMovimiento.ENTRADA_POR_PRODUCCION);
                movimiento.setCantidadMovida(cantidadProducida);
                movimiento.setStockAnterior(stockAnterior);
                movimiento.setStockNuevo(stockNuevo);
                orden.addMovimiento(movimiento);
            }
        }

        orden.setEstado(EstadoProduccion.COMPLETADA);
        orden.setFechaFinalizacion(Instant.now());
        return ordenProduccionRepositorio.save(orden);
    }

    private Usuario getCurrentUser() {
        String nombreUsuario = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepositorio.findByNombreUsuario(nombreUsuario)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado en el contexto de seguridad. La sesión puede ser inválida."));
    }
}