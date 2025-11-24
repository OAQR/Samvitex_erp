package com.samvitex.ui.presentadores;

import com.samvitex.modelos.dto.ProductoInventarioDTO;
import com.samvitex.modelos.entidades.Almacen;
import com.samvitex.modelos.entidades.InventarioPorAlmacen;
import com.samvitex.modelos.entidades.Producto;
import com.samvitex.repositorios.InventarioPorAlmacenRepositorio;
import com.samvitex.servicios.ServicioAlmacen;
import com.samvitex.servicios.ServicioInventario;
import com.samvitex.ui.vistas.interfaces.InventarioView;
import com.samvitex.utilidades.swing.SecureSwingWorker; // Asegúrate de importar la nueva clase
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Presenter para la vista de Gestión de Inventario (Patrón MVP).
 * Orquesta la interacción entre la InventarioView y los servicios de negocio.
 */
@Component
public class InventarioPresenter {

    private InventarioView view;
    private final ServicioInventario servicioInventario;
    private final ServicioAlmacen servicioAlmacen;
    private final InventarioPorAlmacenRepositorio inventarioPorAlmacenRepositorio;

    private String ultimaBusqueda = "";
    private int paginaActual = 0;
    private static final int TAMANO_PAGINA = 50;
    private Integer idProductoASeleccionar = null;

    @Autowired
    public InventarioPresenter(ServicioInventario servicioInventario,
                               ServicioAlmacen servicioAlmacen,
                               InventarioPorAlmacenRepositorio inventarioPorAlmacenRepositorio) {
        this.servicioInventario = servicioInventario;
        this.servicioAlmacen = servicioAlmacen;
        this.inventarioPorAlmacenRepositorio = inventarioPorAlmacenRepositorio;
    }

    public void setView(InventarioView view) {
        this.view = view;
    }

    /**
     * Carga los datos iniciales necesarios para la vista, comenzando por los almacenes.
     * La carga de productos se encadena para ejecutarse después de la carga de almacenes.
     */
    public void cargarDatosIniciales() {
        cargarAlmacenes();
    }

    public void cargarAlmacenes() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
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
                    cargarProductos();
                } catch (Exception e) {
                    handleError(e, "Error al cargar almacenes");
                }
            }
        }.execute();
    }

    public void cargarProductos() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Almacen almacenSeleccionado = view.obtenerAlmacenFiltro();
        Integer almacenId = (almacenSeleccionado != null && almacenSeleccionado.getId() > 0) ? almacenSeleccionado.getId() : null;

        new SwingWorker<Page<ProductoInventarioDTO>, Void>() {
            @Override
            protected Page<ProductoInventarioDTO> doInBackground() throws Exception {
                SecurityContextHolder.getContext().setAuthentication(authentication);
                try {
                    return servicioInventario.buscarProductosPaginadoConStockTotal(ultimaBusqueda, almacenId, paginaActual, TAMANO_PAGINA);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw e;
                } finally {
                    SecurityContextHolder.clearContext();
                }
            }
            @Override
            protected void done() {
                try {
                    Page<ProductoInventarioDTO> pagina = get(); // Si hubo error, aquí salta la excepción
                    view.mostrarProductos(pagina);

                    if (idProductoASeleccionar != null) {
                        view.seleccionarYMostrarProducto(idProductoASeleccionar);
                        idProductoASeleccionar = null;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    handleError(e, "Error al cargar productos");
                }
            }
        }.execute();
    }



    public void buscarProductos(String textoBusqueda) {
        this.ultimaBusqueda = (textoBusqueda != null) ? textoBusqueda.trim() : "";
        this.paginaActual = 0;
        cargarProductos();
    }

    public void cambiarPagina(int nuevaPagina) {
        this.paginaActual = nuevaPagina;
        cargarProductos();
    }

    /**
     * Se llama cuando el usuario selecciona un producto en la tabla principal.
     * Carga y muestra el desglose de stock por almacén para ese producto.
     * @param productoId El ID del producto seleccionado.
     */
    public void onProductoSeleccionado(Integer productoId) {
        if (productoId == null) return;
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        new SwingWorker<List<InventarioPorAlmacen>, Void>() {
            @Override
            protected List<InventarioPorAlmacen> doInBackground() throws Exception {
                SecurityContextHolder.getContext().setAuthentication(authentication);
                try {
                    return servicioInventario.obtenerDesgloseStockPorProducto(productoId);
                } finally {
                    SecurityContextHolder.clearContext();
                }
            }
            @Override
            protected void done() {
                try {
                    view.mostrarStockPorAlmacen(get());
                } catch (Exception e) {
                    handleError(e, "Error al cargar detalle de stock");
                }
            }
        }.execute();
    }

    public void buscarYSeleccionarProducto(Integer productoId) {
        if (productoId == null) return;
        idProductoASeleccionar = productoId;
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        new SwingWorker<Producto, Void>() {
            @Override
            protected Producto doInBackground() throws Exception {
                SecurityContextHolder.getContext().setAuthentication(authentication);
                try {
                    return servicioInventario.findById(productoId)
                            .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + productoId));
                } finally {
                    SecurityContextHolder.clearContext();
                }
            }
            @Override
            protected void done() {
                try {
                    Producto producto = get();
                    view.setTextoBusqueda(producto.getNombre());
                    buscarProductos(producto.getNombre());
                } catch (Exception e) {
                    handleError(e, "No se pudo encontrar el producto para la selección");
                    idProductoASeleccionar = null;
                }
            }
        }.execute();
    }

    public void onNuevoProductoClicked() {
        view.mostrarDialogoProducto(null);
    }

    public void onEditarProductoClicked(Producto producto) {
        view.mostrarDialogoProducto(producto);
    }

    public void onDesactivarProductoClicked(Producto producto) {
        if (producto == null) {
            view.mostrarError("No se ha seleccionado un producto para desactivar.");
            return;
        }
        String mensaje = String.format("¿Está seguro de que desea desactivar el producto '%s' (Código: %s)?", producto.getNombre(), producto.getSku());
        if (view.confirmarAccion(mensaje, "Confirmar Desactivación")) {
            final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    try {
                        servicioInventario.eliminarProducto(producto.getId());
                        return null;
                    } finally {
                        SecurityContextHolder.clearContext();
                    }
                }
                @Override
                protected void done() {
                    try {
                        get();
                        view.mostrarMensajeExito("Producto desactivado correctamente.");
                        view.refrescarVista();
                    } catch (Exception e) {
                        handleError(e, "Error al desactivar el producto");
                    }
                }
            }.execute();
        }
    }

    public int getPaginaActual() {
        return this.paginaActual;
    }

    private void handleError(Exception e, String context) {
        String message = (e instanceof ExecutionException && e.getCause() != null) ? e.getCause().getMessage() : e.getMessage();
        view.mostrarError(String.format("%s: %s", context, message));
    }
}