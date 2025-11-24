package com.samvitex.ui.paneles;

import com.samvitex.modelos.dto.DashboardStatsDTO;
import com.samvitex.ui.componentes.CardKPI;
import com.samvitex.ui.presentadores.DashboardPresenter;
import com.samvitex.ui.theme.SamvitexButton;
import com.samvitex.ui.vistas.interfaces.DashboardView;
import jakarta.annotation.PostConstruct;
import net.miginfocom.swing.MigLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Panel de Swing que implementa la {@link DashboardView}.
 *
 * <p>Este panel es responsable de renderizar la interfaz de usuario del dashboard,
 * que consiste en una serie de tarjetas de KPI ({@link CardKPI}). Delega toda la
 * lógica de carga de datos al {@link DashboardPresenter}.</p>
 */
@Component
public class PanelDashboard extends JPanel implements DashboardView {

    private final DashboardPresenter presenter;

    // Componentes de la UI
    private CardKPI cardTotalProductos;
    private CardKPI cardStockBajo;
    private CardKPI cardValorInventario;
    private CardKPI cardVentasHoy;

    @Autowired
    public PanelDashboard(@Lazy DashboardPresenter presenter) {
        this.presenter = presenter;
        this.presenter.setView(this);
        inicializarUI();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        // Cuando el panel se hace visible, solicita la carga de estadísticas.
        presenter.cargarEstadisticas();
    }

    private void inicializarUI() {
        // Layout principal con 4 columnas y espaciado automático.
        setLayout(new MigLayout("wrap 4, fill, insets 20", "[]push[]push[]push[]", "[][grow]"));

        // -- Título y Botón de Refresco --
        JLabel lblTitulo = new JLabel("Dashboard General");
        lblTitulo.putClientProperty("FlatLaf.style", "font: bold $h1.font");
        JButton btnRefrescar = new SamvitexButton("Refrescar", SamvitexButton.ButtonType.SECONDARY);

        add(lblTitulo, "span 3, align left");
        add(btnRefrescar, "align right, wrap");

        // -- Tarjetas de KPI --
        cardTotalProductos = new CardKPI("Total de Productos", "...");
        cardStockBajo = new CardKPI("Productos con Stock Bajo", "...");
        cardValorInventario = new CardKPI("Valor del Inventario", "...");
        cardVentasHoy = new CardKPI("Ventas de Hoy", "...");

        add(cardTotalProductos, "grow, h 120!");
        add(cardStockBajo, "grow, h 120!");
        add(cardValorInventario, "grow, h 120!");
        add(cardVentasHoy, "grow, h 120!");

        // Espaciador para empujar contenido futuro hacia abajo
        add(new JLabel(), "span, grow, wrap");

        // Listener para el botón de refresco
        btnRefrescar.addActionListener(e -> refrescarVista());
    }

    @Override
    public void mostrarEstadisticas(DashboardStatsDTO stats) {
        // Formateador para moneda local (Soles Peruanos)
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "PE"));

        cardTotalProductos.setValor(String.valueOf(stats.totalProductos()));
        cardStockBajo.setValor(String.valueOf(stats.productosConStockBajo()));
        cardValorInventario.setValor(currencyFormat.format(stats.valorTotalInventario()));
        cardVentasHoy.setValor(currencyFormat.format(stats.ventasHoy()));
    }

    @Override
    public void mostrarError(String mensaje) {
        // En caso de error, se muestra en las tarjetas para no ser intrusivo.
        cardTotalProductos.setValor("Error");
        cardStockBajo.setValor("Error");
        cardValorInventario.setValor("Error");
        cardVentasHoy.setValor("Error");
        // Adicionalmente, se puede mostrar un diálogo
        JOptionPane.showMessageDialog(this, mensaje, "Error en Dashboard", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void refrescarVista() {
        // Muestra un estado de "cargando" antes de solicitar los datos.
        cardTotalProductos.setValor("...");
        cardStockBajo.setValor("...");
        cardValorInventario.setValor("...");
        cardVentasHoy.setValor("...");
        // Delega la acción al presenter
        presenter.cargarEstadisticas();
    }
}