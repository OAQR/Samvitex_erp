package com.samvitex.ui.paneles;

import com.samvitex.modelos.dto.ProductoInventarioDTO;
import com.samvitex.modelos.entidades.Almacen;
import com.samvitex.modelos.entidades.InventarioPorAlmacen;
import com.samvitex.modelos.entidades.Producto;
import com.samvitex.servicios.ServicioAlmacen;
import com.samvitex.servicios.ServicioCategoria;
import com.samvitex.servicios.ServicioInventario;
import com.samvitex.servicios.ServicioProveedor;
import com.samvitex.ui.dialogos.DialogoProducto;
import com.samvitex.ui.modelos_tabla.ProductoTableModel;
import com.samvitex.ui.presentadores.InventarioPresenter;
import com.samvitex.ui.theme.SamvitexButton;
import com.samvitex.ui.vistas.interfaces.InventarioView;
import net.miginfocom.swing.MigLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Objects;

@org.springframework.stereotype.Component
public class PanelInventario extends JPanel implements InventarioView {

    private final InventarioPresenter presenter;
    private final ServicioInventario servicioInventario;
    private final ServicioCategoria servicioCategoria;
    private final ServicioProveedor servicioProveedor;
    private final ServicioAlmacen servicioAlmacen;

    // Componentes de la UI
    private JTable tablaProductos;
    private ProductoTableModel tableModel;
    private JTextField txtBuscar;
    private JComboBox<Almacen> cmbFiltroAlmacen;
    private JTable tablaStockPorAlmacen;
    private DefaultTableModel stockTableModel;
    private JButton btnAnterior, btnSiguiente;
    private JLabel lblPaginacion;
    private JPopupMenu popupMenuTabla;
    private JMenuItem itemEditar;
    private JMenuItem itemDesactivar;

    @Autowired
    public PanelInventario(@Lazy InventarioPresenter presenter,
                           ServicioInventario si, ServicioCategoria sc,
                           ServicioProveedor sp, ServicioAlmacen sa) {
        this.presenter = presenter;
        this.presenter.setView(this);
        this.servicioInventario = si;
        this.servicioCategoria = sc;
        this.servicioProveedor = sp;
        this.servicioAlmacen = sa;

        inicializarUI();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        presenter.cargarDatosIniciales();
    }

