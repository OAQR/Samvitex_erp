package com.samvitex.ui.componentes.renderers;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * Un {@code TableCellRenderer} personalizado que dibuja un fondo con gradiente para las celdas de una tabla.
 * <p>
 * Esta clase ofrece una apariencia visual mejorada para las {@code JTable}, permitiendo:
 * <ul>
 *   <li>Un gradiente de color para las filas seleccionadas.</li>
 *   <li>Un color de fondo alterno para las filas pares, mejorando la legibilidad (efecto "cebra").</li>
 *   <li>Colores totalmente configurables a través de su constructor.</li>
 * </ul>
 * El renderizador se optimiza calculando las dimensiones del gradiente una sola vez por celda para que se alinee
 * perfectamente a lo largo de toda la fila.
 */
public class TableGradientCell extends DefaultTableCellRenderer {

    private final Color gradientColor1;
    private final Color gradientColor2;
    private final Color evenRowColor;
    private final Color oddRowColor;
    private final Color selectedForegroundColor;

    private int x;
    private int width;
    private boolean isSelected;
    private int row;

    /**
     * Constructor por defecto con un esquema de colores predefinido.
     */
    public TableGradientCell() {
        this(
                Color.decode("#009FFF"),  // Inicio del gradiente para selección
                Color.decode("#ec2F4B"),   // Fin del gradiente para selección
                Color.decode("#000000"),    // Color de fondo para filas pares
                Color.decode("#434343"),    // Color de fondo para filas impares (más oscuro)
                Color.WHITE               // Color del texto para filas seleccionadas
        );
    }

    /**
     * Constructor que permite una personalización completa de los colores.
     *
     * @param gradientColor1 Color de inicio para el gradiente de la fila seleccionada.
     * @param gradientColor2 Color de fin para el gradiente de la fila seleccionada.
     * @param evenRowColor   Color de fondo para las filas pares (no seleccionadas).
     * @param oddRowColor    Color de fondo para las filas impares (no seleccionadas).
     * @param selectedForegroundColor Color del texto cuando la fila está seleccionada.
     */
    public TableGradientCell(Color gradientColor1, Color gradientColor2, Color evenRowColor, Color oddRowColor, Color selectedForegroundColor) {
        this.gradientColor1 = gradientColor1;
        this.gradientColor2 = gradientColor2;
        this.evenRowColor = evenRowColor;
        this.oddRowColor = oddRowColor;
        this.selectedForegroundColor = selectedForegroundColor;
        setOpaque(false); // Es crucial que sea no opaco para permitir el dibujo personalizado
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        // Llama al metodo de la superclase para configurar propiedades básicas como el texto y la alineación.
        Component com = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        // Almacena el estado de la celda para usarlo en paintComponent
        this.isSelected = isSelected;
        this.row = row;

        // Calcula las coordenadas para el gradiente, asegurando que cubra toda la fila
        Rectangle cellRect = table.getCellRect(row, column, true);
        this.x = -cellRect.x;
        this.width = table.getWidth() - cellRect.x;

        // Cambia el color del texto si la fila está seleccionada para asegurar la legibilidad
        /*if (isSelected) {
            com.setForeground(selectedForegroundColor);
        } else {
            // Usa el color de texto por defecto de la tabla para filas no seleccionadas
            com.setForeground(table.getForeground());
        }*/

        return com;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Dibuja el fondo personalizado
        if (isSelected) {
            // Dibuja el gradiente para la fila seleccionada
            g2.setPaint(new GradientPaint(x, 0, gradientColor1, width, 0, gradientColor2));
            g2.fill(new Rectangle2D.Double(0, 0, getWidth(), getHeight()));
        } else {
            // Dibuja un color sólido para las filas no seleccionadas, alternando por fila (efecto cebra)
            if (row % 2 == 0) {
                g2.setColor(evenRowColor);
            } else {
                g2.setColor(oddRowColor);
            }
            g2.fill(new Rectangle2D.Double(0, 0, getWidth(), getHeight()));
        }

        g2.dispose();

        // Llama a super.paintComponent al final para que el texto y los íconos se dibujen sobre nuestro fondo.
        super.paintComponent(g);
    }
}