package com.samvitex.ui.menu_lateral;

/**
 * Define el contrato para un listener que responde a eventos de selección en el menú lateral.
 * <p>
 * Las clases que implementen esta interfaz podrán registrarse en el componente {@code MenuLateral}
 * para ser notificadas cuando un usuario haga clic en un ítem del menú principal o en un
 * sub-ítem de un menú desplegable.
 *
 * @see com.samvitex.ui.menu_lateral.MenuLateral
 * @see AccionMenu
 */
@FunctionalInterface
public interface EventoMenu {

    /**
     * Se invoca cuando un ítem de menú o sub-menú es seleccionado.
     *
     * @param indice El índice del menú principal. Cada ítem de menú principal (que no es un título)
     *               tiene un índice único comenzando desde 0.
     * @param subIndice El índice del sub-menú. Si el ítem principal no tiene sub-menús, este valor
     *                  será 0. Para los sub-menús, el conteo comienza desde 1 (el 0 está reservado
     *                  para el ítem principal).
     * @param accion Una instancia de {@link AccionMenu} que permite al listener cancelar el
     *               comportamiento por defecto de la selección (ej. evitar que el ítem quede
     *               marcado como seleccionado).
     */
    void menuSeleccionado(int indice, int subIndice, AccionMenu accion);
}