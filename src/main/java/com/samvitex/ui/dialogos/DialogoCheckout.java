package com.samvitex.ui.dialogos;

import com.samvitex.ui.theme.SamvitexButton;
import net.miginfocom.swing.MigLayout;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Diálogo modal para procesar el pago final de una venta.
 * Permite seleccionar el método de pago, el tipo de comprobante y gestionar
 * el vuelto para pagos en efectivo.
 */
public class DialogoCheckout extends JDialog {

    private final BigDecimal totalVenta;
    private boolean confirmado = false;

    // Componentes de UI
    private JFormattedTextField txtMontoRecibido;
    private JLabel lblVuelto;
    private JPanel panelEfectivo;
    private JComboBox<String> cmbComprobante;
    private JRadioButton radioEfectivo, radioDigital;

    // Formateadores de moneda
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "PE"));
    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(new Locale("es", "PE"));

    public DialogoCheckout(Frame owner, BigDecimal totalVenta) {
        super(owner, "Procesar Pago", true);
        this.totalVenta = totalVenta;
        numberFormat.setMinimumFractionDigits(2);
        numberFormat.setMaximumFractionDigits(2);

        inicializarUI();
        pack(); // Ajusta el tamaño del diálogo a sus componentes
        setMinimumSize(getSize());
        setLocationRelativeTo(owner);
    }

    private void inicializarUI() {
        setLayout(new MigLayout("wrap, fillx, insets 20", "[grow]"));

        // --- Título y Total a Pagar ---
        JLabel lblTitulo = new JLabel("Total a Pagar:", SwingConstants.CENTER);
        lblTitulo.setFont(lblTitulo.getFont().deriveFont(18f));
        JLabel lblTotal = new JLabel(currencyFormat.format(totalVenta), SwingConstants.CENTER);
        lblTotal.setFont(lblTotal.getFont().deriveFont(Font.BOLD, 32f));
        lblTotal.setForeground(Color.decode("#28CD41"));

        add(lblTitulo, "growx");
        add(lblTotal, "growx, gapbottom 20");

        // --- Método de Pago ---
        JPanel panelMetodoPago = new JPanel(new MigLayout("insets 5 10 5 10, fillx"));
        panelMetodoPago.setBorder(BorderFactory.createTitledBorder("Método de Pago"));
        radioEfectivo = new JRadioButton("Efectivo");
        radioDigital = new JRadioButton("Tarjeta / Yape / Plin");
        ButtonGroup grupoMetodo = new ButtonGroup();
        grupoMetodo.add(radioEfectivo);
        grupoMetodo.add(radioDigital);
        panelMetodoPago.add(radioEfectivo);
        panelMetodoPago.add(radioDigital);
        radioEfectivo.setSelected(true);
        add(panelMetodoPago, "growx, wrap");

        // --- Panel de Gestión de Efectivo (inicialmente visible) ---
        panelEfectivo = new JPanel(new MigLayout("insets 10, fillx", "[right]10[grow,fill]"));
        txtMontoRecibido = new JFormattedTextField(numberFormat);
        lblVuelto = new JLabel(currencyFormat.format(BigDecimal.ZERO));
        lblVuelto.setFont(lblVuelto.getFont().deriveFont(Font.BOLD));
        panelEfectivo.add(new JLabel("Monto Recibido:"));
        panelEfectivo.add(txtMontoRecibido, "wrap, growx");
        panelEfectivo.add(new JLabel("Vuelto:"));
        panelEfectivo.add(lblVuelto);
        add(panelEfectivo, "growx, wrap");

        // --- Tipo de Comprobante ---
        JPanel panelComprobante = new JPanel(new MigLayout("insets 0, fillx", "[right]15[grow,fill]"));
        cmbComprobante = new JComboBox<>(new String[]{"Nota de Venta", "Boleta", "Factura"});
        panelComprobante.add(new JLabel("Emitir Comprobante:"));
        panelComprobante.add(cmbComprobante, "growx");
        add(panelComprobante, "growx, gaptop 10, wrap");

        // --- Botones de Acción ---
        JSeparator separator = new JSeparator();
        add(separator, "growx, gaptop 20, wrap");

        JButton btnConfirmar = new SamvitexButton("Confirmar y Registrar Venta");
        JButton btnCancelar = new SamvitexButton("Cancelar", SamvitexButton.ButtonType.SECONDARY);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBotones.add(btnCancelar);
        panelBotones.add(btnConfirmar);
        add(panelBotones, "growx, gaptop 10, dock south"); // Fija los botones en la parte inferior

        // --- Lógica y Listeners ---
        radioEfectivo.addActionListener(e -> panelEfectivo.setVisible(true));
        radioDigital.addActionListener(e -> panelEfectivo.setVisible(false));

        txtMontoRecibido.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { calcularVuelto(); }
            @Override public void removeUpdate(DocumentEvent e) { calcularVuelto(); }
            @Override public void changedUpdate(DocumentEvent e) { calcularVuelto(); }
        });

        // Listeners para los botones de acción
        btnConfirmar.addActionListener(e -> {
            this.confirmado = true;
            dispose();
        });
        btnCancelar.addActionListener(e -> dispose());

        SwingUtilities.invokeLater(txtMontoRecibido::requestFocusInWindow);
    }

    private void calcularVuelto() {
        try {
            Object value = txtMontoRecibido.getValue();
            BigDecimal montoRecibido = BigDecimal.ZERO;

            if (value instanceof Number) {
                montoRecibido = new BigDecimal(value.toString());
            }

            BigDecimal vuelto = montoRecibido.subtract(totalVenta);
            if (vuelto.compareTo(BigDecimal.ZERO) < 0) {
                vuelto = BigDecimal.ZERO;
            }
            lblVuelto.setText(currencyFormat.format(vuelto));

        } catch (Exception e) {
            lblVuelto.setText(currencyFormat.format(BigDecimal.ZERO));
        }
    }

    public BigDecimal getTotalVenta() {
        return totalVenta;
    }

    public void setConfirmado(boolean confirmado) {
        this.confirmado = confirmado;
    }

    public JFormattedTextField getTxtMontoRecibido() {
        return txtMontoRecibido;
    }

    public void setTxtMontoRecibido(JFormattedTextField txtMontoRecibido) {
        this.txtMontoRecibido = txtMontoRecibido;
    }

    public JLabel getLblVuelto() {
        return lblVuelto;
    }

    public void setLblVuelto(JLabel lblVuelto) {
        this.lblVuelto = lblVuelto;
    }

    public JPanel getPanelEfectivo() {
        return panelEfectivo;
    }

    public void setPanelEfectivo(JPanel panelEfectivo) {
        this.panelEfectivo = panelEfectivo;
    }

    public JComboBox<String> getCmbComprobante() {
        return cmbComprobante;
    }

    public void setCmbComprobante(JComboBox<String> cmbComprobante) {
        this.cmbComprobante = cmbComprobante;
    }

    public JRadioButton getRadioEfectivo() {
        return radioEfectivo;
    }

    public void setRadioEfectivo(JRadioButton radioEfectivo) {
        this.radioEfectivo = radioEfectivo;
    }

    public JRadioButton getRadioDigital() {
        return radioDigital;
    }

    public void setRadioDigital(JRadioButton radioDigital) {
        this.radioDigital = radioDigital;
    }

    public NumberFormat getCurrencyFormat() {
        return currencyFormat;
    }

    public NumberFormat getNumberFormat() {
        return numberFormat;
    }

    public boolean isConfirmado() {
        return confirmado;
    }

    public String getMetodoPagoSeleccionado() {
        return radioEfectivo.isSelected() ? "Efectivo" : "Digital";
    }

    public String getTipoComprobanteSeleccionado() {
        return (String) cmbComprobante.getSelectedItem();
    }
}