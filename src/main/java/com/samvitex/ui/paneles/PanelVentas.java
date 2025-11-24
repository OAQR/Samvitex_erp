package com.samvitex.ui.paneles;

import com.samvitex.modelos.dto.VentaItemDTO;
import com.samvitex.modelos.entidades.Almacen;
import com.samvitex.modelos.entidades.Cliente;
import com.samvitex.modelos.entidades.Producto;
import com.samvitex.modelos.excepciones.CarritoException;
import com.samvitex.servicios.ServicioAlmacen;
import com.samvitex.servicios.ServicioCliente;
import com.samvitex.servicios.ServicioInventario;
import com.samvitex.servicios.ServicioVentas;
import com.samvitex.servicios.ServicioImpresion;
import com.samvitex.ui.modelos_tabla.CarritoTableModel;
import com.samvitex.ui.presentadores.VentasPresenter;
import com.samvitex.ui.theme.SamvitexButton;
import com.samvitex.ui.vistas.interfaces.VentasView;
import net.miginfocom.swing.MigLayout;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Panel de Swing que implementa la {@link VentasView}, sirviendo como la interfaz
 * del Punto de Venta (POS). Adaptado para un entorno multi-almacén y optimizado
 * para entrada rápida de productos.
 */
@org.springframework.stereotype.Component
public class PanelVentas extends JPanel implements VentasView {

    private final VentasPresenter presenter;
    private final ServicioInventario servicioInventario; // Para obtener stock máximo en el momento

    // Componentes de la UI
    private JComboBox<Cliente> cmbClientes;
    private JComboBox<Almacen> cmbAlmacenes;
    private JList<Producto> listaResultadosBusqueda;
    private DefaultListModel<Producto> listModel;
    private JTable tablaCarrito;
    private CarritoTableModel carritoTableModel;
    private JLabel lblTotal;
    private JTextField txtBuscarProducto;
    private JTextField txtEntradaSKU; // Campo para lector de código de barras

    @Autowired
    public PanelVentas(ServicioVentas sv, ServicioInventario si, ServicioCliente sc,
                       ServicioAlmacen sa, ServicioImpresion servicioImpresion) {
        this.servicioInventario = si; // Guardamos la referencia para usarla localmente
        this.presenter = new VentasPresenter(this, sv, si, sc, sa, servicioImpresion);
        inicializarUI();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        presenter.cargarDatosIniciales();
    }

