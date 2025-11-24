package com.samvitex.ui.vistas;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.util.UIScale;
import com.samvitex.modelos.dto.SesionUsuario;
import com.samvitex.servicios.ServicioAutenticacion;
import com.samvitex.servicios.ServicioValidacion;
import com.samvitex.utilidades.AnimadorComponentes;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.geom.RoundRectangle2D;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * `PanelLogin` es un componente JPanel que presenta un formulario de inicio de sesión.
 * Incluye campos para usuario y contraseña, una casilla de "Recordar sesión" y un botón
 * para iniciar sesión. Se dibuja con esquinas redondeadas y una ligera transparencia.
 */
public class PanelLogin extends JPanel {

    private final ServicioAutenticacion servicioAutenticacion;
    private final Consumer<SesionUsuario> onLoginExitoso; // Interfaz funcional para pasar el resultado

    /**
     * Constructor que recibe las dependencias necesarias para que el panel funcione.
     * @param servicioAutenticacion El servicio para validar credenciales.
     * @param onLoginExitoso La acción (callback) a ejecutar tras un login exitoso.
     */
    public PanelLogin(ServicioAutenticacion servicioAutenticacion, Consumer<SesionUsuario> onLoginExitoso) {
        // CORRECCIÓN: Se inicializan las variables final en el constructor.
        this.servicioAutenticacion = servicioAutenticacion;
        this.onLoginExitoso = onLoginExitoso;
        inicializarComponentes();
    }

    /**
     * Inicializa y configura los componentes de la interfaz de usuario para el panel de login.
     * Configura el diseño, los estilos de FlatLaf y añade los campos y botones.
     */
    private void inicializarComponentes() {
        setOpaque(false); // Hace que el panel sea transparente para dibujar un fondo personalizado
        // Añade un MouseAdapter vacío para consumir eventos de click y evitar que se propaguen
        // a componentes subyacentes, lo que es útil en overlays.
        addMouseListener(new MouseAdapter() {
        });
        setLayout(new MigLayout("wrap,fillx,insets 45 45 50 45", "[fill]")); // Configura el layout

        JLabel etiquetaTitulo = new JLabel("Inicia sesión en tu cuenta", SwingConstants.CENTER);
        JTextField campoUsuario = new JTextField();
        JPasswordField campoContrasena = new JPasswordField();
        JCheckBox casillaRecordarSesion = new JCheckBox("Recordar sesión");
        JButton botonIniciarSesion = new JButton("Iniciar Sesión");

        botonIniciarSesion.addActionListener(e -> {
            String usuario = campoUsuario.getText();
            char[] password = campoContrasena.getPassword();

            try {
                // 1. Validaciones básicas de UI (pueden ir aquí o en el servicio)
                if (usuario.isBlank()) {
                    throw new IllegalArgumentException("El nombre de usuario no puede estar vacío.");
                }
                if (password.length == 0) {
                    throw new IllegalArgumentException("La contraseña no puede estar vacía.");
                }

                // 2. Llamar al SERVICIO de negocio para autenticar
                Optional<SesionUsuario> sesionOpt = servicioAutenticacion.autenticarYEstablecerSesion(usuario, password);
                // 3. Reaccionar al resultado del Optional
                sesionOpt.ifPresentOrElse(
                        // Acción a ejecutar si el Optional CONTIENE una SesionUsuario (login exitoso)
                        sesion -> {
                            if (onLoginExitoso != null) {
                                // Llamar al metodo onLoginExitoso que está en VentanaLogin,
                                // pasándole los datos de la sesión.
                                onLoginExitoso.accept(sesion);
                            }
                        },
                        // Acción a ejecutar si el Optional está VACÍO (login fallido)
                        () -> {
                            // Lanzamos una excepción para que sea capturada por el bloque catch y se muestre el error.
                            throw new RuntimeException("Usuario o contraseña incorrectos.");
                        }
                );

            } catch (Exception ex) {
                // 4. Capturar cualquier error y mostrarlo al usuario
                AnimadorComponentes.agitar(campoUsuario);
                AnimadorComponentes.agitar(campoContrasena);
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error de Inicio de Sesión", JOptionPane.ERROR_MESSAGE);
            } finally {
                // Limpiar el array de contraseña de la memoria por seguridad
                java.util.Arrays.fill(password, ' ');
            }
        });

        // Aplicación de estilos personalizados de FlatLaf
        etiquetaTitulo.putClientProperty(FlatClientProperties.STYLE, "" +
                "font:bold +10");
        campoUsuario.putClientProperty(FlatClientProperties.STYLE, "" +
                "margin:5,10,5,10;" + // Margen interno
                "focusWidth:1;" +    // Ancho del borde al enfocar
                "innerFocusWidth:0"); // Ancho del borde interno al enfocar
        campoContrasena.putClientProperty(FlatClientProperties.STYLE, "" +
                "margin:5,10,5,10;" +
                "focusWidth:1;" +
                "innerFocusWidth:0;" +
                "showRevealButton:true"); // Muestra el botón para revelar la contraseña
        botonIniciarSesion.putClientProperty(FlatClientProperties.STYLE, "" +
                "background:#141414;" + // Color de fondo del botón
                "borderWidth:0;" +     // Sin borde
                "focusWidth:0;" +
                "innerFocusWidth:0");
        campoUsuario.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Ingrese su nombre de usuario");
        campoContrasena.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Ingrese su contraseña");

        // Añadir componentes al panel
        add(etiquetaTitulo);
        add(new JLabel("Usuario"), "gapy 20"); // Espacio vertical
        add(campoUsuario);
        add(new JLabel("Contraseña"), "gapy 10");
        add(campoContrasena);
        add(casillaRecordarSesion);
        add(botonIniciarSesion, "gapy 30");
    }

    /**
     * Sobrescribe el metodo `paintComponent` para dibujar el fondo personalizado del panel
     * con esquinas redondeadas y transparencia.
     *
     * @param g El contexto gráfico donde se realizará el dibujo.
     */
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int radioEsquina = UIScale.scale(20); // Radio de las esquinas, escalado por UI
        g2.setColor(getBackground()); // Usa el color de fondo del panel (generalmente UIManager.getColor("Panel.background"))
        g2.setComposite(AlphaComposite.SrcOver.derive(0.8f)); // Aplica 80% de opacidad
        // Dibuja un rectángulo redondeado como fondo
        g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), radioEsquina, radioEsquina));
        g2.dispose(); // Libera los recursos gráficos
        super.paintComponent(g); // Asegura que los componentes hijos se dibujen
    }
}