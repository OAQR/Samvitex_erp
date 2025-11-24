package com.samvitex.ui.paneles;

import com.samvitex.modelos.entidades.OrdenProduccion;
import com.samvitex.modelos.enums.EstadoProduccion;
import com.samvitex.repositorios.TallerRepositorio;
import com.samvitex.servicios.ServicioAlmacen;
import com.samvitex.servicios.ServicioInventario;
import com.samvitex.servicios.ServicioProduccion;
import com.samvitex.ui.dialogos.DialogoOrdenProduccion;
import com.samvitex.ui.modelos_tabla.OrdenProduccionTableModel;
import com.samvitex.ui.presentadores.ProduccionPresenter;
import com.samvitex.ui.theme.SamvitexButton;
import com.samvitex.ui.vistas.interfaces.ProduccionView;
import net.miginfocom.swing.MigLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.Component;
import java.util.List;

/**
 * Panel que implementa la {@link ProduccionView} para gestionar órdenes de producción.
 * La UI se ha mejorado para mostrar más detalles y usar colores para los estados.
 */
@org.springframework.stereotype.Component
public class PanelProduccion extends JPanel implements ProduccionView {

    private final ProduccionPresenter presenter;
    private final ApplicationContext springContext;

    private OrdenProduccionTableModel tableModel;
    private JTable tablaOrdenes;
    private JButton btnIniciar, btnFinalizar;

    @Autowired
    public PanelProduccion(ApplicationContext context) {
        this.springContext = context;
        this.presenter = new ProduccionPresenter(this, context.getBean(ServicioProduccion.class));
        inicializarUI();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        presenter.cargarOrdenes();
    }

    private void inicializarUI() {
        setLayout(new MigLayout("fill, insets 10", "[grow]", "[][grow]"));

        JPanel panelAcciones = new JPanel(new MigLayout("insets 0"));
        JButton btnNuevo = new SamvitexButton("Nueva Orden");
        btnIniciar = new SamvitexButton("Iniciar Producción", SamvitexButton.ButtonType.SECONDARY);
        btnFinalizar = new SamvitexButton("Finalizar Producción", SamvitexButton.ButtonType.SECONDARY);

        panelAcciones.add(btnNuevo);
        panelAcciones.add(btnIniciar, "gapleft 15");
        panelAcciones.add(btnFinalizar);
        add(panelAcciones, "dock north, gapy 0 10");

        tableModel = new OrdenProduccionTableModel();
        tablaOrdenes = new JTable(tableModel);
        tablaOrdenes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaOrdenes.setRowHeight(28);
        tablaOrdenes.getTableHeader().setReorderingAllowed(false);

        // CAMBIO: Añadir un renderer para colorear la columna de estado
        tablaOrdenes.getColumnModel().getColumn(2).setCellRenderer(new EstadoProduccionCellRenderer());

        add(new JScrollPane(tablaOrdenes), "grow");

        // Listeners (sin cambios funcionales, pero ahora más robustos)
        tablaOrdenes.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                actualizarEstadoBotones();
            }
        });

        btnNuevo.addActionListener(e -> presenter.onNuevaOrdenClicked());
        btnIniciar.addActionListener(e -> {
            int selectedRow = tablaOrdenes.getSelectedRow();
            if (selectedRow != -1) {
                presenter.onIniciarProduccionClicked(tableModel.getOrdenAt(tablaOrdenes.convertRowIndexToModel(selectedRow)));
            }
        });
        btnFinalizar.addActionListener(e -> {
            int selectedRow = tablaOrdenes.getSelectedRow();
            if (selectedRow != -1) {
                presenter.onFinalizarProduccionClicked(tableModel.getOrdenAt(tablaOrdenes.convertRowIndexToModel(selectedRow)));
            }
        });

        actualizarEstadoBotones(); // Estado inicial
    }

    private void actualizarEstadoBotones() {
        int selectedRow = tablaOrdenes.getSelectedRow();
        if (selectedRow == -1) {
            btnIniciar.setEnabled(false);
            btnFinalizar.setEnabled(false);
        } else {
            OrdenProduccion ordenSeleccionada = tableModel.getOrdenAt(tablaOrdenes.convertRowIndexToModel(selectedRow));
            if (ordenSeleccionada != null) {
                btnIniciar.setEnabled(ordenSeleccionada.getEstado() == EstadoProduccion.PLANIFICADA);
                btnFinalizar.setEnabled(ordenSeleccionada.getEstado() == EstadoProduccion.EN_PRODUCCION);
            }
        }
    }

    @Override
    public void mostrarOrdenes(List<OrdenProduccion> ordenes) {
        tableModel.setOrdenes(ordenes);
        actualizarEstadoBotones();
    }

    @Override
    public void abrirDialogoCrearOrden() {
        DialogoOrdenProduccion dialogo = new DialogoOrdenProduccion(
                (Frame) SwingUtilities.getWindowAncestor(this),
                springContext.getBean(ServicioProduccion.class),
                springContext.getBean(ServicioInventario.class),
                springContext.getBean(TallerRepositorio.class),
                springContext.getBean(ServicioAlmacen.class),
                this::refrescarVista
        );
        dialogo.setVisible(true);
    }

    @Override
    public void refrescarVista() {
        presenter.cargarOrdenes();
    }

    @Override
    public boolean confirmarAccion(String mensaje, String titulo) {
        return JOptionPane.showConfirmDialog(this, mensaje, titulo, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    @Override
    public void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error de Producción", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void mostrarMensajeExito(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Un TableCellRenderer personalizado para dar color a la celda del estado
     * de la orden de producción, mejorando la visibilidad y la UX.
     */
    private static class EstadoProduccionCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (value instanceof EstadoProduccion estado) {
                c.setForeground(switch (estado) {
                    case PLANIFICADA -> Color.BLUE;
                    case EN_PRODUCCION -> new Color(255, 140, 0); // Orange
                    case COMPLETADA -> new Color(0, 128, 0); // Green
                    case CANCELADA -> Color.RED;
                    default -> table.getForeground();
                });
                setText(estado.toString().replace("_", " "));
                setHorizontalAlignment(CENTER);
            }
            return c;
        }
    }
}