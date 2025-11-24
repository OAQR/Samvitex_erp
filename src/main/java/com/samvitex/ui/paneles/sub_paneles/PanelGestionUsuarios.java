package com.samvitex.ui.paneles.sub_paneles;

import com.samvitex.modelos.entidades.Rol;
import com.samvitex.modelos.entidades.Usuario;
import com.samvitex.repositorios.RolRepositorio;
import com.samvitex.servicios.ServicioUsuario;
import com.samvitex.ui.dialogos.DialogoUsuario;
import com.samvitex.ui.presentadores.GestionUsuariosPresenter;
import com.samvitex.ui.theme.SamvitexButton;
import com.samvitex.ui.vistas.interfaces.GestionUsuariosView;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Panel de Swing que implementa la {@link GestionUsuariosView}.
 * <p>
 * Es la implementación concreta de la vista para la gestión de usuarios. Su única
 * responsabilidad es construir la interfaz gráfica (tabla, botones) y delegar
 * todos los eventos de usuario (clics, etc.) al {@link GestionUsuariosPresenter}.
 * Esta clase no contiene lógica de negocio ni de flujo de aplicación.
 */
public class PanelGestionUsuarios extends JPanel implements GestionUsuariosView {

    private final GestionUsuariosPresenter presenter;
    private final ServicioUsuario servicioUsuario; // Dependencia para el diálogo

    private JTable tablaUsuarios;
    private DefaultTableModel tableModel;

    /**
     * Construye el panel de gestión de usuarios.
     *
     * @param servicioUsuario El servicio para las operaciones de usuario.
     * @param rolRepositorio El repositorio para obtener la lista de roles.
     */
    public PanelGestionUsuarios(ServicioUsuario servicioUsuario, RolRepositorio rolRepositorio) {
        this.servicioUsuario = servicioUsuario;
        // La vista crea su Presenter, pasándose a sí misma y sus dependencias de servicio.
        this.presenter = new GestionUsuariosPresenter(this, servicioUsuario, rolRepositorio);

        inicializarUI();
    }

    /**
     * Se invoca cuando el panel es añadido a un contenedor visible.
     * Es el momento ideal para solicitar la carga de datos inicial.
     */
    @Override
    public void addNotify() {
        super.addNotify();
        presenter.cargarUsuarios();
    }

    /**
     * Configura y ensambla todos los componentes de la interfaz gráfica del panel.
     */
    private void inicializarUI() {
        setLayout(new MigLayout("fill, insets 0", "[grow]", "[][grow]"));

        // Panel de Acciones en la parte superior
        JPanel panelAcciones = new JPanel(new MigLayout("insets 0", "[][][]"));
        JButton btnNuevo = new SamvitexButton("Nuevo Usuario");
        JButton btnEditar = new SamvitexButton("Editar", SamvitexButton.ButtonType.SECONDARY);
        JButton btnToggleEstado = new SamvitexButton("Activar/Desactivar", SamvitexButton.ButtonType.SECONDARY);

        panelAcciones.add(btnNuevo);
        panelAcciones.add(btnEditar);
        panelAcciones.add(btnToggleEstado);
        add(panelAcciones, "dock north, gapy 0 10");

        // Tabla de Usuarios
        String[] columnNames = {"ID", "Usuario", "Nombre Completo", "Email", "Rol", "Estado"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaUsuarios = new JTable(tableModel);
        tablaUsuarios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaUsuarios.setAutoCreateRowSorter(true); // Habilita el ordenamiento por columnas
        add(new JScrollPane(tablaUsuarios), "grow");

        // --- Action Listeners: Delegan 100% al Presenter ---
        btnNuevo.addActionListener(e -> presenter.onNuevoUsuarioClicked());

        btnEditar.addActionListener(e -> {
            int selectedRow = tablaUsuarios.getSelectedRow();
            if (selectedRow >= 0) {
                Integer usuarioId = (Integer) tableModel.getValueAt(tablaUsuarios.convertRowIndexToModel(selectedRow), 0);
                presenter.onEditarUsuarioClicked(usuarioId);
            } else {
                mostrarError("Por favor, seleccione un usuario para editar.");
            }
        });

        btnToggleEstado.addActionListener(e -> {
            int selectedRow = tablaUsuarios.getSelectedRow();
            if (selectedRow >= 0) {
                int modelRow = tablaUsuarios.convertRowIndexToModel(selectedRow);
                Integer usuarioId = (Integer) tableModel.getValueAt(modelRow, 0);
                String nombreUsuario = (String) tableModel.getValueAt(modelRow, 1);
                boolean estadoActual = "Activo".equals(tableModel.getValueAt(modelRow, 5));
                presenter.onToggleEstadoUsuarioClicked(usuarioId, nombreUsuario, estadoActual);
            } else {
                mostrarError("Por favor, seleccione un usuario para cambiar su estado.");
            }
        });
    }

    // --- Implementación de los métodos del contrato GestionUsuariosView ---

    @Override
    public void mostrarUsuarios(List<Usuario> usuarios) {
        tableModel.setRowCount(0); // Limpia la tabla antes de poblarla
        for (Usuario usuario : usuarios) {
            // El acceso a getRol() podría causar LazyInitializationException si no se usa JOIN FETCH
            tableModel.addRow(new Object[]{
                    usuario.getId(),
                    usuario.getNombreUsuario(),
                    usuario.getNombreCompleto(),
                    usuario.getEmail(),
                    usuario.getRol().getNombre(), // Acceso seguro gracias al JOIN FETCH en el repositorio
                    usuario.isActivo() ? "Activo" : "Inactivo"
            });
        }
    }

    @Override
    public void mostrarDialogoUsuario(Usuario usuario, List<Rol> rolesDisponibles) {
        DialogoUsuario dialogo = new DialogoUsuario(
                (Frame) SwingUtilities.getWindowAncestor(this),
                this.servicioUsuario, // Se pasa el servicio al diálogo
                rolesDisponibles,
                usuario,
                this::refrescarVista // Se pasa un 'Runnable' para el callback de éxito
        );
        dialogo.setVisible(true);
    }

    @Override
    public void refrescarVista() {
        presenter.cargarUsuarios();
    }

    @Override
    public boolean confirmarAccion(String mensaje, String titulo) {
        return JOptionPane.showConfirmDialog(this, mensaje, titulo, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    @Override
    public void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void mostrarMensajeExito(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }
}