package com.samvitex.modelos.entidades;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Representa una línea de detalle dentro de una transacción de {@link Compra}.
 * Cada instancia de esta entidad corresponde a un producto específico que fue adquirido
 * de un proveedor.
 *
 * <p><b>Punto clave de diseño:</b> Esta entidad almacena una copia del {@code costoUnitarioCompra}
 * en el momento de la transacción. Esto es fundamental para la integridad de los datos contables
 * y de reportes, ya que el costo de un producto puede variar con el tiempo, pero el costo
 * de una compra histórica debe permanecer inalterado.</p>
 *
 * Mapea a la tabla 'compras_detalle' en la base de datos.
 */
@Entity
@Table(name = "compras_detalle")
public class CompraDetalle {

    /**
     * Identificador único de la línea de detalle de la compra.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * La compra maestra a la que pertenece este detalle.
     * Esta es la parte "propietaria" de la relación bidireccional, conteniendo la clave foránea.
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "compra_id")
    private Compra compra;

    /**
     * El producto que fue comprado en esta línea de detalle.
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    /**
     * La cantidad de unidades del producto que se compraron.
     */
    @Column(nullable = false)
    private Integer cantidad;

    /**
     * El costo de una sola unidad del producto en el momento exacto de la compra.
     * Este valor es crucial para el cálculo preciso del valor del inventario (costo promedio, etc.).
     */
    @Column(name = "costo_unitario_compra", nullable = false, precision = 10, scale = 2)
    private BigDecimal costoUnitarioCompra;

    /**
     * El subtotal para esta línea (cantidad * costoUnitarioCompra).
     * Se almacena para simplificar y optimizar las consultas de reportes de compras.
     */
    @Column(name = "subtotal_linea", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotalLinea;

    /**
     * Constructor por defecto requerido por JPA.
     */
    public CompraDetalle() {
    }

    // --- Getters y Setters Estándar ---


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Compra getCompra() {
        return compra;
    }

    public void setCompra(Compra compra) {
        this.compra = compra;
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

    public BigDecimal getCostoUnitarioCompra() {
        return costoUnitarioCompra;
    }

    public void setCostoUnitarioCompra(BigDecimal costoUnitarioCompra) {
        this.costoUnitarioCompra = costoUnitarioCompra;
    }

    public BigDecimal getSubtotalLinea() {
        return subtotalLinea;
    }

    public void setSubtotalLinea(BigDecimal subtotalLinea) {
        this.subtotalLinea = subtotalLinea;
    }
}