package com.samvitex.app;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.fonts.roboto.FlatRobotoFont;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.samvitex.ui.vistas.VentanaLogin;
import com.samvitex.utilidades.PantallaCarga;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import javax.swing.*;
import java.awt.*;

@SpringBootApplication(scanBasePackages = "com.samvitex")
@EnableJpaRepositories(basePackages = "com.samvitex.repositorios")
@EntityScan(basePackages = "com.samvitex.modelos.entidades") // Corregido para que apunte al paquete correcto
public class AplicacionPrincipal {

    public static void main(String[] args) {
        // Inicia el contexto de Spring en modo no-web
        ConfigurableApplicationContext context = new SpringApplicationBuilder(AplicacionPrincipal.class)
                .headless(false).run(args);

        // Lanza la UI en el Event Dispatch Thread (EDT) de Swing
        EventQueue.invokeLater(() -> {
            // Configurar el tema visual global
            FlatRobotoFont.install();
            FlatLaf.registerCustomDefaultsSource("temas");
            FlatMacDarkLaf.setup();
            UIManager.put("defaultFont", new Font(FlatRobotoFont.FAMILY, Font.PLAIN, 13));

            // Lanza la pantalla de carga, que a su vez obtendrá la VentanaLogin del contexto de Spring
            PantallaCarga pantallaCarga = new PantallaCarga();
            pantallaCarga.mostrarConAnimacion(() -> {
                // Obtenemos el bean de VentanaLogin. Spring ya ha inyectado sus dependencias.
                VentanaLogin ventanaLogin = context.getBean(VentanaLogin.class);
                pantallaCarga.dispose();
                ventanaLogin.setVisible(true);
            });
        });
    }
}