package com.samvitex.ui.componentes;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.util.UIScale;
import com.samvitex.utilidades.EfectoOnda; // Renombrado de RippleEffect a EfectoOnda

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * `BotonCabecera` es un componente JButton personalizado diseñado para ser utilizado
 * en la cabecera de la aplicación. Incluye un efecto de onda (ripple effect) al hacer clic
 * y tiene un estilo de fuente negrita.
 */
public class BotonCabecera extends JButton {

    private EfectoOnda efectoOnda;

    /**
     * Constructor de `BotonCabecera`.
     *
     * @param texto El texto que se mostrará en el botón.
     */
    public BotonCabecera(String texto) {
        super(texto);
        inicializar();
    }

    /**
     * Inicializa las propiedades del botón, incluyendo el efecto de onda,
     * el estilo y el cursor.
     */
    private void inicializar() {
        efectoOnda = new EfectoOnda(this); // Inicializa el efecto de onda para este botón
        setContentAreaFilled(false); // No rellena el área de contenido para permitir el dibujo personalizado
        setCursor(new Cursor(Cursor.HAND_CURSOR)); // Cambia el cursor a una mano al pasar por encima
        // Aplica estilos de FlatLaf: fuente negrita y tamaño +3
        putClientProperty(FlatClientProperties.STYLE, "" +
                "font:bold +3");
    }

    /**
     * Sobrescribe el método `paint` para dibujar el botón y el efecto de onda.
     *
     * @param g El contexto gráfico donde se realizará el dibujo.
     */
    @Override
    public void paint(Graphics g) {
        super.paint(g); // Dibuja el botón base
        int radioEsquina = UIScale.scale(20); // Define el radio de las esquinas redondeadas
        // Renderiza el efecto de onda dentro de una forma de rectángulo redondeado
        efectoOnda.renderizar(g, new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), radioEsquina, radioEsquina));
    }
}