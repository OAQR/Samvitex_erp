package com.samvitex.ui.vistas;

import com.formdev.flatlaf.FlatClientProperties;
import com.samvitex.modelos.dto.SesionUsuario;
import com.samvitex.ui.paneles.*;
import net.miginfocom.swing.MigLayout;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * El contenedor principal de la aplicación post-login.
 * <p>
 * Esta clase actúa como el "shell" o marco de la aplicación, proporcionando la
 * estructura visual fundamental que consiste en un menú de navegación lateral y un
 * área de contenido principal. Utiliza un CardLayout para cambiar eficientemente
 * entre los diferentes módulos funcionales (paneles).
 * <p>
 * Implementa un patrón de carga perezosa (lazy loading) para los paneles,
 * lo que mejora el rendimiento de inicio.
 */
@Component
public class ApplicationShell extends JFrame {

    private final ApplicationContext springContext;
    private MigLayout layout;
    private SamvitexMenu menu;
    private HeaderPanel header;
    private JPanel mainPanel;
    private CardLayout mainCardLayout;
    private final Map<String, JPanel> panelCache = new HashMap<>();

    /**
     * Construye el Application Shell.
     *
     * @param context El contexto de Spring para la inyección de dependencias en los paneles.
     */
    public ApplicationShell(ApplicationContext context) {
        this.springContext = context;
        init();
    }

    private void init() {
        setTitle("SamVitex - Sistema de Gestión");
        setUndecorated(true); // Estilo de ventana sin bordes
        setSize(new Dimension(1366, 768));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Permite que el contenido se dibuje bajo la barra de título (si el L&F lo soporta)
        getRootPane().putClientProperty(FlatClientProperties.FULL_WINDOW_CONTENT, true);

        // Layout principal del JFrame
        layout = new MigLayout("fill", "0[230!]0[100%, fill]0", "0[50!]0[100%, fill]0");
        JPanel background = new JPanel(layout);
        background.setBackground(new Color(245, 245, 245)); // Un color de fondo base

        menu = new SamvitexMenu();
        header = new HeaderPanel();
        mainCardLayout = new CardLayout();
        mainPanel = new JPanel(mainCardLayout);
        mainPanel.setOpaque(false);

        // Añadir componentes al panel de fondo
        background.add(menu, "w 230!, spany 2, growy"); // Ocupa 2 filas verticalmente
        background.add(header, "h 50!, growx, wrap");
        background.add(mainPanel, "grow, wrap");

        setContentPane(background);
    }

    /**
     * Muestra la ventana y configura la sesión del usuario.
     * @param sesion La información de la sesión del usuario actual.
     */
    public void mostrar(SesionUsuario sesion) {
        menu.inicializarMenu(sesion.rol(), this::mostrarPanel); // Configura el menú basado en rol
        header.configurarUsuario(sesion.nombreCompleto(), sesion.rol());

        // Carga el panel inicial
        mostrarPanel("DASHBOARD");

        setVisible(true);
    }

    /**
     * Cambia al panel solicitado, creándolo si es la primera vez que se accede (lazy loading).
     * @param panelId El identificador del panel a mostrar (ej. "INVENTARIO").
     */
    private void mostrarPanel(String panelId) {
        if (!panelCache.containsKey(panelId)) {
            JPanel panel = crearPanelPorId(panelId);
            if (panel != null) {
                panelCache.put(panelId, panel);
                mainPanel.add(panel, panelId);
            } else {
                // Panel por defecto si algo falla
                JPanel errorPanel = new JPanel(new GridBagLayout());
                errorPanel.add(new JLabel("Módulo '" + panelId + "' no implementado."));
                mainPanel.add(errorPanel, panelId);
            }
        }
        mainCardLayout.show(mainPanel, panelId);
    }

    /**
     * Fábrica de paneles. Crea una instancia del panel solicitado obteniendo las
     * dependencias necesarias desde el contexto de Spring.
     *
     * @param panelId El identificador del panel.
     * @return Una nueva instancia del JPanel del módulo.
     */
    private JPanel crearPanelPorId(String panelId) {
        return switch (panelId) {
            case "DASHBOARD" -> springContext.getBean(PanelDashboard.class);
            case "INVENTARIO" -> springContext.getBean(PanelInventario.class);
            case "VENTAS" -> springContext.getBean(PanelVentas.class);
            case "COMPRAS" -> springContext.getBean(PanelCompras.class);
            case "PRODUCCION" -> springContext.getBean(PanelProduccion.class);
            case "REPORTES" -> springContext.getBean(PanelReportes.class);
            case "CONFIGURACION" -> springContext.getBean(PanelConfiguracion.class);
            default -> {
                // Panel por defecto si algo falla
                JPanel errorPanel = new JPanel(new GridBagLayout());
                errorPanel.add(new JLabel("Módulo '" + panelId + "' no implementado."));
                yield errorPanel; // 'yield' es la forma moderna de devolver un valor en un switch expression
            }
        };
    }
}