package com.samvitex.ui.menu_lateral;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.util.UIScale;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.Path2D;
import java.util.List;

/**
 * Representa un elemento visual dentro del {@link MenuLateral}.
 * <p>
 * Un {@code ElementoMenu} es un panel que contiene un botón principal y, opcionalmente,
 * una serie de botones de sub-menú. Gestiona su propio estado (seleccionado, desplegado)
 * y las animaciones de despliegue/repliegue de sus sub-menús.
 */
public class ElementoMenu extends JPanel {

    // --- Propiedades Configurables del Diseño ---
    protected final int alturaElementoPrincipal = 38;
    protected final int alturaSubElemento = 35;
    protected final int sangriaSubmenu = 34;
    protected final int espacioSuperiorSubmenu = 5;
    protected final int espacioInferiorSubmenu = 5;

    // --- Estado Interno del Componente ---
    private final List<EventoMenu> eventos;
    private final MenuLateral menu;
    private final String[] nombresMenu;
    private final int indiceMenu;
    private boolean menuDesplegado;
    private float progresoAnimacion;
    private SubmenuPopup popup;

    /**
     * Construye un nuevo ElementoMenu.
     *
     * @param menu La instancia del menú principal al que pertenece este elemento.
     * @param nombresMenu Un array con los nombres de los menús (el primero es el principal).
     * @param indiceMenu El índice base de este elemento en el menú.
     * @param eventos La lista de listeners de eventos a notificar.
     */
    public ElementoMenu(MenuLateral menu, String[] nombresMenu, int indiceMenu, List<EventoMenu> eventos) {
        this.menu = menu;
        this.nombresMenu = nombresMenu;
        this.indiceMenu = indiceMenu;
        this.eventos = eventos;
        inicializar();
    }

    private void inicializar() {
        setLayout(new DisenioElementoMenu());
        putClientProperty(FlatClientProperties.STYLE, ""
                + "background:$Menu.background;"
                + "foreground:$Menu.lineColor;");

        // Crear y configurar los botones para cada nombre de menú
        for (int i = 0; i < nombresMenu.length; i++) {
            JButton boton = crearBoton(nombresMenu[i]);
            boton.setHorizontalAlignment(getComponentOrientation().isLeftToRight() ? JButton.LEADING : JButton.TRAILING);

            if (i == 0) { // Es el botón principal
                boton.setIcon(getIcono());
                boton.addActionListener((ActionEvent e) -> {
                    if (tieneSubmenus()) {
                        if (menu.isMenuCompleto()) {
                            // Si el menú está expandido, animar despliegue/repliegue
                            AnimacionMenu.animar(ElementoMenu.this, !menuDesplegado);
                        } else {
                            // Si el menú está colapsado, mostrar el popup
                            int alturaPopup = UIScale.scale(alturaElementoPrincipal) / 2;
                            popup.mostrar(ElementoMenu.this, (int) ElementoMenu.this.getWidth() + UIScale.scale(5), alturaPopup);
                        }
                    } else {
                        menu.ejecutarEvento(indiceMenu, 0);
                    }
                });
            } else { // Son botones de sub-menú
                final int subIndice = i;
                boton.addActionListener((ActionEvent e) -> menu.ejecutarEvento(indiceMenu, subIndice));
            }
            add(boton);
        }
        // Crear el popup para el modo colapsado
        popup = new SubmenuPopup(getComponentOrientation(), menu, indiceMenu, nombresMenu);
    }

    /**
     * Carga y colorea el icono SVG para este elemento de menú.
     * @return un {@link Icon} listo para ser usado.
     */
    private Icon getIcono() {
        Color colorClaro = FlatUIUtils.getUIColor("Menu.icon.lightColor", Color.WHITE);
        Color colorOscuro = FlatUIUtils.getUIColor("Menu.icon.darkColor", Color.GRAY);
        FlatSVGIcon icon = new FlatSVGIcon("iconos/menu/" + indiceMenu + ".svg");
        FlatSVGIcon.ColorFilter f = new FlatSVGIcon.ColorFilter();
        f.add(Color.decode("#969696"), colorClaro, colorOscuro); // Reemplaza el color gris original
        icon.setColorFilter(f);
        return icon;
    }

    /**
     * Crea un JButton con los estilos estandarizados para el menú.
     * @param texto El texto del botón.
     * @return Un {@link JButton} estilizado.
     */
    private JButton crearBoton(String texto) {
        JButton button = new JButton(texto);
        button.putClientProperty(FlatClientProperties.STYLE, ""
                + "background:$Menu.background;"
                + "foreground:$Menu.foreground;"
                + "selectedBackground:$Menu.button.selectedBackground;"
                + "selectedForeground:$Menu.button.selectedForeground;"
                + "borderWidth:0;"
                + "focusWidth:0;"
                + "innerFocusWidth:0;"
                + "arc:10;"
                + "iconTextGap:10;"
                + "margin:3,11,3,11;");
        return button;
    }

    /**
     * Establece el sub-ítem seleccionado visualmente.
     * @param indice El índice del sub-ítem a seleccionar (1-based). Si es -1, deselecciona todos.
     */
    protected void setIndiceSeleccionado(int indice) {
        boolean seleccionado = false;
        for (int i = 0; i < getComponentCount(); i++) {
            if (getComponent(i) instanceof JButton boton) {
                boton.setSelected(i == indice);
                if (i == indice) {
                    seleccionado = true;
                }
            }
        }
        // El botón principal también se marca como seleccionado si un hijo lo está
        ((JButton) getComponent(0)).setSelected(seleccionado);
        popup.setIndiceSeleccionado(indice);
    }

