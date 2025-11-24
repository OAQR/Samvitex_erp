package com.samvitex.ui.modelos_tabla;

import com.samvitex.modelos.dto.ReporteVentasDTO;

import javax.swing.table.AbstractTableModel;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Un TableModel especializado para mostrar los resultados de un reporte de ventas.
 * Utiliza el DTO {@link ReporteVentasDTO} como fuente de datos para cada fila.
 */
public class ReporteVentasTableModel extends AbstractTableModel {

    private final String[] columnNames = {"Producto", "Unidades Vendidas", "Total Ingresos", "Ganancia Bruta Estimada"};
    private final List<ReporteVentasDTO> datos;

    public ReporteVentasTableModel() {
        this.datos = new ArrayList<>();
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
            case 1 -> Long.class;
            case 2, 3 -> BigDecimal.class;
            default -> String.class;
        };
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        ReporteVentasDTO dto = datos.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> dto.nombreProducto();
            case 1 -> dto.totalUnidadesVendidas();
            case 2 -> dto.totalIngresos();
            case 3 -> dto.gananciaBrutaEstimada();
            default -> null;
        };
    }

    /**
     * Establece los datos del reporte y notifica a la tabla para que se actualice.
     * @param nuevosDatos La nueva lista de DTOs del reporte de ventas.
     */
    public void setDatos(List<ReporteVentasDTO> nuevosDatos) {
        this.datos.clear();
        this.datos.addAll(nuevosDatos);
        fireTableDataChanged();
    }
}