package com.samvitex.ui.dialogos;

import com.samvitex.modelos.entidades.*;
import com.samvitex.servicios.ServicioAlmacen;
import com.samvitex.servicios.ServicioCategoria;
import com.samvitex.servicios.ServicioInventario;
import com.samvitex.servicios.ServicioProveedor;
import com.samvitex.ui.theme.SamvitexButton;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * Diálogo modal para la creación y edición de entidades {@link Producto}.
 *
 * <p>Este diálogo gestiona los atributos maestros de un producto (SKU, nombre, precios)
 * y muestra una vista de solo lectura del stock desglosado por almacén.
 * El stock NO es editable desde este diálogo; se gestiona a través de transacciones
 * de compra, venta o ajustes de inventario.</p>
 */
public class DialogoProducto extends JDialog {

    private final ServicioInventario servicioInventario;
    private final Producto producto;
    private final Runnable onSaveSuccess;

    // Componentes del formulario
    private JTextField txtSku, txtNombre;
    private JTextArea txtDescripcion;
    private JSpinner spinStockMinimo;
    private JFormattedTextField txtPrecioCosto, txtPrecioVenta;
    private JComboBox<Categoria> cmbCategoria;
    private JComboBox<Proveedor> cmbProveedor;
    private JCheckBox chkActivo;
    private JTable tablaStock;
    private DefaultTableModel stockTableModel;

    /**
     * Construye el diálogo.
     * Se han eliminado las dependencias de repositorios, la vista solo habla con servicios.
     */
    public DialogoProducto(Frame owner,
                           ServicioInventario si, ServicioCategoria sc,
                           ServicioProveedor sp, ServicioAlmacen sa,
                           Producto producto, Runnable onSaveSuccess) {
        super(owner, true);
        this.servicioInventario = si;
        this.producto = (producto != null) ? producto : new Producto();
        this.onSaveSuccess = onSaveSuccess;

        setTitle(this.producto.getId() != null ? "Editar Producto" : "Nuevo Producto");
        setSize(550, 620);
        setMinimumSize(new Dimension(550, 620));
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        inicializarUI();
        cargarDatosAsincronos(sc, sp);
    }

