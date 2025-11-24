package com.samvitex.ui.menu_lateral;

import com.formdev.flatlaf.util.Animator;

import java.util.HashMap;
import java.util.Map;

/**
 * Clase de utilidad para gestionar las animaciones de despliegue y repliegue de los sub-menús.
 * <p>
 * Utiliza la clase {@link Animator} de FlatLaf para crear transiciones suaves.
 * Mantiene un registro estático de las animaciones en curso para un {@link ElementoMenu}
 * determinado, permitiendo detener una animación si se solicita una nueva antes de que
 * la anterior haya terminado.
 */
public final class AnimacionMenu {

    /**
     * Mapa estático para mantener un seguimiento de las animaciones activas por cada ElementoMenu.
     * La clave es el {@link ElementoMenu} que se está animando y el valor es su {@link Animator}.
     */
    private static final Map<ElementoMenu, Animator> animadoresActivos = new HashMap<>();

    /**
     * Constructor privado para evitar la instanciación de esta clase de utilidad.
     */
    private AnimacionMenu() {
    }

    /**
     * Inicia una animación para mostrar u ocultar los sub-menús de un {@link ElementoMenu}.
     *
     * @param elemento El {@code ElementoMenu} cuyos sub-menús se van a animar.
     * @param mostrar  {@code true} para desplegar (mostrar) los sub-menús, {@code false} para replegar (ocultar).
     */
    public static void animar(ElementoMenu elemento, boolean mostrar) {
        // Si ya hay una animación en curso para este elemento, la detenemos.
        if (animadoresActivos.containsKey(elemento) && animadoresActivos.get(elemento).isRunning()) {
            animadoresActivos.get(elemento).stop();
        }

        // Establece el estado objetivo final de la visibilidad del menú.
        elemento.setMenuDesplegado(mostrar);

        // Crea un nuevo Animator con una duración de 400ms.
        Animator animator = new Animator(400, new Animator.TimingTarget() {
            /**
             * Este metodo se llama en cada "frame" de la animación.
             * @param fraccion Un valor que va de 0.0 a 1.0 indicando el progreso de la animación.
             */
            @Override
            public void timingEvent(float fraccion) {
                // Si estamos mostrando, el progreso va de 0 a 1.
                // Si estamos ocultando, el progreso va de 1 a 0.
                float progreso = mostrar ? fraccion : 1f - fraccion;
                elemento.setProgresoAnimacion(progreso);
                elemento.revalidate(); // Revalida el layout para aplicar los cambios de tamaño.
            }

            /**
             * Se llama cuando la animación ha terminado.
             */
            @Override
            public void end() {
                // Elimina el animator del mapa de activos.
                animadoresActivos.remove(elemento);
            }
        });

        animator.setResolution(1); // Frecuencia de actualización. 1ms es muy suave.
        // Interpolador que crea un efecto de "ease-out" (desaceleración al final).
        animator.setInterpolator(fraccion -> (float) (1 - Math.pow(1 - fraccion, 3)));
        animator.start();

        // Registra el nuevo animator como activo para este elemento.
        animadoresActivos.put(elemento, animator);
    }
}