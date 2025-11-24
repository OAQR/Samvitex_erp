package com.samvitex.ui.modelos_tabla;

import com.samvitex.modelos.entidades.Producto;
import com.samvitex.modelos.dto.ProductoInventarioDTO;

import javax.swing.table.AbstractTableModel;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Un {@link AbstractTableModel} especializado para mostrar una lista de entidades {@link Producto}.
 * <p>
 * Este modelo de tabla define las columnas a mostrar, cómo obtener los datos de cada celda
 * y los tipos de datos de cada columna. Esto es crucial para que la {@link javax.swing.JTable}
 * utilice los comparadores (para ordenamiento) y renderizadores correctos de forma automática.
 * Encapsula la lógica de mapeo entre el objeto de negocio {@code Producto} y su representación tabular.
 *
 * @see Producto
 * @see com.samvitex.ui.paneles.PanelInventario
 */
public class ProductoTableModel extends AbstractTableModel {

    /**
     * Define los nombres de las columnas que se mostrarán en la cabecera de la tabla.
     */
    private final String[] columnNames = {"Código", "Nombre", "Categoría", "Stock Total", "Precio Venta", "Estado"};

    /**
     * Almacena la lista de productos que se están mostrando actualmente en la tabla.
     */
    private final List<ProductoInventarioDTO> productosDTO;

    /**
     * Constructor que inicializa el modelo con una lista vacía de productos.
     */
    public ProductoTableModel() {
        this.productosDTO = new ArrayList<>();
    }

    @Override
    public int getRowCount() { return productosDTO.size(); }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    /**
     * Especifica el tipo de dato para cada columna, permitiendo a la JTable aplicar
     * ordenamiento numérico para la cantidad y el precio.
     *
     * @param columnIndex el índice de la columna.
     * @return la clase del objeto de la columna.
     */
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return switch (columnIndex) {
            case 3 -> Long.class;
            case 4 -> BigDecimal.class;
            default -> String.class;
        };
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex < 0 || rowIndex >= productosDTO.size()) return null;

        ProductoInventarioDTO dto = productosDTO.get(rowIndex);
        Producto producto = dto.producto();
        return switch (columnIndex) {
            case 0 -> producto.getSku();
            case 1 -> producto.getNombre();
            case 2 -> producto.getCategoria() != null ? producto.getCategoria().getNombre() : "N/A";
            case 3 -> dto.stockTotal();
            case 4 -> producto.getPrecioVenta();
            case 5 -> producto.isActivo() ? "Activo" : "Inactivo";
            default -> null;
        };
    }

    /**
     * Reemplaza la lista actual de productos con una nueva y notifica a la
     * JTable que los datos han cambiado para que se repinte por completo.
     */
    public void setProductos(List<ProductoInventarioDTO> nuevosProductosDTO) {
        this.productosDTO.clear();
        if (nuevosProductosDTO != null) {
            this.productosDTO.addAll(nuevosProductosDTO);
        }

        fireTableDataChanged();
    }

    /**
     * Obtiene la entidad {@link Producto} completa correspondiente a una fila específica de la tabla.
     *
     * @param rowIndex el índice de la fila en la vista (puede ser diferente al del modelo si hay ordenamiento).
     * @return el objeto {@link Producto} en esa fila.
     */
    public Producto getProductoAt(int rowIndex) {
        return productosDTO.get(rowIndex).producto();
    }

    /**
     * Obtiene el texto del tooltip para una fila específica.
     */
    public String getTooltipAt(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < productosDTO.size()) {
            return productosDTO.get(rowIndex).resumenUbicacion();
        }
        return null;
    }
}