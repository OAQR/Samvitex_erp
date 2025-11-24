package com.samvitex.ui.dialogos;

import com.samvitex.modelos.dto.OrdenProduccionDTO;
import com.samvitex.modelos.entidades.Almacen;
import com.samvitex.modelos.entidades.Producto;
import com.samvitex.modelos.entidades.Taller;
import com.samvitex.modelos.enums.TipoDetalleProduccion;
import com.samvitex.modelos.excepciones.ProduccionException;
import com.samvitex.servicios.ServicioAlmacen;
import com.samvitex.servicios.ServicioInventario;
import com.samvitex.servicios.ServicioProduccion;
import com.samvitex.repositorios.TallerRepositorio;
import com.samvitex.ui.modelos_tabla.OrdenProduccionDetalleTableModel;
import com.samvitex.ui.theme.SamvitexButton;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.stream.Stream;

/**
 * Diálogo modal para la creación de una nueva Orden de Producción.
 */
public class DialogoOrdenProduccion extends JDialog {

    private final ServicioProduccion servicioProduccion;
    private final Runnable onSaveSuccess;

    // Componentes del formulario
    private JTextField txtCodigo;
    private JComboBox<Taller> cmbTaller;
    private JComboBox<Almacen> cmbAlmacenInsumos;
    private JComboBox<Almacen> cmbAlmacenDestino;

    // Componentes para añadir ítems
    private JComboBox<Producto> cmbProducto;
    private JSpinner spinCantidad;

    // Modelos y Tablas separadas para Insumos y Productos Finales
    private OrdenProduccionDetalleTableModel insumosTableModel;
    private JTable tablaInsumos;
    private OrdenProduccionDetalleTableModel productosFinalesTableModel;
    private JTable tablaProductosFinales;

    public DialogoOrdenProduccion(Frame owner,
                                  ServicioProduccion sp, ServicioInventario si,
                                  TallerRepositorio tr, ServicioAlmacen sa,
                                  Runnable onSave) {
        super(owner, "Nueva Orden de Producción", true);
        this.servicioProduccion = sp;
        this.onSaveSuccess = onSave;

        setSize(950, 700);
        setMinimumSize(new Dimension(950, 700));
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        inicializarUI();
        cargarDatosAsincronos(si, tr, sa);
    }

    private void inicializarUI() {
        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // --- Panel de Cabecera (Datos Generales) ---
        JPanel panelCabecera = new JPanel(new MigLayout("fillx, insets 10", "[right]15[grow,fill]"));
        panelCabecera.setBorder(BorderFactory.createTitledBorder("Datos Generales de la Orden"));
        txtCodigo = new JTextField();
        cmbTaller = new JComboBox<>(new DefaultComboBoxModel<>());
        cmbAlmacenInsumos = new JComboBox<>(new DefaultComboBoxModel<>());
        cmbAlmacenDestino = new JComboBox<>(new DefaultComboBoxModel<>());
        cmbProducto = new JComboBox<>(new DefaultComboBoxModel<>());
        panelCabecera.add(new JLabel("Código de Orden:"));
        panelCabecera.add(txtCodigo, "wrap");
        panelCabecera.add(new JLabel("Taller Asignado:"));
        panelCabecera.add(cmbTaller, "wrap");
        panelCabecera.add(new JLabel("Almacén de Origen (Insumos):"), "gaptop 5");
        panelCabecera.add(cmbAlmacenInsumos, "wrap");
        panelCabecera.add(new JLabel("Almacén de Destino (Productos):"), "gaptop 5");
        panelCabecera.add(cmbAlmacenDestino);

        // --- Panel Central (Añadir ítems y dos tablas) ---
        JPanel panelCentro = new JPanel(new MigLayout("fill, insets 0", "[grow, fill]", "[][grow, fill]"));

        // Panel para añadir nuevos ítems
        JPanel panelAgregar = new JPanel(new MigLayout("insets 10, fillx", "[grow,fill]10[80px!,fill]15[sg btns]15[sg btns]"));
        panelAgregar.setBorder(BorderFactory.createTitledBorder("Añadir Ítem a la Orden"));
        cmbProducto = new JComboBox<>();
        spinCantidad = new JSpinner(new SpinnerNumberModel(1, 1, 99999, 1));
        JButton btnAnadirInsumo = new SamvitexButton("Añadir como Insumo");
        JButton btnAnadirProductoFinal = new SamvitexButton("Añadir como Producto");
        panelAgregar.add(new JLabel("Producto:"));
        panelAgregar.add(new JLabel("Cantidad:"), "wrap");
        panelAgregar.add(cmbProducto);
        panelAgregar.add(spinCantidad);
        panelAgregar.add(btnAnadirInsumo, "growx");
        panelAgregar.add(btnAnadirProductoFinal, "growx");
        panelCentro.add(panelAgregar, "wrap, growx");

        // Split Pane para las dos tablas
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.5); // Distribución equitativa

