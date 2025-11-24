package com.samvitex.ui.paneles.sub_paneles;

import com.samvitex.modelos.entidades.Cliente;
import com.samvitex.servicios.ServicioCliente;
import com.samvitex.ui.dialogos.DialogoCliente;
import com.samvitex.ui.presentadores.GestionClientesPresenter;
import com.samvitex.ui.theme.SamvitexButton;
import com.samvitex.ui.vistas.interfaces.GestionClientesView;
import net.miginfocom.swing.MigLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel de Swing que implementa la {@link GestionClientesView}.
 * Muestra y permite la gestión de la lista de clientes.
 */
@org.springframework.stereotype.Component
public class PanelGestionClientes extends JPanel implements GestionClientesView {

    private final GestionClientesPresenter presenter;
    private final ServicioCliente servicioCliente;
    private JTable tablaClientes;
    private DefaultTableModel tableModel;
    private List<Cliente> cacheClientes = new ArrayList<>();
    private JTextField txtBuscar;
    private Timer searchTimer;

    @Autowired
    public PanelGestionClientes(@Lazy GestionClientesPresenter presenter, ServicioCliente servicioCliente) {
        this.presenter = presenter;
        this.presenter.setView(this);
        this.servicioCliente = servicioCliente;
        inicializarUI();
    }


    /**
     * Expone el presenter para que otros componentes (PanelConfiguracion) puedan interactuar con él.
     * @return La instancia del presenter asociado.
     */
    public GestionClientesPresenter getPresenter() {
        return presenter;
    }

    @Override
    public void addNotify() {
        super.addNotify();
        presenter.cargarClientes();
    }

    private void inicializarUI() {
        setLayout(new MigLayout("fill, insets 0", "[grow]", "[][][grow]"));

        txtBuscar = new JTextField();
        txtBuscar.putClientProperty("JTextField.placeholderText", "Buscar por Nombre, DNI/RUC o Email...");

        JButton btnNuevo = new SamvitexButton("Nuevo Cliente");
        JButton btnEditar = new SamvitexButton("Editar", SamvitexButton.ButtonType.SECONDARY);
        JButton btnDesactivar = new SamvitexButton("Desactivar", SamvitexButton.ButtonType.SECONDARY);

        String[] columnNames = {"ID", "Nombre Completo", "DNI/RUC", "Email", "Teléfono", "Estado"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaClientes = new JTable(tableModel);
        tablaClientes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Panel de Búsqueda
        JPanel panelBusqueda = new JPanel(new MigLayout("insets 0, fillx", "[][grow]"));
        panelBusqueda.add(new JLabel("Buscar Cliente:"));
        panelBusqueda.add(txtBuscar, "growx");
        add(panelBusqueda, "wrap, growx, gapy 0 10");

        // Panel de Acciones
        JPanel panelAcciones = new JPanel(new MigLayout("insets 0", "[][][]"));
        panelAcciones.add(btnNuevo);
        panelAcciones.add(btnEditar);
        panelAcciones.add(btnDesactivar);
        add(panelAcciones, "wrap, gapbottom 10");

        // Tabla
        add(new JScrollPane(tablaClientes), "grow");

        // Listener para la barra de búsqueda
        searchTimer = new Timer(300, e -> presenter.buscarClientes(txtBuscar.getText()));
        searchTimer.setRepeats(false);
        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { searchTimer.restart(); }
            public void removeUpdate(DocumentEvent e) { searchTimer.restart(); }
            public void changedUpdate(DocumentEvent e) { searchTimer.restart(); }
        });

        // Listeners de los botones
        btnNuevo.addActionListener(e -> presenter.onNuevoClienteClicked());

        btnEditar.addActionListener(e -> {
            int selectedRow = tablaClientes.getSelectedRow();
            if (selectedRow >= 0) {
                // Obtenemos el cliente del cache para evitar problemas con el ordenamiento de la tabla
                presenter.onEditarClienteClicked(cacheClientes.get(tablaClientes.convertRowIndexToModel(selectedRow)));
            } else {
                mostrarError("Por favor, seleccione un cliente para editar.");
            }
        });

        btnDesactivar.addActionListener(e -> {
            int selectedRow = tablaClientes.getSelectedRow();
            if (selectedRow >= 0) {
                presenter.onDesactivarClienteClicked(cacheClientes.get(tablaClientes.convertRowIndexToModel(selectedRow)));
            } else {
                mostrarError("Por favor, seleccione un cliente para desactivar.");
            }
        });
    }

    @Override
    public void mostrarClientes(List<Cliente> clientes) {
        this.cacheClientes = clientes;
        tableModel.setRowCount(0);
        for (Cliente c : clientes) {
            tableModel.addRow(new Object[]{
                    c.getId(),
                    c.getNombreCompleto(),
                    c.getDniRuc(),
                    c.getEmail(),
                    c.getTelefono(),
                    c.isActivo() ? "Activo" : "Inactivo"
            });
        }
    }

    @Override
    public void mostrarDialogoCliente(Cliente cliente) {
        DialogoCliente dialogo = new DialogoCliente(
                (Frame) SwingUtilities.getWindowAncestor(this),
                this.servicioCliente,
                cliente,
                this::refrescarVista
        );
        dialogo.setVisible(true);
    }

    @Override
    public void seleccionarYMostrarCliente(Integer clienteId) {
        SwingUtilities.invokeLater(() -> {
            for (int i = 0; i < cacheClientes.size(); i++) {
                if (cacheClientes.get(i).getId().equals(clienteId)) {
                    int filaEnVista = tablaClientes.convertRowIndexToView(i);
                    tablaClientes.setRowSelectionInterval(filaEnVista, filaEnVista);
                    tablaClientes.scrollRectToVisible(tablaClientes.getCellRect(filaEnVista, 0, true));
                    break;
                }
            }
        });
    }

    @Override
    public void refrescarVista() {
        presenter.cargarClientes();
    }

    @Override
    public boolean confirmarAccion(String mensaje, String titulo) {
        return JOptionPane.showConfirmDialog(this, mensaje, titulo, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    @Override
    public void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void mostrarMensajeExito(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void setTerminoBusqueda(String termino) {
        txtBuscar.setText(termino);
    }

}