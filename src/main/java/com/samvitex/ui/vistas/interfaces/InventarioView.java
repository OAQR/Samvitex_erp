package com.samvitex.ui.vistas.interfaces;

import com.samvitex.modelos.dto.ProductoInventarioDTO;
import com.samvitex.modelos.entidades.Almacen;
import com.samvitex.modelos.entidades.InventarioPorAlmacen;
import com.samvitex.modelos.entidades.Producto;
import org.springframework.data.domain.Page;

import javax.swing.*;
import java.util.List;

/**
 * Define el contrato para la vista de gestión de inventario, siguiendo el patrón MVP.
 * <p>
 * Esta interfaz abstrae las operaciones de la UI, permitiendo al {@code InventarioPresenter}
 * dirigir el flujo de trabajo sin conocer los detalles de implementación de Swing.
 *
 */
public interface InventarioView {

    /**
     * Instruye a la vista para que renderice una página de productos.
     * La implementación es responsable de actualizar la tabla y los controles de paginación.
     */
    void mostrarProductos(Page<ProductoInventarioDTO> pagina);
    /**
     * Instruye a la vista para que abra el formulario de creación/edición de un producto.
     *
     * @param producto El producto a editar. Si es {@code null}, se abre en modo de creación.
     */
    void mostrarDialogoProducto(Producto producto);

    /**
     * Indica a la vista que sus datos deben ser recargados.
     */
    void refrescarVista();

    /**
     * Muestra un diálogo modal con un mensaje de error.
     *
     * @param mensaje El mensaje de error a mostrar.
     */
    void mostrarError(String mensaje);

    /**
     * Muestra un diálogo modal con un mensaje de éxito.
     *
     * @param mensaje El contenido del mensaje a mostrar.
     */
    void mostrarMensajeExito(String mensaje);

    /**
     * Presenta un diálogo de confirmación para una acción crítica.
     *
     * @param mensaje La pregunta de confirmación.
     * @param titulo El título del diálogo.
     * @return {@code true} si el usuario confirma la acción, {@code false} en caso contrario.
     */
    boolean confirmarAccion(String mensaje, String titulo);

    /**
     * Solicita a la vista que encuentre, muestre y seleccione un producto específico por su ID.
     * @param productoId El ID del producto a seleccionar.
     */
    void seleccionarYMostrarProducto(Integer productoId);

    /**
     * Establece el texto en el campo de búsqueda de la vista.
     * Esto permite al presentador controlar el filtro de búsqueda de la UI.
     *
     * @param texto El texto que se establecerá en el campo de búsqueda.
     */
    void setTextoBusqueda(String texto);

    void mostrarStockPorAlmacen(List<InventarioPorAlmacen> inventario);

    void mostrarAlmacenes(List<Almacen> almacenes);

    /**
     * Devuelve el almacén seleccionado en el ComboBox de filtro.
     * @return El Almacen seleccionado, o un objeto especial si se seleccionó "Todos".
     */
    Almacen obtenerAlmacenFiltro();

    /**
     * Devuelve la instancia del panel principal de la vista.
     * Es necesario para ser el 'padre' de los diálogos modales.
     * @return El JPanel de la vista.
     */
    JPanel getPanel();
}