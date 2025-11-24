package com.samvitex.ui.vistas;

import javax.swing.*;
import java.awt.*;

/**
 * Panel de cabecera que muestra información del usuario y controles de la ventana.
 */
public class HeaderPanel extends JPanel {

    private JLabel lbUserName;
    private JLabel lbRole;

    public HeaderPanel() {
        setOpaque(false);
        setLayout(new FlowLayout(FlowLayout.RIGHT)); // Alinea los componentes a la derecha

        lbUserName = new JLabel("Nombre de Usuario");
        lbUserName.setFont(new Font("sansserif", Font.BOLD, 12));
        lbRole = new JLabel("Rol");
        lbRole.setForeground(Color.GRAY);

        JPanel userInfoPanel = new JPanel();
        userInfoPanel.setOpaque(false);
        userInfoPanel.setLayout(new BoxLayout(userInfoPanel, BoxLayout.Y_AXIS));
        userInfoPanel.add(lbUserName);
        userInfoPanel.add(lbRole);

        add(userInfoPanel);
        // Aquí podrías añadir botones de minimizar, maximizar, cerrar, etc.
    }

    public void configurarUsuario(String nombre, String rol) {
        lbUserName.setText(nombre);
        lbRole.setText(rol);
    }
}