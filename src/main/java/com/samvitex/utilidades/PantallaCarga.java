package com.samvitex.utilidades;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.util.Animator;
import com.formdev.flatlaf.util.CubicBezierEasing;
import com.formdev.flatlaf.util.UIScale;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * `PantallaCarga` es una ventana de carga (Splash Screen) que se muestra al iniciar la aplicación.
 * Presenta el logo de la empresa y una barra de progreso animada, con una animación de desvanecimiento
 * al finalizar la carga.
 */
public class PantallaCarga extends JWindow {

    private static final Logger LOGGER = Logger.getLogger(PantallaCarga.class.getName());

    private FlatSVGIcon iconoLogo;
    private Animator animadorProgreso;
    private Animator animadorDesvanecimiento;

    private float progresoCarga = 0f;
    private float progresoDesvanecimiento = 0f;

    // Indica si la pantalla está en la fase de carga (mostrando barra de progreso)
    private boolean esFaseCarga = true;

    /**
     * Constructor de la `PantallaCarga`.
     * Inicializa la interfaz y carga los recursos necesarios.
     */
    public PantallaCarga() {
        inicializar();
    }

    /**
     * Inicializa la configuración de la ventana y el panel de dibujo.
     * Carga el logo SVG y configura el tamaño y la posición.
     */
    private void inicializar() {
        //setSize(UIScale.scale(new Dimension(1280, 720))); // Establece el tamaño de la ventana
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(screenSize);
        setLocationRelativeTo(null); // Centra la ventana en la pantalla

        try {
            // Intenta cargar el icono SVG desde los recursos
            iconoLogo = new FlatSVGIcon(getClass().getResource("/imagenes/samvitex_logo_blanco.svg"));
            if (iconoLogo == null) {
                LOGGER.log(Level.WARNING, "El logo SVG 'samvitex_logo_blanco.svg' no pudo ser cargado.");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al cargar el logo SVG: " + e.getMessage(), e);
            iconoLogo = null; // Asegura que el logo sea nulo si hay un error
        }

        // Panel personalizado para dibujar el logo y la barra de progreso
        JPanel panelDibujo = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                // Rellena el fondo con color negro
                g.setColor(Color.BLACK);
                g.fillRect(0, 0, getWidth(), getHeight());

                if (iconoLogo != null) {
                    // Aplica un efecto de transparencia si la pantalla se está desvaneciendo
                    float alfa = 1.0f - progresoDesvanecimiento;
                    g2.setComposite(AlphaComposite.SrcOver.derive(alfa));

                    // Calcula el tamaño y la posición del logo para centrarlo y escalarlo
                    int anchoLogoDeseado = getWidth() / 4;
                    double anchoOriginal = iconoLogo.getIconWidth();
                    double altoOriginal = iconoLogo.getIconHeight();

                    if (anchoOriginal <= 0 || altoOriginal <= 0) return; // Evita división por cero

                    double relacionAspecto = altoOriginal / anchoOriginal;
                    int altoLogoCalculado = (int) (anchoLogoDeseado * relacionAspecto);
                    int x = (getWidth() - anchoLogoDeseado) / 2;
                    int y = (getHeight() - altoLogoCalculado) / 2;

                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.drawImage(iconoLogo.getImage(), x, y, anchoLogoDeseado, altoLogoCalculado, this);
                }

                if (esFaseCarga) {
                    // Restaura la composición de alfa si la barra de carga está visible
                    g2.setComposite(AlphaComposite.SrcOver);

                    // Dibuja la barra de progreso
                    int alturaBarra = 5;
                    int anchoBarra = getWidth() / 3;
                    int xBarra = (getWidth() - anchoBarra) / 2;
                    int yBarra = getHeight() - (int) (getHeight() * 0.15); // Posición vertical de la barra

                    g2.setColor(new Color(50, 50, 50)); // Fondo de la barra (gris oscuro)
                    g2.fillRect(xBarra, yBarra, anchoBarra, alturaBarra);

                    int anchoRelleno = (int) (anchoBarra * progresoCarga);
                    g2.setColor(Color.WHITE); // Color del progreso (blanco)
                    g2.fillRect(xBarra, yBarra, anchoRelleno, alturaBarra);
                }

                g2.dispose(); // Libera los recursos gráficos
            }
        };
        add(panelDibujo);
    }

    /**
     * Muestra la pantalla de carga y luego ejecuta una acción cuando la animación termina.
     *
     * @param alFinalizar Una interfaz {@link Runnable} que se ejecutará después de que
     *                    la pantalla de carga se haya desvanecido.
     */
    public void mostrarConAnimacion(Runnable alFinalizar) {
        setVisible(true);
        iniciarCadenaAnimacion(alFinalizar);
    }

    /**
     * Inicia una secuencia de animaciones para la barra de progreso y el desvanecimiento.
     * Esta cadena simula una carga en etapas con pausas intermedias.
     *
     * @param alFinalizar La acción a ejecutar al finalizar todas las animaciones.
     */
    private void iniciarCadenaAnimacion(Runnable alFinalizar) {
        // Animación para llenar el progreso hasta el 65%
        Animator animacionFase1 = new Animator(500, new Animator.TimingTarget() {
            @Override
            public void timingEvent(float fraccion) {
                progresoCarga = fraccion * 0.65f;
                repaint(); // Redibuja el componente para mostrar el progreso
            }

            @Override
            public void end() {
                // Pausa después de la primera fase de carga
                Timer pausa1 = new Timer(200, e -> {
                    // Animación para llenar el progreso del 65% al 75%
                    Animator animacionFase2 = new Animator(500, new Animator.TimingTarget() {
                        @Override
                        public void timingEvent(float fraccion) {
                            progresoCarga = 0.65f + (fraccion * 0.10f);
                            repaint();
                        }

                        @Override
                        public void end() {
                            // Pausa después de la segunda fase de carga
                            Timer pausa2 = new Timer(200, e2 -> {
                                // Animación para llenar el progreso del 75% al 100%
                                Animator animacionFase3 = new Animator(500, new Animator.TimingTarget() {
                                    @Override
                                    public void timingEvent(float fraccion) {
                                        progresoCarga = 0.75f + (fraccion * 0.25f);
                                        repaint();
                                    }

                                    @Override
                                    public void end() {
                                        esFaseCarga = false; // Oculta la barra de progreso
                                        // Animación de desvanecimiento de toda la pantalla
                                        Animator animacionDesvanecimiento = new Animator(1000, new Animator.TimingTarget() {
                                            @Override
                                            public void timingEvent(float fraccion) {
                                                progresoDesvanecimiento = fraccion;
                                                repaint();
                                            }

                                            @Override
                                            public void end() {
                                                alFinalizar.run(); // Ejecuta la acción final
                                            }
                                        });
                                        animacionDesvanecimiento.setInterpolator(CubicBezierEasing.EASE_IN);
                                        animacionDesvanecimiento.start();
                                    }
                                });
                                animacionFase3.setInterpolator(CubicBezierEasing.EASE_IN);
                                animacionFase3.start();
                            });
                            pausa2.setRepeats(false);
                            pausa2.start();
                        }
                    });
                    animacionFase2.setInterpolator(CubicBezierEasing.EASE_IN_OUT);
                    animacionFase2.start();
                });
                pausa1.setRepeats(false);
                pausa1.start();
            }
        });
        animacionFase1.setInterpolator(CubicBezierEasing.EASE_OUT);
        animacionFase1.start();
    }
}