package com.samvitex.ui.presentadores;

import com.samvitex.modelos.dto.VentaItemDTO;
import com.samvitex.modelos.entidades.Almacen;
import com.samvitex.modelos.entidades.Cliente;
import com.samvitex.modelos.entidades.Producto;
import com.samvitex.servicios.ServicioAlmacen;
import com.samvitex.servicios.ServicioCliente;
import com.samvitex.servicios.ServicioInventario;
import com.samvitex.servicios.ServicioVentas;
import com.samvitex.servicios.ServicioImpresion;
import com.samvitex.ui.dialogos.DialogoVistaPrevia;
import com.samvitex.modelos.entidades.Venta;
import com.samvitex.ui.dialogos.DialogoCheckout;
import com.samvitex.ui.vistas.interfaces.VentasView;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.swing.*;
import java.awt.Frame;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Presenter para la vista del Punto de Venta (POS), implementando la lógica de presentación del patrón MVP.
 * Refactorizado para trabajar con un modelo multi-almacén y una arquitectura simplificada sin DTOs intermedios en la UI.
 */
public class VentasPresenter {

    private final VentasView view;
    private final ServicioVentas servicioVentas;
    private final ServicioInventario servicioInventario;
    private final ServicioCliente servicioCliente;
    private final ServicioAlmacen servicioAlmacen;
    private final ServicioImpresion servicioImpresion;

    public VentasPresenter(VentasView view, ServicioVentas sv, ServicioInventario si,
                           ServicioCliente sc, ServicioAlmacen sa,
                           ServicioImpresion servicioImpresion) {
        this.view = view;
        this.servicioVentas = sv;
        this.servicioInventario = si;
        this.servicioCliente = sc;
        this.servicioAlmacen = sa;
        this.servicioImpresion = servicioImpresion;
    }

