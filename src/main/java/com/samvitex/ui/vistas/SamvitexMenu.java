package com.samvitex.ui.vistas;

import com.samvitex.ui.theme.SamvitexButton;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Panel de menú lateral para la navegación principal de la aplicación.
 * Muestra los módulos disponibles según el rol del usuario.
 */
public class SamvitexMenu extends JPanel {

    // Define los ítems del menú y sus permisos
    private static final Map<String, String> NAV_ITEMS = new LinkedHashMap<>();
    private static final Map<String, Set<String>> NAV_PERMISSIONS = new HashMap<>();

    static {
        NAV_ITEMS.put("DASHBOARD", "Dashboard");
        NAV_ITEMS.put("INVENTARIO", "Inventario");
        NAV_ITEMS.put("VENTAS", "Ventas");
        NAV_ITEMS.put("COMPRAS", "Compras");
        NAV_ITEMS.put("PRODUCCION", "Producción");
        NAV_ITEMS.put("REPORTES", "Reportes");
        NAV_ITEMS.put("CONFIGURACION", "Configuración");

        NAV_PERMISSIONS.put("DASHBOARD", Set.of("ADMINISTRADOR", "VENDEDOR", "ALMACENISTA"));
        NAV_PERMISSIONS.put("INVENTARIO", Set.of("ADMINISTRADOR", "ALMACENISTA"));
        NAV_PERMISSIONS.put("VENTAS", Set.of("ADMINISTRADOR", "VENDEDOR"));
        NAV_PERMISSIONS.put("COMPRAS", Set.of("ADMINISTRADOR", "ALMACENISTA"));
        NAV_PERMISSIONS.put("PRODUCCION", Set.of("ADMINISTRADOR", "ALMACENISTA"));
        NAV_PERMISSIONS.put("REPORTES", Set.of("ADMINISTRADOR"));
        NAV_PERMISSIONS.put("CONFIGURACION", Set.of("ADMINISTRADOR"));
    }

    public SamvitexMenu() {
        setOpaque(false);
        setLayout(new MigLayout("wrap, fillx, insets 10 5 10 5", "[fill]", "[]"));
        // Aquí podrías añadir un logo o título
        add(new JLabel("<html><b>SAMVITEX</b></html>"), "align center, h 40!, wrap, gapbottom 10");
    }

    /**
     * Construye los botones del menú basándose en el rol del usuario.
     * @param rolUsuario El rol del usuario actual.
     * @param onMenuClick La acción a ejecutar cuando se hace clic en un botón.
     */
    public void inicializarMenu(String rolUsuario, Consumer<String> onMenuClick) {
        NAV_ITEMS.forEach((panelId, buttonText) -> {
            if (NAV_PERMISSIONS.getOrDefault(panelId, Set.of()).contains(rolUsuario)) {
                JButton button = new SamvitexButton(buttonText, SamvitexButton.ButtonType.SECONDARY);
                button.setHorizontalAlignment(SwingConstants.LEFT);
                button.addActionListener(e -> onMenuClick.accept(panelId));
                add(button, "growx, h 35!");
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.WHITE); // Fondo blanco para el menú
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
        g2.dispose();
        super.paintComponent(g);
    }
}