    /**
     * Actualiza el estado visual del elemento (texto y alineación) cuando el menú
     * cambia entre modo completo y colapsado.
     * @param completo {@code true} si el menú se está expandiendo.
     */
    public void setMenuCompleto(boolean completo) {
        if (completo) {
            for (int i = 0; i < getComponentCount(); i++) {
                if (getComponent(i) instanceof JButton boton) {
                    boton.setText(nombresMenu[i]);
                    boton.setHorizontalAlignment(getComponentOrientation().isLeftToRight() ? JButton.LEFT : JButton.RIGHT);
                }
            }
        } else {
            for (Component com : getComponents()) {
                if (com instanceof JButton boton) {
                    boton.setText("");
                    boton.setHorizontalAlignment(JButton.CENTER);
                }
            }

            progresoAnimacion = 0f;
            menuDesplegado = false;
        }
    }

    public void ocultarSubmenus() {
        progresoAnimacion = 0;
        menuDesplegado = false;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Dibuja las líneas de conexión de los sub-menús si la animación está en progreso
        if (progresoAnimacion > 0) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int sAlturaSubElemento = UIScale.scale(alturaSubElemento);
            int sSangriaSubmenu = UIScale.scale(sangriaSubmenu);
            int sAlturaElementoPrincipal = UIScale.scale(alturaElementoPrincipal);
            int sEspacioSuperiorSubmenu = UIScale.scale(espacioSuperiorSubmenu);

            Path2D.Double p = new Path2D.Double();
            int ultimoY = getComponent(getComponentCount() - 1).getY() + (sAlturaSubElemento / 2);
            boolean ltr = getComponentOrientation().isLeftToRight();
            int round = UIScale.scale(10);
            int x = ltr ? (sSangriaSubmenu - round) : (getWidth() - (sSangriaSubmenu - round));

            p.moveTo(x, sAlturaElementoPrincipal + sEspacioSuperiorSubmenu);
            p.lineTo(x, ultimoY - round);

            for (int i = 1; i < getComponentCount(); i++) {
                int comY = getComponent(i).getY() + (sAlturaSubElemento / 2);
                p.append(crearCurva(round, x, comY, ltr), false);
            }

            g2.setColor(getForeground());
            g2.setStroke(new BasicStroke(UIScale.scale(1f)));
            g2.draw(p);
            g2.dispose();
        }
    }

    /**
     * Dibuja la flecha indicadora de despliegue.
     */
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (tieneSubmenus()) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(FlatUIUtils.getUIColor("Menu.arrowColor", getForeground()));
            g2.setStroke(new BasicStroke(UIScale.scale(1f)));

            int sAlturaElementoPrincipal = UIScale.scale(alturaElementoPrincipal);
            boolean ltr = getComponentOrientation().isLeftToRight();

            if (menu.isMenuCompleto()) { // Flecha arriba/abajo en modo completo
                int arrowWidth = UIScale.scale(10);
                int arrowHeight = UIScale.scale(5);
                int ax = ltr ? (getWidth() - arrowWidth * 2) : arrowWidth;
                int ay = (sAlturaElementoPrincipal - arrowHeight) / 2;
                Path2D p = new Path2D.Double();
                p.moveTo(0, progresoAnimacion * arrowHeight);
                p.lineTo(arrowWidth / 2f, (1f - progresoAnimacion) * arrowHeight);
                p.lineTo(arrowWidth, progresoAnimacion * arrowHeight);
                g2.translate(ax, ay);
                g2.draw(p);
            } else { // Flecha derecha/izquierda en modo colapsado
                int arrowWidth = UIScale.scale(4);
                int arrowHeight = UIScale.scale(8);
                int ax = ltr ? (getWidth() - arrowWidth - UIScale.scale(3)) : UIScale.scale(3);
                int ay = (sAlturaElementoPrincipal - arrowHeight) / 2;
                Path2D p = new Path2D.Double();
                if (ltr) {
                    p.moveTo(0, 0); p.lineTo(arrowWidth, arrowHeight / 2f); p.lineTo(0, arrowHeight);
                } else {
                    p.moveTo(arrowWidth, 0); p.lineTo(0, arrowHeight / 2f); p.lineTo(arrowWidth, arrowHeight);
                }
                g2.translate(ax, ay);
                g2.draw(p);
            }
            g2.dispose();
        }
    }

    private Shape crearCurva(int round, int x, int y, boolean ltr) {
        Path2D p2 = new Path2D.Double();
        p2.moveTo(x, y - round);
        p2.curveTo(x, y - round, x, y, x + (ltr ? round : -round), y);
        return p2;
    }

    // --- Getters y Setters de estado ---

    public boolean isMenuDesplegado() { return menuDesplegado; }
    public void setMenuDesplegado(boolean menuDesplegado) { this.menuDesplegado = menuDesplegado; }
    public float getProgresoAnimacion() { return progresoAnimacion; }
    public void setProgresoAnimacion(float progresoAnimacion) { this.progresoAnimacion = progresoAnimacion; }
    public String[] getNombresMenu() { return nombresMenu; }
    public int getIndiceMenu() { return indiceMenu; }
    public int getAlturaElementoPrincipal() { return alturaElementoPrincipal; }
    public int getAlturaSubElemento() { return alturaSubElemento; }
    public int getSangriaSubmenu() { return sangriaSubmenu; }
    public int getEspacioSuperiorSubmenu() { return espacioSuperiorSubmenu; }
    public int getEspacioInferiorSubmenu() { return espacioInferiorSubmenu; }
    private boolean tieneSubmenus() { return nombresMenu.length > 1; }
}