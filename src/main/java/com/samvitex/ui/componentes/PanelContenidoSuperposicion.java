package com.samvitex.ui.componentes;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.util.Animator;
import com.formdev.flatlaf.util.CubicBezierEasing;
import com.samvitex.eventos.EventoSuperposicionPrincipal;
import com.samvitex.modelos.dto.ModeloUbicacion;
import com.samvitex.modelos.dto.SesionUsuario;
import com.samvitex.servicios.ServicioAutenticacion;
import com.samvitex.ui.vistas.PanelLogin;
import com.samvitex.ui.vistas.SuperposicionPrincipal.TipoAnimacion; // Importa el enum

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * `PanelContenidoSuperposicion` es un JPanel que gestiona el contenido visual
 * y las animaciones de la superposición principal de la aplicación.
 * Incluye la cabecera, los botones de paginación, el logo de Samvitex y
 * un panel de inicio de sesión que puede aparecer y desaparecer con animación.
 */
public class PanelContenidoSuperposicion extends JPanel {

    private static final Logger LOGGER = Logger.getLogger(PanelContenidoSuperposicion.class.getName());

    private MigLayout layoutMig;
    private EventoSuperposicionPrincipal eventoSuperposicionPrincipal;
    private TipoAnimacion tipoAnimacionActual = TipoAnimacion.NINGUNA;
    private Animator animadorVideo;
    private Animator animadorLogin;
    private JPanel panelBotonesPaginacion;
    private float progresoAnimacion;
    private int indiceActual;
    private boolean mostrandoLogin;
    private final List<ModeloUbicacion> ubicaciones;
    private final ServicioAutenticacion servicioAutenticacion;
    private final Consumer<SesionUsuario> onLoginExitoso;

    // Componentes de la UI
    private JPanel panelCabecera;
    private PanelLogin panelLogin;
    private FlatSVGIcon iconoLogoSamvitex;

    /**
     * Constructor de `PanelContenidoSuperposicion`.
     *
     * @param ubicaciones Una lista de {@link ModeloUbicacion} que se utilizarán para
     *                    los botones de paginación y posibles detalles.
     */
    public PanelContenidoSuperposicion(List<ModeloUbicacion> ubicaciones, ServicioAutenticacion servicioAutenticacion, Consumer<SesionUsuario> onLoginExitoso) {
        this.ubicaciones = ubicaciones;
        this.servicioAutenticacion = servicioAutenticacion;
        this.onLoginExitoso = onLoginExitoso;
        inicializarComponentes();
    }

    /**
     * Establece el listener para los eventos de cambio en la superposición.
     *
     * @param eventoSuperposicionPrincipal La implementación de {@link EventoSuperposicionPrincipal}.
     */
    public void setEventoSuperposicionPrincipal(EventoSuperposicionPrincipal eventoSuperposicionPrincipal) {
        this.eventoSuperposicionPrincipal = eventoSuperposicionPrincipal;
    }

    /**
     * Establece el índice de la ubicación actual.
     *
     * @param indice El nuevo índice de la ubicación.
     */
    public void setIndiceActual(int indice) {
        this.indiceActual = indice;
        // Actualiza los botones según el índice.
        if (panelBotonesPaginacion != null) {
            establecerBotonSeleccionado(panelBotonesPaginacion, this.indiceActual);
        }
        // Si hubiera JTextPane para título/descripción, se actualizarían aquí:
        // ModeloUbicacion ubicacion = ubicaciones.get(indice);
        // textoTitulo.setText(ubicacion.getTitulo());
        // textoDescripcion.setText(ubicacion.getDescripcion());
    }

    /**
     * Inicializa los componentes visuales y las animaciones del panel.
     */
    private void inicializarComponentes() {
        setOpaque(false); // Fondo transparente para permitir el dibujo personalizado
        layoutMig = new MigLayout("fill,insets 10 180 10 180", "fill", "[grow 0][]");
        setLayout(layoutMig);

        // Intenta cargar el logo SVG
        try {
            iconoLogoSamvitex = new FlatSVGIcon(getClass().getResource("/imagenes/samvitex_logo_blanco.svg"));
            if (iconoLogoSamvitex == null) {
                LOGGER.log(Level.WARNING, "El logo SVG no pudo ser cargado para la superposición.");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al cargar el logo SVG de Samvitex para superposición: " + e.getMessage(), e);
            iconoLogoSamvitex = null;
        }

        crearCabecera();
        crearBotonesPaginacion();
        crearPanelLogin();

        // Añade el logo de Samvitex al panel
        // Se dibuja directamente en paintComponent, no como un componente JLabel en este caso
        // Se añade un panel vacío para ocupar su espacio y dibujarlo encima
        JPanel panelLogoDibujo = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (iconoLogoSamvitex == null) return;

                Graphics2D g2 = (Graphics2D) g.create();
                g2.setComposite(AlphaComposite.SrcOver.derive(0.7f)); // Opacidad del 70%

                int anchoLogoDeseado = getWidth() / 2;
                double anchoOriginal = iconoLogoSamvitex.getIconWidth();
                double altoOriginal = iconoLogoSamvitex.getIconHeight();
                if (anchoOriginal <= 0) return;
                double relacionAspecto = altoOriginal / anchoOriginal;
                int altoLogoCalculado = (int) (anchoLogoDeseado * relacionAspecto);

                int x = (getWidth() - anchoLogoDeseado) / 2;
                int y = (getHeight() - altoLogoCalculado) / 2;

                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.drawImage(iconoLogoSamvitex.getImage(), x, y, anchoLogoDeseado, altoLogoCalculado, this);
                g2.dispose();
            }
        };
        panelLogoDibujo.setOpaque(false);
        add(panelLogoDibujo, "pos 10% 25%, w 45%, h 50%!"); // Posiciona el panel donde se dibujará el logo

