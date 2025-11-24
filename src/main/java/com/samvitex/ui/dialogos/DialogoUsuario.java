package com.samvitex.ui.dialogos;

import com.samvitex.modelos.entidades.Rol;
import com.samvitex.modelos.entidades.Usuario;
import com.samvitex.repositorios.RolRepositorio;
import com.samvitex.servicios.ServicioUsuario;
import com.samvitex.ui.theme.SamvitexButton;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

/**
 * Diálogo modal para la creación y edición de entidades {@link Usuario}.
 * Proporciona un formulario para los datos del usuario y se comunica
 * con el {@link ServicioUsuario} para su persistencia.
 */
public class DialogoUsuario extends JDialog {

    private final ServicioUsuario servicioUsuario;
    private final List<Rol> rolesDisponibles;
    private final Usuario usuario;
    private final Runnable onSaveSuccess;

    // Componentes del formulario
    private JTextField txtNombreUsuario, txtNombreCompleto, txtEmail;
    private JComboBox<Rol> cmbRol;
    private JPasswordField txtPassword, txtConfirmPassword;
    private JCheckBox chkActivo;

    /**
     * Construye el diálogo.
     *
     * @param owner El Frame padre sobre el cual el diálogo será modal.
     * @param su El servicio de negocio para las operaciones de usuario.
     * @param rolesDisponibles La lista de todos los {@link Rol} que se deben mostrar en el selector de roles.
     * @param usuario El usuario a editar. Si es {@code null}, el diálogo se abre en modo creación.
     * @param onSaveSuccess Un callback {@link Runnable} que se ejecuta tras un guardado exitoso.
     */
    public DialogoUsuario(Frame owner, ServicioUsuario su, List<Rol> rolesDisponibles, Usuario usuario, Runnable onSaveSuccess) {
        super(owner, "Gestión de Usuario", true);
        this.servicioUsuario = su;
        this.rolesDisponibles = rolesDisponibles;
        this.usuario = (usuario != null) ? usuario : new Usuario();
        this.onSaveSuccess = onSaveSuccess;

        setSize(450, 400);
        setMinimumSize(new Dimension(450, 400));
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        inicializarUI();
        cargarDatos();
    }

    private void inicializarUI() {
        JPanel panelFormulario = new JPanel(new MigLayout("wrap 2, fillx, insets 15", "[right]15[grow,fill]"));

        txtNombreUsuario = new JTextField();
        txtNombreCompleto = new JTextField();
        txtEmail = new JTextField();

        cmbRol = new JComboBox<>(rolesDisponibles.toArray(new Rol[0]));

        txtPassword = new JPasswordField();
        txtConfirmPassword = new JPasswordField();
        chkActivo = new JCheckBox("Usuario Activo");

        panelFormulario.add(new JLabel("Nombre de Usuario:"));
        panelFormulario.add(txtNombreUsuario, "growx");
        panelFormulario.add(new JLabel("Nombre Completo:"));
        panelFormulario.add(txtNombreCompleto, "growx");
        panelFormulario.add(new JLabel("Email:"));
        panelFormulario.add(txtEmail, "growx");
        panelFormulario.add(new JLabel("Rol:"));
        panelFormulario.add(cmbRol, "growx");
        panelFormulario.add(new JLabel("Contraseña:"));
        panelFormulario.add(txtPassword, "growx");
        panelFormulario.add(new JLabel("Confirmar Contraseña:"));
        panelFormulario.add(txtConfirmPassword, "growx");
        panelFormulario.add(chkActivo, "span 2, gapleft 5, gaptop 10");

        add(panelFormulario, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnGuardar = new SamvitexButton("Guardar");
        JButton btnCancelar = new SamvitexButton("Cancelar", SamvitexButton.ButtonType.SECONDARY);
        panelBotones.add(btnGuardar);
        panelBotones.add(btnCancelar);
        add(panelBotones, BorderLayout.SOUTH);

        btnCancelar.addActionListener(e -> dispose());
        btnGuardar.addActionListener(e -> guardarUsuario());
    }

    private void cargarDatos() {
        if (usuario.getId() != null) { // Modo Edición
            setTitle("Editar Usuario");
            txtNombreUsuario.setText(usuario.getNombreUsuario());
            txtNombreUsuario.setEditable(false);
            txtNombreCompleto.setText(usuario.getNombreCompleto());
            txtEmail.setText(usuario.getEmail());
            cmbRol.setSelectedItem(usuario.getRol());
            chkActivo.setSelected(usuario.isActivo());

            JLabel lblInfoPassword = new JLabel("Dejar en blanco para no cambiar la contraseña");

        } else { // Modo Creación
            setTitle("Nuevo Usuario");
            chkActivo.setSelected(true);
            chkActivo.setEnabled(false);
        }
    }

    /**
     * Recoge y valida los datos del formulario. Si son válidos, inicia una
     * operación asíncrona para persistir los cambios del usuario a través del servicio.
     * La ejecución en un hilo de fondo (SwingWorker) previene que la interfaz de usuario
     * se congele durante la operación de base de datos.
     * La contraseña se maneja de forma segura, limpiando los arrays de la memoria al finalizar.
     */
    private void guardarUsuario() {
        char[] password = txtPassword.getPassword();
        char[] confirmPassword = txtConfirmPassword.getPassword();

        try {
            // Sección de validación de datos de entrada en la UI.
            if (txtNombreUsuario.getText().isBlank() || txtNombreCompleto.getText().isBlank() || txtEmail.getText().isBlank()) {
                throw new IllegalArgumentException("Todos los campos de texto son obligatorios.");
            }
            if (usuario.getId() == null && password.length == 0) {
                throw new IllegalArgumentException("La contraseña es obligatoria para nuevos usuarios.");
            }
            if (!Arrays.equals(password, confirmPassword)) {
                throw new IllegalArgumentException("Las contraseñas no coinciden.");
            }

            // Asignación de los datos del formulario al objeto entidad.
            usuario.setNombreUsuario(txtNombreUsuario.getText().trim());
            usuario.setNombreCompleto(txtNombreCompleto.getText().trim());
            usuario.setEmail(txtEmail.getText().trim());
            usuario.setRol((Rol) cmbRol.getSelectedItem());
            usuario.setActivo(chkActivo.isSelected());

            String newPasswordStr = new String(password);

            // Tarea asíncrona para la operación de guardado.
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    // Lógica de negocio ejecutada en un hilo de fondo.
                    if (usuario.getId() == null) {
                        servicioUsuario.crearUsuario(usuario, newPasswordStr);
                    } else {
                        servicioUsuario.actualizarUsuario(usuario, newPasswordStr);
                    }
                    return null;
                }

                @Override
                protected void done() {
                    // Lógica ejecutada en el hilo de la UI al finalizar.
                    try {
                        get(); // Lanza excepción si doInBackground falló.
                        JOptionPane.showMessageDialog(DialogoUsuario.this, "Usuario guardado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                        onSaveSuccess.run();
                        dispose();
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(DialogoUsuario.this, "Error al guardar el usuario: " + e.getCause().getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }.execute();

        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error de Validación", JOptionPane.ERROR_MESSAGE);
        } finally {
            // Limpieza segura de las contraseñas en memoria, independientemente del resultado.
            Arrays.fill(password, ' ');
            Arrays.fill(confirmPassword, ' ');
        }
    }
}