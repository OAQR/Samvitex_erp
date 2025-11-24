package com.samvitex.ui.modelos_tabla;

import com.samvitex.modelos.entidades.OrdenProduccion;
import com.samvitex.modelos.enums.EstadoProduccion;

import javax.swing.table.AbstractTableModel;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * TableModel para mostrar una lista de {@link OrdenProduccion} en una JTable.
 */
public class OrdenProduccionTableModel extends AbstractTableModel {

    private final List<OrdenProduccion> ordenes;
    private final String[] columnNames = {
            "Código", "Taller", "Estado", "Almacén Insumos", "Almacén Destino", "Fecha Creación", "Responsable"
    };
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(ZoneId.systemDefault());

    public OrdenProduccionTableModel() {
        this.ordenes = new ArrayList<>();
    }

    @Override
    public int getRowCount() {
        return ordenes.size();
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
        if (columnIndex == 2) {
            return EstadoProduccion.class;
        }
        return String.class;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        OrdenProduccion orden = ordenes.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> orden.getCodigo();
            case 1 -> orden.getTaller() != null ? orden.getTaller().getNombre() : "N/A";
            case 2 -> orden.getEstado();
            case 3 -> orden.getAlmacenInsumos() != null ? orden.getAlmacenInsumos().getNombre() : "N/A";
            case 4 -> orden.getAlmacenDestino() != null ? orden.getAlmacenDestino().getNombre() : "N/A";
            case 5 -> formatter.format(orden.getFechaCreacion());
            case 6 -> orden.getUsuarioResponsable() != null ? orden.getUsuarioResponsable().getNombreCompleto() : "N/A";
            default -> null;
        };
    }

    public void setOrdenes(List<OrdenProduccion> nuevasOrdenes) {
        this.ordenes.clear();
        this.ordenes.addAll(nuevasOrdenes);
        fireTableDataChanged();
    }

    public OrdenProduccion getOrdenAt(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < ordenes.size()) {
            return ordenes.get(rowIndex);
        }
        return null;
    }
}