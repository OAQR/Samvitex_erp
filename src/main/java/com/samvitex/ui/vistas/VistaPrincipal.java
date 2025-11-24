package com.samvitex.ui.vistas;

import com.formdev.flatlaf.FlatLaf;
import com.samvitex.modelos.dto.ModeloUbicacion;
import com.samvitex.modelos.dto.SesionUsuario;
import com.samvitex.servicios.ServicioAutenticacion;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * `VistaPrincipal` es el JPanel principal de la aplicación que gestiona la reproducción de video
 * y la superposición de la interfaz de usuario. Utiliza VLCJ para reproducir videos y
 * {@link SuperposicionPrincipal} para mostrar elementos interactivos encima del video.
 */
public class VistaPrincipal extends JPanel {

    private static final Logger LOGGER = Logger.getLogger(VistaPrincipal.class.getName());

    private List<ModeloUbicacion> ubicaciones;
    private int indiceVideoActual = 0;
    private SuperposicionPrincipal superposicionPrincipal;

    private MediaPlayerFactory fabricaReproductor;
    private EmbeddedMediaPlayer reproductorMultimedia;
    private Canvas lienzoVideo;

    /**
     * Constructor de `VistaPrincipal`.
     * Inicializa el reproductor de video y carga los datos de prueba.
     */
    public VistaPrincipal() {
        inicializarComponentes();
        cargarDatosPrueba();
    }

