package com.samvitex.ui.menu_lateral;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.util.UIScale;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.Path2D;

/**
 * Un JPanel diseñado para ser mostrado dentro de un JPopupMenu, presentando los sub-ítems
 * de un menú cuando el {@link MenuLateral} está en su modo colapsado.
 * <p>
 * Este componente es visualmente similar a la sección de sub-menús desplegada,
 * incluyendo los botones y las líneas decorativas que los conectan.
 */
public class SubmenuPopup extends JPanel {

    private final MenuLateral menu;
    private final int indiceMenu;
    private final String[] menus;
    private JPopupMenu popup;

    // Constantes de diseño escalables
    private final int sangriaSubmenu = 20;
    private final int alturaSubElemento = 30;

    /**
     * Construye el panel de sub-menú flotante.
     *
     * @param orientation La orientación del componente (de izquierda a derecha o viceversa).
     * @param menu La instancia del menú principal, para disparar eventos.
     * @param indiceMenu El índice del menú principal al que pertenecen estos sub-ítems.
     * @param menus Un array de strings con los nombres de los ítems, donde el primero es
     *              el principal y los siguientes son los sub-ítems.
     */
    public SubmenuPopup(ComponentOrientation orientation, MenuLateral menu, int indiceMenu, String[] menus) {
        this.menu = menu;
        this.indiceMenu = indiceMenu;
        this.menus = menus;
        applyComponentOrientation(orientation);
        inicializar();
    }

    private void inicializar() {
        setLayout(new DisenioPopupLayout());
        popup = new JPopupMenu();

        // Estilos FlatLaf para el popup y el panel
        popup.putClientProperty(FlatClientProperties.STYLE, ""
                + "background:$Menu.background;"
                + "borderColor:$Menu.background;");
        putClientProperty(FlatClientProperties.STYLE, ""
                + "border:0,3,0,3;"
                + "background:$Menu.background;"
                + "foreground:$Menu.lineColor;");

        // Crear botones para cada sub-ítem (se omite el índice 0, que es el principal)
        for (int i = 1; i < menus.length; i++) {
            JButton boton = crearBotonItem(menus[i]);
            final int subIndice = i;
            boton.addActionListener((ActionEvent e) -> {
                menu.ejecutarEvento(indiceMenu, subIndice);
                popup.setVisible(false);
            });
            add(boton);
        }
        popup.add(this);
    }

    /**
     * Crea y estiliza un JButton para ser usado como un sub-ítem en el popup.
     *
     * @param texto El texto del botón.
     * @return Un {@link JButton} estilizado.
     */
    private JButton crearBotonItem(String texto) {
        JButton button = new JButton(texto);
        button.putClientProperty(FlatClientProperties.STYLE, ""
                + "background:$Menu.background;"
                + "foreground:$Menu.foreground;"
                + "selectedBackground:$Menu.button.selectedBackground;"
                + "selectedForeground:$Menu.button.selectedForeground;"
                + "borderWidth:0;"
                + "arc:10;"
                + "focusWidth:0;"
                + "iconTextGap:10;"
                + "margin:5,11,5,11;");
        return button;
    }

    /**
     * Muestra el popup en una posición relativa al componente invocador.
     *
     * @param com El componente desde el cual se invoca el popup.
     * @param x La coordenada x relativa.
     * @param y La coordenada y relativa.
     */
    public void mostrar(Component com, int x, int y) {
        // Ajusta la posición del popup basado en la orientación del componente
        if (menu.getComponentOrientation().isLeftToRight()) {
            popup.show(com, x, y);
        } else {
            int px = getPreferredSize().width + UIScale.scale(5);
            popup.show(com, -px, y);
        }
        aplicarAlineacion();
        SwingUtilities.updateComponentTreeUI(popup);
    }

    /**
     * Aplica la orientación de componente correcta a los botones del sub-menú.
     */
    private void aplicarAlineacion() {
        setComponentOrientation(menu.getComponentOrientation());
        for (Component c : getComponents()) {
            if (c instanceof JButton) {
                ((JButton) c).setHorizontalAlignment(menu.getComponentOrientation().isLeftToRight() ? JButton.LEFT : JButton.RIGHT);
            }
        }
    }

