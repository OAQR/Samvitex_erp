package com.samvitex.ui.menu_lateral;

/**
 * Representa una acción que puede ser cancelada dentro del contexto de un evento de menú.
 * <p>
 * Cuando se dispara un {@link EventoMenu}, una instancia de esta clase se pasa al listener.
 * El listener puede invocar al metodo {@link #cancelar()} para indicar al menú que no
 * debe proceder con su comportamiento por defecto (como cambiar la selección visual del ítem).
 * Esto es útil para manejar casos especiales, como ítems que no deben permanecer seleccionados
 * (ej. un botón de "Cerrar Sesión").
 *
 * @see EventoMenu
 */
public class AccionMenu {

    /**
     * Bandera para indicar si la acción ha sido cancelada.
     */
    private boolean cancelada = false;

    /**
     * Verifica si la acción ha sido marcada como cancelada.
     * El menú utiliza este metodo para decidir si debe actualizar su estado de selección.
     *
     * @return {@code true} si la acción fue cancelada, {@code false} en caso contrario.
     */
    protected boolean haSidoCancelada() {
        return cancelada;
    }

    /**
     * Marca la acción actual como cancelada.
     * Un listener de eventos de menú debe llamar a este metodo si desea prevenir
     * la acción de selección por defecto del menú.
     */
    public void cancelar() {
        this.cancelada = true;
    }
}