        // Panel Izquierdo: Insumos
        JPanel panelInsumos = new JPanel(new BorderLayout(5, 5));
        panelInsumos.setBorder(BorderFactory.createTitledBorder("Insumos a Utilizar"));
        insumosTableModel = new OrdenProduccionDetalleTableModel();
        tablaInsumos = new JTable(insumosTableModel);
        JButton btnQuitarInsumo = new SamvitexButton("Quitar Insumo", SamvitexButton.ButtonType.SECONDARY);
        panelInsumos.add(new JScrollPane(tablaInsumos), BorderLayout.CENTER);
        panelInsumos.add(btnQuitarInsumo, BorderLayout.SOUTH);
        splitPane.setLeftComponent(panelInsumos);

        // Panel Derecho: Productos Finales
        JPanel panelProductosFinales = new JPanel(new BorderLayout(5, 5));
        panelProductosFinales.setBorder(BorderFactory.createTitledBorder("Productos a Generar"));
        productosFinalesTableModel = new OrdenProduccionDetalleTableModel();
        tablaProductosFinales = new JTable(productosFinalesTableModel);
        JButton btnQuitarProductoFinal = new SamvitexButton("Quitar Producto", SamvitexButton.ButtonType.SECONDARY);
        panelProductosFinales.add(new JScrollPane(tablaProductosFinales), BorderLayout.CENTER);
        panelProductosFinales.add(btnQuitarProductoFinal, BorderLayout.SOUTH);
        splitPane.setRightComponent(panelProductosFinales);

        panelCentro.add(splitPane, "grow");

        // --- Panel de Botones Inferior ---
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnGuardar = new SamvitexButton("Crear Orden de Producción");
        JButton btnCancelar = new SamvitexButton("Cancelar", SamvitexButton.ButtonType.SECONDARY);
        panelBotones.add(btnCancelar);
        panelBotones.add(btnGuardar);

        // --- Ensamblaje Final ---
        panelPrincipal.add(panelCabecera, BorderLayout.NORTH);
        panelPrincipal.add(panelCentro, BorderLayout.CENTER);
        panelPrincipal.add(panelBotones, BorderLayout.SOUTH);
        add(panelPrincipal);

