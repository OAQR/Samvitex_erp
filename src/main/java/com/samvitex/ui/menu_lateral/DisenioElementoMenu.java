package com.samvitex.ui.menu_lateral;

import com.formdev.flatlaf.util.UIScale;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

/**
 * Un LayoutManager personalizado para organizar los componentes dentro de un {@link ElementoMenu}.
 * <p>
 * Esta clase es la responsable de calcular el tamaño y la posición del ítem principal y de sus
 * sub-ítems. Su lógica es fundamental para la animación de despliegue/repliegue, ya que el método
 * {@link #preferredLayoutSize(Container)} recalcula la altura total del componente basándose
 * en el progreso de la animación (la variable {@code animate} del {@code ElementoMenu}).
 */
public class DisenioElementoMenu implements LayoutManager {

    /**
     * Calcula el tamaño preferido para el contenedor del {@link ElementoMenu}.
     * <p>
     * La altura se calcula dinámicamente:
     * <ul>
     *     <li>Siempre incluye la altura del ítem principal.</li>
     *     <li>Si hay sub-ítems, calcula la altura total que ocuparían y la multiplica por el
     *     progreso de la animación (un valor entre 0.0 y 1.0).</li>
     * </ul>
     * Esto permite que el contenedor crezca y se encoja suavemente durante la animación.
     *
     * @param parent el contenedor (el {@code ElementoMenu}) cuyo tamaño se va a calcular.
     * @return una {@link Dimension} con el tamaño preferido.
     */
    @Override
    public Dimension preferredLayoutSize(Container parent) {
        synchronized (parent.getTreeLock()) {
            Insets insets = parent.getInsets();
            int width = parent.getWidth();
            int height = insets.top + insets.bottom;
            int totalComponentes = parent.getComponentCount();

            if (totalComponentes > 0) {
                // La altura base es siempre la del primer componente (el ítem principal).
                height += UIScale.scale(((ElementoMenu) parent).getAlturaElementoPrincipal());

                if (totalComponentes > 1) {
                    // Si hay sub-ítems, calculamos su altura total.
                    int alturaSubmenu = UIScale.scale(((ElementoMenu) parent).getEspacioSuperiorSubmenu()) +
                            UIScale.scale(((ElementoMenu) parent).getEspacioInferiorSubmenu());

                    for (int i = 1; i < totalComponentes; i++) {
                        alturaSubmenu += UIScale.scale(((ElementoMenu) parent).getAlturaSubElemento());
                    }
                    // La altura de los sub-menús se añade multiplicada por el progreso de la animación.
                    height += (int) (alturaSubmenu * ((ElementoMenu) parent).getProgresoAnimacion());
                }
            }
            return new Dimension(width, height);
        }
    }

    /**
     * Posiciona los componentes (ítem principal y sub-ítems) dentro del contenedor.
     *
     * @param parent el contenedor (el {@code ElementoMenu}) cuyos componentes se van a posicionar.
     */
    @Override
    public void layoutContainer(Container parent) {
        synchronized (parent.getTreeLock()) {
            ElementoMenu elementoMenu = (ElementoMenu) parent;
            boolean ltr = parent.getComponentOrientation().isLeftToRight();
            Insets insets = parent.getInsets();
            int x = insets.left;
            int y = insets.top;
            int width = parent.getWidth() - (insets.left + insets.right);
            int totalComponentes = parent.getComponentCount();

            for (int i = 0; i < totalComponentes; i++) {
                Component com = parent.getComponent(i);
                if (com.isVisible()) {
                    if (i == 0) { // Es el ítem principal
                        int alturaElementoPrincipal = UIScale.scale(elementoMenu.getAlturaElementoPrincipal());
                        com.setBounds(x, y, width, alturaElementoPrincipal);
                        y += alturaElementoPrincipal + UIScale.scale(elementoMenu.getEspacioSuperiorSubmenu());
                    } else { // Son sub-ítems
                        int sangriaSubmenu = UIScale.scale(elementoMenu.getSangriaSubmenu());
                        int subMenuX = ltr ? sangriaSubmenu : 0;
                        int alturaSubElemento = UIScale.scale(elementoMenu.getAlturaSubElemento());
                        com.setBounds(x + subMenuX, y, width - sangriaSubmenu, alturaSubElemento);
                        y += alturaSubElemento;
                    }
                }
            }
        }
    }

    @Override
    public void addLayoutComponent(String name, Component comp) {
        // No se utiliza en este layout manager.
    }

    @Override
    public void removeLayoutComponent(Component comp) {
        // No se utiliza en este layout manager.
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
        synchronized (parent.getTreeLock()) {
            return new Dimension(0, 0);
        }
    }
}