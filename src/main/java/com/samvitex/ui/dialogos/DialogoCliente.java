package com.samvitex.ui.dialogos;

import com.samvitex.modelos.entidades.Cliente;
import com.samvitex.servicios.ServicioCliente;
import com.samvitex.ui.theme.SamvitexButton;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

/**
 * Diálogo modal para la creación y edición de entidades {@link Cliente}.
 */
public class DialogoCliente extends JDialog {

    private final ServicioCliente servicioCliente;
    private final Cliente cliente;
    private final Runnable onSaveSuccess;

    // Componentes del formulario
    private JTextField txtNombreCompleto;
    private JTextField txtDniRuc;
    private JTextField txtEmail;
    private JTextField txtTelefono;
    private JTextArea txtDireccion;
    private JCheckBox chkActivo;

    /**
     * Construye el diálogo.
     *
     * @param owner El Frame padre.
     * @param sc El servicio de negocio para clientes.
     * @param cliente El cliente a editar, o {@code null} para crear uno nuevo.
     * @param onSaveSuccess Callback para ejecutar tras un guardado exitoso.
     */
    public DialogoCliente(Frame owner, ServicioCliente sc, Cliente cliente, Runnable onSaveSuccess) {
        super(owner, true);
        this.servicioCliente = sc;
        this.cliente = (cliente != null) ? cliente : new Cliente();
        this.onSaveSuccess = onSaveSuccess;

        setTitle(cliente != null ? "Editar Cliente" : "Nuevo Cliente");
        setSize(480, 400);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        inicializarUI();
        cargarDatos();
    }

    /**
     * Inicializa y organiza los componentes de la UI.
     */
    private void inicializarUI() {
        JPanel panelFormulario = new JPanel(new MigLayout("wrap 2, fillx, insets 15", "[right]15[grow,fill]"));

        txtNombreCompleto = new JTextField();
        txtDniRuc = new JTextField();
        txtEmail = new JTextField();
        txtTelefono = new JTextField();
        txtDireccion = new JTextArea(3, 20);
        chkActivo = new JCheckBox("Activo");

        panelFormulario.add(new JLabel("Nombre Completo/Razón Social:"));
        panelFormulario.add(txtNombreCompleto);
        panelFormulario.add(new JLabel("DNI/RUC:"));
        panelFormulario.add(txtDniRuc);
        panelFormulario.add(new JLabel("Email:"));
        panelFormulario.add(txtEmail);
        panelFormulario.add(new JLabel("Teléfono:"));
        panelFormulario.add(txtTelefono);
        panelFormulario.add(new JLabel("Dirección:"), "top");
        panelFormulario.add(new JScrollPane(txtDireccion), "grow");
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
     * Carga los datos del cliente en el formulario si se está editando.
     */
    private void cargarDatos() {
        if (cliente.getId() != null) {
            txtNombreCompleto.setText(cliente.getNombreCompleto());
            txtDniRuc.setText(cliente.getDniRuc());
            txtEmail.setText(cliente.getEmail());
            txtTelefono.setText(cliente.getTelefono());
            txtDireccion.setText(cliente.getDireccion());
            chkActivo.setSelected(cliente.isActivo());
        } else {
            chkActivo.setSelected(true);
        }
    }

    /**
     * Valida y persiste los datos del cliente de forma asíncrona.
     * Reúne la información del formulario, la valida y utiliza el servicio correspondiente
     * para guardar los cambios en la base de datos en un hilo de fondo.
     */
    private void guardar() {
        if (txtNombreCompleto.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "El nombre del cliente no puede estar vacío.", "Error de Validación", JOptionPane.ERROR_MESSAGE);
            return;
        }

        cliente.setNombreCompleto(txtNombreCompleto.getText().trim());
        cliente.setDniRuc(txtDniRuc.getText().trim());
        cliente.setEmail(txtEmail.getText().trim());
        cliente.setTelefono(txtTelefono.getText().trim());
        cliente.setDireccion(txtDireccion.getText().trim());
        cliente.setActivo(chkActivo.isSelected());

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                // Ejecución de la lógica de negocio en segundo plano.
                servicioCliente.guardar(cliente);
                return null;
            }

            @Override
            protected void done() {
                // Actualización de la UI tras la finalización de la tarea.
                try {
                    get();
                    JOptionPane.showMessageDialog(DialogoCliente.this, "Cliente guardado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    onSaveSuccess.run();
                    dispose();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(DialogoCliente.this, "Error al guardar el cliente: " + e.getCause().getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }
}