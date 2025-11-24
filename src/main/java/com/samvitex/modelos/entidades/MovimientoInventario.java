package com.samvitex.modelos.entidades;

import com.samvitex.modelos.enums.TipoMovimiento;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

/**
 * Representa un registro de auditoría para cada movimiento de stock de un producto.
 * Esta entidad es la piedra angular para la trazabilidad del inventario (Kardex),
 * permitiendo saber quién, cuándo, cómo y por qué cambió el stock de un artículo.
 *
 * <p>Cada vez que el stock de un producto es alterado (por una venta, una compra, un ajuste, etc.),
 * se debe crear una instancia de esta entidad y persistirla. La tabla está diseñada para
 * ser de "solo inserción" (append-only); los registros no deben modificarse ni eliminarse
 * una vez creados para mantener un historial fidedigno.</p>
 *
 * Mapea a la tabla 'movimientos_inventario' en la base de datos.
 */
@Entity
@Table(name = "movimientos_inventario")
public class MovimientoInventario {

    /**
     * Identificador único del movimiento.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * El producto cuyo stock fue afectado por este movimiento.
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "almacen_id")
    private Almacen almacen;

    /**
     * El usuario que realizó la operación que generó este movimiento.
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    /**
     * El tipo de movimiento, mapeado desde el enum {@link TipoMovimiento}.
     * Almacenado como un String en la base de datos para mayor legibilidad.
     */
    @Column(name = "tipo", nullable = false)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private TipoMovimiento tipo;

    /**
     * La cantidad de unidades que se movieron.
     * Es <b>positiva</b> para entradas (compras, ajustes positivos, devoluciones)
     * y <b>negativa</b> para salidas (ventas, ajustes negativos).
     */
    @Column(name = "cantidad_movida", nullable = false)
    private Integer cantidadMovida;

    /**
     * El stock del producto <b>antes</b> de que ocurriera este movimiento.
     * Este campo es vital para la auditoría y para reconstruir el historial de stock.
     */
    @Column(name = "stock_anterior", nullable = false)
    private Integer stockAnterior;

    /**
     * El stock del producto <b>después</b> de que ocurriera este movimiento.
     * Debe ser igual a {@code stockAnterior + cantidadMovida}.
     */
    @Column(name = "stock_nuevo", nullable = false)
    private Integer stockNuevo;

    /**
     * Notas o comentarios adicionales sobre el movimiento, especialmente útil
     * para movimientos de ajuste manual.
     */
    @Column(columnDefinition = "TEXT")
    private String notas;

    /**
     * Fecha y hora en que se registró el movimiento.
     */
    @CreationTimestamp
    @Column(name = "fecha_movimiento", nullable = false, updatable = false)
    private Instant fechaMovimiento;

    // Las siguientes dos relaciones son opcionales y establecen el vínculo directo
    // con la transacción que originó el movimiento. Un movimiento puede tener una
    // venta_id O una compra_id, pero no ambas.

    /**
     * La venta que originó este movimiento. Será {@code null} si el movimiento
     * no fue causado por una venta (ej. fue una compra o un ajuste).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venta_id")
    private Venta venta;

    /**
     * La compra que originó este movimiento. Será {@code null} si el movimiento
     * no fue causado por una compra.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compra_id")
    private Compra compra;

    /**
     * Constructor por defecto requerido por JPA.
     */
    public MovimientoInventario() {}

    // --- Getters y Setters Estándar ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public TipoMovimiento getTipo() {
        return tipo;
    }

    public void setTipo(TipoMovimiento tipo) {
        this.tipo = tipo;
    }

    public Integer getCantidadMovida() {
        return cantidadMovida;
    }

    public void setCantidadMovida(Integer cantidadMovida) {
        this.cantidadMovida = cantidadMovida;
    }

    public Integer getStockAnterior() {
        return stockAnterior;
    }

    public void setStockAnterior(Integer stockAnterior) {
        this.stockAnterior = stockAnterior;
    }

    public Integer getStockNuevo() {
        return stockNuevo;
    }

    public void setStockNuevo(Integer stockNuevo) {
        this.stockNuevo = stockNuevo;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }

    public Instant getFechaMovimiento() {
        return fechaMovimiento;
    }

    public void setFechaMovimiento(Instant fechaMovimiento) {
        this.fechaMovimiento = fechaMovimiento;
    }

    public Venta getVenta() {
        return venta;
    }

    public void setVenta(Venta venta) {
        this.venta = venta;
    }

    public Compra getCompra() {
        return compra;
    }

    public void setCompra(Compra compra) {
        this.compra = compra;
    }

    public Almacen getAlmacen() {
        return almacen;
    }

    public void setAlmacen(Almacen almacen) {
        this.almacen = almacen;
    }
}