package com.samvitex.ui.dialogos;

import com.samvitex.modelos.entidades.Proveedor;
import com.samvitex.servicios.ServicioProveedor;
import com.samvitex.ui.theme.SamvitexButton;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

/**
 * Diálogo modal para la creación y edición de entidades {@link Proveedor}.
 * Proporciona un formulario para introducir los datos del proveedor y gestiona
 * la persistencia a través del {@link ServicioProveedor}.
 */
public class DialogoProveedor extends JDialog {

    private final ServicioProveedor servicioProveedor;
    private final Proveedor proveedor;
    private final Runnable onSaveSuccess;

    // Componentes del formulario
    private JTextField txtNombre;
    private JTextField txtRuc;
    private JTextField txtEmail;
    private JTextField txtTelefono;
    private JCheckBox chkActivo;

    /**
     * Construye el diálogo.
     *
     * @param owner El Frame padre sobre el cual el diálogo será modal.
     * @param sp El servicio de negocio para las operaciones de proveedor.
     * @param proveedor El proveedor a editar. Si es {@code null}, el diálogo se abre en modo creación.
     * @param onSaveSuccess Un callback {@link Runnable} que se ejecuta tras un guardado exitoso para refrescar la vista principal.
     */
    public DialogoProveedor(Frame owner, ServicioProveedor sp, Proveedor proveedor, Runnable onSaveSuccess) {
        super(owner, true);
        this.servicioProveedor = sp;
        this.proveedor = (proveedor != null) ? proveedor : new Proveedor();
        this.onSaveSuccess = onSaveSuccess;

        setTitle(proveedor != null ? "Editar Proveedor" : "Nuevo Proveedor");
        setSize(450, 350);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        inicializarUI();
        cargarDatos();
    }

    /**
     * Inicializa y organiza los componentes de la interfaz de usuario.
     */
    private void inicializarUI() {
        JPanel panelFormulario = new JPanel(new MigLayout("wrap 2, fillx, insets 15", "[right]15[grow,fill]"));

        txtNombre = new JTextField();
        txtRuc = new JTextField();
        txtEmail = new JTextField();
        txtTelefono = new JTextField();
        chkActivo = new JCheckBox("Activo");

        panelFormulario.add(new JLabel("Nombre/Razón Social:"));
        panelFormulario.add(txtNombre);
        panelFormulario.add(new JLabel("RUC:"));
        panelFormulario.add(txtRuc);
        panelFormulario.add(new JLabel("Email de Contacto:"));
        panelFormulario.add(txtEmail);
        panelFormulario.add(new JLabel("Teléfono de Contacto:"));
        panelFormulario.add(txtTelefono);
        panelFormulario.add(chkActivo, "span 2, gapleft 5");

        add(panelFormulario, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnGuardar = new SamvitexButton("Guardar");
        JButton btnCancelar = new SamvitexButton("Cancelar", SamvitexButton.ButtonType.SECONDARY);
        panelBotones.add(btnGuardar);
        panelBotones.add(btnCancelar);
        add(panelBotones, BorderLayout.SOUTH);

        btnCancelar.addActionListener(e -> dispose());
        btnGuardar.addActionListener(e -> guardar());
    }

    /**
     * Carga los datos del proveedor en el formulario si se está en modo edición.
     */
    private void cargarDatos() {
        if (proveedor.getId() != null) {
            txtNombre.setText(proveedor.getNombre());
            txtRuc.setText(proveedor.getRuc());
            txtEmail.setText(proveedor.getContactoEmail());
            txtTelefono.setText(proveedor.getContactoTelefono());
            chkActivo.setSelected(proveedor.isActivo());
        } else {
            chkActivo.setSelected(true); // Por defecto, un nuevo proveedor está activo.
        }
    }

    /**
     * Valida y persiste los datos del proveedor de forma asíncrona.
     * Recoge los datos del formulario, los asigna al objeto proveedor y llama al servicio
     * para persistirlos en un hilo de fondo, manejando el resultado en la UI al finalizar.
     */
    private void guardar() {
        if (txtNombre.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "El nombre del proveedor no puede estar vacío.", "Error de Validación", JOptionPane.ERROR_MESSAGE);
            return;
        }

        proveedor.setNombre(txtNombre.getText().trim());
        proveedor.setRuc(txtRuc.getText().trim());
        proveedor.setContactoEmail(txtEmail.getText().trim());
        proveedor.setContactoTelefono(txtTelefono.getText().trim());
        proveedor.setActivo(chkActivo.isSelected());

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                // Llama al servicio en un hilo de fondo.
                servicioProveedor.guardar(proveedor);
                return null;
            }

            @Override
            protected void done() {
                // Procesa el resultado en el hilo de la UI.
                try {
                    get();
                    JOptionPane.showMessageDialog(DialogoProveedor.this, "Proveedor guardado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    onSaveSuccess.run();
                    dispose();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(DialogoProveedor.this, "Error al guardar el proveedor: " + e.getCause().getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }
}