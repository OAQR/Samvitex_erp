package com.samvitex.utilidades;

import com.formdev.flatlaf.util.Animator;
import com.formdev.flatlaf.util.CubicBezierEasing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * `EfectoOnda` es una utilidad para aplicar un efecto visual de "onda" o "ripple"
 * a componentes Swing al hacer clic. Simula la propagación de una onda desde
 * el punto de clic, desvaneciéndose con el tiempo.
 */
public class EfectoOnda {

    private final JComponent componente;
    private final List<AnimacionOnda> animacionesActivas;

    private Color colorOnda = new Color(255, 255, 255, 150); // Color por defecto de la onda (blanco semitransparente)

    /**
     * Constructor de `EfectoOnda`.
     *
     * @param componente El componente Swing al que se aplicará el efecto de onda.
     *                   Debe ser un JComponent que se pueda repintar.
     */
    public EfectoOnda(JComponent componente) {
        this.componente = componente;
        this.animacionesActivas = new ArrayList<>();
        inicializarListeners();
    }

    /**
     * Inicializa los listeners del ratón para detectar clics en el componente
     * y comenzar una nueva animación de onda.
     */
    private void inicializarListeners() {
        componente.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // Solo activa la onda si el componente está habilitado
                if (componente.isEnabled()) {
                    agregarOnda(e.getPoint());
                }
            }
        });
    }

    /**
     * Agrega una nueva animación de onda en el punto especificado.
     *
     * @param puntoClic El punto (coordenadas X, Y) donde se hizo clic.
     */
    private void agregarOnda(Point puntoClic) {
        AnimacionOnda nuevaOnda = new AnimacionOnda(puntoClic);
        animacionesActivas.add(nuevaOnda);
        nuevaOnda.iniciar();
    }

    /**
     * Renderiza todas las ondas activas en el contexto gráfico.
     * Este método debe ser llamado desde el método `paintComponent` del JComponent.
     *
     * @param g     El contexto gráfico (Graphics) donde se dibujarán las ondas.
     * @param forma La forma del componente dentro de la cual se dibujarán las ondas
     *              (por ejemplo, un rectángulo o un rectángulo redondeado).
     */
    public void renderizar(Graphics g, Shape forma) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Se usa una Area para recortar las ondas dentro de la forma del componente
        Area areaRecorte = new Area(forma);

        // Iterar sobre una copia para evitar ConcurrentModificationException
        List<AnimacionOnda> animacionesParaEliminar = new ArrayList<>();
        for (AnimacionOnda onda : animacionesActivas) {
            // Calcula el tamaño máximo que podría alcanzar la onda
            float tamanoMaximo = Math.max(componente.getWidth(), componente.getHeight()) * 1.2f; // Un poco más grande
            // Calcula el radio actual de la onda
            float radioActual = tamanoMaximo * onda.getProgreso();

            // Crea un círculo centrado en el punto de clic de la onda
            Ellipse2D.Double circuloOnda = new Ellipse2D.Double(
                    onda.getPuntoClic().x - radioActual / 2,
                    onda.getPuntoClic().y - radioActual / 2,
                    radioActual, radioActual);

            // Crea un área con la forma del círculo de la onda
            Area areaOnda = new Area(circuloOnda);
            // Interseca el área de la onda con el área de recorte del componente
            areaOnda.intersect(areaRecorte);

            // Dibuja la onda con transparencia basada en el progreso de la animación
            g2.setComposite(AlphaComposite.SrcOver.derive(1f - onda.getProgreso()));
            g2.setColor(colorOnda);
            g2.fill(areaOnda);

            // Si la animación ha terminado, marcarla para eliminación
            if (onda.estaFinalizada()) {
                animacionesParaEliminar.add(onda);
            }
        }
        // Elimina las animaciones finalizadas
        animacionesActivas.removeAll(animacionesParaEliminar);

        g2.dispose();
    }

    /**
     * Establece el color de las ondas.
     *
     * @param colorOnda El nuevo color para las ondas.
     */
    public void setColorOnda(Color colorOnda) {
        this.colorOnda = colorOnda;
    }

    /**
     * Clase interna para gestionar una animación individual de onda.
     */
    private class AnimacionOnda implements Animator.TimingTarget {
        private final Point puntoClic;
        private final Animator animador;
        private float progreso;
        private boolean finalizada;

        /**
         * Constructor de `AnimacionOnda`.
         *
         * @param puntoClic El punto donde se originó la onda.
         */
        public AnimacionOnda(Point puntoClic) {
            this.puntoClic = puntoClic;
            this.progreso = 0f;
            this.finalizada = false;
            // Configura el animador para esta onda específica
            this.animador = new Animator(500, this); // Duración de 500ms
            this.animador.setInterpolator(CubicBezierEasing.EASE_OUT); // Curva de interpolación
        }

        /**
         * Inicia la animación de la onda.
         */
        public void iniciar() {
            animador.start();
        }

        /**
         * Obtiene el punto de origen de la onda.
         *
         * @return El {@link Point} donde se hizo clic.
         */
        public Point getPuntoClic() {
            return puntoClic;
        }

        /**
         * Obtiene el progreso actual de la animación (0.0f a 1.0f).
         *
         * @return El progreso de la animación.
         */
        public float getProgreso() {
            return progreso;
        }

        /**
         * Indica si la animación ha finalizado.
         *
         * @return `true` si la animación ha terminado, `false` en caso contrario.
         */
        public boolean estaFinalizada() {
            return finalizada;
        }

        @Override
        public void timingEvent(float fraccion) {
            this.progreso = fraccion;
            componente.repaint(); // Solicita un repintado del componente para mostrar la onda
        }

        @Override
        public void end() {
            this.finalizada = true;
            componente.repaint(); // Asegura un repintado final
        }
    }
}