package com.samvitex.ui.paneles;

import com.samvitex.modelos.dto.ReporteVentasDTO;
import com.samvitex.modelos.entidades.MovimientoInventario;
import com.samvitex.modelos.entidades.Producto;
import com.samvitex.servicios.ServicioInventario;
import com.samvitex.servicios.ServicioReportes;
import com.samvitex.ui.modelos_tabla.KardexTableModel;
import com.samvitex.ui.modelos_tabla.ReporteVentasTableModel;
import com.samvitex.ui.presentadores.ReportesPresenter;
import com.samvitex.ui.theme.SamvitexButton;
import com.samvitex.ui.vistas.interfaces.ReportesView;
import net.miginfocom.swing.MigLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Vector;

/**
 * Panel de Swing que implementa la {@link ReportesView}.
 * Proporciona la UI para configurar y visualizar reportes de negocio.
 */
@Component
public class PanelReportes extends JPanel implements ReportesView {

    private final ReportesPresenter presenter;

    private JComboBox<String> cmbTipoReporte;
    private JSpinner dateInicio, dateFin;
    private JComboBox<Producto> cmbProductoKardex;
    private JPanel panelFiltroProducto;
    private JTable tablaResultados;
    private ReporteVentasTableModel ventasTableModel;
    private KardexTableModel kardexTableModel;

    @Autowired
    public PanelReportes(ServicioReportes servicioReportes, ServicioInventario servicioInventario) {
        this.presenter = new ReportesPresenter(this, servicioReportes, servicioInventario);
        inicializarUI();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        presenter.cargarDatosIniciales();
    }

    private void inicializarUI() {
        setLayout(new MigLayout("fill, insets 10", "[280px!][grow]", "[grow]"));

        // Panel de Filtros
        JPanel panelFiltros = new JPanel(new MigLayout("wrap, fillx", "[grow]"));
        panelFiltros.setBorder(BorderFactory.createTitledBorder("Configuración del Reporte"));

        cmbTipoReporte = new JComboBox<>(new String[]{"Ventas por Producto", "Kardex de Producto"});

        Date hoy = new Date();
        Date haceUnMes = Date.from(Instant.now().minus(30, ChronoUnit.DAYS));
        dateInicio = new JSpinner(new SpinnerDateModel(haceUnMes, null, hoy, java.util.Calendar.DAY_OF_MONTH));
        dateFin = new JSpinner(new SpinnerDateModel(hoy, null, hoy, java.util.Calendar.DAY_OF_MONTH));
        dateInicio.setEditor(new JSpinner.DateEditor(dateInicio, "dd/MM/yyyy"));
        dateFin.setEditor(new JSpinner.DateEditor(dateFin, "dd/MM/yyyy"));

        panelFiltroProducto = new JPanel(new MigLayout("wrap, fillx, insets 0", "[grow]"));
        panelFiltroProducto.add(new JLabel("Seleccione un Producto:"));
        cmbProductoKardex = new JComboBox<>();
        panelFiltroProducto.add(cmbProductoKardex, "growx");
        panelFiltroProducto.setVisible(false);

        JButton btnGenerar = new SamvitexButton("Generar Reporte");

        panelFiltros.add(new JLabel("Tipo de Reporte:"));
        panelFiltros.add(cmbTipoReporte, "growx");
        panelFiltros.add(new JLabel("Fecha de Inicio:"), "gaptop 15");
        panelFiltros.add(dateInicio, "growx");
        panelFiltros.add(new JLabel("Fecha de Fin:"));
        panelFiltros.add(dateFin, "growx");
        panelFiltros.add(panelFiltroProducto, "growx, gaptop 10");
        panelFiltros.add(btnGenerar, "gaptop 20, growx, h 35!");

        // Panel de Resultados
        JPanel panelResultados = new JPanel(new BorderLayout());
        panelResultados.setBorder(BorderFactory.createTitledBorder("Resultados"));

        ventasTableModel = new ReporteVentasTableModel();
        kardexTableModel = new KardexTableModel();
        tablaResultados = new JTable(ventasTableModel); // Empezamos con el modelo de ventas por defecto
        tablaResultados.setAutoCreateRowSorter(true);

        panelResultados.add(new JScrollPane(tablaResultados), BorderLayout.CENTER);

        add(panelFiltros, "growy, top");
        add(panelResultados, "grow");

        // Listeners
        cmbTipoReporte.addActionListener(e -> presenter.onTipoReporteCambiado((String) cmbTipoReporte.getSelectedItem()));
        btnGenerar.addActionListener(e -> presenter.generarReporte(
                (String) cmbTipoReporte.getSelectedItem(),
                ((Date) dateInicio.getValue()).toInstant(),
                ((Date) dateFin.getValue()).toInstant(),
                (Producto) cmbProductoKardex.getSelectedItem()
        ));
    }

    @Override
    public void mostrarReporteVentas(List<ReporteVentasDTO> datos) {
        tablaResultados.setModel(ventasTableModel);
        ventasTableModel.setDatos(datos);
    }

    @Override
    public void mostrarReporteKardex(List<MovimientoInventario> datos) {
        tablaResultados.setModel(kardexTableModel);
        kardexTableModel.setDatos(datos);
    }

    @Override
    public void mostrarListaProductosParaSeleccion(List<Producto> productos) {
        cmbProductoKardex.setModel(new DefaultComboBoxModel<>(new Vector<>(productos)));
    }

    @Override
    public void cambiarFiltrosVisibles(String tipoReporte) {
        panelFiltroProducto.setVisible("Kardex de Producto".equals(tipoReporte));
        // Limpiamos la tabla al cambiar de tipo de reporte
        if ("Kardex de Producto".equals(tipoReporte)) {
            tablaResultados.setModel(kardexTableModel);
            kardexTableModel.setDatos(List.of());
        } else {
            tablaResultados.setModel(ventasTableModel);
            ventasTableModel.setDatos(List.of());
        }
    }

    @Override
    public void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error en Reportes", JOptionPane.ERROR_MESSAGE);
    }
}