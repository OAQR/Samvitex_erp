package com.samvitex.ui.componentes;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.samvitex.modelos.dto.SearchResultDTO;

import javax.swing.*;
import java.awt.*;

/**
 * Un {@link ListCellRenderer} personalizado para mostrar objetos {@link SearchResultDTO}
 * en una JList de resultados de búsqueda universal.
 * <p>
 * Muestra un ícono distintivo para cada tipo de resultado (producto, cliente),
 * junto con un texto principal y uno secundario, mejorando la claridad y la experiencia de usuario.
 */
public class UniversalSearchRenderer extends JPanel implements ListCellRenderer<SearchResultDTO> {

    private final JLabel lblIcono = new JLabel();
    private final JLabel lblTextoPrincipal = new JLabel();
    private final JLabel lblTextoSecundario = new JLabel();

    private final FlatSVGIcon iconProducto;
    private final FlatSVGIcon iconCliente;

    public UniversalSearchRenderer() {
        setLayout(new BorderLayout(10, 0));
        setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // Cargar íconos (es buena práctica tenerlos como recursos)
        // Usaremos íconos de FlatLaf por conveniencia
        iconProducto = new FlatSVGIcon("com/formdev/flatlaf/demo/icons/package.svg");
        iconCliente = new FlatSVGIcon("com/formdev/flatlaf/demo/icons/user.svg");

        Font fuenteSecundaria = lblTextoSecundario.getFont().deriveFont(Font.ITALIC, lblTextoSecundario.getFont().getSize() - 2f);
        lblTextoSecundario.setFont(fuenteSecundaria);
        lblTextoSecundario.setForeground(Color.GRAY);

        JPanel panelTexto = new JPanel(new GridLayout(2, 1));
        panelTexto.setOpaque(false);
        panelTexto.add(lblTextoPrincipal);
        panelTexto.add(lblTextoSecundario);

        add(lblIcono, BorderLayout.WEST);
        add(panelTexto, BorderLayout.CENTER);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends SearchResultDTO> list, SearchResultDTO value, int index, boolean isSelected, boolean cellHasFocus) {
        lblTextoPrincipal.setText(value.textoPrincipal());
        lblTextoSecundario.setText(value.textoSecundario());

        // Asignar el ícono correspondiente al tipo de resultado
        switch (value.tipo()) {
            case "PRODUCTO":
                lblIcono.setIcon(iconProducto);
                break;
            case "CLIENTE":
                lblIcono.setIcon(iconCliente);
                break;
            default:
                lblIcono.setIcon(null);
        }

        // Gestionar los colores de fondo y texto para la selección
        if (isSelected) {
            setBackground(list.getSelectionBackground());
            lblTextoPrincipal.setForeground(list.getSelectionForeground());
            lblTextoSecundario.setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            lblTextoPrincipal.setForeground(list.getForeground());
            lblTextoSecundario.setForeground(Color.GRAY);
        }

        return this;
    }
}