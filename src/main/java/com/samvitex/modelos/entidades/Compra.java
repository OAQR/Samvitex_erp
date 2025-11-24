package com.samvitex.modelos.entidades;

import com.samvitex.modelos.enums.EstadoTransaccion;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Representa una transacción de compra de mercancía a un {@link Proveedor}.
 * Esta entidad actúa como la tabla maestra para una orden de compra, agrupando
 * los detalles de los productos adquiridos y los movimientos de inventario de entrada
 * que se generan.
 *
 * <p>Al igual que la entidad {@link Venta}, una Compra es una transacción que, una vez
 * registrada, se considera inmutable. Las correcciones se manejarían a través de
 * transacciones de ajuste.</p>
 *
 * <p><b>Diseño de Persistencia:</b> La entidad Compra es propietaria de sus
 * {@link CompraDetalle} y {@link MovimientoInventario} asociados. Gracias a
 * {@code CascadeType.ALL}, al persistir un objeto Compra, JPA se encargará
 * de guardar automáticamente todos los objetos relacionados en sus respectivas tablas
 * dentro de la misma transacción.</p>
 *
 * Mapea a la tabla 'compras' en la base de datos.
 */
@Entity
@Table(name = "compras")
public class Compra {

    /**
     * Identificador único de la compra, generado automáticamente.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * El proveedor al que se le realizó la compra.
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "proveedor_id")
    private Proveedor proveedor;

    /**
     * El usuario del sistema que registró la compra.
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "almacen_destino_id")
    private Almacen almacenDestino;

    /**
     * Fecha y hora en que se registró la compra.
     * Se asigna automáticamente al momento de la creación.
     */
    @CreationTimestamp
    @Column(name = "fecha_compra", nullable = false, updatable = false)
    private Instant fechaCompra;

    /**
     * Número de factura, guía de remisión u otro documento de referencia
     * proporcionado por el proveedor.
     */
    @Column(name = "referencia_factura", length = 100)
    private String referenciaFactura;

    /**
     * El costo total de la compra.
     */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    /**
     * El estado de la compra (ej. "RECIBIDA", "ORDENADA", "CANCELADA").
     */
    @Column(nullable = false)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private EstadoTransaccion estado;

    /**
     * Lista de los ítems de producto que componen esta compra.
     * La entidad {@link CompraDetalle} es la propietaria de esta relación bidireccional.
     */
    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<CompraDetalle> detalles = new ArrayList<>();

    /**
     * Conjunto de movimientos de inventario de entrada generados por esta compra.
     * Esta es la relación clave que garantiza la trazabilidad del stock desde su origen.
     * La Compra es la propietaria de estos movimientos.
     */
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "compra_id") // Esta es la FK en la tabla 'movimientos_inventario'.
    private Set<MovimientoInventario> movimientosGenerados = new HashSet<>();

    /**
     * Constructor por defecto requerido por JPA.
     */
    public Compra() {
    }

    // --- Métodos de Ayuda (Helpers) ---

    /**
     * Metodo de utilidad para añadir un detalle a la compra de forma segura,
     * manteniendo la consistencia de la relación bidireccional.
     *
     * @param detalle El objeto {@link CompraDetalle} a añadir a esta compra.
     */
    public void addDetalle(CompraDetalle detalle) {
        detalles.add(detalle);
        detalle.setCompra(this);
    }

    /**
     * Metodo de utilidad para asociar un movimiento de inventario a esta compra.
     *
     * @param movimiento El {@link MovimientoInventario} generado por la recepción de un ítem de esta compra.
     */
    public void addMovimiento(MovimientoInventario movimiento) {
        movimientosGenerados.add(movimiento);
    }

    // --- Getters y Setters Estándar ---


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Proveedor getProveedor() {
        return proveedor;
    }

    public void setProveedor(Proveedor proveedor) {
        this.proveedor = proveedor;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Almacen getAlmacenDestino() {
        return almacenDestino;
    }

    public void setAlmacenDestino(Almacen almacenDestino) {
        this.almacenDestino = almacenDestino;
    }

    public Instant getFechaCompra() {
        return fechaCompra;
    }

    public void setFechaCompra(Instant fechaCompra) {
        this.fechaCompra = fechaCompra;
    }

    public String getReferenciaFactura() {
        return referenciaFactura;
    }

    public void setReferenciaFactura(String referenciaFactura) {
        this.referenciaFactura = referenciaFactura;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public EstadoTransaccion getEstado() {
        return estado;
    }

    public void setEstado(EstadoTransaccion estado) {
        this.estado = estado;
    }

    public List<CompraDetalle> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<CompraDetalle> detalles) {
        this.detalles = detalles;
    }

    public Set<MovimientoInventario> getMovimientosGenerados() {
        return movimientosGenerados;
    }

    public void setMovimientosGenerados(Set<MovimientoInventario> movimientosGenerados) {
        this.movimientosGenerados = movimientosGenerados;
    }
}