    private void inicializarUI() {
        setLayout(new MigLayout("fill, insets 10", "[40%, grow][60%, grow]", "[grow]"));

        // --- Panel Izquierdo ---
        JPanel panelIzquierdo = new JPanel(new MigLayout("wrap, fill", "[grow]", "[][][][][][grow]"));
        panelIzquierdo.setBorder(BorderFactory.createTitledBorder("Datos de la Venta"));

        cmbClientes = new JComboBox<>(new DefaultComboBoxModel<>());
        cmbAlmacenes = new JComboBox<>(new DefaultComboBoxModel<>());
        txtEntradaSKU = new JTextField();
        txtEntradaSKU.putClientProperty("JTextField.placeholderText", "Escanear o ingresar SKU y presionar Enter");
        txtBuscarProducto = new JTextField();
        txtBuscarProducto.putClientProperty("JTextField.placeholderText", "Buscar por nombre...");

        listModel = new DefaultListModel<>();
        listaResultadosBusqueda = new JList<>(listModel);
        listaResultadosBusqueda.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // El CellRenderer se simplifica, ya no necesita DTO
        listaResultadosBusqueda.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Producto p) {
                    setText(p.getNombre());
                }
                return this;
            }
        });

        panelIzquierdo.add(new JLabel("Cliente:"));
        panelIzquierdo.add(cmbClientes, "growx");
        panelIzquierdo.add(new JLabel("Vender desde Almacén:"), "gaptop 10");
        panelIzquierdo.add(cmbAlmacenes, "growx");
        panelIzquierdo.add(new JLabel("Código de Producto (Enter para añadir):"), "gaptop 20");
        panelIzquierdo.add(txtEntradaSKU, "growx");
        panelIzquierdo.add(new JLabel("Buscar por Nombre (doble clic para añadir):"), "gaptop 10");
        panelIzquierdo.add(txtBuscarProducto, "growx");
        panelIzquierdo.add(new JScrollPane(listaResultadosBusqueda), "grow");

        // --- Panel Derecho ---
        JPanel panelDerecho = new JPanel(new MigLayout("wrap, fill", "[grow]", "[grow][][]"));
        panelDerecho.setBorder(BorderFactory.createTitledBorder("Carrito de Compra"));
        carritoTableModel = new CarritoTableModel();
        tablaCarrito = new JTable(carritoTableModel);
        JButton btnFinalizarVenta = new SamvitexButton("Procesar Venta..."); // Texto más claro
        JButton btnEliminarItem = new SamvitexButton("Eliminar Ítem", SamvitexButton.ButtonType.SECONDARY);
        lblTotal = new JLabel("Total: S/ 0.00");
        lblTotal.setFont(lblTotal.getFont().deriveFont(Font.BOLD, 16f));

        panelDerecho.add(new JScrollPane(tablaCarrito), "grow, span");
        JPanel panelBotonesCarrito = new JPanel(new MigLayout("insets 0, fillx", "[]push[]"));
        panelBotonesCarrito.add(btnEliminarItem);
        panelBotonesCarrito.add(lblTotal, "align right");
        panelDerecho.add(panelBotonesCarrito, "growx, span");
        panelDerecho.add(btnFinalizarVenta, "growx, h 40!, gaptop 10, span");

        add(panelIzquierdo, "grow");
        add(panelDerecho, "grow");

        // --- Listeners ---
        // Listener para el campo de búsqueda por SKU (lector de código de barras)
        txtEntradaSKU.addActionListener(e -> {
            String sku = txtEntradaSKU.getText().trim();
            if (!sku.isBlank()) {
                presenter.agregarProductoPorSku(sku);
                txtEntradaSKU.setText(""); // Limpiar para el siguiente escaneo
            }
        });

        // Listener para el campo de búsqueda por nombre
        Timer searchTimer = new Timer(300, e -> presenter.buscarProductos(txtBuscarProducto.getText()));
        searchTimer.setRepeats(false);
        txtBuscarProducto.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { searchTimer.restart(); }
            public void removeUpdate(DocumentEvent e) { searchTimer.restart(); }
            public void changedUpdate(DocumentEvent e) { searchTimer.restart(); }
        });

        // Cuando cambia el almacén, se debe actualizar la lista de productos
        cmbAlmacenes.addActionListener(e -> presenter.buscarProductos(txtBuscarProducto.getText()));

        // Listener para añadir desde la lista de búsqueda
        listaResultadosBusqueda.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2 && listaResultadosBusqueda.getSelectedValue() != null) {
                    agregarProductoAlCarrito(listaResultadosBusqueda.getSelectedValue());
                }
            }
        });

        carritoTableModel.addTableModelListener(e -> presenter.onCarritoChanged(carritoTableModel.getTotal()));

        btnEliminarItem.addActionListener(e -> {
            int selectedRow = tablaCarrito.getSelectedRow();
            if (selectedRow >= 0) {
                carritoTableModel.eliminarItem(tablaCarrito.convertRowIndexToModel(selectedRow));
            } else {
                mostrarError("Seleccione un ítem del carrito para eliminar.");
            }
        });

        btnFinalizarVenta.addActionListener(e -> presenter.finalizarVenta());

        // Pone el foco en el campo de SKU cuando el panel se muestra
        addAncestorListener(new AncestorListener() {
            public void ancestorAdded(AncestorEvent event) {
                txtEntradaSKU.requestFocusInWindow();
            }
            public void ancestorRemoved(AncestorEvent event) {}
            public void ancestorMoved(AncestorEvent event) {}
        });
    }

    // --- Implementación de Métodos de la Vista (Contrato VentasView) ---

    @Override
    public void mostrarAlmacenes(List<Almacen> almacenes) {
        cmbAlmacenes.setModel(new DefaultComboBoxModel<>(almacenes.toArray(new Almacen[0])));
    }

    @Override
    public Almacen obtenerAlmacenSeleccionado() {
        return (Almacen) cmbAlmacenes.getSelectedItem();
    }

    @Override
    public void mostrarClientes(List<Cliente> clientes) {
        cmbClientes.setModel(new DefaultComboBoxModel<>(clientes.toArray(new Cliente[0])));
    }

    @Override
    public void mostrarResultadosBusqueda(List<Producto> productos) {
        listModel.clear();
        listModel.addAll(productos);
    }

    @Override
    public void agregarProductoAlCarrito(Producto producto) {
        Almacen almacen = obtenerAlmacenSeleccionado();
        if (almacen == null) {
            mostrarError("No se ha seleccionado un almacén.");
            return;
        }
        int stockMaximo = servicioInventario.obtenerStockDeProductoEnAlmacen(producto.getId(), almacen.getId());

        try {
            carritoTableModel.agregarProducto(producto, stockMaximo);

            int rowCount = tablaCarrito.getRowCount();
            if (rowCount > 0) {
                tablaCarrito.scrollRectToVisible(tablaCarrito.getCellRect(rowCount - 1, 0, true));
            }
        } catch (CarritoException e) {
            mostrarError(e.getMessage());
        }
    }

    @Override
    public void actualizarTotalVenta(BigDecimal total) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "PE"));
        lblTotal.setText("Total: " + currencyFormat.format(total));
    }

    @Override
    public void limpiarVistaPostVenta() {
        carritoTableModel.limpiar();
        listModel.clear();
        txtBuscarProducto.setText("");
        txtEntradaSKU.setText("");
        txtEntradaSKU.requestFocusInWindow();
    }

    @Override
    public Cliente obtenerClienteSeleccionado() {
        return (Cliente) cmbClientes.getSelectedItem();
    }

    @Override
    public List<VentaItemDTO> obtenerItemsCarrito() {
        return carritoTableModel.getItems().stream()
                .map(item -> new VentaItemDTO(item.getProducto().getId(), item.getCantidad()))
                .collect(Collectors.toList());
    }

    @Override
    public BigDecimal obtenerTotalCarrito() {
        return carritoTableModel.getTotal();
    }

    @Override
    public JPanel getPanel() {
        return this;
    }

    @Override
    public void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error en Venta", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void mostrarMensajeExito(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }
}