package com.samvitex.ui.presentadores;

import com.samvitex.modelos.dto.CompraItemDTO;
import com.samvitex.modelos.entidades.Almacen;
import com.samvitex.modelos.entidades.Producto;
import com.samvitex.modelos.entidades.Proveedor;
import com.samvitex.servicios.ServicioAlmacen;
import com.samvitex.servicios.ServicioCompras;
import com.samvitex.servicios.ServicioInventario;
import com.samvitex.servicios.ServicioProveedor;
import com.samvitex.ui.vistas.interfaces.ComprasView;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.swing.SwingWorker;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Presenter para la vista de Registro de Compras (Patrón MVP).
 *
 * <p>Esta clase encapsula la lógica de presentación para el registro de entrada de mercancía.
 * Orquesta la comunicación entre la {@link ComprasView} y los servicios de negocio
 * para cargar datos, buscar productos y persistir la nueva compra.
 * Todas las operaciones de larga duración se ejecutan en hilos de fondo.</p>
 */
public class ComprasPresenter {

    private final ComprasView view;
    private final ServicioCompras servicioCompras;
    private final ServicioInventario servicioInventario;
    private final ServicioProveedor servicioProveedor;
    private final ServicioAlmacen servicioAlmacen;

    public ComprasPresenter(ComprasView view, ServicioCompras sc, ServicioInventario si,
                            ServicioProveedor sp, ServicioAlmacen sa) {
        this.view = view;
        this.servicioCompras = sc;
        this.servicioInventario = si;
        this.servicioProveedor = sp;
        this.servicioAlmacen = sa;
    }

    public void cargarDatosIniciales() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        new SwingWorker<List<Proveedor>, Void>() {
            @Override
            protected List<Proveedor> doInBackground() throws Exception {
                SecurityContextHolder.getContext().setAuthentication(authentication);
                try {
                    return servicioProveedor.obtenerTodosActivos();
                } finally {
                    SecurityContextHolder.clearContext();
                }
            }
            @Override
            protected void done() {
                try {
                    view.mostrarProveedores(get());
                } catch (Exception e) {
                    handleError(e, "Error al cargar proveedores");
                }
            }
        }.execute();

        new SwingWorker<List<Almacen>, Void>() {
            @Override
            protected List<Almacen> doInBackground() throws Exception {
                SecurityContextHolder.getContext().setAuthentication(authentication);
                try {
                    return servicioAlmacen.obtenerTodosActivos();
                } finally {
                    SecurityContextHolder.clearContext();
                }
            }
            @Override
            protected void done() {
                try {
                    view.mostrarAlmacenes(get());
                } catch (Exception e) {
                    handleError(e, "Error al cargar almacenes");
                }
            }
        }.execute();
    }

    public void buscarProductos(String texto) {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        new SwingWorker<List<Producto>, Void>() {
            @Override
            protected List<Producto> doInBackground() throws Exception {
                SecurityContextHolder.getContext().setAuthentication(authentication);
                try {
                    return servicioInventario.buscarProductos(texto);
                } finally {
                    SecurityContextHolder.clearContext();
                }
            }
            @Override
            protected void done() {
                try {
                    view.mostrarResultadosBusqueda(get());
                } catch (Exception e) {
                    handleError(e, "Error al buscar productos");
                }
            }
        }.execute();
    }

    public void onTablaCompraChanged(BigDecimal total) {
        view.actualizarTotalCompra(total);
    }

    /**
     * Orquesta el proceso completo de registro de una nueva compra.
     * Recopila los datos de la vista, realiza validaciones y, si todo es correcto,
     * invoca al servicio de compras en un hilo de fondo.
     */
    public void registrarCompra() {
        // 1. Recopilar datos de la vista
        Proveedor proveedorSeleccionado = view.obtenerProveedorSeleccionado();
        Almacen almacenDestino = view.obtenerAlmacenDestinoSeleccionado();
        String referenciaFactura = view.obtenerReferenciaFactura();
        List<CompraItemDTO> items = view.obtenerItemsCompra();

        if (almacenDestino == null) {
            view.mostrarError("Debe seleccionar un almacén de destino para la compra.");
            return;
        }
        if (proveedorSeleccionado == null) {
            view.mostrarError("Debe seleccionar un proveedor para la compra.");
            return;
        }
        if (items.isEmpty()) {
            view.mostrarError("La lista de productos a comprar está vacía.");
            return;
        }
        if (referenciaFactura.isBlank()) {
            view.mostrarError("Debe ingresar una referencia (ej. número de factura).");
            return;
        }

        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            view.mostrarError("No se pudo identificar al usuario. Por favor, reinicie sesión.");
            return;
        }
        final String nombreUsuario = authentication.getName();

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                SecurityContextHolder.getContext().setAuthentication(authentication);
                try {
                    servicioCompras.crearCompra(proveedorSeleccionado, almacenDestino, nombreUsuario, items, referenciaFactura);
                    return null;
                } finally {
                    SecurityContextHolder.clearContext();
                }
            }
            @Override
            protected void done() {
                try {
                    get();
                    view.mostrarMensajeExito("Compra registrada exitosamente. El stock ha sido actualizado.");
                    view.limpiarVistaPostCompra();
                } catch (Exception e) {
                    handleError(e, "Error al registrar la compra");
                }
            }
        }.execute();
    }

    private void handleError(Exception e, String context) {
        String message = (e instanceof ExecutionException && e.getCause() != null) ? e.getCause().getMessage() : e.getMessage();
        view.mostrarError(String.format("%s: %s", context, message));
    }
}