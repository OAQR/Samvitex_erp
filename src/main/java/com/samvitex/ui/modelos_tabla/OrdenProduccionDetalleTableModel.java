package com.samvitex.ui.modelos_tabla;

import com.samvitex.modelos.dto.OrdenProduccionDTO;
import com.samvitex.modelos.entidades.Producto;
import com.samvitex.modelos.enums.TipoDetalleProduccion;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Un {@link AbstractTableModel} para gestionar los ítems de detalle (insumos y productos finales)
 * dentro del diálogo de creación de una Orden de Producción.
 *
 * <p>Este modelo no almacena las entidades {@link Producto} completas, sino una representación
 * ligera a través de {@link OrdenProduccionDTO.DetalleDTO}, junto con un caché de nombres
 * de productos para una renderización eficiente.</p>
 */
public class OrdenProduccionDetalleTableModel extends AbstractTableModel {

    private final List<OrdenProduccionDTO.DetalleDTO> detalles;
    private final Map<Integer, String> cacheNombresProductos;
    private final String[] columnNames = {"Producto", "Tipo", "Cantidad"};

    public OrdenProduccionDetalleTableModel() {
        this.detalles = new ArrayList<>();
        this.cacheNombresProductos = new HashMap<>();
    }

    @Override
    public int getRowCount() {
        return detalles.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return switch (columnIndex) {
            case 1 -> TipoDetalleProduccion.class;
            case 2 -> Integer.class;
            default -> String.class;
        };
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        OrdenProduccionDTO.DetalleDTO detalle = detalles.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> cacheNombresProductos.getOrDefault(detalle.productoId(), "Desconocido");
            case 1 -> detalle.tipoDetalle();
            case 2 -> detalle.cantidad();
            default -> null;
        };
    }

    /**
     * Añade un nuevo detalle a la tabla.
     *
     * @param producto El producto a añadir.
     * @param tipo El tipo de detalle (INSUMO o PRODUCTO_FINAL).
     * @param cantidad La cantidad del producto.
     */
    public void addDetalle(Producto producto, TipoDetalleProduccion tipo, int cantidad) {
        if (producto == null || tipo == null || cantidad <= 0) return;

        // Guarda el nombre del producto en el caché si aún no existe.
        cacheNombresProductos.putIfAbsent(producto.getId(), producto.getNombre());

        detalles.add(new OrdenProduccionDTO.DetalleDTO(producto.getId(), tipo, cantidad));
        fireTableRowsInserted(detalles.size() - 1, detalles.size() - 1);
    }

    /**
     * Elimina un detalle de la tabla basado en su índice de fila.
     *
     * @param rowIndex El índice de la fila a eliminar.
     */
    public void removeDetalle(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < detalles.size()) {
            detalles.remove(rowIndex);
            fireTableRowsDeleted(rowIndex, rowIndex);
        }
    }

    /**
     * Devuelve la lista de DTOs de detalle que representa el estado actual de la tabla.
     *
     * @return una lista de {@link OrdenProduccionDTO.DetalleDTO}.
     */
    public List<OrdenProduccionDTO.DetalleDTO> getDetalles() {
        return new ArrayList<>(detalles); // Devuelve una copia para evitar modificaciones externas.
    }
}