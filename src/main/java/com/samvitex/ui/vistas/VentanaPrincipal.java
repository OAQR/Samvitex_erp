package com.samvitex.ui.vistas;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.util.UIScale;
import com.samvitex.modelos.dto.SearchResultDTO;
import com.samvitex.modelos.dto.SesionUsuario;
import com.samvitex.servicios.*;
import com.samvitex.ui.componentes.UniversalSearchRenderer;
import com.samvitex.ui.menu_lateral.MenuLateral;
import com.samvitex.ui.paneles.*;
import com.samvitex.ui.presentadores.InventarioPresenter;
import com.samvitex.ui.vistas.interfaces.InventarioView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.Component;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;


/**
 * Contenedor principal de la aplicación post-autenticación, implementado como un JLayeredPane.
 * <p>
 * Esta clase orquesta la interfaz de usuario principal, combinando una barra de herramientas superior,
 * un menú de navegación lateral retráctil y un área de contenido dinámica. Sus responsabilidades son:
 * <ul>
 *   <li><b>Estructura Visual:</b> Utiliza un LayoutManager personalizado para posicionar el menú lateral,
 *       el contenido principal, un botón para colapsar/expandir el menú y la nueva barra de búsqueda universal.</li>
 *   <li><b>Navegación y Carga Perezosa:</b> Gestiona la transición entre los diferentes módulos (paneles)
 *       usando un {@link CardLayout}. Los paneles se crean "bajo demanda" (lazy loading) la primera vez
 *       que se acceden y se guardan en caché para un acceso posterior instantáneo.</li>
 *   <li><b>Búsqueda Universal:</b> Integra una barra de búsqueda que permite encontrar entidades (como Productos o Clientes)
 *       desde cualquier parte de la aplicación, mostrando resultados en un popup y navegando al módulo
 *       correspondiente al seleccionar un resultado.</li>
 *   <li><b>Autorización de UI:</b> Configura el {@link MenuLateral} basándose en el rol del usuario autenticado,
 *       mostrando únicamente las opciones a las que tiene permiso.</li>
 * </ul>
 */
@org.springframework.stereotype.Component
public class VentanaPrincipal extends JLayeredPane { // Cambiado de JFrame a JLayeredPane

    private final ApplicationContext springContext;
    private MenuLateral menuLateral;
    private JPanel panelContenido;
    private JPanel panelCardLayout;
    private JButton botonMenu;
    private SesionUsuario sesionUsuario;
    private boolean uiInicializada = false;

    private JTextField txtBusquedaUniversal;
    private JPopupMenu popupBusqueda;
    private JList<SearchResultDTO> listaResultados;
    private DefaultListModel<SearchResultDTO> listModelBusqueda;
    private Timer searchTimer;

    private final Map<String, JPanel> panelCache = new HashMap<>();

    // Mapa de permisos para el menú
    private static final Map<String, Set<String>> MENU_PERMISSIONS = Map.of(
            "Dashboard", Set.of("ADMINISTRADOR", "VENDEDOR", "ALMACENISTA"),
            "Inventario", Set.of("ADMINISTRADOR", "ALMACENISTA"),
            "Ventas", Set.of("ADMINISTRADOR", "VENDEDOR"),
            "Compras", Set.of("ADMINISTRADOR", "ALMACENISTA"),
            "Producción", Set.of("ADMINISTRADOR", "ALMACENISTA"),
            "Reportes", Set.of("ADMINISTRADOR"),
            "Configuración", Set.of("ADMINISTRADOR"),
            "Cerrar Sesión", Set.of("ADMINISTRADOR", "VENDEDOR", "ALMACENISTA")
    );


    /**
     * Constructor inyectado por Spring.
     * @param context El contexto de la aplicación Spring.
     */
    @Autowired
    public VentanaPrincipal(ApplicationContext context) {
        this.springContext = context;
    }

    /**
     * Concentra toda la lógica de inicialización y visualización.
     * Se llama DESPUÉS de que el contexto de Spring esté listo y FlatLaf configurado.
     *
     * @param frame El JFrame contenedor de la aplicación.
     * @param sesion La sesión del usuario autenticado.
     */
    public void inicializarYMostrar(JFrame frame, SesionUsuario sesion) {
        this.sesionUsuario = sesion;
        frame.setTitle("SamVitex - Sistema de Gestión | Usuario: " + sesion.nombreCompleto() + " (" + sesion.rol() + ")");

        inicializarComponentesUI();
        configurarMenu();
        actualizarIconoBotonMenu();

        mostrarPanel("DASHBOARD");
        menuLateral.setIndiceSeleccionado(0, 0);

        uiInicializada = true;
    }

