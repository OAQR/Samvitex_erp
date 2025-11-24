package com.samvitex.ui.paneles.sub_paneles;

import com.samvitex.modelos.entidades.Almacen;
import com.samvitex.servicios.ServicioAlmacen;
import com.samvitex.ui.dialogos.DialogoAlmacen;
import com.samvitex.ui.presentadores.GestionAlmacenesPresenter;
import com.samvitex.ui.theme.SamvitexButton;
import com.samvitex.ui.vistas.interfaces.GestionAlmacenesView;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel de Swing que implementa la {@link GestionAlmacenesView}.
 * Muestra y permite la gestión de los almacenes o depósitos.
 */
public class PanelGestionAlmacenes extends JPanel implements GestionAlmacenesView {

    private final GestionAlmacenesPresenter presenter;
    private final ServicioAlmacen servicioAlmacen;
    private JTable tablaAlmacenes;
    private DefaultTableModel tableModel;
    private List<Almacen> cacheAlmacenes = new ArrayList<>();

    public PanelGestionAlmacenes(ServicioAlmacen servicioAlmacen) {
        this.servicioAlmacen = servicioAlmacen;
        this.presenter = new GestionAlmacenesPresenter(this, servicioAlmacen);
        inicializarUI();
        presenter.cargarAlmacenes();
    }

    private void inicializarUI() {
        setLayout(new MigLayout("fill, insets 0", "[grow]", "[][grow]"));

        JPanel panelAcciones = new JPanel(new MigLayout("insets 0", "[][][]"));
        JButton btnNuevo = new SamvitexButton("Nuevo Almacén");
        JButton btnEditar = new SamvitexButton("Editar", SamvitexButton.ButtonType.SECONDARY);
        JButton btnDesactivar = new SamvitexButton("Desactivar", SamvitexButton.ButtonType.SECONDARY);
        panelAcciones.add(btnNuevo);
        panelAcciones.add(btnEditar);
        panelAcciones.add(btnDesactivar);
        add(panelAcciones, "dock north, gapy 0 10");

        String[] columnNames = {"ID", "Nombre", "Descripción/Ubicación", "Estado"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaAlmacenes = new JTable(tableModel);
        tablaAlmacenes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(tablaAlmacenes), "grow");

        btnNuevo.addActionListener(e -> presenter.onNuevoAlmacenClicked());

        btnEditar.addActionListener(e -> {
            int selectedRow = tablaAlmacenes.getSelectedRow();
            if (selectedRow >= 0) {
                presenter.onEditarAlmacenClicked(cacheAlmacenes.get(tablaAlmacenes.convertRowIndexToModel(selectedRow)));
            } else {
                mostrarError("Por favor, seleccione un almacén para editar.");
            }
        });

        btnDesactivar.addActionListener(e -> {
            int selectedRow = tablaAlmacenes.getSelectedRow();
            if (selectedRow >= 0) {
                presenter.onDesactivarAlmacenClicked(cacheAlmacenes.get(tablaAlmacenes.convertRowIndexToModel(selectedRow)));
            } else {
                mostrarError("Por favor, seleccione un almacén para desactivar.");
            }
        });
    }

    @Override
    public void mostrarAlmacenes(List<Almacen> almacenes) {
        this.cacheAlmacenes = almacenes;
        tableModel.setRowCount(0);
        for (Almacen a : almacenes) {
            tableModel.addRow(new Object[]{
                    a.getId(),
                    a.getNombre(),
                    a.getUbicacionDescripcion(),
                    a.isActivo() ? "Activo" : "Inactivo"
            });
        }
    }

    @Override
    public void mostrarDialogoAlmacen(Almacen almacen) {
        DialogoAlmacen dialogo = new DialogoAlmacen(
                (Frame) SwingUtilities.getWindowAncestor(this),
                this.servicioAlmacen,
                almacen,
                this::refrescarVista
        );
        dialogo.setVisible(true);
    }

    @Override
    public void refrescarVista() {
        presenter.cargarAlmacenes();
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