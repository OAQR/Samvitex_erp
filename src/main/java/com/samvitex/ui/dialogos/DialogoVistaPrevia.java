package com.samvitex.ui.dialogos;

import org.icepdf.ri.common.SwingController;
import org.icepdf.ri.common.SwingViewBuilder;

import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Diálogo modal que incrusta un visor de PDF completo.
 * Permite ver, hacer zoom e imprimir el documento.
 */
public class DialogoVistaPrevia extends JDialog {

    public DialogoVistaPrevia(Frame owner, byte[] contenidoPdf) {
        super(owner, "Vista Previa del Comprobante", true);
        setSize(900, 700); // Tamaño grande para ver bien
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        // 1. Configurar el controlador de ICEpdf
        SwingController controller = new SwingController();
        SwingViewBuilder factory = new SwingViewBuilder(controller);

        // 2. Construir el panel del visor (con barra de herramientas incluida)
        JPanel viewerPanel = factory.buildViewerPanel();
        add(viewerPanel, BorderLayout.CENTER);

        // 3. Cargar el PDF desde la memoria
        InputStream input = new ByteArrayInputStream(contenidoPdf);
        controller.openDocument(input, "Comprobante", "samvitex");

        // 4. Botón de cerrar en la parte inferior
        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.addActionListener(e -> dispose());
        JPanel panelBoton = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBoton.add(btnCerrar);
        add(panelBoton, BorderLayout.SOUTH);
    }
}