    /**
     * Inicializa los componentes relacionados con la reproducción de video.
     * Configura el reproductor VLCJ, el lienzo de video y los listeners de eventos.
     */
    private void inicializarComponentes() {
        // Inicializa la fábrica del reproductor VLCJ
        fabricaReproductor = new MediaPlayerFactory();
        reproductorMultimedia = fabricaReproductor.mediaPlayers().newEmbeddedMediaPlayer();
        reproductorMultimedia.audio().setMute(true); // Silencia el audio por defecto

        // Crea un lienzo (Canvas) donde se renderizará el video
        lienzoVideo = new Canvas();
        reproductorMultimedia.videoSurface().set(fabricaReproductor.videoSurfaces().newVideoSurface(lienzoVideo));

        // Añade un listener para manejar eventos del reproductor, como el final del video
        reproductorMultimedia.events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
            /**
             * Se invoca cuando la reproducción del medio ha finalizado.
             * @param mediaPlayer La instancia del reproductor de medios.
             */
            @Override
            public void finished(MediaPlayer mediaPlayer) {
                // Las actualizaciones de la UI desde hilos de VLCJ deben hacerse en el Event Dispatch Thread (EDT)
                SwingUtilities.invokeLater(() -> {
                    avanzarSiguienteVideo();
                });
            }
        });

        // Añade un listener para manejar el redimensionamiento del lienzo del video
        lienzoVideo.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int ancho = e.getComponent().getWidth();
                int alto = e.getComponent().getHeight();
                if (alto > 0) {
                    // Establece la relación de aspecto del video para que coincida con el lienzo
                    String relacionAspecto = String.format("%d:%d", ancho, alto);
                    reproductorMultimedia.video().setAspectRatio(relacionAspecto);
                }
            }
        });

        setLayout(new BorderLayout()); // Usa BorderLayout para el diseño del panel
        add(lienzoVideo, BorderLayout.CENTER); // Añade el lienzo de video al centro del panel
    }

    /**
     * Carga datos de prueba para las ubicaciones y sus videos asociados.
     */
    private void cargarDatosPrueba() {
        ubicaciones = new ArrayList<>();
        ubicaciones.add(new ModeloUbicacion("Retiro Cala\nSerenidad",
                "Retiro Cala Serenidad es un refugio aislado en medio de la naturaleza virgen. Rodeado de frondosos bosques, cascadas y lagos serenos, este encantador retiro ofrece un escape tranquilo para quienes buscan consuelo y rejuvenecimiento.",
                "video/video 1.mp4"));

        ubicaciones.add(new ModeloUbicacion("Retiro Pinos\nSusurrantes",
                "Ubicado en un bosque sereno, Retiro Pinos Susurrantes es un remanso de paz rodeado de imponentes árboles y paisajes impresionantes. Esta escapada tranquila ofrece la oportunidad de reconectar con la naturaleza, relajarse y encontrar la paz interior.",
                "video/video 2.mp4"));

        ubicaciones.add(new ModeloUbicacion("Resort Ensenada\nSerena",
                "Situado a orillas de un lago de aguas cristalinas, Resort Ensenada Serena es un paraíso pintoresco que ofrece una mezcla perfecta de relajación y aventura. Con alojamientos de lujo y vistas impresionantes, este retiro idílico promete una experiencia inolvidable.",
                "video/video 3.mp4"));
    }


    /**
     * Inicializa la superposición, ahora pasándole las dependencias necesarias.
     * @param frame El JFrame padre.
     * @param servicioAuth El servicio de autenticación para el PanelLogin.
     * @param onLoginExitoso La acción a ejecutar en caso de login exitoso.
     */
    public void inicializarSuperposicion(JFrame frame, ServicioAutenticacion servicioAuth, Consumer<SesionUsuario> onLoginExitoso) {
        superposicionPrincipal = new SuperposicionPrincipal(frame, ubicaciones, servicioAuth, onLoginExitoso);
        superposicionPrincipal.getPanelContenido().setEventoSuperposicionPrincipal(this::reproducirVideo);
        reproductorMultimedia.overlay().set(superposicionPrincipal);
        reproductorMultimedia.overlay().enable(true);
    }

    /**
     * Reproduce el video asociado al índice de ubicación especificado.
     *
     * @param indice El índice de la {@link ModeloUbicacion} cuyo video se va a reproducir.
     */
    public void reproducirVideo(int indice) {
        this.indiceVideoActual = indice;
        if (indice < 0 || indice >= ubicaciones.size()) {
            LOGGER.log(Level.WARNING, "Índice de video fuera de rango: " + indice);
            return;
        }
        ModeloUbicacion ubicacion = ubicaciones.get(indice);

        if (reproductorMultimedia.status().isPlaying()) {
            reproductorMultimedia.controls().stop(); // Detiene el video actual si está reproduciéndose
        }
        // Inicia la reproducción del nuevo video
        reproductorMultimedia.media().play(ubicacion.getRutaVideo());
        reproductorMultimedia.controls().play();

        // Actualiza el índice en el panel de contenido de la superposición
        if (superposicionPrincipal != null) {
            superposicionPrincipal.getPanelContenido().setIndiceActual(indice);
        }
    }

    /**
     * Detiene la reproducción del video y libera los recursos del reproductor multimedia.
     * Este metodo debe llamarse al cerrar la aplicación para evitar fugas de memoria.
     */
    public void detenerReproduccion() {
        if (reproductorMultimedia != null) {
            reproductorMultimedia.controls().stop();
            reproductorMultimedia.release(); // Libera el reproductor de video
        }
        if (fabricaReproductor != null) {
            fabricaReproductor.release(); // Libera la fábrica de reproductores
        }
        if (superposicionPrincipal != null) {
            superposicionPrincipal.dispose(); // Cierra la ventana de superposición
        }
    }

    /**
     * Calcula el índice del siguiente video en la lista y lo reproduce.
     * Si llega al final de la lista, vuelve al principio (loop).
     */
    private void avanzarSiguienteVideo() {
        if (ubicaciones == null || ubicaciones.isEmpty()) {
            return; // No hacer nada si no hay videos
        }

        // Calcula el siguiente índice, volviendo a 0 si llega al final
        int siguienteIndice = (indiceVideoActual + 1) % ubicaciones.size();

        // Llama al metodo para reproducir el video
        reproducirVideo(siguienteIndice);

        // **IMPORTANTE**: También debemos actualizar visualmente el botón de paginación
        if (superposicionPrincipal != null) {
            superposicionPrincipal.getPanelContenido().actualizarSeleccionPaginacion(siguienteIndice);
        }
    }
}