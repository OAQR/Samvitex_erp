package com.samvitex.ui.vistas;

import com.samvitex.modelos.dto.ModeloUbicacion;
import com.samvitex.ui.componentes.PanelContenidoSuperposicion;
import com.samvitex.servicios.ServicioAutenticacion;
import com.samvitex.modelos.dto.SesionUsuario;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;


/**
 * `SuperposicionPrincipal` es una ventana de superposición (JWindow) que se muestra
 * sobre la ventana principal de la aplicación. Contiene el {@link PanelContenidoSuperposicion}
 * que gestiona la interfaz de usuario de la superposición, incluyendo el logo, botones
 * de navegación y un panel de inicio de sesión.
 */
public class SuperposicionPrincipal extends JWindow {

    private PanelContenidoSuperposicion panelContenido;
    private final List<ModeloUbicacion> ubicaciones;
    private final ServicioAutenticacion servicioAutenticacion;
    private final Consumer<SesionUsuario> onLoginExitoso;

    /**
     * Constructor de `SuperposicionPrincipal`.
     *
     * @param frame     El JFrame padre al que se asociará esta JWindow.
     * @param ubicaciones Una lista de {@link ModeloUbicacion} para mostrar en la superposición.
     */
    public SuperposicionPrincipal(JFrame frame, List<ModeloUbicacion> ubicaciones, ServicioAutenticacion servicioAutenticacion, Consumer<SesionUsuario> onLoginExitoso) {
        super(frame);
        this.ubicaciones = ubicaciones;
        this.servicioAutenticacion = servicioAutenticacion;
        this.onLoginExitoso = onLoginExitoso;
        inicializar();
    }

    /**
     * Inicializa los componentes de la superposición, configurando su fondo
     * y añadiendo el {@link PanelContenidoSuperposicion}.
     */
    private void inicializar() {
        // Establece un fondo semitransparente para la superposición
        setBackground(new Color(0, 0, 0, 100)); // Negro con 100 de alfa (aprox 40% opacidad)
        setLayout(new BorderLayout()); // Usa BorderLayout para el diseño

        panelContenido = new PanelContenidoSuperposicion(ubicaciones, servicioAutenticacion, onLoginExitoso);
        add(panelContenido); // Añade el panel de contenido al centro de la JWindow
    }

    /**
     * Obtiene el panel de contenido principal de la superposición.
     * Esto permite interactuar con el {@link PanelContenidoSuperposicion}
     * desde fuera de esta clase.
     *
     * @return La instancia de {@link PanelContenidoSuperposicion}.
     */
    public PanelContenidoSuperposicion getPanelContenido() {
        return panelContenido;
    }

    /**
     * Enumera los tipos de animación disponibles para la superposición.
     */
    public enum TipoAnimacion {
        CERRAR_VIDEO,       // Animación para cerrar el video actual
        MOSTRAR_VIDEO,      // Animación para mostrar el siguiente video
        NINGUNA             // Sin animación activa
    }
}