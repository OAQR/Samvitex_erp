package com.samvitex.ui.vistas.interfaces;

import com.samvitex.modelos.dto.CompraItemDTO;
import com.samvitex.modelos.entidades.Almacen;
import com.samvitex.modelos.entidades.Producto;
import com.samvitex.modelos.entidades.Proveedor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Define el contrato para la vista de registro de compras a proveedores, siguiendo el patrón MVP.
 *
 * <p>Esta interfaz abstrae las operaciones de la UI para el registro de entrada de mercancía.
 * El {@code ComprasPresenter} utilizará esta interfaz para dirigir el flujo de trabajo
 * sin conocer los detalles de implementación de Swing.</p>
 *
 * <p>La vista es responsable de renderizar los componentes para seleccionar un proveedor,
 * buscar productos, gestionar una tabla de ítems de compra (con cantidades y costos editables)
 * y capturar la acción de registrar la compra.</p>
 */
public interface ComprasView {

    /**
     * Pobla el componente de selección de proveedores con la lista de proveedores activos.
     *
     * @param proveedores La lista de entidades {@link Proveedor} activas.
     */
    void mostrarProveedores(List<Proveedor> proveedores);

    /**
     * Muestra los resultados de la búsqueda de productos en el componente visual correspondiente.
     *
     * @param productos La lista de entidades {@link Producto} que coinciden con la búsqueda.
     */
    void mostrarResultadosBusqueda(List<Producto> productos);

    /**
     * Actualiza el componente visual que muestra el monto total de la compra en curso.
     *
     * @param total El {@link BigDecimal} que representa el total calculado de la orden de compra.
     */
    void actualizarTotalCompra(BigDecimal total);

    /**
     * Restablece la vista a su estado inicial después de que una compra se ha completado
     * exitosamente (ej. limpiar la tabla de ítems, el campo de búsqueda y la referencia de factura).
     */
    void limpiarVistaPostCompra();

    /**
     * Muestra un diálogo modal con un mensaje de error.
     *
     * @param mensaje El texto del error a mostrar.
     */
    void mostrarError(String mensaje);

    /**
     * Muestra un diálogo modal con un mensaje de éxito o informativo.
     *
     * @param mensaje El contenido del mensaje a mostrar.
     */
    void mostrarMensajeExito(String mensaje);

    /**
     * Solicita a la vista que devuelva la entidad {@link Proveedor} que está
     * actualmente seleccionada por el usuario.
     *
     * @return El {@link Proveedor} seleccionado, o {@code null} si no hay ninguno.
     */
    Proveedor obtenerProveedorSeleccionado();

    /**
     * Solicita a la vista que devuelva el número de factura o documento de referencia
     * introducido por el usuario.
     *
     * @return Un {@link String} con la referencia de la factura.
     */
    String obtenerReferenciaFactura();

    /**
     * Solicita a la vista que recopile todos los ítems de la orden de compra y los
     * transforme en una lista de {@link CompraItemDTO}. Este DTO es el objeto de
     * transferencia de datos que se enviará a la capa de servicio.
     *
     * @return Una lista de {@link CompraItemDTO} que representa el estado actual de la orden de compra.
     */
    List<CompraItemDTO> obtenerItemsCompra();

    /**
     * Muestra la lista de almacenes disponibles para recibir la compra.
     * @param almacenes Lista de entidades Almacen.
     */
    void mostrarAlmacenes(List<Almacen> almacenes);

    /**
     * Devuelve el almacén que el usuario ha seleccionado como destino de la compra.
     * @return El Almacen seleccionado.
     */
    Almacen obtenerAlmacenDestinoSeleccionado();
}