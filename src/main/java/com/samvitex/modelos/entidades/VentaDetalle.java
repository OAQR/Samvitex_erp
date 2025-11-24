package com.samvitex.modelos.entidades;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Representa una línea de detalle dentro de una transacción de {@link Venta}.
 * Cada instancia de esta entidad corresponde a un producto específico que
 * fue vendido como parte de una transacción más grande.
 *
 * <p><b>Punto clave de diseño:</b> Se almacena el {@code precioUnitario} en el momento
 * de la venta. Esto es crucial para la integridad histórica de los reportes, ya que
 * el precio de un producto en la tabla {@code productos} puede cambiar con el tiempo,
 * pero el precio al que se vendió en una transacción pasada debe permanecer fijo.</p>
 *
 * Mapea a la tabla 'ventas_detalle' en la base de datos.
 */
@Entity
@Table(name = "ventas_detalle")
public class VentaDetalle {

    /**
     * Identificador único de la línea de detalle de la venta.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * La venta maestra a la que pertenece este detalle.
     * Esta es la parte "propietaria" de la relación bidireccional con {@link Venta}.
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "venta_id")
    private Venta venta;

    /**
     * El producto que fue vendido en esta línea de detalle.
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    /**
     * La cantidad de unidades del producto que se vendieron.
     */
    @Column(nullable = false)
    private Integer cantidad;

    /**
     * El precio de una sola unidad del producto en el momento exacto de la venta.
     * Este valor se copia desde la entidad {@link Producto} y se almacena aquí
     * para garantizar que los reportes históricos sean precisos.
     */
    @Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    /**
     * El subtotal para esta línea (cantidad * precioUnitario).
     * Se pre-calcula y almacena para simplificar las consultas de reportes.
     */
    @Column(name = "subtotal_linea", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotalLinea;

    /**
     * Constructor por defecto requerido por JPA.
     */
    public VentaDetalle() {}

    // --- Getters y Setters Estándar ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Venta getVenta() {
        return venta;
    }

    public void setVenta(Venta venta) {
        this.venta = venta;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public BigDecimal getSubtotalLinea() {
        return subtotalLinea;
    }

    public void setSubtotalLinea(BigDecimal subtotalLinea) {
        this.subtotalLinea = subtotalLinea;
    }
}