package com.samvitex.ui.modelos_tabla;

import com.samvitex.modelos.entidades.Producto;
import com.samvitex.modelos.excepciones.CarritoException;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Un {@link AbstractTableModel} para gestionar los ítems en el carrito de compras del panel de ventas.
 *
 * <p>Este modelo de tabla ha sido refactorizado para un entorno multi-almacén.
 * La validación de stock ya no se realiza contra la entidad Producto, sino contra el
 * stock disponible en el almacén de origen, que se almacena en cada {@link ItemCarrito}
 * al momento de ser añadido.</p>
 */
public class CarritoTableModel extends AbstractTableModel {

    private final List<ItemCarrito> items;
    private final String[] columnNames = {"Producto", "Cantidad", "Precio Unit.", "Subtotal"};

    public CarritoTableModel() {
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
            case 1 -> Integer.class;
            case 2, 3 -> BigDecimal.class;
            default -> String.class;
        };
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 1; // Solo la columna de cantidad es editable.
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        ItemCarrito item = items.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> item.getProducto().getNombre();
            case 1 -> item.getCantidad();
            case 2 -> item.getProducto().getPrecioVenta();
            case 3 -> item.getSubtotal();
            default -> null;
        };
    }

    /**
     * Se invoca cuando un usuario edita la celda de cantidad.
     * La validación de stock ahora se hace contra el 'stockMaximoEnAlmacen' guardado en el ItemCarrito.
     */
    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (columnIndex == 1) {
            ItemCarrito item = items.get(rowIndex);
            int nuevaCantidad;
            try {
                nuevaCantidad = Integer.parseInt(aValue.toString());
            } catch (NumberFormatException e) {
                // Si el valor no es un número, no hacer nada.
                return;
            }

            // Validar que la cantidad no sea menor a 1.
            if (nuevaCantidad < 1) {
                nuevaCantidad = 1;
            }

            // Validar que la cantidad no exceda el stock máximo permitido para este ítem.
            if (nuevaCantidad > item.getStockMaximoEnAlmacen()) {
                nuevaCantidad = item.getStockMaximoEnAlmacen();
                // Opcional: Mostrar un aviso. El tooltip de la celda podría ser un mejor lugar para esta info.
                // javax.swing.JOptionPane.showMessageDialog(null, "Stock máximo alcanzado: " + item.getStockMaximoEnAlmacen());
            }

            item.setCantidad(nuevaCantidad);
            fireTableRowsUpdated(rowIndex, rowIndex);
        }
    }

    /**
     * Añade un producto al carrito. Si ya existe, incrementa su cantidad.
     * Si no hay stock suficiente para añadir una unidad más, lanza una CarritoException.
     *
     * @param producto El Producto a añadir.
     * @param stockMaximoEnAlmacen El stock actual de ese producto en el almacén de origen.
     * @throws CarritoException si se intenta añadir un producto que ya alcanzó su stock máximo.
     */
    public void agregarProducto(Producto producto, int stockMaximoEnAlmacen) throws CarritoException { // CAMBIO: Añadir "throws"
        Optional<ItemCarrito> itemExistente = items.stream()
                .filter(item -> Objects.equals(item.getProducto().getId(), producto.getId()))
                .findFirst();

        if (itemExistente.isPresent()) {
            ItemCarrito item = itemExistente.get();
            int nuevaCantidad = item.getCantidad() + 1;
            if (nuevaCantidad <= item.getStockMaximoEnAlmacen()) {
                item.setCantidad(nuevaCantidad);
            } else {
                throw new CarritoException("Ya ha alcanzado el stock máximo para '" + producto.getNombre() + "' (" + item.getStockMaximoEnAlmacen() + " unidades).");
            }
        } else {
            if (stockMaximoEnAlmacen > 0) {
                items.add(new ItemCarrito(producto, 1, stockMaximoEnAlmacen));
            } else {
                throw new CarritoException("El producto '" + producto.getNombre() + "' no tiene stock disponible en este almacén.");
            }
        }
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

    public List<ItemCarrito> getItems() {
        return items;
    }

    public BigDecimal getTotal() {
        return items.stream()
                .map(ItemCarrito::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Clase interna que representa una línea de producto dentro del carrito.
     * Almacena el stock disponible en el momento de añadir el producto ('snapshot' del stock),
     * para realizar validaciones de cantidad de forma independiente y segura.
     */
    public static class ItemCarrito {
        private final Producto producto;
        private int cantidad;
        private final int stockMaximoEnAlmacen;

        public ItemCarrito(Producto producto, int cantidad, int stockMaximoEnAlmacen) {
            this.producto = producto;
            this.cantidad = cantidad;
            this.stockMaximoEnAlmacen = stockMaximoEnAlmacen;
        }

        public Producto getProducto() {
            return producto;
        }

        public int getCantidad() {
            return cantidad;
        }

        public void setCantidad(int cantidad) {
            this.cantidad = cantidad;
        }

        public int getStockMaximoEnAlmacen() {
            return stockMaximoEnAlmacen;
        }

        public BigDecimal getSubtotal() {
            return producto.getPrecioVenta().multiply(new BigDecimal(cantidad));
        }
    }
}