package com.samvitex.ui.menu_lateral;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.util.UIScale;
import com.samvitex.ui.menu_lateral.modo.SelectorColorAcento;
import com.samvitex.ui.menu_lateral.modo.SelectorModoClaroOscuro;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Componente principal del menú de navegación lateral.
 * <p>
 * Este panel organiza y muestra una lista jerárquica de opciones de menú.
 * Es altamente configurable y se adapta visualmente para mostrarse en modo completo
 * (con texto e iconos) o en modo colapsado (solo iconos).
 */
public class MenuLateral extends JPanel {

    // --- Propiedades Configurables de Diseño ---
    protected final boolean ocultarTituloEnModoMinimo = true;
    protected final int sangriaIzquierdaTitulo = 5;
    protected final int espacioVerticalTitulo = 5;
    protected final int anchoMaximoMenu = 250;
    protected final int anchoMinimoMenu = 60;
    protected final int espacioHorizontalCabeceraCompleto = 5;

    // --- Estado Interno ---
    private final List<EventoMenu> eventos = new ArrayList<>();
    private boolean menuCompleto = true;
    private final String nombreCabecera = "";

    // --- Componentes de la UI ---
    private JLabel cabecera;
    private JScrollPane scroll;
    private JPanel panelMenu;
    private SelectorModoClaroOscuro selectorModoClaroOscuro;
    private SelectorColorAcento selectorColorAcento;
    private FlatSVGIcon logoTemaClaro;
    private FlatSVGIcon logoTemaOscuro;

