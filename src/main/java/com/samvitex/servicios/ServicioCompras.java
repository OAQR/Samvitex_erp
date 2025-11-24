package com.samvitex.servicios;

import com.samvitex.modelos.dto.CompraItemDTO;
import com.samvitex.modelos.entidades.*;
import com.samvitex.modelos.enums.EstadoTransaccion;
import com.samvitex.modelos.enums.TipoMovimiento;
import com.samvitex.modelos.excepciones.InventarioException;
import com.samvitex.repositorios.CompraRepositorio;
import com.samvitex.repositorios.InventarioPorAlmacenRepositorio;
import com.samvitex.repositorios.ProductoRepositorio;
import com.samvitex.repositorios.UsuarioRepositorio;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;

/**
 * Servicio de negocio para gestionar las operaciones de Compra de mercancía.
 * Este servicio encapsula la lógica transaccional para registrar una compra a un proveedor,
 * actualizar el stock de los productos correspondientes y generar los registros de auditoría
 * (movimientos de inventario) necesarios.
 */
@Service
public class ServicioCompras {

    private final CompraRepositorio compraRepositorio;
    private final ProductoRepositorio productoRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;
    private final InventarioPorAlmacenRepositorio inventarioPorAlmacenRepositorio;


    public ServicioCompras(CompraRepositorio compraRepositorio,
                           ProductoRepositorio productoRepositorio,
                           UsuarioRepositorio usuarioRepositorio,
                           InventarioPorAlmacenRepositorio inventarioPorAlmacenRepositorio) {
        this.compraRepositorio = compraRepositorio;
        this.productoRepositorio = productoRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
        this.inventarioPorAlmacenRepositorio = inventarioPorAlmacenRepositorio;
    }

    /**
     * Procesa y guarda una nueva compra de forma transaccional.
     * La operación está protegida y requiere que el usuario tenga el rol 'ALMACENISTA' o 'ADMINISTRADOR'.
     *
     * <p>El flujo de trabajo es el siguiente:</p>
     * <ol>
     *     <li>Obtiene la entidad del usuario que registra la compra desde el contexto de seguridad.</li>
     *     <li>Crea una nueva entidad {@link Compra} en memoria.</li>
     *     <li>Para cada ítem en la orden de compra:
     *         <ul>
     *             <li>Incrementa la cantidad de stock del producto.</li>
     *             <li>Opcionalmente, actualiza el precio de costo del producto con el de esta compra.</li>
     *             <li>Crea un registro de detalle de compra ({@link CompraDetalle}).</li>
     *             <li>Crea un registro de auditoría ({@link MovimientoInventario}) y lo asocia a la compra.</li>
     *         </ul>
     *     </li>
     *     <li>Calcula el total de la compra y la persiste. Gracias a la cascada de JPA, todos los
     *         detalles y movimientos asociados se guardan en la misma transacción.</li>
     * </ol>
     *
     * @param proveedor El proveedor al que se le realiza la compra.
     * @param nombreUsuarioRegistra El nombre del usuario autenticado que está registrando la compra.
     * @param items La lista de DTOs con los productos, cantidades y costos de la compra.
     * @param referenciaFactura El número de factura o documento de referencia del proveedor.
     * @return La entidad {@link Compra} guardada y persistida.
     * @throws InventarioException si un producto de la compra no se encuentra en la base de datos.
     */
    @Transactional
    @PreAuthorize("hasAnyRole('ALMACENISTA', 'ADMINISTRADOR')")
    public Compra crearCompra(Proveedor proveedor, Almacen almacenDestino, String nombreUsuarioRegistra, List<CompraItemDTO> items, String referenciaFactura) {

        Usuario usuario = usuarioRepositorio.findByNombreUsuario(nombreUsuarioRegistra)
                .orElseThrow(() -> new RuntimeException("El usuario '" + nombreUsuarioRegistra + "' no fue encontrado."));

        Compra compra = new Compra();
        compra.setProveedor(proveedor);
        compra.setUsuario(usuario);
        compra.setAlmacenDestino(almacenDestino);
        compra.setReferenciaFactura(referenciaFactura);
        compra.setEstado(EstadoTransaccion.COMPLETADA);

        BigDecimal totalCompra = BigDecimal.ZERO;

        for (CompraItemDTO item : items) {
            Producto producto = productoRepositorio.findById(item.productoId())
                    .orElseThrow(() -> new InventarioException("El producto con ID " + item.productoId() + " no fue encontrado."));

            InventarioPorAlmacen inventario = inventarioPorAlmacenRepositorio
                    .findByProductoIdAndAlmacenId(producto.getId(), almacenDestino.getId())
                    .orElseGet(() -> {
                        // Si no existe, crea un nuevo registro de inventario para este producto/almacén
                        InventarioPorAlmacen nuevoInventario = new InventarioPorAlmacen();
                        nuevoInventario.setProducto(producto);
                        nuevoInventario.setAlmacen(almacenDestino);
                        nuevoInventario.setCantidad(0);
                        return nuevoInventario;
                    });

            int stockAnterior = inventario.getCantidad();
            int stockNuevo = stockAnterior + item.cantidad();
            inventario.setCantidad(stockNuevo);
            inventario.setFechaModificacion(Instant.now());
            inventario.setUsuarioModificacion(usuario);
            inventarioPorAlmacenRepositorio.save(inventario);

            producto.setPrecioCosto(item.costoUnitario()); // Actualizar costo en el producto maestro

            CompraDetalle detalle = new CompraDetalle();
            detalle.setProducto(producto);
            detalle.setCantidad(item.cantidad());
            detalle.setCostoUnitarioCompra(item.costoUnitario());
            BigDecimal subtotalLinea = item.costoUnitario().multiply(new BigDecimal(item.cantidad())).setScale(2, RoundingMode.HALF_UP);
            detalle.setSubtotalLinea(subtotalLinea);
            compra.addDetalle(detalle);

            totalCompra = totalCompra.add(subtotalLinea);

            MovimientoInventario movimiento = new MovimientoInventario();
            movimiento.setProducto(producto);
            movimiento.setAlmacen(almacenDestino);
            movimiento.setUsuario(usuario);
            movimiento.setTipo(TipoMovimiento.ENTRADA_COMPRA);
            movimiento.setCantidadMovida(item.cantidad());
            movimiento.setStockAnterior(stockAnterior);
            movimiento.setStockNuevo(stockNuevo);
            compra.addMovimiento(movimiento);
        }

        compra.setTotal(totalCompra);
        return compraRepositorio.save(compra);
    }
}