    /**
     * Carga los datos iniciales necesarios para la vista: la lista de clientes y la lista de almacenes.
     * Ambas operaciones se ejecutan de forma asíncrona en hilos de fondo.
     */
    public void cargarDatosIniciales() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        new SwingWorker<List<Cliente>, Void>() {
            @Override
            protected List<Cliente> doInBackground() throws Exception {
                SecurityContextHolder.getContext().setAuthentication(authentication);
                try {
                    return servicioCliente.obtenerTodosActivos();
                } finally {
                    SecurityContextHolder.clearContext();
                }
            }
            @Override
            protected void done() {
                try {
                    view.mostrarClientes(get());
                } catch (Exception e) { handleError(e, "Error al cargar clientes"); }
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
                } catch (Exception e) { handleError(e, "Error al cargar almacenes"); }
            }
        }.execute();
    }

    /**
     * Realiza una búsqueda de productos vendibles (activos y con stock) en el almacén seleccionado.
     *
     * @param texto El criterio de búsqueda.
     */
    public void buscarProductos(String texto) {
        Almacen almacenSeleccionado = view.obtenerAlmacenSeleccionado();
        if (almacenSeleccionado == null) return;
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        new SwingWorker<List<Producto>, Void>() {
            @Override
            protected List<Producto> doInBackground() throws Exception {
                SecurityContextHolder.getContext().setAuthentication(authentication);
                try {
                    return servicioInventario.buscarProductosActivosConStock(texto, almacenSeleccionado.getId());
                } finally {
                    SecurityContextHolder.clearContext();
                }
            }
            @Override
            protected void done() {
                try {
                    view.mostrarResultadosBusqueda(get());
                } catch (Exception e) { handleError(e, "Error al buscar productos"); }
            }
        }.execute();
    }

    /**
     * Busca un producto por su SKU y lo añade al carrito si lo encuentra.
     * @param sku El SKU del producto a añadir (generalmente desde un lector de código de barras).
     */
    public void agregarProductoPorSku(String sku) {
        Almacen almacenSeleccionado = view.obtenerAlmacenSeleccionado();
        if (almacenSeleccionado == null) {
            view.mostrarError("Por favor, seleccione un almacén primero.");
            return;
        }
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        new SwingWorker<Producto, Void>() {
            @Override
            protected Producto doInBackground() throws Exception {
                SecurityContextHolder.getContext().setAuthentication(authentication);
                try {
                    return servicioInventario.buscarProductoActivoConStockPorSku(sku, almacenSeleccionado.getId())
                            .orElse(null);
                } finally {
                    SecurityContextHolder.clearContext();
                }
            }
            @Override
            protected void done() {
                try {
                    Producto producto = get();
                    if (producto != null) {
                        view.agregarProductoAlCarrito(producto);
                    } else {
                        view.mostrarError("Producto con SKU '" + sku + "' no encontrado o sin stock en este almacén.");
                        java.awt.Toolkit.getDefaultToolkit().beep();
                    }
                } catch (Exception e) { handleError(e, "Error al buscar por SKU"); }
            }
        }.execute();
    }

    /**
     * Es invocado por la vista cuando el carrito cambia para actualizar el total.
     * @param total El nuevo total calculado.
     */
    public void onCarritoChanged(BigDecimal total) {
        view.actualizarTotalVenta(total);
    }

    /**
     * Orquesta el proceso de finalización de una venta, mostrando el diálogo de checkout
     * y, si se confirma, invocando al servicio de ventas de forma asíncrona.
     */
    public void finalizarVenta() {
        Almacen almacenSeleccionado = view.obtenerAlmacenSeleccionado();
        Cliente clienteSeleccionado = view.obtenerClienteSeleccionado();
        List<VentaItemDTO> items = view.obtenerItemsCarrito();

        if (almacenSeleccionado == null || clienteSeleccionado == null || items.isEmpty()) {
            view.mostrarError("Debe seleccionar un cliente, un almacén y tener productos en el carrito.");
            return;
        }

        // 1. Obtener total y mostrar diálogo de checkout
        BigDecimal total = view.obtenerTotalCarrito();
        Frame owner = (Frame) SwingUtilities.getWindowAncestor(view.getPanel());
        DialogoCheckout dialogo = new DialogoCheckout(owner, total);
        dialogo.setVisible(true);

        // 2. Proceder solo si el pago fue confirmado
        if (dialogo.isConfirmado()) {
            String nombreUsuario = SecurityContextHolder.getContext().getAuthentication().getName();
            final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            new SwingWorker<Venta, Void>() {
                @Override
                protected Venta doInBackground() throws Exception {
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    try {
                        return servicioVentas.crearVenta(clienteSeleccionado, almacenSeleccionado, nombreUsuario, items);
                    } finally {
                        SecurityContextHolder.clearContext();
                    }
                }

                @Override
                protected void done() {
                    try {
                        Venta ventaGuardada = get();
                        view.mostrarMensajeExito("Venta registrada exitosamente.");

                        // --- CAMBIO: ABRIR EN NAVEGADOR/VISOR DEL SISTEMA ---
                        try {
                            // 1. Generar el PDF en memoria
                            byte[] pdfBytes = servicioImpresion.generarComprobanteEnMemoria(ventaGuardada);

                            // 2. Crear un archivo temporal único
                            java.io.File tempFile = java.io.File.createTempFile("Comprobante_Venta_" + ventaGuardada.getId() + "_", ".pdf");

                            // 3. Escribir los bytes en el archivo
                            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(tempFile)) {
                                fos.write(pdfBytes);
                            }

                            // 4. Abrir el archivo con la aplicación predeterminada (Chrome, Edge, Adobe, etc.)
                            if (java.awt.Desktop.isDesktopSupported()) {
                                java.awt.Desktop.getDesktop().open(tempFile);
                            } else {
                                view.mostrarError("No se puede abrir el PDF automáticamente en este sistema.");
                            }

                        } catch (Exception ex) {
                            view.mostrarError("Venta guardada, pero error al abrir el PDF: " + ex.getMessage());
                            ex.printStackTrace(); // Para ver el error en consola si ocurre
                        }
                        // ----------------------------------------------------

                        view.limpiarVistaPostVenta();
                    } catch (Exception e) {
                        handleError(e, "Error al registrar la venta");
                    }
                }
            }.execute();
        }
    }

    private void handleError(Exception e, String context) {
        String message = (e instanceof ExecutionException && e.getCause() != null) ? e.getCause().getMessage() : e.getMessage();
        view.mostrarError(String.format("%s: %s", context, message));
    }
}