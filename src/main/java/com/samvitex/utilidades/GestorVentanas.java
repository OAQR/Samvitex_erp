    package com.samvitex.utilidades;

    import javax.swing.*;
    import java.awt.*;
    import java.awt.event.ActionEvent;
    import java.awt.event.ActionListener;
    import java.util.logging.Logger;

    /**
     * GestorVentanas es una utilidad para manejar transiciones suaves entre ventanas Swing.
     * Permite que una ventana nueva aparezca antes de que la ventana anterior se cierre,
     * con un retardo configurable.
     */
    public class GestorVentanas {

        private static final Logger LOGGER = Logger.getLogger(GestorVentanas.class.getName());
        private static final int RETRASO_CIERRE_MS = 500; // Retraso por defecto en milisegundos

        private GestorVentanas() {
            // Constructor privado para clase de utilidad estática
        }

        /**
         * Realiza un cambio visual de una ventana (emisor) a otra (receptor).
         * La ventana receptora se hace visible inmediatamente, y la ventana emisora
         * se cierra después de un pequeño retraso.
         *
         * @param emisor   La ventana (JFrame o JWindow) que se va a cerrar.
         * @param receptor La ventana (JFrame o JWindow) que se va a mostrar.
         */
        public static void cambiarVentanaConRetraso(Window emisor, Window receptor) {
            cambiarVentanaConRetraso(emisor, receptor, RETRASO_CIERRE_MS);
        }

        /**
         * Realiza un cambio visual de una ventana (emisor) a otra (receptor) con un retraso configurable.
         * La ventana receptora se hace visible inmediatamente, y la ventana emisora
         * se cierra después del retraso especificado.
         *
         * @param emisor   La ventana (JFrame o JWindow) que se va a cerrar.
         * @param receptor La ventana (JFrame o JWindow) que se va a mostrar.
         * @param retrasoMs El tiempo en milisegundos que se esperará antes de cerrar la ventana emisora.
         */
        public static void cambiarVentanaConRetraso(final Window emisor, final Window receptor, int retrasoMs) {
            if (receptor == null) {
                LOGGER.warning("La ventana receptora es nula. No se puede realizar la transición.");
                return;
            }
            receptor.setVisible(true); // Hacer la ventana receptora visible de inmediato

            if (emisor != null && emisor != receptor) { // Asegurarse de que hay un emisor diferente para cerrar
                Timer timer = new Timer(retrasoMs, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        emisor.dispose(); // Cerrar la ventana emisora
                        ((Timer) e.getSource()).stop(); // Detener el timer
                    }
                });
                timer.setRepeats(false); // Asegurar que el timer se ejecute solo una vez
                timer.start();
            } else if (emisor == receptor) {
                LOGGER.info("La ventana emisora y receptora son la misma. No se cerrará la ventana emisora.");
            }
        }
    }