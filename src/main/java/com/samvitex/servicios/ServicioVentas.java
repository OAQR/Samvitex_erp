package com.samvitex.servicios;

import com.samvitex.modelos.dto.VentaItemDTO;
import com.samvitex.modelos.entidades.*;
import com.samvitex.modelos.enums.EstadoTransaccion;
import com.samvitex.modelos.enums.TipoMovimiento;
import com.samvitex.modelos.excepciones.InventarioException;
import com.samvitex.repositorios.InventarioPorAlmacenRepositorio;
import com.samvitex.repositorios.UsuarioRepositorio;
import com.samvitex.repositorios.VentaRepositorio;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;

/**
 * Servicio de negocio para gestionar las operaciones de Venta.
 * <p>
 * Contiene la lógica transaccional para registrar una venta, garantizando la atomicidad
 * de la operación en un entorno multi-almacén: actualización de inventario específico
 * del almacén, creación de detalles de venta y registro de los movimientos de
 * auditoría correspondientes.
 */
@Service
public class ServicioVentas {

    private final VentaRepositorio ventaRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;
    /**
     * CAMBIO: Se inyecta el repositorio de inventario por almacén.
     * Este repositorio es ahora la única fuente de verdad para consultar y modificar el stock.
     */
    private final InventarioPorAlmacenRepositorio inventarioPorAlmacenRepositorio;

    public ServicioVentas(VentaRepositorio ventaRepositorio,
                          UsuarioRepositorio usuarioRepositorio,
                          InventarioPorAlmacenRepositorio inventarioPorAlmacenRepositorio) {
        this.ventaRepositorio = ventaRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
        this.inventarioPorAlmacenRepositorio = inventarioPorAlmacenRepositorio;
    }

    /**
     * Procesa y guarda una nueva venta de forma transaccional desde un almacén específico.
     * <p>
     * La lógica ha sido refactorizada para operar sobre la tabla `inventario_por_almacen`.
     * El flujo es el siguiente:
     * <ol>
     *     <li>Obtiene el usuario y crea la entidad {@link Venta} maestra, asignando el almacén de origen.</li>
     *     <li>Para cada ítem del pedido:
     *         <ul>
     *             <li>Busca el registro de stock para el producto Y el almacén especificados.</li>
     *             <li>Valida que haya stock suficiente en ESE almacén.</li>
     *             <li>Reduce la cantidad en el registro de `inventario_por_almacen`.</li>
     *             <li>Crea el detalle de la venta.</li>
     *             <li>Crea el movimiento de inventario (Kardex), registrando el almacén correcto.</li>
     *         </ul>
     *     </li>
     *     <li>Calcula totales y persiste la venta y todas sus entidades asociadas en cascada.</li>
     * </ol>
     *
     * @param cliente El cliente al que se le realiza la venta.
     * @param almacenOrigen El almacén desde el cual se están vendiendo los productos.
     * @param nombreUsuarioVendedor El nombre del usuario autenticado que realiza la venta.
     * @param items La lista de DTOs con los productos y cantidades a vender.
     * @return La entidad {@link Venta} guardada y persistida.
     * @throws InventarioException si no hay stock suficiente para algún producto en el almacén especificado.
     * @throws RuntimeException si el usuario no se encuentra.
     */
    @Transactional
    @PreAuthorize("hasAnyRole('VENDEDOR', 'ADMINISTRADOR')")
    public Venta crearVenta(Cliente cliente, Almacen almacenOrigen, String nombreUsuarioVendedor, List<VentaItemDTO> items) {
        Usuario usuario = usuarioRepositorio.findByNombreUsuario(nombreUsuarioVendedor)
                .orElseThrow(() -> new RuntimeException("Usuario '" + nombreUsuarioVendedor + "' no encontrado. Sesión inválida."));

        Venta venta = new Venta();
        venta.setCliente(cliente);
        venta.setUsuario(usuario);
        venta.setAlmacenOrigen(almacenOrigen);
        venta.setEstado(EstadoTransaccion.COMPLETADA);

        BigDecimal subtotalGeneral = BigDecimal.ZERO;

        for (VentaItemDTO item : items) {

            InventarioPorAlmacen inventario = inventarioPorAlmacenRepositorio
                    .findByProductoIdAndAlmacenId(item.productoId(), almacenOrigen.getId())
                    .orElseThrow(() -> new InventarioException(
                            String.format("El producto ID %d no existe o no tiene stock registrado en el almacén '%s'.",
                                    item.productoId(), almacenOrigen.getNombre())));

            if (inventario.getCantidad() < item.cantidad()) {
                throw new InventarioException(String.format("Stock insuficiente para '%s' en el almacén '%s'. Disponible: %d, Solicitado: %d",
                        inventario.getProducto().getNombre(), almacenOrigen.getNombre(), inventario.getCantidad(), item.cantidad()));
            }

            int stockAnterior = inventario.getCantidad();
            int stockNuevo = stockAnterior - item.cantidad();

            inventario.setCantidad(stockNuevo);
            inventario.setFechaModificacion(Instant.now());
            inventario.setUsuarioModificacion(usuario);


            Producto producto = inventario.getProducto();

            VentaDetalle detalle = new VentaDetalle();
            detalle.setProducto(producto);
            detalle.setCantidad(item.cantidad());
            detalle.setPrecioUnitario(producto.getPrecioVenta());
            BigDecimal subtotalLinea = producto.getPrecioVenta().multiply(new BigDecimal(item.cantidad()));
            detalle.setSubtotalLinea(subtotalLinea);
            venta.addDetalle(detalle);

            subtotalGeneral = subtotalGeneral.add(subtotalLinea);

            MovimientoInventario movimiento = new MovimientoInventario();
            movimiento.setProducto(producto);
            movimiento.setAlmacen(almacenOrigen);
            movimiento.setUsuario(usuario);
            movimiento.setTipo(TipoMovimiento.SALIDA_VENTA);
            movimiento.setCantidadMovida(-item.cantidad());
            movimiento.setStockAnterior(stockAnterior);
            movimiento.setStockNuevo(stockNuevo);
            venta.addMovimiento(movimiento);
        }

        // Cálculo de impuestos y total
        venta.setSubtotal(subtotalGeneral);
        BigDecimal impuestos = subtotalGeneral.multiply(new BigDecimal("0.18")).setScale(2, RoundingMode.HALF_UP);
        venta.setImpuestos(impuestos);
        venta.setTotal(subtotalGeneral.add(impuestos));

        return ventaRepositorio.save(venta);
    }
}