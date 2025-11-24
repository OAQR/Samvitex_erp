package com.samvitex.utilidades;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * `AnimadorComponentes` provee métodos estáticos para aplicar animaciones
 * a componentes Swing, mejorando la experiencia de usuario (UX).
 */
public final class AnimadorComponentes {

    /**
     * Constructor privado para prevenir la instanciación.
     */
    private AnimadorComponentes() {
    }

    /**
     * Aplica una animación de "sacudida" horizontal a un componente.
     * Es útil para indicar un error de validación de forma visual.
     * También emite un sonido de "beep" del sistema.
     *
     * @param componente El JComponent que se va a animar.
     */
    public static void agitar(JComponent componente) {
        Toolkit.getDefaultToolkit().beep();
        final Point puntoInicial = componente.getLocation();
        final int retraso = 10;
        final int numIteraciones = 6;
        final int distancia = 5;

        Timer timer = new Timer(retraso, new ActionListener() {
            private int iteracionActual = 0;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (iteracionActual >= numIteraciones * 2) {
                    componente.setLocation(puntoInicial);
                    ((Timer) e.getSource()).stop();
                    return;
                }

                int offsetX = (iteracionActual % 2 == 0) ? distancia : -distancia;
                componente.setLocation(puntoInicial.x + offsetX, puntoInicial.y);
                iteracionActual++;
            }
        });
        timer.start();
    }
}
