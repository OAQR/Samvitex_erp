package com.samvitex.ui.dialogos;

import com.samvitex.modelos.entidades.Almacen;
import com.samvitex.servicios.ServicioAlmacen;
import com.samvitex.ui.theme.SamvitexButton;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

/**
 * Diálogo modal para la creación y edición de entidades {@link Almacen}.
 */
public class DialogoAlmacen extends JDialog {

    private final ServicioAlmacen servicioAlmacen;
    private final Almacen almacen;
    private final Runnable onSaveSuccess;

    // Componentes del formulario
    private JTextField txtNombre;
    private JTextArea txtDescripcion;
    private JCheckBox chkActivo;

    /**
     * Construye el diálogo.
     *
     * @param owner El Frame padre.
     * @param sa El servicio de negocio para almacenes.
     * @param almacen El almacén a editar, o {@code null} para crear uno nuevo.
     * @param onSaveSuccess Callback para ejecutar tras un guardado exitoso.
     */
    public DialogoAlmacen(Frame owner, ServicioAlmacen sa, Almacen almacen, Runnable onSaveSuccess) {
        super(owner, true);
        this.servicioAlmacen = sa;
        this.almacen = (almacen != null) ? almacen : new Almacen();
        this.onSaveSuccess = onSaveSuccess;

        setTitle(almacen != null ? "Editar Almacén" : "Nuevo Almacén");
        setSize(400, 300);
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

        txtNombre = new JTextField();
        txtDescripcion = new JTextArea(5, 20);
        chkActivo = new JCheckBox("Activo");

        panelFormulario.add(new JLabel("Nombre:"));
        panelFormulario.add(txtNombre);
        panelFormulario.add(new JLabel("Descripción/Ubicación:"), "top");
        panelFormulario.add(new JScrollPane(txtDescripcion), "grow");
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
     * Carga los datos del almacén en el formulario si se está editando.
     */
    private void cargarDatos() {
        if (almacen.getId() != null) {
            txtNombre.setText(almacen.getNombre());
            txtDescripcion.setText(almacen.getUbicacionDescripcion());
            chkActivo.setSelected(almacen.isActivo());
        } else {
            chkActivo.setSelected(true);
        }
    }

    /**
     * Valida y persiste los datos del almacén de forma asíncrona.
     * Este metodo recopila los datos del formulario, los valida y luego invoca al
     * servicio para guardar la información en un hilo de fondo, mostrando el resultado al usuario
     * una vez completado.
     */
    private void guardar() {
        if (txtNombre.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "El nombre del almacén no puede estar vacío.", "Error de Validación", JOptionPane.ERROR_MESSAGE);
            return;
        }

        almacen.setNombre(txtNombre.getText().trim());
        almacen.setUbicacionDescripcion(txtDescripcion.getText().trim());
        almacen.setActivo(chkActivo.isSelected());

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                // Lógica de guardado en hilo secundario.
                servicioAlmacen.guardar(almacen);
                return null;
            }

            @Override
            protected void done() {
                // Resultado manejado en el hilo de la UI.
                try {
                    get();
                    JOptionPane.showMessageDialog(DialogoAlmacen.this, "Almacén guardado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    onSaveSuccess.run();
                    dispose();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(DialogoAlmacen.this, "Error al guardar el almacén: " + e.getCause().getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }
}