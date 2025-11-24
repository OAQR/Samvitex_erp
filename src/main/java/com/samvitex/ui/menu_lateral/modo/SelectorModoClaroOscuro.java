package com.samvitex.ui.menu_lateral.modo;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.extras.FlatAnimatedLafChange;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 * Un componente de UI que permite al usuario cambiar entre el tema claro y oscuro de la aplicación.
 * Adapta su apariencia dependiendo de si el menú lateral está en modo completo o colapsado.
 */
public class SelectorModoClaroOscuro extends JPanel {

    private boolean menuCompleto = true;
    private JButton botonClaro;
    private JButton botonOscuro;
    private JButton botonClaroOscuro; // Botón único para modo colapsado

    public SelectorModoClaroOscuro() {
        inicializar();
    }

    private void inicializar() {
        setBorder(new EmptyBorder(2, 2, 2, 2));
        setLayout(new DisenioModoClaroOscuroLayout());
        putClientProperty(FlatClientProperties.STYLE, ""
                + "arc:999;"
                + "background:$Menu.lightdark.background");

        // Creación de botones con sus iconos
        botonClaro = new JButton("Claro", new FlatSVGIcon("iconos/menu/light.svg"));
        botonOscuro = new JButton("Oscuro", new FlatSVGIcon("iconos/menu/dark.svg"));
        botonClaroOscuro = new JButton();

        // Estilos y listeners
        configurarBoton(botonClaro, false);
        configurarBoton(botonOscuro, true);
        configurarBotonUnico(botonClaroOscuro);

        verificarEstiloActual();

        add(botonClaro);
        add(botonOscuro);
        add(botonClaroOscuro);
    }

    private void configurarBoton(JButton boton, boolean esModoOscuro) {
        boton.addActionListener((ActionEvent e) -> cambiarModo(esModoOscuro));
    }

    private void configurarBotonUnico(JButton boton) {
        boton.putClientProperty(FlatClientProperties.STYLE, ""
                + "arc:999;"
                + "background:$Menu.lightdark.button.background;"
                + "foreground:$Menu.foreground;"
                + "focusWidth:0;"
                + "borderWidth:0;"
                + "innerFocusWidth:0");
        boton.addActionListener((ActionEvent e) -> cambiarModo(!FlatLaf.isLafDark()));
    }

    /**
     * Cambia el Look and Feel de la aplicación de forma animada.
     * @param esOscuro true para cambiar a tema oscuro, false para tema claro.
     */
    private void cambiarModo(boolean esOscuro) {
        if (FlatLaf.isLafDark() != esOscuro) {
            EventQueue.invokeLater(() -> {
                FlatLaf.registerCustomDefaultsSource("com.samvitex.themes");
                FlatAnimatedLafChange.showSnapshot();
                if (esOscuro) {
                    FlatMacDarkLaf.setup();
                } else {
                    FlatMacLightLaf.setup();
                }
                FlatLaf.updateUI();
                verificarEstiloActual();
                FlatAnimatedLafChange.hideSnapshotWithAnimation();
            });
        }
    }

    /**
     * Actualiza el estado visual de los botones para reflejar el tema actual.
     */
    private void verificarEstiloActual() {
        boolean esOscuro = FlatLaf.isLafDark();
        aplicarEstiloBoton(botonClaro, !esOscuro);
        aplicarEstiloBoton(botonOscuro, esOscuro);
        if (esOscuro) {
            botonClaroOscuro.setIcon(new FlatSVGIcon("iconos/menu/dark.svg"));
        } else {
            botonClaroOscuro.setIcon(new FlatSVGIcon("iconos/menu/light.svg"));
        }
    }

    private void aplicarEstiloBoton(JButton boton, boolean seleccionado) {
        String estiloBase = "arc:999; focusWidth:0; borderWidth:0; innerFocusWidth:0;";
        if (seleccionado) {
            boton.putClientProperty(FlatClientProperties.STYLE, estiloBase
                    + "background:$Menu.lightdark.button.background;"
                    + "foreground:$Menu.foreground;");
        } else {
            boton.putClientProperty(FlatClientProperties.STYLE, estiloBase + "background:null;");
        }
    }

    public void setMenuCompleto(boolean menuCompleto) {
        this.menuCompleto = menuCompleto;
        botonClaro.setVisible(menuCompleto);
        botonOscuro.setVisible(menuCompleto);
        botonClaroOscuro.setVisible(!menuCompleto);
    }

    /**
     * LayoutManager interno que posiciona los botones según el modo del menú.
     */
    private class DisenioModoClaroOscuroLayout implements LayoutManager {
        @Override
        public void layoutContainer(Container parent) {
            synchronized (parent.getTreeLock()) {
                Insets insets = parent.getInsets();
                int x = insets.left;
                int y = insets.top;
                int gap = 5;
                int ancho = parent.getWidth() - (insets.left + insets.right);
                int alto = parent.getHeight() - (insets.top + insets.bottom);
                if (menuCompleto) {
                    int anchoBoton = (ancho - gap) / 2;
                    botonClaro.setBounds(x, y, anchoBoton, alto);
                    botonOscuro.setBounds(x + anchoBoton + gap, y, anchoBoton, alto);
                } else {
                    botonClaroOscuro.setBounds(x, y, ancho, alto);
                }
            }
        }

        @Override public void addLayoutComponent(String name, Component comp) {}
        @Override public void removeLayoutComponent(Component comp) {}
        @Override public Dimension preferredLayoutSize(Container parent) {
            synchronized (parent.getTreeLock()) {
                return new Dimension(5, botonOscuro.getPreferredSize().height + (menuCompleto ? 0 : 5));
            }
        }
        @Override public Dimension minimumLayoutSize(Container parent) {
            synchronized (parent.getTreeLock()) { return new Dimension(0, 0); }
        }
    }
}