package com.samvitex.ui.dialogos;

import com.samvitex.modelos.entidades.Categoria;
import com.samvitex.servicios.ServicioCategoria;
import com.samvitex.ui.theme.SamvitexButton;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

/**
 * Diálogo modal para la creación y edición de entidades {@link Categoria}.
 * Proporciona un formulario para los datos de la categoría y se comunica
 * con el {@link ServicioCategoria} para su persistencia.
 */
public class DialogoCategoria extends JDialog {

    private final ServicioCategoria servicioCategoria;
    private final Categoria categoria;
    private final Runnable onSaveSuccess;

    // Componentes del formulario
    private JTextField txtNombre;
    private JTextArea txtDescripcion;

    /**
     * Construye el diálogo para gestionar una categoría.
     *
     * @param owner El Frame padre sobre el cual el diálogo será modal.
     * @param sc El servicio de negocio para las operaciones de categoría.
     * @param categoria La categoría a editar. Si es {@code null}, el diálogo se abre en modo creación.
     * @param onSaveSuccess Un callback {@link Runnable} que se ejecuta tras un guardado exitoso,
     *                      típicamente para refrescar la vista principal.
     */
    public DialogoCategoria(Frame owner, ServicioCategoria sc, Categoria categoria, Runnable onSaveSuccess) {
        super(owner, true);
        this.servicioCategoria = sc;
        this.categoria = (categoria != null) ? categoria : new Categoria();
        this.onSaveSuccess = onSaveSuccess;

        setTitle(categoria != null ? "Editar Categoría" : "Nueva Categoría");
        setSize(400, 300);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        inicializarUI();
        cargarDatos();
    }

    /**
     * Inicializa y organiza los componentes de la interfaz de usuario del diálogo.
     */
    private void inicializarUI() {
        JPanel panelFormulario = new JPanel(new MigLayout("wrap 2, fillx, insets 15", "[right]15[grow,fill]"));

        txtNombre = new JTextField();
        txtDescripcion = new JTextArea(5, 20);
        txtDescripcion.setLineWrap(true);
        txtDescripcion.setWrapStyleWord(true);

        panelFormulario.add(new JLabel("Nombre:"));
        panelFormulario.add(txtNombre, "growx");
        panelFormulario.add(new JLabel("Descripción:"), "top, aligny top"); // Alineación vertical
        panelFormulario.add(new JScrollPane(txtDescripcion), "grow");

        add(panelFormulario, BorderLayout.CENTER);

        // Panel inferior con botones de acción
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnGuardar = new SamvitexButton("Guardar");
        JButton btnCancelar = new SamvitexButton("Cancelar", SamvitexButton.ButtonType.SECONDARY);

        panelBotones.add(btnGuardar);
        panelBotones.add(btnCancelar);
        add(panelBotones, BorderLayout.SOUTH);

        // Asignación de listeners
        btnCancelar.addActionListener(e -> dispose());
        btnGuardar.addActionListener(e -> guardar());
    }

    /**
     * Carga los datos de la entidad {@link Categoria} en los componentes del formulario.
     * Este metodo se invoca al abrir el diálogo en modo de edición.
     */
    private void cargarDatos() {
        if (categoria.getId() != null) {
            txtNombre.setText(categoria.getNombre());
            txtDescripcion.setText(categoria.getDescripcion());
        }
    }

    /**
     * Recoge los datos del formulario, valida que el nombre no esté vacío, y luego
     * ejecuta la operación de guardado de forma asíncrona utilizando un SwingWorker.
     * Esto asegura que la llamada al servicio (que puede acceder a la red/disco)
     * no bloquee la interfaz de usuario.
     */
    private void guardar() {
        if (txtNombre.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "El nombre de la categoría no puede estar vacío.", "Error de Validación", JOptionPane.ERROR_MESSAGE);
            return;
        }

        categoria.setNombre(txtNombre.getText().trim());
        categoria.setDescripcion(txtDescripcion.getText().trim());

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                // Llama al servicio en un hilo de fondo.
                servicioCategoria.guardar(categoria);
                return null;
            }

            @Override
            protected void done() {
                // Procesa el resultado en el hilo de la UI.
                try {
                    get();
                    JOptionPane.showMessageDialog(DialogoCategoria.this, "Categoría guardada exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    onSaveSuccess.run();
                    dispose();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(DialogoCategoria.this, "Error al guardar la categoría: " + e.getCause().getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }
}