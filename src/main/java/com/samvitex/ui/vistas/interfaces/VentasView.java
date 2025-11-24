package com.samvitex.ui.vistas.interfaces;

import com.samvitex.modelos.dto.VentaItemDTO;
import com.samvitex.modelos.entidades.Almacen;
import com.samvitex.modelos.entidades.Cliente;
import com.samvitex.modelos.entidades.Producto;

import javax.swing.JPanel;
import java.math.BigDecimal;
import java.util.List;

/**
 * Define el contrato para la vista del Punto de Venta (POS), siguiendo el patrón MVP.
 * <p>
 * Abstrae las operaciones de la UI de registro de ventas, permitiendo al {@code VentasPresenter}
 * orquestar el flujo de la venta sin depender de la implementación de Swing.
 */
public interface VentasView {

    void mostrarClientes(List<Cliente> clientes);

    void mostrarResultadosBusqueda(List<Producto> productos);

    void actualizarTotalVenta(BigDecimal total);

    void limpiarVistaPostVenta();

    void mostrarError(String mensaje);

    void mostrarMensajeExito(String mensaje);

    Cliente obtenerClienteSeleccionado();

    List<VentaItemDTO> obtenerItemsCarrito();

    /**
     * Muestra la lista de almacenes disponibles para la venta.
     * @param almacenes Lista de entidades Almacen.
     */
    void mostrarAlmacenes(List<Almacen> almacenes);

    /**
     * Devuelve el almacén que el usuario ha seleccionado como origen de la venta.
     * @return El Almacen seleccionado.
     */
    Almacen obtenerAlmacenSeleccionado();

    /**
     * Añade un producto directamente al carrito de la UI.
     */
    void agregarProductoAlCarrito(Producto producto);

    /**
     * Devuelve el monto total actual del carrito de compras.
     * Este método es llamado por el Presenter antes de mostrar el diálogo de checkout.
     * @return BigDecimal con el total.
     */
    BigDecimal obtenerTotalCarrito();

    /**
     * Devuelve la instancia del panel principal de la vista.
     * Es necesario para que el Presenter pueda obtener la ventana padre para los diálogos modales.
     * @return El JPanel de la vista.
     */
    JPanel getPanel();
}