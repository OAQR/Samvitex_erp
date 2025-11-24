package com.samvitex.ui.paneles.sub_paneles;

import com.samvitex.modelos.entidades.Proveedor;
import com.samvitex.servicios.ServicioProveedor;
import com.samvitex.ui.dialogos.DialogoProveedor;
import com.samvitex.ui.presentadores.GestionProveedoresPresenter;
import com.samvitex.ui.theme.SamvitexButton;
import com.samvitex.ui.vistas.interfaces.GestionProveedoresView;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel de Swing que implementa la {@link GestionProveedoresView}.
 * Muestra una tabla con los proveedores y permite su gestión a través de un Presenter.
 */
public class PanelGestionProveedores extends JPanel implements GestionProveedoresView {

    private final GestionProveedoresPresenter presenter;
    private final ServicioProveedor servicioProveedor;
    private JTable tablaProveedores;
    private DefaultTableModel tableModel;
    private List<Proveedor> cacheProveedores = new ArrayList<>();

    public PanelGestionProveedores(ServicioProveedor servicioProveedor) {
        this.servicioProveedor = servicioProveedor;
        this.presenter = new GestionProveedoresPresenter(this, servicioProveedor);
        inicializarUI();
        presenter.cargarProveedores();
    }

    private void inicializarUI() {
        setLayout(new MigLayout("fill, insets 0", "[grow]", "[][grow]"));

        JPanel panelAcciones = new JPanel(new MigLayout("insets 0", "[][][]"));
        JButton btnNuevo = new SamvitexButton("Nuevo Proveedor");
        JButton btnEditar = new SamvitexButton("Editar", SamvitexButton.ButtonType.SECONDARY);
        JButton btnDesactivar = new SamvitexButton("Desactivar", SamvitexButton.ButtonType.SECONDARY);
        panelAcciones.add(btnNuevo);
        panelAcciones.add(btnEditar);
        panelAcciones.add(btnDesactivar);
        add(panelAcciones, "dock north, gapy 0 10");

        String[] columnNames = {"ID", "Nombre", "RUC", "Email", "Teléfono", "Estado"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaProveedores = new JTable(tableModel);
        tablaProveedores.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(tablaProveedores), "grow");

        btnNuevo.addActionListener(e -> presenter.onNuevoProveedorClicked());

        btnEditar.addActionListener(e -> {
            int selectedRow = tablaProveedores.getSelectedRow();
            if (selectedRow >= 0) {
                presenter.onEditarProveedorClicked(cacheProveedores.get(tablaProveedores.convertRowIndexToModel(selectedRow)));
            } else {
                mostrarError("Por favor, seleccione un proveedor para editar.");
            }
        });

        btnDesactivar.addActionListener(e -> {
            int selectedRow = tablaProveedores.getSelectedRow();
            if (selectedRow >= 0) {
                presenter.onDesactivarProveedorClicked(cacheProveedores.get(tablaProveedores.convertRowIndexToModel(selectedRow)));
            } else {
                mostrarError("Por favor, seleccione un proveedor para desactivar.");
            }
        });
    }

    @Override
    public void mostrarProveedores(List<Proveedor> proveedores) {
        this.cacheProveedores = proveedores;
        tableModel.setRowCount(0);
        for (Proveedor p : proveedores) {
            tableModel.addRow(new Object[]{
                    p.getId(),
                    p.getNombre(),
                    p.getRuc(),
                    p.getContactoEmail(),
                    p.getContactoTelefono(),
                    p.isActivo() ? "Activo" : "Inactivo"
            });
        }
    }

    @Override
    public void mostrarDialogoProveedor(Proveedor proveedor) {
        DialogoProveedor dialogo = new DialogoProveedor(
                (Frame) SwingUtilities.getWindowAncestor(this),
                this.servicioProveedor,
                proveedor,
                this::refrescarVista
        );
        dialogo.setVisible(true);
    }

    @Override
    public void refrescarVista() {
        presenter.cargarProveedores();
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
}