    /**
     * Marca un sub-ítem como seleccionado.
     *
     * @param indice El índice del sub-ítem a seleccionar (1-based).
     */
    protected void setIndiceSeleccionado(int indice) {
        for (int i = 0; i < getComponentCount(); i++) {
            Component com = getComponent(i);
            if (com instanceof JButton) {
                // El índice del botón es `i`, que es 0-based. El subIndice es 1-based.
                ((JButton) com).setSelected(i == indice - 1);
            }
        }
    }

    /**
     * Dibuja las líneas decorativas que conectan los sub-ítems.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        int sAlturaSubElemento = UIScale.scale(alturaSubElemento);
        int sSangriaSubmenu = UIScale.scale(sangriaSubmenu);
        Path2D.Double p = new Path2D.Double();
        int ultimoY = getComponent(getComponentCount() - 1).getY() + (sAlturaSubElemento / 2);
        boolean ltr = getComponentOrientation().isLeftToRight();
        int round = UIScale.scale(10);
        int x = ltr ? (sSangriaSubmenu - round) : (getWidth() - (sSangriaSubmenu - round));

        p.moveTo(x, 0);
        p.lineTo(x, ultimoY - round);

        for (Component com : getComponents()) {
            int comY = com.getY() + (sAlturaSubElemento / 2);
            p.append(crearCurva(round, x, comY, ltr), false);
        }

        g2.setColor(getForeground());
        g2.setStroke(new BasicStroke(UIScale.scale(1f)));
        g2.draw(p);
        g2.dispose();
    }

    private Shape crearCurva(int round, int x, int y, boolean ltr) {
        Path2D p2 = new Path2D.Double();
        p2.moveTo(x, y - round);
        p2.curveTo(x, y - round, x, y, x + (ltr ? round : -round), y);
        return p2;
    }

    /**
     * LayoutManager personalizado para el panel del popup.
     */
    private class DisenioPopupLayout implements LayoutManager {

        @Override
        public Dimension preferredLayoutSize(Container parent) {
            synchronized (parent.getTreeLock()) {
                Insets insets = parent.getInsets();
                int maxWidth = UIScale.scale(150);
                int sSangriaSubmenu = UIScale.scale(sangriaSubmenu);
                int width = getMaxWidth(parent) + sSangriaSubmenu;
                int height = insets.top + insets.bottom;
                for (Component com : parent.getComponents()) {
                    if (com.isVisible()) {
                        height += UIScale.scale(alturaSubElemento);
                    }
                }
                width += insets.left + insets.right;
                return new Dimension(Math.max(width, maxWidth), height);
            }
        }

        @Override
        public void layoutContainer(Container parent) {
            synchronized (parent.getTreeLock()) {
                boolean ltr = parent.getComponentOrientation().isLeftToRight();
                Insets insets = parent.getInsets();
                int sSangriaSubmenu = UIScale.scale(sangriaSubmenu);
                int sAlturaSubElemento = UIScale.scale(alturaSubElemento);
                int x = insets.left + (ltr ? sSangriaSubmenu : 0);
                int y = insets.top;
                int width = getMaxWidth(parent);

                for (Component com : parent.getComponents()) {
                    if (com.isVisible()) {
                        com.setBounds(x, y, width, sAlturaSubElemento);
                        y += sAlturaSubElemento;
                    }
                }
            }
        }

        private int getMaxWidth(Container parent) {
            int maxWidth = UIScale.scale(150);
            int max = 0;
            for (Component com : parent.getComponents()) {
                if (com.isVisible()) {
                    max = Math.max(max, com.getPreferredSize().width);
                }
            }
            return Math.max(max, maxWidth);
        }

        @Override
        public void addLayoutComponent(String name, Component comp) {}
        @Override
        public void removeLayoutComponent(Component comp) {}
        @Override
        public Dimension minimumLayoutSize(Container parent) {
            synchronized (parent.getTreeLock()) {
                return new Dimension(0, 0);
            }
        }
    }
}