    /**
     * Contiene la lógica de construcción de componentes que antes estaba en el constructor.
     */
    private void inicializarComponentesUI() {
        setBorder(new EmptyBorder(5, 5, 5, 5));
        setLayout(new DisenioVentanaPrincipalLayout());

        menuLateral = new MenuLateral();
        panelContenido = new JPanel(new BorderLayout());

        botonMenu = new JButton();
        botonMenu.putClientProperty(FlatClientProperties.STYLE, ""
                + "background:$Menu.button.background;"
                + "arc:999;"
                + "focusWidth:0;"
                + "borderWidth:0;");
        botonMenu.addActionListener((ActionEvent e) -> {
            setMenuCompleto(!menuLateral.isMenuCompleto());
        });

        JPanel panelCabecera = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelCabecera.setBorder(BorderFactory.createEmptyBorder(0, 10, 5, 10));
        panelCabecera.add(new JLabel("Búsqueda Rápida:"));
        inicializarBusquedaUniversal();
        panelCabecera.add(txtBusquedaUniversal);
        panelContenido.add(panelCabecera, BorderLayout.NORTH);

        panelCardLayout = new JPanel(new CardLayout());
        panelContenido.add(this.panelCardLayout, BorderLayout.CENTER);

        setLayer(botonMenu, JLayeredPane.POPUP_LAYER);
        add(botonMenu);
        add(menuLateral);
        add(panelContenido);
    }

    /**
     * Configura y muestra la ventana principal después de un inicio de sesión exitoso.
     *
     * @param frame El JFrame contenedor de la aplicación.
     * @param sesion La sesión del usuario autenticado.
     */
    public void mostrar(JFrame frame, SesionUsuario sesion) {
        this.sesionUsuario = sesion;
        frame.setTitle("SamVitex - Sistema de Gestión | Usuario: " + sesion.nombreCompleto() + " (" + sesion.rol() + ")");

        configurarMenu();
        actualizarIconoBotonMenu();

        mostrarPanel("DASHBOARD");
        menuLateral.setIndiceSeleccionado(0, 0);
    }

