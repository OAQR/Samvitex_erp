package com.samvitex.ui.menu_lateral;

import com.formdev.flatlaf.util.UIScale;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import javax.swing.JLabel;

/**
 * Un LayoutManager personalizado para organizar los componentes (títulos y ElementoMenu)
 * dentro del panel principal del {@link MenuLateral}.
 * <p>
 * Su función es apilar verticalmente cada componente. Calcula la altura preferida total del contenido,
 * lo cual es esencial para que el {@link javax.swing.JScrollPane} que lo contiene funcione correctamente.
 * También gestiona la visibilidad y el espaciado de los títulos de sección dependiendo de si el
 * menú está en modo completo o colapsado.
 */
public class DisenioPanelMenu implements LayoutManager {

    private final MenuLateral menu;

    /**
     * Construye un nuevo LayoutManager para el panel de menú.
     *
     * @param menu La instancia del {@link MenuLateral} principal. Se necesita para consultar
     *             su estado (ej. si es completo o colapsado).
     */
    public DisenioPanelMenu(MenuLateral menu) {
        this.menu = menu;
    }

    /**
     * Calcula el tamaño preferido del panel que contiene todos los elementos del menú.
     * La altura total es la suma de las alturas preferidas de todos sus componentes visibles.
     */
    @Override
    public Dimension preferredLayoutSize(Container parent) {
        synchronized (parent.getTreeLock()) {
            Insets insets = parent.getInsets();
            int alturaTotal = insets.top + insets.bottom;

            for (Component com : parent.getComponents()) {
                if (com.isVisible()) {
                    if (com instanceof JLabel) {
                        // Los títulos de sección solo ocupan espacio si el menú está completo
                        // o si la configuración permite mostrarlos en modo mínimo.
                        if (menu.isMenuCompleto() || !menu.isOcultarTituloEnModoMinimo()) {
                            alturaTotal += com.getPreferredSize().height + (UIScale.scale(menu.getEspacioVerticalTitulo()) * 2);
                        }
                    } else {
                        alturaTotal += com.getPreferredSize().height;
                    }
                }
            }
            return new Dimension(5, alturaTotal); // El ancho no es relevante, se ajustará.
        }
    }

    /**
     * Posiciona cada componente (título o ElementoMenu) verticalmente dentro del panel.
     */
    @Override
    public void layoutContainer(Container parent) {
        synchronized (parent.getTreeLock()) {
            Insets insets = parent.getInsets();
            int x = insets.left;
            int y = insets.top;
            int ancho = parent.getWidth() - (insets.left + insets.right);

            for (Component com : parent.getComponents()) {
                if (com.isVisible()) {
                    int alturaComponente = com.getPreferredSize().height;
                    if (com instanceof JLabel label) {
                        if (menu.isMenuCompleto() || !menu.isOcultarTituloEnModoMinimo()) {
                            int sangriaTitulo = UIScale.scale(menu.getSangriaIzquierdaTitulo());
                            int espacioVertical = UIScale.scale(menu.getEspacioVerticalTitulo());
                            int anchoTitulo = ancho - sangriaTitulo;
                            y += espacioVertical;
                            label.setBounds(x + sangriaTitulo, y, anchoTitulo, alturaComponente);
                            y += alturaComponente + espacioVertical;
                        } else {
                            // Si el título debe ocultarse, se le da tamaño cero.
                            label.setBounds(0, 0, 0, 0);
                        }
                    } else {
                        // Un ElementoMenu ocupa todo el ancho disponible.
                        com.setBounds(x, y, ancho, alturaComponente);
                        y += alturaComponente;
                    }
                }
            }
        }
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