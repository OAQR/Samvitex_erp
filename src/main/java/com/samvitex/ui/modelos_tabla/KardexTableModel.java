package com.samvitex.ui.modelos_tabla;

import com.samvitex.modelos.entidades.MovimientoInventario;
import com.samvitex.modelos.enums.TipoMovimiento;

import javax.swing.table.AbstractTableModel;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Un TableModel especializado para mostrar los datos de un reporte de Kardex (trazabilidad de inventario).
 * Utiliza la entidad {@link MovimientoInventario} como fuente de datos para cada fila.
 */
public class KardexTableModel extends AbstractTableModel {

    private final String[] columnNames = {"Fecha", "Tipo", "Usuario", "Cantidad Movida", "Stock Anterior", "Stock Nuevo"};
    private final List<MovimientoInventario> datos;
    private final DateTimeFormatter formatter;

    public KardexTableModel() {
        this.datos = new ArrayList<>();
        this.formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").withZone(ZoneId.systemDefault());
    }

    @Override
    public int getRowCount() {
        return datos.size();
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
            case 0 -> String.class; // Formateamos la fecha como String
            case 1 -> TipoMovimiento.class;
            case 3, 4, 5 -> Integer.class;
            default -> String.class;
        };
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        MovimientoInventario m = datos.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> formatter.format(m.getFechaMovimiento());
            case 1 -> m.getTipo();
            case 2 -> m.getUsuario().getNombreUsuario();
            case 3 -> m.getCantidadMovida();
            case 4 -> m.getStockAnterior();
            case 5 -> m.getStockNuevo();
            default -> null;
        };
    }

    /**
     * Establece los datos del reporte y notifica a la tabla para que se actualice.
     * @param nuevosDatos La nueva lista de movimientos de inventario.
     */
    public void setDatos(List<MovimientoInventario> nuevosDatos) {
        this.datos.clear();
        this.datos.addAll(nuevosDatos);
        fireTableDataChanged();
    }
}