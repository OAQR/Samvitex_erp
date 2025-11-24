package com.samvitex.ui.paneles;

import com.samvitex.modelos.dto.CompraItemDTO;
import com.samvitex.modelos.entidades.Producto;
import com.samvitex.modelos.entidades.Proveedor;
import com.samvitex.modelos.entidades.Almacen;
import com.samvitex.servicios.ServicioAlmacen;
import com.samvitex.servicios.ServicioCompras;
import com.samvitex.servicios.ServicioInventario;
import com.samvitex.servicios.ServicioProveedor;
import com.samvitex.ui.modelos_tabla.CompraTableModel;
import com.samvitex.ui.presentadores.ComprasPresenter;
import com.samvitex.ui.theme.SamvitexButton;
import com.samvitex.ui.vistas.interfaces.ComprasView;
import net.miginfocom.swing.MigLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Panel de Swing que implementa la {@link ComprasView}, proporcionando la interfaz
 * de usuario para el registro de compras de mercancía a proveedores.
 *
 * <p>Este panel sigue el patrón MVP, delegando toda la lógica de eventos y de negocio
 * al {@link ComprasPresenter}. Su responsabilidad es renderizar los componentes,
 * capturar la entrada del usuario y mostrar los datos y resultados que el Presenter le indica.</p>
 */
@Component
public class PanelCompras extends JPanel implements ComprasView {

    private final ComprasPresenter presenter;

    // Componentes de la UI
    private JComboBox<Proveedor> cmbProveedores;
    private JComboBox<Almacen> cmbAlmacenes;
    private JTextField txtReferenciaFactura;
    private JTextField txtBuscarProducto;
    private DefaultListModel<Producto> listModel;
    private JList<Producto> listaResultadosBusqueda;
    private CompraTableModel compraTableModel;
    private JTable tablaCompra;
    private JLabel lblTotal;
    private Timer searchTimer;

    @Autowired
    public PanelCompras(ServicioCompras sc, ServicioInventario si, ServicioProveedor sp, ServicioAlmacen sa) { // CAMBIO: Inyectar ServicioAlmacen
        this.presenter = new ComprasPresenter(this, sc, si, sp, sa);
        inicializarUI();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        presenter.cargarDatosIniciales();
    }

