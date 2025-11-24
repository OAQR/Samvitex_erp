package com.samvitex.ui.componentes;

import com.formdev.flatlaf.FlatClientProperties;
import com.samvitex.ui.componentes.renderers.TableGradientCell;

import javax.swing.*;
import javax.swing.table.TableModel;

/**
 * Una clase {@code JTable} personalizada para la aplicación Samvitex.
 * <p>
 * Esta clase encapsula una serie de configuraciones de estilo y comportamiento
 * para asegurar que todas las tablas en la aplicación tengan una apariencia consistente y profesional.
 * Las configuraciones incluyen:
 * <ul>
 *   <li>Aplicación automática del renderizador de celdas con gradiente ({@link TableGradientCell}).</li>
 *   <li>Estilos de cabecera y scroll pane definidos a través de FlatLaf.</li>
 *   <li>Configuraciones de usabilidad por defecto, como altura de fila y modo de selección.</li>
 * </ul>
 */
public class SamvitexTable extends JTable {

    /**
     * Constructor que recibe un modelo de tabla.
     * @param model El {@link TableModel} que esta tabla mostrará.
     */
    public SamvitexTable(TableModel model) {
        super(model);
        inicializarEstilos();
    }

    /**
     * Aplica todas las configuraciones de estilo y renderizadores a la tabla.
     */
    private void inicializarEstilos() {
        // 1. Aplicar nuestro renderizador de celdas personalizado
        setDefaultRenderer(Object.class, new TableGradientCell());

        // 2. Configuraciones de usabilidad de la tabla
        setRowHeight(32); // Aumenta la altura de las filas para mejor legibilidad
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setAutoCreateRowSorter(true);
        getTableHeader().setReorderingAllowed(false); // Evita que el usuario reordene columnas

        // 3. Estilos de la cabecera usando propiedades de cliente de FlatLaf
        getTableHeader().putClientProperty(FlatClientProperties.STYLE, ""
                + "height: 35;"
                + "font: bold;"
                + "hoverBackground: null;"
                + "pressedBackground: null;"
                + "separatorColor: $TableHeader.background;"
        );
    }

    /**
     * Metodo de utilidad para envolver esta tabla en un JScrollPane con el estilo de la aplicación.
     * Esto asegura que la barra de desplazamiento también sea consistente.
     *
     * @return Un {@link JScrollPane} que contiene esta tabla.
     */
    public JScrollPane enScrollPane() {
        JScrollPane scroll = new JScrollPane(this);

        // Estilos del JScrollPane y su barra de scroll
        scroll.putClientProperty(FlatClientProperties.STYLE, ""
                + "border:3,0,3,0,$Table.background,10,10;"
        );
        scroll.getVerticalScrollBar().putClientProperty(FlatClientProperties.STYLE, ""
                + "hoverTrackColor: null;" // Evita cambio de color de la pista del scroll al pasar el mouse
                + "track:'$Control.background';"
                + "thumb:'lighten($Control.background, 15%)';"
        );

        // Estilo del panel que contiene la tabla dentro del scroll, eliminando bordes extraños
        scroll.getViewport().putClientProperty(FlatClientProperties.STYLE, "background:null;");

        return scroll;
    }
}