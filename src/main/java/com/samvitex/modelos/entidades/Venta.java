package com.samvitex.modelos.entidades;

import com.samvitex.modelos.enums.EstadoTransaccion;
import com.samvitex.modelos.enums.TipoComprobante;
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
 * Representa una transacción de venta completa en el sistema.
 * Esta entidad es la tabla maestra que agrupa los detalles de los productos vendidos
 * y los movimientos de inventario resultantes.
 *
 * <p><b>Características Clave:</b></p>
 * <ul>
 *   <li><b>Inmutabilidad Transaccional:</b> Una vez creada, una venta no debe ser modificada.
 *       Cualquier cambio (como una devolución) se gestiona a través de una nueva transacción que
 *       hace referencia a esta.</li>
 *   <li><b>Propiedad de Cascada:</b> La Venta es "propietaria" de sus {@code VentaDetalle} y
 *       {@code MovimientoInventario}. Al guardar una venta, JPA/Hibernate guardará
 *       automáticamente en cascada todos los detalles y movimientos asociados.
 *       Si una venta se elimina (lo cual no debería ocurrir en producción), sus detalles
 *       y movimientos asociados también se eliminarían gracias a {@code orphanRemoval=true}.</li>
 * </ul>
 *
 * Mapea a la tabla 'ventas' en la base de datos.
 */
@Entity
@Table(name = "ventas")
public class Venta {

    /**
     * Identificador único de la venta, generado automáticamente.
     * Es de tipo {@code Long} para soportar un gran volumen de transacciones.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * El cliente asociado a esta venta.
     * Es una relación obligatoria {@code ManyToOne}. La carga es perezosa (LAZY)
     * para optimizar el rendimiento, cargando el cliente solo cuando es necesario.
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    /**
     * El usuario (vendedor) que registró la venta.
     * Relación obligatoria y de carga perezosa.
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "almacen_origen_id")
    private Almacen almacenOrigen;

    /**
     * Fecha y hora en que se registró la transacción.
     * Se genera automáticamente en el momento de la creación y no puede ser actualizada.
     */
    @CreationTimestamp
    @Column(name = "fecha_venta", nullable = false, updatable = false)
    private Instant fechaVenta;

    /**
     * Suma de los subtotales de línea de todos los productos, sin incluir impuestos.
     * Se utiliza {@code BigDecimal} para una precisión monetaria exacta.
     */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    /**
     * Monto total de impuestos calculados para esta venta.
     */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal impuestos;

    /**
     * Monto final a pagar (subtotal + impuestos).
     */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    /**
     * Estado actual de la venta (ej. "COMPLETADA", "CANCELADA").
     * Permite gestionar el ciclo de vida de la transacción.
     */
    @Column(nullable = false)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private EstadoTransaccion estado;

    @Column(name = "tipo_comprobante")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private TipoComprobante tipoComprobante;

    /**
     * Lista de los ítems de producto que componen esta venta.
     * La anotación {@code mappedBy="venta"} indica que la entidad {@link VentaDetalle}
     * es la propietaria de esta relación bidireccional.
     */
    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<VentaDetalle> detalles = new ArrayList<>();

    /**
     * Conjunto de movimientos de inventario generados por esta venta.
     * Esta es una relación unidireccional clave para la trazabilidad. La venta
     * es propietaria de estos movimientos.
     * Se utiliza un {@link Set} para evitar la posibilidad de registrar movimientos duplicados.
     */
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "venta_id") // Esta es la FK en la tabla 'movimientos_inventario'.
    private Set<MovimientoInventario> movimientosGenerados = new HashSet<>();


    /**
     * Constructor por defecto requerido por JPA.
     */
    public Venta() {
    }

    // --- Métodos de Ayuda (Helpers) ---

    /**
     * Metodo de utilidad para añadir un detalle a la venta de forma segura,
     * manteniendo la consistencia de la relación bidireccional.
     *
     * @param detalle El objeto {@link VentaDetalle} a añadir a esta venta.
     */
    public void addDetalle(VentaDetalle detalle) {
        detalles.add(detalle);
        detalle.setVenta(this);
    }

    /**
     * Metodo de utilidad para asociar un movimiento de inventario a esta venta.
     *
     * @param movimiento El {@link MovimientoInventario} generado por uno de los ítems de esta venta.
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

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Almacen getAlmacenOrigen() {
        return almacenOrigen;
    }

    public void setAlmacenOrigen(Almacen almacenOrigen) {
        this.almacenOrigen = almacenOrigen;
    }

    public Instant getFechaVenta() {
        return fechaVenta;
    }

    public void setFechaVenta(Instant fechaVenta) {
        this.fechaVenta = fechaVenta;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getImpuestos() {
        return impuestos;
    }

    public void setImpuestos(BigDecimal impuestos) {
        this.impuestos = impuestos;
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

    public TipoComprobante getTipoComprobante() {
        return tipoComprobante;
    }

    public void setTipoComprobante(TipoComprobante tipoComprobante) {
        this.tipoComprobante = tipoComprobante;
    }

    public List<VentaDetalle> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<VentaDetalle> detalles) {
        this.detalles = detalles;
    }

    public Set<MovimientoInventario> getMovimientosGenerados() {
        return movimientosGenerados;
    }

    public void setMovimientosGenerados(Set<MovimientoInventario> movimientosGenerados) {
        this.movimientosGenerados = movimientosGenerados;
    }
}