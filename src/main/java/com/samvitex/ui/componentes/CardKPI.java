package com.samvitex.ui.componentes;

import com.formdev.flatlaf.FlatClientProperties;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

/**
 * Un panel reutilizable (tarjeta) para mostrar un Indicador Clave de Rendimiento (KPI)
 * en el dashboard.
 */
public class CardKPI extends JPanel {

    private final JLabel lblValor;
    private final JLabel lblTitulo;

    public CardKPI(String titulo, String valorInicial) {
        setLayout(new MigLayout("wrap, fill", "[center]", "[center][center]"));
        putClientProperty(FlatClientProperties.STYLE, "" +
                "arc: 20;" + // Esquinas redondeadas
                "background: lighten($Panel.background, 5%);");

        lblTitulo = new JLabel(titulo);
        lblTitulo.putClientProperty(FlatClientProperties.STYLE, "font: 14;");

        lblValor = new JLabel(valorInicial);
        lblValor.putClientProperty(FlatClientProperties.STYLE, "font: bold 32;");

        add(lblValor);
        add(lblTitulo);
    }

    /**
     * Actualiza el valor mostrado en la tarjeta.
     * @param nuevoValor El nuevo texto a mostrar como valor del KPI.
     */
    public void setValor(String nuevoValor) {
        lblValor.setText(nuevoValor);
    }
}