    private void inicializarUI() {
        // Layout principal: Una columna que crece
        setLayout(new MigLayout("fill, insets 10", "[grow]", "[][grow][]"));

        // 1. PANEL DE ACCIONES (Superior)
        JPanel panelAcciones = new JPanel(new MigLayout("fillx, insets 0", "[][grow]push[]"));
        txtBuscar = new JTextField();
        txtBuscar.putClientProperty("JTextField.placeholderText", "Buscar por SKU o Nombre...");
        cmbFiltroAlmacen = new JComboBox<>(new DefaultComboBoxModel<>());

        JPanel panelBotonesAccion = new JPanel(new MigLayout("insets 0"));
        JButton btnNuevo = new SamvitexButton("Nuevo Producto");
        JButton btnEditar = new SamvitexButton("Editar", SamvitexButton.ButtonType.SECONDARY);
        JButton btnDesactivar = new SamvitexButton("Desactivar", SamvitexButton.ButtonType.SECONDARY);
        panelBotonesAccion.add(btnNuevo);
        panelBotonesAccion.add(btnEditar);
        panelBotonesAccion.add(btnDesactivar);

        panelAcciones.add(new JLabel("Buscar:"));
        panelAcciones.add(txtBuscar, "growx, split 2");
        panelAcciones.add(cmbFiltroAlmacen, "w 200!");
        panelAcciones.add(panelBotonesAccion);
        add(panelAcciones, "growx, wrap");

        // 2. TABLA DE PRODUCTOS (Izquierda)
        tableModel = new ProductoTableModel();

        // Inicialización ÚNICA de la tabla con Tooltips personalizados
        tablaProductos = new JTable(tableModel) {
            @Override
            public String getToolTipText(MouseEvent e) {
                java.awt.Point p = e.getPoint();
                int rowIndex = rowAtPoint(p);
                int colIndex = columnAtPoint(p);
                // Si el mouse está sobre la columna de Stock (índice 3)
                if (rowIndex >= 0 && colIndex == 3) {
                    int realRowIndex = convertRowIndexToModel(rowIndex);
                    return ((ProductoTableModel)getModel()).getTooltipAt(realRowIndex);
                }
                return super.getToolTipText(e);
            }
        };

        tablaProductos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaProductos.setAutoCreateRowSorter(true);
        tablaProductos.setRowHeight(30);
        // Asignar el renderer de colores a la columna de Stock (índice 3)
        tablaProductos.getColumnModel().getColumn(3).setCellRenderer(new StockCellRenderer());

        JScrollPane scrollProductos = new JScrollPane(tablaProductos);

        // 3. PANEL DE DETALLES DE STOCK (Derecha)
        JPanel panelDetalleStock = new JPanel(new MigLayout("fill, insets 0", "[grow]", "[grow]"));
        panelDetalleStock.setBorder(BorderFactory.createTitledBorder("Stock por Almacén"));
        String[] stockColumns = {"Almacén", "Cantidad"};
        stockTableModel = new DefaultTableModel(stockColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaStockPorAlmacen = new JTable(stockTableModel);
        panelDetalleStock.add(new JScrollPane(tablaStockPorAlmacen), "grow");
        panelDetalleStock.setMinimumSize(new Dimension(150, 0));

        // 4. SPLIT PANE (Divisor)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollProductos, panelDetalleStock);
        splitPane.setResizeWeight(0.85); // 85% para productos
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerSize(10);

        add(splitPane, "grow, push, wrap"); // IMPORTANTE: push asegura que ocupe el espacio

        // 5. MENÚ CONTEXTUAL
        popupMenuTabla = new JPopupMenu();
        itemEditar = new JMenuItem("Editar Producto");
        itemDesactivar = new JMenuItem("Desactivar");

        itemEditar.addActionListener(e -> editarFilaSeleccionada());
        itemDesactivar.addActionListener(e -> desactivarFilaSeleccionada());

        popupMenuTabla.add(itemEditar);
        popupMenuTabla.addSeparator();
        popupMenuTabla.add(itemDesactivar);

        // 6. LISTENERS

        // Listener de Click Derecho y Doble Click
        tablaProductos.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) { procesarClick(e); }
            @Override
            public void mousePressed(MouseEvent e) { procesarClick(e); }

            private void procesarClick(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    JTable source = (JTable)e.getSource();
                    int row = source.rowAtPoint(e.getPoint());
                    if (row != -1 && !source.isRowSelected(row)) {
                        source.changeSelection(row, source.columnAtPoint(e.getPoint()), false, false);
                    }
                    if (row != -1) popupMenuTabla.show(e.getComponent(), e.getX(), e.getY());
                } else if (e.getClickCount() == 2) {
                    editarFilaSeleccionada();
                }
            }
        });

        // Listener de selección para actualizar tabla lateral
        tablaProductos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = tablaProductos.getSelectedRow();
                stockTableModel.setRowCount(0);
                if (selectedRow != -1) {
                    Producto producto = tableModel.getProductoAt(tablaProductos.convertRowIndexToModel(selectedRow));
                    presenter.onProductoSeleccionado(producto.getId());
                }
            }
        });

        // Botones superiores
        btnNuevo.addActionListener(e -> presenter.onNuevoProductoClicked());
        btnEditar.addActionListener(e -> editarFilaSeleccionada());
        btnDesactivar.addActionListener(e -> desactivarFilaSeleccionada());

        // Buscador
        Timer searchTimer = new Timer(300, e -> presenter.buscarProductos(txtBuscar.getText()));
        searchTimer.setRepeats(false);
        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { searchTimer.restart(); }
            public void removeUpdate(DocumentEvent e) { searchTimer.restart(); }
            public void changedUpdate(DocumentEvent e) { searchTimer.restart(); }
        });

        cmbFiltroAlmacen.addActionListener(e -> presenter.buscarProductos(txtBuscar.getText()));

        // 7. PANEL DE PAGINACIÓN (Inferior)
        JPanel panelPaginacion = new JPanel(new MigLayout("fillx", "[]push[]push[]"));
        btnAnterior = new JButton("< Anterior");
        btnSiguiente = new JButton("Siguiente >");
        lblPaginacion = new JLabel("Página 0 de 0");
        panelPaginacion.add(btnAnterior);
        panelPaginacion.add(lblPaginacion);
        panelPaginacion.add(btnSiguiente);
        add(panelPaginacion, "growx");

        btnAnterior.addActionListener(e -> presenter.cambiarPagina(presenter.getPaginaActual() - 1));
        btnSiguiente.addActionListener(e -> presenter.cambiarPagina(presenter.getPaginaActual() + 1));
    }

    // --- Métodos Auxiliares ---

    private void editarFilaSeleccionada() {
        int row = tablaProductos.getSelectedRow();
        if (row >= 0) {
            presenter.onEditarProductoClicked(tableModel.getProductoAt(tablaProductos.convertRowIndexToModel(row)));
        } else {
            mostrarError("Por favor, seleccione un producto para editar.");
        }
    }

    private void desactivarFilaSeleccionada() {
        int row = tablaProductos.getSelectedRow();
        if (row >= 0) {
            presenter.onDesactivarProductoClicked(tableModel.getProductoAt(tablaProductos.convertRowIndexToModel(row)));
        } else {
            mostrarError("Por favor, seleccione un producto para desactivar.");
        }
    }

    // --- Getters & Setters del Contrato ---

    public InventarioPresenter getPresenter() { return presenter; }
    @Override public JPanel getPanel() { return this; }
    @Override public Almacen obtenerAlmacenFiltro() { return (Almacen) cmbFiltroAlmacen.getSelectedItem(); }

    @Override
    public void mostrarAlmacenes(List<Almacen> almacenes) {
        Object selected = cmbFiltroAlmacen.getSelectedItem();
        cmbFiltroAlmacen.removeAllItems();
        Almacen todos = new Almacen();
        todos.setId(-1);
        todos.setNombre("Todos los Almacenes");
        cmbFiltroAlmacen.addItem(todos);
        almacenes.forEach(cmbFiltroAlmacen::addItem);
        if (selected instanceof Almacen selectedAlmacen) {
            for (int i = 0; i < cmbFiltroAlmacen.getItemCount(); i++) {
                if (Objects.equals(cmbFiltroAlmacen.getItemAt(i).getId(), selectedAlmacen.getId())) {
                    cmbFiltroAlmacen.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    @Override
    public void mostrarStockPorAlmacen(List<InventarioPorAlmacen> inventario) {
        stockTableModel.setRowCount(0);
        if (inventario != null) {
            for (InventarioPorAlmacen item : inventario) {
                stockTableModel.addRow(new Object[]{item.getAlmacen().getNombre(), item.getCantidad()});
            }
        }
    }

    @Override
    public void mostrarProductos(Page<ProductoInventarioDTO> pagina) {
        // Aquí actualizamos el modelo, y como la tabla usa este modelo, se actualizará visualmente
        tableModel.setProductos(pagina.getContent());
        stockTableModel.setRowCount(0);
        actualizarControlesPaginacion(pagina);
    }

    private void actualizarControlesPaginacion(Page<?> pagina) {
        int paginaMostrada = pagina.getNumber() + 1;
        int totalPaginas = pagina.getTotalPages();
        lblPaginacion.setText(String.format("Página %d de %d (Total: %d registros)",
                totalPaginas == 0 ? 0 : paginaMostrada, totalPaginas, pagina.getTotalElements()));
        btnAnterior.setEnabled(pagina.hasPrevious());
        btnSiguiente.setEnabled(pagina.hasNext());
    }

    @Override
    public void mostrarDialogoProducto(Producto producto) {
        DialogoProducto dialogo = new DialogoProducto(
                (Frame) SwingUtilities.getWindowAncestor(this),
                servicioInventario, servicioCategoria, servicioProveedor, servicioAlmacen,
                producto, this::refrescarVista
        );
        dialogo.setVisible(true);
    }

    @Override
    public void seleccionarYMostrarProducto(Integer productoId) {
        SwingUtilities.invokeLater(() -> {
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                if (tableModel.getProductoAt(i).getId().equals(productoId)) {
                    int filaEnVista = tablaProductos.convertRowIndexToView(i);
                    if (filaEnVista != -1) {
                        tablaProductos.setRowSelectionInterval(filaEnVista, filaEnVista);
                        tablaProductos.scrollRectToVisible(tablaProductos.getCellRect(filaEnVista, 0, true));
                    }
                    break;
                }
            }
        });
    }

    @Override public void refrescarVista() { presenter.cargarProductos(); }
    @Override public void setTextoBusqueda(String texto) { txtBuscar.setText(texto); }
    @Override public void mostrarError(String mensaje) { JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE); }
    @Override public void mostrarMensajeExito(String mensaje) { JOptionPane.showMessageDialog(this, mensaje, "Éxito", JOptionPane.INFORMATION_MESSAGE); }
    @Override public boolean confirmarAccion(String mensaje, String titulo) {
        return JOptionPane.showConfirmDialog(this, mensaje, titulo, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    // --- RENDERIZADOR DE CELDAS ---
    private static class StockCellRenderer extends javax.swing.table.DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (!isSelected) {
                c.setForeground(table.getForeground());
            } else {
                c.setForeground(table.getSelectionForeground());
            }

            if (!isSelected && value instanceof Number) {
                long stock = ((Number) value).longValue();
                if (stock == 0) {
                    c.setForeground(new Color(220, 53, 69));
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                } else if (stock <= 10) {
                    c.setForeground(new Color(255, 140, 0));
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                }
            }
            setHorizontalAlignment(SwingConstants.CENTER);
            return c;
        }
    }
}