    private void inicializarUI() {
        setLayout(new MigLayout("fill, insets 10", "[40%, grow][60%, grow]", "[grow]"));

        // --- Panel Izquierdo: Datos de la Compra y Búsqueda ---
        JPanel panelIzquierdo = new JPanel(new MigLayout("wrap, fill", "[grow]", "[][][][][grow]"));
        panelIzquierdo.setBorder(BorderFactory.createTitledBorder("Datos de la Compra"));
        cmbProveedores = new JComboBox<>(new DefaultComboBoxModel<>());
        cmbAlmacenes = new JComboBox<>(new DefaultComboBoxModel<>());
        txtReferenciaFactura = new JTextField();
        txtBuscarProducto = new JTextField();
        listModel = new DefaultListModel<>();
        listaResultadosBusqueda = new JList<>(listModel);
        listaResultadosBusqueda.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        panelIzquierdo.add(new JLabel("Proveedor:"));
        panelIzquierdo.add(cmbProveedores, "growx");
        panelIzquierdo.add(new JLabel("Ingresar a Almacén:"), "gaptop 10");
        panelIzquierdo.add(cmbAlmacenes, "growx");
        panelIzquierdo.add(new JLabel("Referencia (Factura/Guía):"), "gaptop 10");
        panelIzquierdo.add(txtReferenciaFactura, "growx");
        panelIzquierdo.add(new JLabel("Buscar Producto (doble clic para añadir):"), "gaptop 20");
        panelIzquierdo.add(txtBuscarProducto, "growx");
        panelIzquierdo.add(new JScrollPane(listaResultadosBusqueda), "grow");

        // --- Panel Derecho: Detalle de Compra y Registro ---
        JPanel panelDerecho = new JPanel(new MigLayout("wrap, fill", "[grow]", "[][grow][][]"));
        panelDerecho.setBorder(BorderFactory.createTitledBorder("Ítems de la Compra"));
        compraTableModel = new CompraTableModel();
        tablaCompra = new JTable(compraTableModel);
        JButton btnRegistrarCompra = new SamvitexButton("Registrar Compra");
        JButton btnEliminarItem = new SamvitexButton("Eliminar Ítem", SamvitexButton.ButtonType.SECONDARY);
        lblTotal = new JLabel("Total: S/ 0.00");
        lblTotal.setFont(lblTotal.getFont().deriveFont(Font.BOLD, 16f));

        panelDerecho.add(new JScrollPane(tablaCompra), "grow, span");

        JPanel panelBotones = new JPanel(new MigLayout("insets 0, fillx", "[grow]push[]"));
        panelBotones.add(btnEliminarItem);
        panelBotones.add(lblTotal);

        panelDerecho.add(panelBotones, "growx, span");
        panelDerecho.add(btnRegistrarCompra, "growx, h 40!, gaptop 10, span");

        add(panelIzquierdo, "grow");
        add(panelDerecho, "grow");

        // --- Listeners ---
        searchTimer = new Timer(300, e -> presenter.buscarProductos(txtBuscarProducto.getText()));
        searchTimer.setRepeats(false);
        txtBuscarProducto.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { searchTimer.restart(); }
            public void removeUpdate(DocumentEvent e) { searchTimer.restart(); }
            public void changedUpdate(DocumentEvent e) { searchTimer.restart(); }
        });

        listaResultadosBusqueda.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    Producto seleccionado = listaResultadosBusqueda.getSelectedValue();
                    if (seleccionado != null) {
                        compraTableModel.agregarProducto(seleccionado);
                    }
                }
            }
        });

        compraTableModel.addTableModelListener(e -> presenter.onTablaCompraChanged(compraTableModel.getTotal()));

        btnEliminarItem.addActionListener(e -> {
            int selectedRow = tablaCompra.getSelectedRow();
            if (selectedRow >= 0) {
                compraTableModel.eliminarItem(tablaCompra.convertRowIndexToModel(selectedRow));
            } else {
                mostrarError("Seleccione un ítem de la tabla para eliminar.");
            }
        });

        btnRegistrarCompra.addActionListener(e -> presenter.registrarCompra());
    }

    @Override
    public void mostrarAlmacenes(List<Almacen> almacenes) {
        cmbAlmacenes.setModel(new DefaultComboBoxModel<>(almacenes.toArray(new Almacen[0])));
    }

    @Override
    public Almacen obtenerAlmacenDestinoSeleccionado() {
        return (Almacen) cmbAlmacenes.getSelectedItem();
    }

    @Override
    public void mostrarProveedores(List<Proveedor> proveedores) {
        cmbProveedores.removeAllItems();
        proveedores.forEach(cmbProveedores::addItem);
    }

    @Override
    public void mostrarResultadosBusqueda(List<Producto> productos) {
        listModel.clear();
        listModel.addAll(productos);
    }

    @Override
    public void actualizarTotalCompra(BigDecimal total) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "PE"));
        lblTotal.setText("Total: " + currencyFormat.format(total));
    }

    @Override
    public void limpiarVistaPostCompra() {
        compraTableModel.limpiar();
        listModel.clear();
        txtBuscarProducto.setText("");
        txtReferenciaFactura.setText("");
        if (cmbProveedores.getItemCount() > 0) {
            cmbProveedores.setSelectedIndex(0);
        }
    }

    @Override
    public Proveedor obtenerProveedorSeleccionado() {
        return (Proveedor) cmbProveedores.getSelectedItem();
    }

    @Override
    public String obtenerReferenciaFactura() {
        return txtReferenciaFactura.getText();
    }

    @Override
    public List<CompraItemDTO> obtenerItemsCompra() {
        return compraTableModel.getItems().stream()
                .map(item -> new CompraItemDTO(
                        item.getProducto().getId(),
                        item.getCantidad(),
                        item.getCostoUnitario()))
                .collect(Collectors.toList());
    }

    @Override
    public void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error en Compra", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void mostrarMensajeExito(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }
}