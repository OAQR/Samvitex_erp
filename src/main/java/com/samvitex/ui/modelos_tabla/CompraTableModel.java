package com.samvitex.ui.modelos_tabla;

import com.samvitex.modelos.entidades.Producto;

import javax.swing.table.AbstractTableModel;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Un {@link AbstractTableModel} para gestionar los ítems en el formulario de registro de compras.
 *
 * <p>Este modelo está diseñado para ser interactivo y es la pieza central del {@code PanelCompras}:</p>
 * <ul>
 *   <li>Permite la edición tanto de la <b>cantidad</b> como del <b>costo unitario</b> de cada producto,
 *       ya que el costo puede variar en cada compra.</li>
 *   <li>Realiza validaciones para asegurar que los valores introducidos sean numéricos y positivos.</li>
 *   <li>Calcula automáticamente los subtotales y el total general de la orden de compra.</li>
 * </ul>
 */
public class CompraTableModel extends AbstractTableModel {

    private final List<ItemCompra> items;
    private final String[] columnNames = {"Producto", "Cantidad", "Costo Unit.", "Subtotal"};

    public CompraTableModel() {
        this.items = new ArrayList<>();
    }

    @Override
    public int getRowCount() {
        return items.size();
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
            case 1 -> Integer.class;        // Cantidad
            case 2, 3 -> BigDecimal.class;  // Costo y Subtotal
            default -> String.class;
        };
    }

    /**
     * Define las columnas "Cantidad" y "Costo Unit." como editables.
     */
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 1 || columnIndex == 2;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        ItemCompra item = items.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> item.getProducto().getNombre();
            case 1 -> item.getCantidad();
            case 2 -> item.getCostoUnitario();
            case 3 -> item.getSubtotal();
            default -> null;
        };
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        ItemCompra item = items.get(rowIndex);

        switch (columnIndex) {
            case 1: // Cantidad
                try {
                    int nuevaCantidad = Integer.parseInt(aValue.toString());
                    if (nuevaCantidad > 0) {
                        item.setCantidad(nuevaCantidad);
                    }
                } catch (NumberFormatException ignored) {}
                break;
            case 2: // Costo Unitario
                try {
                    BigDecimal nuevoCosto = new BigDecimal(aValue.toString()).setScale(2, RoundingMode.HALF_UP);
                    if (nuevoCosto.compareTo(BigDecimal.ZERO) >= 0) {
                        item.setCostoUnitario(nuevoCosto);
                    }
                } catch (NumberFormatException ignored) {}
                break;
        }
        // Notifica a la tabla que la fila se actualizó para recalcular el subtotal.
        fireTableRowsUpdated(rowIndex, rowIndex);
    }

    public void agregarProducto(Producto producto) {
        for (ItemCompra item : items) {
            if (item.getProducto().getId().equals(producto.getId())) {
                item.setCantidad(item.getCantidad() + 1);
                fireTableDataChanged();
                return;
            }
        }
        // Al añadir un nuevo producto, se usa su precio de costo actual como valor por defecto.
        items.add(new ItemCompra(producto, 1, producto.getPrecioCosto()));
        fireTableDataChanged();
    }

    public void eliminarItem(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < items.size()) {
            items.remove(rowIndex);
            fireTableRowsDeleted(rowIndex, rowIndex);
        }
    }

    public void limpiar() {
        int rowCount = getRowCount();
        if (rowCount > 0) {
            items.clear();
            fireTableRowsDeleted(0, rowCount - 1);
        }
    }

    public List<ItemCompra> getItems() {
        return items;
    }

    public BigDecimal getTotal() {
        return items.stream()
                .map(ItemCompra::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Clase interna anidada que representa una línea de producto en la orden de compra.
     * A diferencia de ItemCarrito, esta clase debe almacenar un 'costoUnitario' editable.
     */
    public static class ItemCompra {
        private final Producto producto;
        private int cantidad;
        private BigDecimal costoUnitario;

        public ItemCompra(Producto producto, int cantidad, BigDecimal costoUnitario) {
            this.producto = producto;
            this.cantidad = cantidad;
            this.costoUnitario = costoUnitario;
        }

        public Producto getProducto() { return producto; }
        public int getCantidad() { return cantidad; }
        public BigDecimal getCostoUnitario() { return costoUnitario; }

        public void setCantidad(int cantidad) { this.cantidad = cantidad; }
        public void setCostoUnitario(BigDecimal costoUnitario) { this.costoUnitario = costoUnitario; }

        public BigDecimal getSubtotal() {
            return costoUnitario.multiply(new BigDecimal(cantidad));
        }
    }
}