        // --- Action Listeners ---
        btnAnadirInsumo.addActionListener(e -> anadirDetalle(TipoDetalleProduccion.INSUMO));
        btnAnadirProductoFinal.addActionListener(e -> anadirDetalle(TipoDetalleProduccion.PRODUCTO_FINAL));
        btnQuitarInsumo.addActionListener(e -> quitarDetalle(tablaInsumos, insumosTableModel));
        btnQuitarProductoFinal.addActionListener(e -> quitarDetalle(tablaProductosFinales, productosFinalesTableModel));
        btnGuardar.addActionListener(e -> guardarOrden());
        btnCancelar.addActionListener(e -> dispose());
    }

    private void cargarDatosAsincronos(ServicioInventario si, TallerRepositorio tr, ServicioAlmacen sa) {
        // Deshabilitar ComboBoxes mientras se cargan los datos
        Stream.of(cmbTaller, cmbProducto, cmbAlmacenInsumos, cmbAlmacenDestino).forEach(cmb -> cmb.setEnabled(false));

        new SwingWorker<Map<String, List<?>>, Void>() {
            @Override
            protected Map<String, List<?>> doInBackground() throws Exception {
                Map<String, List<?>> datos = new HashMap<>();
                datos.put("talleres", tr.findByActivoTrueOrderByNombreAsc());
                datos.put("productos", si.obtenerTodosLosProductos());
                datos.put("almacenes", sa.obtenerTodosActivos());
                return datos;
            }
            @Override
            protected void done() {
                try {
                    Map<String, List<?>> datos = get();
                    List<Almacen> almacenes = (List<Almacen>) datos.get("almacenes");

                    cmbTaller.setModel(new DefaultComboBoxModel<>(new Vector<>((List<Taller>) datos.get("talleres"))));
                    cmbProducto.setModel(new DefaultComboBoxModel<>(new Vector<>((List<Producto>) datos.get("productos"))));
                    cmbAlmacenInsumos.setModel(new DefaultComboBoxModel<>(new Vector<>(almacenes)));
                    cmbAlmacenDestino.setModel(new DefaultComboBoxModel<>(new Vector<>(almacenes)));
                } catch (Exception e) {
                    mostrarError("Error al cargar datos iniciales: " + e.getCause().getMessage());
                } finally {
                    Stream.of(cmbTaller, cmbProducto, cmbAlmacenInsumos, cmbAlmacenDestino).forEach(cmb -> cmb.setEnabled(true));
                }
            }
        }.execute();
    }

    private void anadirDetalle(TipoDetalleProduccion tipo) {
        Producto producto = (Producto) cmbProducto.getSelectedItem();
        int cantidad = (Integer) spinCantidad.getValue();

        if (tipo == TipoDetalleProduccion.INSUMO) {
            insumosTableModel.addDetalle(producto, tipo, cantidad);
        } else {
            productosFinalesTableModel.addDetalle(producto, tipo, cantidad);
        }
    }

    private void quitarDetalle(JTable tabla, OrdenProduccionDetalleTableModel model) {
        int selectedRow = tabla.getSelectedRow();
        if (selectedRow >= 0) {
            model.removeDetalle(tabla.convertRowIndexToModel(selectedRow));
        } else {
            mostrarError("Seleccione un ítem de la tabla para quitar.");
        }
    }

    private void guardarOrden() {
        try {
            final org.springframework.security.core.Authentication authentication =
                    org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            // Validaciones de UI
            String codigo = txtCodigo.getText().trim();
            if (codigo.isBlank()) throw new ProduccionException("El código de la orden es obligatorio.");

            Taller taller = (Taller) cmbTaller.getSelectedItem();
            Almacen almacenInsumos = (Almacen) cmbAlmacenInsumos.getSelectedItem();
            Almacen almacenDestino = (Almacen) cmbAlmacenDestino.getSelectedItem();
            if (taller == null || almacenInsumos == null || almacenDestino == null) {
                throw new ProduccionException("Debe seleccionar taller y almacenes de origen/destino.");
            }

            List<OrdenProduccionDTO.DetalleDTO> detallesInsumos = insumosTableModel.getDetalles();
            List<OrdenProduccionDTO.DetalleDTO> detallesProductos = productosFinalesTableModel.getDetalles();
            if (detallesInsumos.isEmpty() || detallesProductos.isEmpty()) {
                throw new ProduccionException("La orden debe tener al menos un insumo y un producto final.");
            }

            // Combinar ambas listas de detalles en una sola
            List<OrdenProduccionDTO.DetalleDTO> todosLosDetalles = new ArrayList<>();
            todosLosDetalles.addAll(detallesInsumos);
            todosLosDetalles.addAll(detallesProductos);

            // Crear el DTO con todos los datos
            OrdenProduccionDTO dto = new OrdenProduccionDTO(
                    taller.getId(),
                    codigo,
                    almacenInsumos.getId(),
                    almacenDestino.getId(),
                    todosLosDetalles
            );

            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(authentication);
                    try {
                        servicioProduccion.crearOrdenProduccion(dto);
                        return null;
                    } finally {
                        org.springframework.security.core.context.SecurityContextHolder.clearContext();
                    }
                }
                @Override
                protected void done() {
                    try {
                        get();
                        JOptionPane.showMessageDialog(DialogoOrdenProduccion.this,
                                "Orden de Producción '" + codigo + "' creada exitosamente.",
                                "Éxito", JOptionPane.INFORMATION_MESSAGE);
                        onSaveSuccess.run();
                        dispose();
                    } catch (Exception e) {
                        mostrarError("Error al crear la orden: " + e.getCause().getMessage());
                    }
                }
            }.execute();

        } catch (ProduccionException | HeadlessException e) {
            mostrarError(e.getMessage());
        }
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }
}