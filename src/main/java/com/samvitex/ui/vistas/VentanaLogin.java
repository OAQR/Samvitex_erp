package com.samvitex.ui.vistas;

import com.formdev.flatlaf.FlatLaf;
import com.samvitex.modelos.dto.SesionUsuario;
import com.samvitex.servicios.ServicioAutenticacion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

@Component
public class VentanaLogin extends JFrame {

    private final VistaPrincipal vistaPrincipal;
    private final ServicioAutenticacion servicioAutenticacion;
    private final ApplicationContext springContext; // Contexto de Spring para obtener otros beans

    @Autowired
    public VentanaLogin(ServicioAutenticacion servicioAutenticacion, ApplicationContext springContext) {
        this.servicioAutenticacion = servicioAutenticacion;
        this.springContext = springContext;
        this.vistaPrincipal = new VistaPrincipal();
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setUndecorated(true);
        setSize(Toolkit.getDefaultToolkit().getScreenSize());
        setLocationRelativeTo(null);
        setContentPane(vistaPrincipal);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                // Aquí se pasan el servicio y la acción a ejecutar en caso de éxito
                vistaPrincipal.inicializarSuperposicion(VentanaLogin.this, servicioAutenticacion, VentanaLogin.this::onLoginExitoso);
                vistaPrincipal.reproducirVideo(0);
            }

            @Override
            public void windowClosing(WindowEvent e) {
                vistaPrincipal.detenerReproduccion();
            }
        });
    }

    /**
     * Este metodo es el 'callback' que se ejecuta cuando el PanelLogin
     * confirma una autenticación exitosa.
     * @param sesion La sesión del usuario autenticado.
     */
    private void onLoginExitoso(SesionUsuario sesion) {
        SwingUtilities.invokeLater(() -> {
            vistaPrincipal.detenerReproduccion();
            this.dispose();

            FlatLaf.registerCustomDefaultsSource("com.samvitex.themes");
            com.formdev.flatlaf.themes.FlatMacDarkLaf.setup();
            FlatLaf.updateUI();

            JFrame framePrincipal = new JFrame();
            framePrincipal.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            framePrincipal.setSize(1366, 768);
            framePrincipal.setMinimumSize(new Dimension(1100, 650));
            framePrincipal.setLocationRelativeTo(null);


            VentanaPrincipal panelPrincipal = springContext.getBean(VentanaPrincipal.class);

            panelPrincipal.inicializarYMostrar(framePrincipal, sesion);

            framePrincipal.setContentPane(panelPrincipal);
            framePrincipal.setVisible(true);
        });
    }
}