    private void configurarMenu() {
        String[][] menuItemsBase = {
                {"~PRINCIPAL~"}, {"Dashboard"},
                {"~OPERACIONES~"}, {"Inventario"}, {"Ventas"}, {"Compras"}, {"Producción"},
                {"~ADMINISTRACIÓN~"}, {"Reportes"}, {"Configuración"},
                {"~SESIÓN~"}, {"Cerrar Sesión"}
        };

        String[][] menuItemsFiltrados = Stream.of(menuItemsBase)
                .filter(item -> {
                    String itemName = item[0];
                    if (itemName.startsWith("~")) return true;
                    Set<String> permissions = MENU_PERMISSIONS.get(itemName);
                    return permissions != null && permissions.contains(sesionUsuario.rol());
                })
                .toArray(String[][]::new);

        menuLateral.setMenuModel(menuItemsFiltrados);

        Map<String, String> panelNameMap = Map.of(
                "Dashboard", "DASHBOARD",
                "Inventario", "INVENTARIO",
                "Ventas", "VENTAS",
                "Compras", "COMPRAS",
                "Producción", "PRODUCCION",
                "Reportes", "REPORTES",
                "Configuración", "CONFIGURACION"
        );

        menuLateral.addEventoMenu((indice, subIndice, accion) -> {
            // Encontrar el nombre real del ítem de menú es clave
            String menuName = "";
            int itemRealIndex = 0;
            for (String[] menuItem : menuItemsFiltrados) {
                if (!menuItem[0].startsWith("~")) {
                    if (itemRealIndex == indice) {
                        menuName = menuItem[0];
                        break;
                    }
                    itemRealIndex++;
                }
            }

            if (panelNameMap.containsKey(menuName)) {
                mostrarPanel(panelNameMap.get(menuName));
            } else if (menuName.equals("Cerrar Sesión")) {
                accion.cancelar();
                if (JOptionPane.showConfirmDialog(this, "¿Está seguro de que desea cerrar la sesión?", "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            } else {
                accion.cancelar();
            }
        });
    }

    private void inicializarBusquedaUniversal() {
        txtBusquedaUniversal = new JTextField(25);
        txtBusquedaUniversal.putClientProperty("JTextField.placeholderText", "Buscar productos, clientes...");

        popupBusqueda = new JPopupMenu();
        popupBusqueda.setFocusable(false);
        popupBusqueda.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        listModelBusqueda = new DefaultListModel<>();
        listaResultados = new JList<>(listModelBusqueda);
        listaResultados.setCellRenderer(new UniversalSearchRenderer());

        popupBusqueda.add(new JScrollPane(listaResultados));

        searchTimer = new Timer(350, e -> ejecutarBusqueda());
        searchTimer.setRepeats(false);

        txtBusquedaUniversal.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { searchTimer.restart(); }
            @Override public void removeUpdate(DocumentEvent e) { searchTimer.restart(); }
            @Override public void changedUpdate(DocumentEvent e) { searchTimer.restart(); }
        });

        txtBusquedaUniversal.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (popupBusqueda.isVisible()) {
                    int nuevoIndice = listaResultados.getSelectedIndex();
                    if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                        nuevoIndice = Math.min(listModelBusqueda.getSize() - 1, nuevoIndice + 1);
                        listaResultados.setSelectedIndex(nuevoIndice);
                        listaResultados.ensureIndexIsVisible(nuevoIndice);
                    } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                        nuevoIndice = Math.max(0, nuevoIndice - 1);
                        listaResultados.setSelectedIndex(nuevoIndice);
                        listaResultados.ensureIndexIsVisible(nuevoIndice);
                    } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        navegarAResultadoSeleccionado();
                        e.consume();
                    }
                }
            }
        });

        listaResultados.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    navegarAResultadoSeleccionado();
                }
            }
        });
    }

    private void ejecutarBusqueda() {
        String termino = txtBusquedaUniversal.getText();
        if (termino.length() < 2) {
            popupBusqueda.setVisible(false);
            return;
        }

        ServicioBusquedaUniversal servicio = springContext.getBean(ServicioBusquedaUniversal.class);
        new SwingWorker<List<SearchResultDTO>, Void>() {
            @Override
            protected List<SearchResultDTO> doInBackground() throws Exception {
                return servicio.buscar(termino, 10);
            }

            @Override
            protected void done() {
                try {
                    List<SearchResultDTO> resultados = get();
                    listModelBusqueda.clear();
                    if (!resultados.isEmpty()) {
                        listModelBusqueda.addAll(resultados);
                        popupBusqueda.setPreferredSize(new Dimension(txtBusquedaUniversal.getWidth(), Math.min(resultados.size() * 50, 300)));
                        popupBusqueda.show(txtBusquedaUniversal, 0, txtBusquedaUniversal.getHeight());
                        txtBusquedaUniversal.requestFocusInWindow();
                    } else {
                        popupBusqueda.setVisible(false);
                    }
                } catch (Exception e) {
                    popupBusqueda.setVisible(false);
                    handleWorkerError(e, "Error en búsqueda universal");
                }
            }
        }.execute();
    }

    private void navegarAResultadoSeleccionado() {
        SearchResultDTO seleccionado = listaResultados.getSelectedValue();
        if (seleccionado == null) return;

        popupBusqueda.setVisible(false);
        txtBusquedaUniversal.setText(seleccionado.textoPrincipal());

        switch (seleccionado.tipo()) {
            case "PRODUCTO" -> {
                mostrarPanel("INVENTARIO");
                SwingUtilities.invokeLater(() -> {
                    InventarioPresenter presenter = springContext.getBean(PanelInventario.class).getPresenter();
                    presenter.buscarYSeleccionarProducto(seleccionado.id());
                });
            }
            case "CLIENTE" -> {
                mostrarPanel("CONFIGURACION");
                SwingUtilities.invokeLater(() -> {
                    PanelConfiguracion panelConfig = springContext.getBean(PanelConfiguracion.class);
                    panelConfig.seleccionarPestanaYCliente("Clientes", seleccionado.textoPrincipal(), seleccionado.id());
                });
            }
        }
        txtBusquedaUniversal.setText("");
    }

    private void setMenuCompleto(boolean completo) {
        menuLateral.setMenuCompleto(completo);
        actualizarIconoBotonMenu();
        // Dispara una revalidación del layout
        revalidate();
    }

    private void actualizarIconoBotonMenu() {
        try {
            String iconPath = getComponentOrientation().isLeftToRight()
                    ? (menuLateral.isMenuCompleto() ? "menu_left.svg" : "menu_right.svg")
                    : (menuLateral.isMenuCompleto() ? "menu_right.svg" : "menu_left.svg");
            botonMenu.setIcon(new FlatSVGIcon("iconos/menu/" + iconPath, 0.8f));
        } catch (NullPointerException e) {
            System.err.println("Error: No se encontraron los iconos del menú en la ruta /iconos/menu/. Verifica que los archivos 'menu_left.svg' y 'menu_right.svg' existan en esa ubicación en tus recursos.");
            // Opcional: poner un texto de fallback
            botonMenu.setText(menuLateral.isMenuCompleto() ? "<" : ">");
        }
    }

    /**
     * Centraliza el manejo de excepciones de los SwingWorkers, mostrando un
     * diálogo de error con un mensaje claro para el usuario.
     * @param e La excepción capturada.
     * @param context Un texto descriptivo de la operación que falló.
     */
    private void handleWorkerError(Exception e, String context) {
        String message = (e instanceof ExecutionException && e.getCause() != null) ? e.getCause().getMessage() : e.getMessage();
        JOptionPane.showMessageDialog(this, String.format("%s: %s", context, message), "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void mostrarPanel(String nombrePanel) {
        // Implementación de carga perezosa y cacheo
        if (!panelCache.containsKey(nombrePanel)) {
            JPanel nuevoPanel = crearPanelPorNombre(nombrePanel);
            panelCache.put(nombrePanel, nuevoPanel);
            panelCardLayout.add(nuevoPanel, nombrePanel);
        }
        ((CardLayout) panelCardLayout.getLayout()).show(panelCardLayout, nombrePanel);
        JPanel panelAMostrar = panelCache.get(nombrePanel);
        SwingUtilities.updateComponentTreeUI(panelAMostrar);
    }

    private boolean panelYaCargado(String nombrePanel) {
        for (Component comp : panelContenido.getComponents()) {
            if (comp.getName() != null && comp.getName().equals(nombrePanel)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Fábrica de paneles que implementa la carga perezosa (lazy loading).
     * Cuando se solicita un panel por primera vez, este metodo lo obtiene del contexto de Spring.
     * Spring se encarga de instanciar el bean y de inyectar todas sus dependencias
     * (como los presenters y servicios necesarios), devolviendo un objeto completamente inicializado.
     *
     * @param panelName El identificador del panel a crear (ej. "DASHBOARD").
     * @return Una nueva instancia del JPanel del módulo solicitado.
     */
    private JPanel crearPanelPorNombre(String panelName) {
        JPanel panel;
        switch (panelName) {
            case "DASHBOARD" -> panel = springContext.getBean(PanelDashboard.class);
            case "INVENTARIO" -> panel = springContext.getBean(PanelInventario.class);
            case "VENTAS" -> panel = springContext.getBean(PanelVentas.class);
            case "COMPRAS" -> panel = springContext.getBean(PanelCompras.class);
            case "PRODUCCION" -> panel = springContext.getBean(PanelProduccion.class);
            case "REPORTES" -> panel = springContext.getBean(PanelReportes.class);
            case "CONFIGURACION" -> panel = springContext.getBean(PanelConfiguracion.class);
            default -> {
                JLabel label = new JLabel("Panel '" + panelName + "' no implementado.");
                label.setHorizontalAlignment(SwingConstants.CENTER);
                panel = new JPanel(new BorderLayout());
                panel.add(label);
            }
        }
        panel.setName(panelName); // Asignar nombre para el cacheo
        return panel;
    }

    /**
     * LayoutManager personalizado para la ventana principal.
     */
    private class DisenioVentanaPrincipalLayout implements LayoutManager {

        @Override
        public void layoutContainer(Container parent) {
            synchronized (parent.getTreeLock()) {
                boolean ltr = parent.getComponentOrientation().isLeftToRight();
                Insets insets = UIScale.scale(parent.getInsets());
                int x = insets.left;
                int y = insets.top;
                int ancho = parent.getWidth() - (insets.left + insets.right);
                int alto = parent.getHeight() - (insets.top + insets.bottom);

                int anchoMenu = UIScale.scale(menuLateral.isMenuCompleto() ? menuLateral.getAnchoMaximoMenu() : menuLateral.getAnchoMinimoMenu());
                int menuX = ltr ? x : x + ancho - anchoMenu;
                menuLateral.setBounds(menuX, y, anchoMenu, alto);

                int anchoBotonMenu = botonMenu.getPreferredSize().width;
                int altoBotonMenu = botonMenu.getPreferredSize().height;
                int botonX;
                if (ltr) {
                    float offset = menuLateral.isMenuCompleto() ? 0.5f : 0.3f;
                    botonX = (int) (x + anchoMenu - (anchoBotonMenu * offset));
                } else {
                    float offset = menuLateral.isMenuCompleto() ? 0.5f : 0.7f;
                    botonX = (int) (menuX - (anchoBotonMenu * offset));
                }
                botonMenu.setBounds(botonX, UIScale.scale(30), anchoBotonMenu, altoBotonMenu);

                int gap = UIScale.scale(5);
                int cuerpoAncho = ancho - anchoMenu - gap;
                int cuerpoAlto = alto;
                int cuerpoX = ltr ? (x + anchoMenu + gap) : x;
                int cuerpoY = y;
                panelContenido.setBounds(cuerpoX, cuerpoY, cuerpoAncho, cuerpoAlto);
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