    private void inicializarUI() {
        JPanel panelFormulario = new JPanel(new MigLayout("wrap 2, fillx, insets 15", "[right]15[grow,fill]"));

        // Formateadores
        NumberFormat currencyFormat = new DecimalFormat("#,##0.00");
        txtPrecioCosto = new JFormattedTextField(currencyFormat);
        txtPrecioVenta = new JFormattedTextField(currencyFormat);

        // Inicialización
        txtSku = new JTextField();
        txtNombre = new JTextField();
        txtDescripcion = new JTextArea(3, 20);
        spinStockMinimo = new JSpinner(new SpinnerNumberModel(0, 0, 999999, 1));
        cmbCategoria = new JComboBox<>(new DefaultComboBoxModel<>());
        cmbProveedor = new JComboBox<>(new DefaultComboBoxModel<>());
        chkActivo = new JCheckBox("Producto Activo");

        cmbCategoria.putClientProperty("JComponent.outline", "warning");
        cmbProveedor.putClientProperty("JComponent.outline", "warning");

        cmbCategoria.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value == null) {
                    setText("--- Seleccione ---");
                } else if (value instanceof Categoria c) {
                    setText(c.getNombre());
                } else if (value instanceof Proveedor p) {
                    setText(p.getNombre());
                }
                return this;
            }
        });

        // Layout de componentes
        panelFormulario.add(new JLabel("Código de Producto (SKU):"));
        panelFormulario.add(txtSku, "growx");
        panelFormulario.add(new JLabel("Nombre:"));
        panelFormulario.add(txtNombre, "growx");
        panelFormulario.add(new JLabel("Descripción:"), "top");
        panelFormulario.add(new JScrollPane(txtDescripcion), "growx, h 60!");
        panelFormulario.add(new JLabel("Categoría:"));
        panelFormulario.add(cmbCategoria, "growx");
        panelFormulario.add(new JLabel("Proveedor Principal:"));
        panelFormulario.add(cmbProveedor, "growx");
        panelFormulario.add(new JLabel("Stock Mínimo (Alerta):"), "gaptop 10");
        panelFormulario.add(spinStockMinimo);
        panelFormulario.add(new JLabel("Precio de Costo:"));
        panelFormulario.add(txtPrecioCosto, "growx");
        panelFormulario.add(new JLabel("Precio de Venta:"));
        panelFormulario.add(txtPrecioVenta, "growx");
        panelFormulario.add(chkActivo, "span 2, gaptop 10");

        // Panel para mostrar el stock por almacén (no editable)
        JPanel panelStock = new JPanel(new BorderLayout(0, 5));
        panelStock.setBorder(BorderFactory.createTitledBorder("Stock Actual (Solo lectura, gestionar en Módulo de Compras)"));
        stockTableModel = new DefaultTableModel(new String[]{"Almacén", "Cantidad"}, 0);
        tablaStock = new JTable(stockTableModel) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JScrollPane scrollStock = new JScrollPane(tablaStock);
        scrollStock.setPreferredSize(new Dimension(100, 100));
        panelStock.add(scrollStock);
        panelFormulario.add(panelStock, "span 2, growx, gaptop 15");

        add(panelFormulario, BorderLayout.CENTER);

        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnGuardar = new SamvitexButton("Guardar");
        JButton btnCancelar = new SamvitexButton("Cancelar", SamvitexButton.ButtonType.SECONDARY);
        panelBotones.add(btnCancelar);
        panelBotones.add(btnGuardar);
        add(panelBotones, BorderLayout.SOUTH);

        // Action Listeners
        btnCancelar.addActionListener(e -> dispose());
        btnGuardar.addActionListener(e -> guardarProducto());
    }

    private void cargarDatosAsincronos(ServicioCategoria sc, ServicioProveedor sp) {
        cmbCategoria.setEnabled(false);
        cmbProveedor.setEnabled(false);

        new SwingWorker<Map<String, List<?>>, Void>() {
            @Override
            protected Map<String, List<?>> doInBackground() throws Exception {
                Map<String, List<?>> datos = new java.util.HashMap<>();
                datos.put("categorias", sc.obtenerTodas());
                datos.put("proveedores", sp.obtenerTodosActivos());
                return datos;
            }

            @Override
            protected void done() {
                try {
                    Map<String, List<?>> datos = get();
                    cmbCategoria.setModel(new DefaultComboBoxModel<>(new Vector<>((List<Categoria>) datos.get("categorias"))));
                    cmbProveedor.setModel(new DefaultComboBoxModel<>(new Vector<>((List<Proveedor>) datos.get("proveedores"))));
                    cmbCategoria.insertItemAt(null, 0); // Permite no asignar categoría
                    cmbProveedor.insertItemAt(null, 0); // Permite no asignar proveedor

                    cmbCategoria.setSelectedIndex(-1);
                    cmbProveedor.setSelectedIndex(-1);

                    // Cargar datos del producto DESPUÉS de poblar los combos
                    cargarDatosDelProducto();
                } catch (Exception e) {
                    mostrarError("Error al cargar datos necesarios: " + e.getMessage());
                } finally {
                    cmbCategoria.setEnabled(true);
                    cmbProveedor.setEnabled(true);
                }
            }
        }.execute();
    }

    private void cargarDatosDelProducto() {
        if (producto.getId() != null) { // Modo Edición
            txtSku.setText(producto.getSku());
            txtSku.setEditable(false);
            txtNombre.setText(producto.getNombre());
            txtDescripcion.setText(producto.getDescripcion());
            spinStockMinimo.setValue(producto.getStockMinimo());
            txtPrecioCosto.setValue(producto.getPrecioCosto());
            txtPrecioVenta.setValue(producto.getPrecioVenta());
            chkActivo.setSelected(producto.isActivo());
            cmbCategoria.setSelectedItem(producto.getCategoria());
            cmbProveedor.setSelectedItem(producto.getProveedor());

            // Carga el desglose de stock para el producto existente
            cargarStockDelProducto();
        } else { // Modo Creación
            chkActivo.setSelected(true);
            // La tabla de stock permanecerá vacía para un producto nuevo
        }
    }

    private void cargarStockDelProducto() {
        if (producto == null || producto.getId() == null) return;

        new SwingWorker<List<InventarioPorAlmacen>, Void>() {
            @Override
            protected List<InventarioPorAlmacen> doInBackground() throws Exception {
                // Llama al servicio para obtener los datos, no al repositorio
                return servicioInventario.obtenerDesgloseStockPorProducto(producto.getId());
            }

            @Override
            protected void done() {
                try {
                    stockTableModel.setRowCount(0);
                    for (InventarioPorAlmacen item : get()) {
                        stockTableModel.addRow(new Object[]{
                                item.getAlmacen().getNombre(),
                                item.getCantidad()
                        });
                    }
                } catch (Exception e) {
                    mostrarError("No se pudo cargar el detalle de stock: " + e.getMessage());
                }
            }
        }.execute();
    }

    private void guardarProducto() {
        try {
            String sku = txtSku.getText().trim();
            String nombre = txtNombre.getText().trim();
            if (sku.isBlank() || nombre.isBlank()) {
                throw new IllegalArgumentException("El SKU y el Nombre son campos obligatorios.");
            }

            Object costoValue = txtPrecioCosto.getValue();
            Object ventaValue = txtPrecioVenta.getValue();
            if (costoValue == null || ventaValue == null) {
                throw new IllegalArgumentException("Los precios no pueden estar vacíos.");
            }
            BigDecimal precioCosto = new BigDecimal(costoValue.toString().replace(",", ""));
            BigDecimal precioVenta = new BigDecimal(ventaValue.toString().replace(",", ""));
            if (precioCosto.compareTo(BigDecimal.ZERO) < 0 || precioVenta.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Los precios no pueden ser negativos.");
            }

            producto.setSku(sku);
            producto.setNombre(nombre);
            producto.setDescripcion(txtDescripcion.getText().trim());
            producto.setStockMinimo((Integer) spinStockMinimo.getValue());
            producto.setPrecioCosto(precioCosto);
            producto.setPrecioVenta(precioVenta);
            producto.setActivo(chkActivo.isSelected());
            producto.setCategoria((Categoria) cmbCategoria.getSelectedItem());
            producto.setProveedor((Proveedor) cmbProveedor.getSelectedItem());

            final org.springframework.security.core.Authentication auth =
                    org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();

            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);
                    try {
                        servicioInventario.guardarProducto(producto);
                    } finally {
                        org.springframework.security.core.context.SecurityContextHolder.clearContext();
                    }
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        JOptionPane.showMessageDialog(DialogoProducto.this, "Producto guardado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                        onSaveSuccess.run();
                        dispose();
                    } catch (Exception e) {
                        String message = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
                        mostrarError("Error al guardar el producto: " + message);
                    }
                }
            }.execute();

        } catch (Exception e) {
            mostrarError("Error de validación: " + e.getMessage());
        }
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }
}