        // Listener para cerrar el panel de login si se hace clic fuera de él
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                // Solo cierra el login si está abierto y el clic no fue dentro del panel de login
                if (mostrandoLogin && !panelLogin.getBounds().contains(e.getPoint())) {
                    ejecutarAnimacionLogin(false);
                }
            }
        });

        // Inicialización del animador para los cambios de video
        animadorVideo = new Animator(500, new Animator.TimingTarget() {
            @Override
            public void timingEvent(float fraccion) {
                progresoAnimacion = fraccion;
                repaint(); // Solicita repintado para la animación
            }

            @Override
            public void end() {
                if (tipoAnimacionActual == TipoAnimacion.CERRAR_VIDEO) {
                    // Después de cerrar el video, notifica al listener y prepara para mostrar el siguiente
                    if (eventoSuperposicionPrincipal != null) {
                        eventoSuperposicionPrincipal.alCambiar(indiceActual);
                    }
                    // Pequeña pausa antes de iniciar la animación de "mostrar"
                    SwingUtilities.invokeLater(() -> {
                        dormir(500); // Esto es un hack. Considerar un `Timer` o animador encadenado
                        ejecutarAnimacionVideo(indiceActual, TipoAnimacion.MOSTRAR_VIDEO);
                    });
                } else {
                    tipoAnimacionActual = TipoAnimacion.NINGUNA; // Animación finalizada
                }
            }
        });
        // Inicialización del animador para el panel de login
        animadorLogin = new Animator(500, new Animator.TimingTarget() {
            @Override
            public void timingEvent(float fraccion) {
                float f = mostrandoLogin ? fraccion : 1f - fraccion; // Dirección de la animación
                int xOffset = (int) ((350 + 180) * f); // Calcula el desplazamiento X del panel de login
                layoutMig.setComponentConstraints(panelLogin, "pos 100%-" + xOffset + " 0.5al, w 350");
                revalidate(); // Revalida el layout para aplicar los cambios de posición
            }
        });

        animadorVideo.setInterpolator(CubicBezierEasing.EASE_IN);
        animadorLogin.setInterpolator(CubicBezierEasing.EASE);
    }

    /**
     * Pausa la ejecución del hilo actual. Usar con precaución en el EDT.
     * Es preferible usar {@link Timer} para pausas en la UI.
     *
     * @param milisegundos El tiempo en milisegundos para dormir.
     */
    private void dormir(long milisegundos) {
        try {
            Thread.sleep(milisegundos);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restaura el estado de interrupción
            LOGGER.log(Level.WARNING, "El hilo de animación fue interrumpido.", e);
        }
    }

    /**
     * Crea y añade la cabecera al panel.
     */
    private void crearCabecera() {
        panelCabecera = new JPanel(new MigLayout("fill", "[]push[][]"));
        panelCabecera.setOpaque(false);

        JLabel etiquetaTitulo = new JLabel("SAMVITEX");
        etiquetaTitulo.putClientProperty(FlatClientProperties.STYLE, "font:bold +10");

        BotonCabecera botonIniciarSesion = new BotonCabecera("Iniciar Sesión");
        botonIniciarSesion.addActionListener(e -> ejecutarAnimacionLogin(true)); // Muestra el panel de login al hacer clic

        panelCabecera.add(etiquetaTitulo);
        panelCabecera.add(botonIniciarSesion); // Añade el botón de login
        add(panelCabecera, "wrap"); // Añade la cabecera al layout principal
    }

    /**
     * Crea y añade el panel de inicio de sesión al panel.
     */
    private void crearPanelLogin() {
        panelLogin = new PanelLogin(servicioAutenticacion, onLoginExitoso);
        add(panelLogin, "pos 100% 0.5al,w 350");
    }

    /**
     * Crea y añade los botones de paginación para navegar entre ubicaciones.
     */
    private void crearBotonesPaginacion() {
        JPanel panelBotones = new JPanel(new MigLayout("gapx 20"));
        panelBotonesPaginacion = new JPanel(new MigLayout("gapx 20"));
        panelBotonesPaginacion.setOpaque(false);
        panelBotones.setOpaque(false);

        for (int i = 0; i < ubicaciones.size(); i++) {
            JButton botonPaginacion = new JButton("");
            botonPaginacion.putClientProperty(FlatClientProperties.STYLE, "" +
                    "margin:5,5,5,5;" +
                    "arc:999;" +           // Botón circular
                    "borderWidth:0;" +
                    "focusWidth:0;" +
                    "innerFocusWidth:0;" +
                    "selectedBackground:#FFFFFF"); // Color de fondo cuando está seleccionado
            botonPaginacion.setCursor(new Cursor(Cursor.HAND_CURSOR));

            final int indice = i;
            botonPaginacion.addActionListener(e -> {
                // Solo cambia si el índice es diferente y no hay una animación en curso
                if (this.indiceActual != indice) {
                    boolean animacionIniciada = ejecutarAnimacionVideo(indice, TipoAnimacion.CERRAR_VIDEO);
                    if (animacionIniciada) {
                        establecerBotonSeleccionado(panelBotones, indice);
                    }
                }
            });
            panelBotones.add(botonPaginacion);
        }
        add(panelBotones, "pos 0.5al 80%"); // Posiciona los botones en la parte inferior central
        establecerBotonSeleccionado(panelBotones, indiceActual); // Selecciona el botón inicial
    }

    /**
     * Establece el estado de seleccionado para el botón de paginación correspondiente.
     *
     * @param panelBotones El JPanel que contiene los botones de paginación.
     * @param indice       El índice del botón que debe ser seleccionado.
     */
    private void establecerBotonSeleccionado(JPanel panelBotones, int indice) {
        int cantidadComponentes = panelBotones.getComponentCount();
        for (int i = 0; i < cantidadComponentes; i++) {
            JButton boton = (JButton) panelBotones.getComponent(i);
            boton.setSelected(i == indice);
        }
    }

    /**
     * Ejecuta la animación de transición de video.
     *
     * @param nuevoIndice  El índice del video al que se va a transicionar.
     * @param tipoAnimacion El tipo de animación a realizar (cerrar o mostrar video).
     * @return `true` si la animación se pudo iniciar, `false` si ya había una en curso.
     */
    private boolean ejecutarAnimacionVideo(int nuevoIndice, TipoAnimacion tipoAnimacion) {
        if (!animadorVideo.isRunning()) {
            this.progresoAnimacion = 0;
            this.tipoAnimacionActual = tipoAnimacion;
            this.indiceActual = nuevoIndice;
            animadorVideo.start();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Ejecuta la animación para mostrar u ocultar el panel de inicio de sesión.
     *
     * @param mostrar `true` para mostrar el panel de login, `false` para ocultarlo.
     */
    private void ejecutarAnimacionLogin(boolean mostrar) {
        if (mostrandoLogin != mostrar) {
            if (!animadorLogin.isRunning()) {
                mostrandoLogin = mostrar;
                animadorLogin.start();
            }
        }
    }

    /**
     * Sobrescribe el método `paintComponent` para dibujar las animaciones de transición
     * de video (círculo que se expande o contrae).
     *
     * @param g El contexto gráfico donde se realizará el dibujo.
     */
    @Override
    protected void paintComponent(Graphics g) {
        if (tipoAnimacionActual != TipoAnimacion.NINGUNA) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int ancho = getWidth();
            int alto = getHeight();
            g2.setColor(Color.BLACK);

            Rectangle rectanguloPanel = new Rectangle(0, 0, ancho, alto);

            if (tipoAnimacionActual == TipoAnimacion.CERRAR_VIDEO) {
                // Animación de cierre: un círculo crece hasta cubrir la pantalla
                g2.setComposite(AlphaComposite.SrcOver.derive(progresoAnimacion));
                g2.fill(rectanguloPanel);
            } else { // TipoAnimacion.MOSTRAR_VIDEO
                // Animación de mostrar: un círculo se contrae revelando el contenido
                Area areaTotal = new Area(rectanguloPanel);
                areaTotal.subtract(new Area(crearFormaCircular(rectanguloPanel)));
                g2.fill(areaTotal);
            }
            g2.dispose();
        }
        super.paintComponent(g); // Dibuja los componentes hijos
    }

    /**
     * Crea una forma circular para la animación de transición de video.
     *
     * @param rectanguloBase El rectángulo que define el área base para la animación.
     * @return Una {@link Shape} circular que representa la animación en su estado actual.
     */
    private Shape crearFormaCircular(Rectangle rectanguloBase) {
        int tamanoMaximo = Math.max(rectanguloBase.width, rectanguloBase.height);
        float tamanoActual = tamanoMaximo * progresoAnimacion;
        float x = (rectanguloBase.width - tamanoActual) / 2;
        float y = (rectanguloBase.height - tamanoActual) / 2;
        return new Ellipse2D.Double(x, y, tamanoActual, tamanoActual);
    }

    /**
     * Actualiza la selección visual de los botones de paginación.
     * Este método es llamado externamente (por VistaPrincipal) cuando el video
     * cambia automáticamente.
     *
     * @param nuevoIndice El índice del nuevo botón que debe ser seleccionado.
     */
    public void actualizarSeleccionPaginacion(int nuevoIndice) {
        if (panelBotonesPaginacion != null) {
            establecerBotonSeleccionado(panelBotonesPaginacion, nuevoIndice);
        }
    }
}