    public MenuLateral() {
        inicializar();

        UIManager.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                // Escuchamos el evento "lookAndFeel" que se dispara al cambiar de tema
                if ("lookAndFeel".equals(evt.getPropertyName())) {
                    actualizarLogoPorTema();
                }
            }
        });
    }

    private void inicializar() {
        setLayout(new DisenioMenuLayout());
        putClientProperty(FlatClientProperties.STYLE, ""
                + "border:20,2,2,2;"
                + "background:$Menu.background;"
                + "arc:10;");

        logoTemaClaro = cargarIconoSvg("/imagenes/samvitex_logo_blanco.svg");
        logoTemaOscuro = cargarIconoSvg("/imagenes/samvitex_logo_blanco.svg");

        // Cabecera
        cabecera = new JLabel(nombreCabecera);
        cabecera.putClientProperty(FlatClientProperties.STYLE, ""
                + "font:$Menu.header.font;"
                + "foreground:$Menu.foreground;");

        actualizarLogoPorTema();

        // Panel contenedor de los elementos de menú
        panelMenu = new JPanel(new DisenioPanelMenu(this));
        panelMenu.putClientProperty(FlatClientProperties.STYLE, "background:$Menu.background;");

        // ScrollPane
        scroll = new JScrollPane(panelMenu);
        scroll.putClientProperty(FlatClientProperties.STYLE, "border:null;");
        JScrollBar vscroll = scroll.getVerticalScrollBar();
        vscroll.setUnitIncrement(10);
        vscroll.putClientProperty(FlatClientProperties.STYLE, ""
                + "width:$Menu.scroll.width;"
                + "trackInsets:$Menu.scroll.trackInsets;"
                + "thumbInsets:$Menu.scroll.thumbInsets;"
                + "background:$Menu.ScrollBar.background;"
                + "thumb:$Menu.ScrollBar.thumb;");

        // Controles de Tema
        selectorModoClaroOscuro = new SelectorModoClaroOscuro();
        selectorColorAcento = new SelectorColorAcento(this);

        selectorColorAcento.setVisible(true);

        add(cabecera);
        add(scroll);
        add(selectorModoClaroOscuro);
        add(selectorColorAcento);
    }

    /**
     * Carga un ícono SVG desde la ruta de recursos de forma segura.
     * @param ruta La ruta al archivo SVG dentro de los recursos (ej. "/iconos/menu/logo.svg").
     * @return El FlatSVGIcon cargado, o null si no se encuentra.
     */
    private FlatSVGIcon cargarIconoSvg(String ruta) {
        URL url = getClass().getResource(ruta);
        if (url == null) {
            System.err.println("Error: No se pudo encontrar el recurso SVG en la ruta: " + ruta);
            return null;
        }
        return new FlatSVGIcon(url);
    }


    /**
     * Verifica el tema actual de FlatLaf y asigna el logo correspondiente a la cabecera.
     */
    private void actualizarLogoPorTema() {
        boolean esOscuro = FlatLaf.isLafDark();
        FlatSVGIcon logoActivo = esOscuro ? logoTemaClaro : logoTemaOscuro;

        if (logoActivo == null) {
            cabecera.setIcon(null);
            System.err.println("El logo para el tema actual no está disponible.");
            return;
        }

        cabecera.setIcon(new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                if (c.getWidth() > 0 && c.getHeight() > 0) {
                    Graphics2D g2 = (Graphics2D) g.create();

                    if (!esOscuro) { //modo claro
                        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f));
                    }else{
                        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
                    }

                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    int tamanoBase = Math.min(c.getWidth(), c.getHeight());
                    int tamanoIcono = tamanoBase - UIScale.scale(10);
                    if (tamanoIcono < 0) tamanoIcono = 0;

                    int xIcono = (c.getWidth() - tamanoIcono) / 2;
                    int yIcono = (c.getHeight() - tamanoIcono) / 2;

                    Image imagenLogo = logoActivo.getImage();
                    if (imagenLogo != null) {
                        g2.drawImage(imagenLogo, xIcono, yIcono, tamanoIcono, tamanoIcono, null);
                    }

                    g2.dispose();
                }
            }

            @Override
            public int getIconWidth() {
                // El tamaño preferido es dinámico
                int tamanoBase = Math.min(cabecera.getWidth(), cabecera.getHeight());
                return Math.max(0, tamanoBase - UIScale.scale(10));
            }

            @Override
            public int getIconHeight() {
                int tamanoBase = Math.min(cabecera.getWidth(), cabecera.getHeight());
                return Math.max(0, tamanoBase - UIScale.scale(10));
            }
        });

        // Forzamos una actualización del componente
        cabecera.revalidate();
        cabecera.repaint();
    }
    /**
     * Construye dinámicamente el contenido del menú a partir de un modelo de datos.
     * Este metodo reemplaza la lista de ítems hardcodeada.
     *
     * @param menuItems Un array de arrays de String, donde cada array interno representa un ítem
     *                  o un título de sección.
     */
    public void setMenuModel(String[][] menuItems) {
        panelMenu.removeAll();
        int indice = 0;
        for (String[] menuItem : menuItems) {
            String menuName = menuItem[0];
            if (menuName.startsWith("~") && menuName.endsWith("~")) {
                panelMenu.add(crearTitulo(menuName));
            } else {
                ElementoMenu elemento = new ElementoMenu(this, menuItem, indice++, eventos);
                panelMenu.add(elemento);
            }
        }
        revalidate();
        repaint();
    }

    private JLabel crearTitulo(String titulo) {
        String nombreSeccion = titulo.substring(1, titulo.length() - 1);
        JLabel lblTitulo = new JLabel(nombreSeccion);
        lblTitulo.putClientProperty(FlatClientProperties.STYLE, ""
                + "font:$Menu.label.font;"
                + "foreground:$Menu.title.foreground;");
        return lblTitulo;
    }

    public void setMenuCompleto(boolean completo) {
        this.menuCompleto = completo;
        if (completo) {
            cabecera.setText(nombreCabecera);
            cabecera.setHorizontalAlignment(getComponentOrientation().isLeftToRight() ? JLabel.LEFT : JLabel.RIGHT);
        } else {
            cabecera.setText("");
            cabecera.setHorizontalAlignment(JLabel.CENTER);
        }
        for (Component com : panelMenu.getComponents()) {
            if (com instanceof ElementoMenu elemento) {
                elemento.setMenuCompleto(completo);
            }
        }
        selectorModoClaroOscuro.setMenuCompleto(completo);
        selectorColorAcento.setMenuCompleto(completo);
    }

    public void setIndiceSeleccionado(int indice, int subIndice) {
        ejecutarEvento(indice, subIndice);
    }

    protected void actualizarSeleccion(int indice, int subIndice) {
        for (Component com : panelMenu.getComponents()) {
            if (com instanceof ElementoMenu item) {
                if (item.getIndiceMenu() == indice) {
                    item.setIndiceSeleccionado(subIndice);
                } else {
                    item.setIndiceSeleccionado(-1);
                }
            }
        }
    }

    protected void ejecutarEvento(int indice, int subIndice) {
        AccionMenu accion = new AccionMenu();
        for (EventoMenu evento : eventos) {
            evento.menuSeleccionado(indice, subIndice, accion);
        }
        if (!accion.haSidoCancelada()) {
            actualizarSeleccion(indice, subIndice);
        }
    }

    public void addEventoMenu(EventoMenu evento) {
        eventos.add(evento);
    }

    public void ocultarTodosLosSubmenus() {
        for (Component com : panelMenu.getComponents()) {
            if (com instanceof ElementoMenu elemento) {
                elemento.ocultarSubmenus();
            }
        }
        revalidate();
    }

    // --- Getters para el LayoutManager ---
    public boolean isMenuCompleto() { return menuCompleto; }
    public boolean isOcultarTituloEnModoMinimo() { return ocultarTituloEnModoMinimo; }
    public int getSangriaIzquierdaTitulo() { return sangriaIzquierdaTitulo; }
    public int getEspacioVerticalTitulo() { return espacioVerticalTitulo; }
    public int getAnchoMaximoMenu() { return anchoMaximoMenu; }
    public int getAnchoMinimoMenu() { return anchoMinimoMenu; }

    /**
     * LayoutManager interno para organizar la cabecera, el scrollpane y los controles de tema.
     */
    private class DisenioMenuLayout implements LayoutManager {
        @Override
        public void layoutContainer(Container parent) {
            synchronized (parent.getTreeLock()) {
                Insets insets = parent.getInsets();
                int x = insets.left;
                int y = insets.top;
                int ancho = parent.getWidth() - (insets.left + insets.right);
                int alto = parent.getHeight() - (insets.top + insets.bottom);
                int espacio = UIScale.scale(5);
                int espacioCabecera = menuCompleto ? UIScale.scale(espacioHorizontalCabeceraCompleto) : 0;

                int alturaColorAcento = 0;
                if (selectorColorAcento.isVisible()) {
                    alturaColorAcento = selectorColorAcento.getPreferredSize().height + espacio;
                }

                int alturaCabecera = UIScale.scale(menuCompleto ? 100 : 50);
                cabecera.setBounds(x + espacioCabecera, y, ancho - (espacioCabecera * 2), alturaCabecera);

                int espacioLD = UIScale.scale(10);
                int anchoLD = ancho - espacioLD * 3;
                int alturaLD = selectorModoClaroOscuro.getPreferredSize().height;
                int xLD = x + espacioLD;
                int yLD = y + alto - alturaLD - espacioLD - alturaColorAcento;

                int menuX = x;
                int menuY = y + alturaCabecera + espacio;
                int menuAncho = menuCompleto ? ancho * 2 : ancho;
                int menuAlto = alto - (alturaCabecera + espacio) - (alturaLD + espacioLD * 2) - alturaColorAcento;                scroll.setBounds(menuX, menuY, menuAncho, menuAlto);

                selectorModoClaroOscuro.setBounds(xLD, yLD, anchoLD, alturaLD);

                if (selectorColorAcento.isVisible()) {
                    int tbAltura = selectorColorAcento.getPreferredSize().height;
                    int tbAncho = Math.min(selectorColorAcento.getPreferredSize().width, anchoLD);
                    int tbY = y + alto - tbAltura - espacioLD;
                    int tbX = xLD + ((anchoLD - tbAncho) / 2);
                    selectorColorAcento.setBounds(tbX, tbY, tbAncho, tbAltura);
                }
            }
        }

        @Override public void addLayoutComponent(String name, Component comp) {}
        @Override public void removeLayoutComponent(Component comp) {}
        @Override public Dimension preferredLayoutSize(Container parent) {
            synchronized (parent.getTreeLock()) { return new Dimension(5, 5); }
        }
        @Override public Dimension minimumLayoutSize(Container parent) {
            synchronized (parent.getTreeLock()) { return new Dimension(0, 0); }
        }
    }
}