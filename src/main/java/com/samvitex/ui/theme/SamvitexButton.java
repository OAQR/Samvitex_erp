package com.samvitex.ui.theme;

import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.JButton;
import java.awt.Cursor;

/**
 * Componente de botón personalizado para la aplicación Samvitex.
 * Encapsula estilos visuales consistentes y comportamientos, como el cursor de mano.
 * Esto centraliza el diseño y facilita cambios de tema en toda la aplicación.
 */
public class SamvitexButton extends JButton {

    /**
     * Enumera los tipos de estilos visuales disponibles para los botones.
     */
    public enum ButtonType {
        /** Estilo para acciones principales (ej. Guardar, Nuevo). */
        PRIMARY,
        /** Estilo para acciones secundarias (ej. Editar, Cancelar). */
        SECONDARY
    }

    /**
     * Crea un botón con estilo primario por defecto.
     *
     * @param text El texto que se mostrará en el botón.
     */
    public SamvitexButton(String text) {
        this(text, ButtonType.PRIMARY);
    }

    /**
     * Crea un botón con un tipo de estilo específico.
     *
     * @param text El texto que se mostrará en el botón.
     * @param type El {@link ButtonType} que define el estilo del botón.
     */
    public SamvitexButton(String text, ButtonType type) {
        super(text);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Estilos base compartidos por todos los botones
        String style = "borderWidth:0; focusWidth:0; innerFocusWidth:0; arc:10; margin:5,10,5,10;";

        // Estilos específicos según el tipo
        if (type == ButtonType.PRIMARY) {
            style += "background:$Component.accentColor; foreground:#FFFFFF; font:bold;";
        } else { // SECONDARY
            style += "background:lighten($Panel.background, 10%);";
        }

        putClientProperty(FlatClientProperties.STYLE, style);
    }
}