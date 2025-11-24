package com.samvitex.ui.paneles.sub_paneles;

import com.samvitex.modelos.entidades.Categoria;
import com.samvitex.servicios.ServicioCategoria;
import com.samvitex.ui.dialogos.DialogoCategoria;
import com.samvitex.ui.presentadores.GestionCategoriasPresenter;
import com.samvitex.ui.theme.SamvitexButton;
import com.samvitex.ui.vistas.interfaces.GestionCategoriasView;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel de Swing que implementa la {@link GestionCategoriasView}.
 * Muestra una tabla con las categorías y botones para interactuar con ellas.
 */
public class PanelGestionCategorias extends JPanel implements GestionCategoriasView {

    private final GestionCategoriasPresenter presenter;
    private final ServicioCategoria servicioCategoria;
    private JTable tablaCategorias;
    private DefaultTableModel tableModel;
    private List<Categoria> cacheCategorias = new ArrayList<>();

    /**
     * Construye el panel de gestión de categorías.
     *
     * @param servicioCategoria El servicio para las operaciones de categoría.
     */
    public PanelGestionCategorias(ServicioCategoria servicioCategoria) {
        this.servicioCategoria = servicioCategoria;
        this.presenter = new GestionCategoriasPresenter(this, servicioCategoria);
        inicializarUI();
        presenter.cargarCategorias();
    }

    private void inicializarUI() {
        setLayout(new MigLayout("fill, insets 0", "[grow]", "[][grow]"));

        // Panel de Acciones
        JPanel panelAcciones = new JPanel(new MigLayout("insets 0", "[][][]"));
        JButton btnNuevo = new SamvitexButton("Nueva Categoría");
        JButton btnEditar = new SamvitexButton("Editar", SamvitexButton.ButtonType.SECONDARY);
        JButton btnEliminar = new SamvitexButton("Eliminar", SamvitexButton.ButtonType.SECONDARY);

        panelAcciones.add(btnNuevo);
        panelAcciones.add(btnEditar);
        panelAcciones.add(btnEliminar);
        add(panelAcciones, "dock north, gapy 0 10");

        // Tabla de Categorías
        String[] columnNames = {"ID", "Nombre", "Descripción"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaCategorias = new JTable(tableModel);
        tablaCategorias.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(tablaCategorias), "grow");

        // Listeners
        btnNuevo.addActionListener(e -> presenter.onNuevaCategoriaClicked());

        btnEditar.addActionListener(e -> {
            int selectedRow = tablaCategorias.getSelectedRow();
            if (selectedRow >= 0) {
                presenter.onEditarCategoriaClicked(cacheCategorias.get(tablaCategorias.convertRowIndexToModel(selectedRow)));
            } else {
                mostrarError("Por favor, seleccione una categoría para editar.");
            }
        });

        btnEliminar.addActionListener(e -> {
            int selectedRow = tablaCategorias.getSelectedRow();
            if (selectedRow >= 0) {
                presenter.onEliminarCategoriaClicked(cacheCategorias.get(tablaCategorias.convertRowIndexToModel(selectedRow)));
            } else {
                mostrarError("Por favor, seleccione una categoría para eliminar.");
            }
        });
    }

    @Override
    public void mostrarCategorias(List<Categoria> categorias) {
        this.cacheCategorias = categorias;
        tableModel.setRowCount(0);
        for (Categoria categoria : categorias) {
            tableModel.addRow(new Object[]{
                    categoria.getId(),
                    categoria.getNombre(),
                    categoria.getDescripcion()
            });
        }
    }

    @Override
    public void mostrarDialogoCategoria(Categoria categoria) {
        DialogoCategoria dialogo = new DialogoCategoria(
                (Frame) SwingUtilities.getWindowAncestor(this),
                this.servicioCategoria,
                categoria,
                this::refrescarVista
        );
        dialogo.setVisible(true);
    }

    @Override
    public void refrescarVista() {
        presenter.cargarCategorias();
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