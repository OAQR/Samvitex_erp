package com.samvitex.ui.menu_lateral.modo;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.icons.FlatAbstractIcon;
import com.formdev.flatlaf.util.ColorFunctions;
import com.formdev.flatlaf.util.LoggingFacade;
import com.formdev.flatlaf.util.UIScale;
import com.samvitex.ui.menu_lateral.MenuLateral;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Un componente que permite al usuario seleccionar el "color de acento" de la aplicación.
 * Muestra una paleta de colores en modo completo y un botón que despliega la paleta
 * en un popup en modo colapsado.
 */
public class SelectorColorAcento extends JPanel {

    private final MenuLateral menu;
    private final JPopupMenu popup = new JPopupMenu();
    private boolean menuCompleto = true;
    private final Map<String, Color> coloresAcento = new HashMap<>();

    private final String[] clavesColorAcento = {
            "App.accent.default", "App.accent.blue", "App.accent.purple", "App.accent.red",
            "App.accent.orange", "App.accent.yellow", "App.accent.green",
    };

    private JToolBar barraHerramientas;
    private JToggleButton botonSeleccionado;

    public SelectorColorAcento(MenuLateral menu) {
        this.menu = menu;
        inicializar();
    }

    private void inicializar() {
        setLayout(new BorderLayout());
        barraHerramientas = new JToolBar();
        add(barraHerramientas);

        putClientProperty(FlatClientProperties.STYLE, "background:$Menu.background;");
        barraHerramientas.putClientProperty(FlatClientProperties.STYLE, "background:$Menu.background;");
        popup.putClientProperty(FlatClientProperties.STYLE, "background:$Menu.background; borderColor:$Menu.background;");

        cargarColoresAcento();

        ButtonGroup grupo = new ButtonGroup();
        botonSeleccionado = new JToggleButton(new IconoColorAcento(clavesColorAcento[0]));
        botonSeleccionado.addActionListener(this::mostrarPopup);

        for (String claveColor : clavesColorAcento) {
            JToggleButton boton = new JToggleButton(new IconoColorAcento(claveColor));
            Color colorActual = UIManager.getColor("Component.accentColor");
            Color colorBoton = coloresAcento.get(claveColor);

            if (colorActual != null && colorBoton != null && colorActual.getRGB() == colorBoton.getRGB()) {
                boton.setSelected(true);
                botonSeleccionado.setIcon(new IconoColorAcento(claveColor));
            }

            boton.addActionListener(e -> cambiarColorAcento(claveColor));
            grupo.add(boton);
            barraHerramientas.add(boton);
        }
    }

    private void cargarColoresAcento() {
        // Estos son los colores definidos en tus archivos .properties.
        // Podríamos leerlos de los archivos, pero hardcodearlos aquí es más simple y seguro.
        coloresAcento.put("App.accent.default", Color.decode("#2675BF")); // De FlatLightLaf.properties
        coloresAcento.put("App.accent.blue", Color.decode("#007AFF"));
        coloresAcento.put("App.accent.purple", Color.decode("#BF5AF2"));
        coloresAcento.put("App.accent.red", Color.decode("#FF3B30"));
        coloresAcento.put("App.accent.orange", Color.decode("#FF9500"));
        coloresAcento.put("App.accent.yellow", Color.decode("#FFCC00"));
        coloresAcento.put("App.accent.green", Color.decode("#28CD41"));
    }

    private void cambiarColorAcento(String claveColor) {
        if (popup.isVisible()) {
            popup.setVisible(false);
        }

        Color color = coloresAcento.get(claveColor);
        if (color == null) {
            System.err.println("Color no encontrado para la clave: " + claveColor);
            return;
        }

        botonSeleccionado.setIcon(new IconoColorAcento(claveColor));
        Class<? extends LookAndFeel> lafClass = UIManager.getLookAndFeel().getClass();
        try {
            FlatLaf.setGlobalExtraDefaults(Collections.singletonMap("@accentColor", aHexCode(color)));
            FlatLaf.setup(lafClass.newInstance());
            FlatLaf.updateUI();
        } catch (InstantiationException | IllegalAccessException ex) {
            LoggingFacade.INSTANCE.logSevere(null, ex);
        }
    }

    public void setMenuCompleto(boolean menuCompleto) {
        this.menuCompleto = menuCompleto;
        removeAll();
        if (menuCompleto) {
            add(barraHerramientas);
            popup.remove(barraHerramientas);
        } else {
            add(botonSeleccionado);
            popup.add(barraHerramientas);
        }
        SwingUtilities.updateComponentTreeUI(this);
    }

    private void mostrarPopup(ActionEvent e) {
        int y = (botonSeleccionado.getPreferredSize().height - (barraHerramientas.getPreferredSize().height + UIScale.scale(10))) / 2;
        if (menu.getComponentOrientation().isLeftToRight()) {
            popup.show(SelectorColorAcento.this, getWidth() + UIScale.scale(4), y);
        } else {
            int px = barraHerramientas.getPreferredSize().width + UIScale.scale(5);
            popup.show(SelectorColorAcento.this, -px, y);
        }
        SwingUtilities.updateComponentTreeUI(popup);
    }

    private String aHexCode(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    /**
     * Un icono personalizado que dibuja un cuadrado redondeado del color de acento especificado.
     */
    private class IconoColorAcento extends FlatAbstractIcon {
        private final String claveColor;

        public IconoColorAcento(String claveColor) {
            super(16, 16, null);
            this.claveColor = claveColor;
        }

        @Override
        protected void paintIcon(Component c, Graphics2D g) {
            Color colorAcento = UIManager.getColor(claveColor);
            if (colorAcento == null) {
                colorAcento = Color.lightGray;
            } else if (!c.isEnabled()) {
                colorAcento = FlatLaf.isLafDark() ? ColorFunctions.shade(colorAcento, 0.5f) : ColorFunctions.tint(colorAcento, 0.6f);
            }
            g.setColor(colorAcento);
            g.fillRoundRect(1, 1, width - 2, height - 2, 5, 5);
        }
    }
}