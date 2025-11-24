package com.samvitex.modelos.entidades;

import com.samvitex.modelos.enums.EstadoProduccion;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Entidad maestra que representa una orden de producción o fabricación.
 * <p>
 * Documenta la intención de transformar insumos (de un almacén de origen) en productos
 * terminados (que ingresarán a un almacén de destino), asignando la tarea a un {@link Taller}.
 * </p>
 */
@Entity
@Table(name = "ordenes_produccion")
public class OrdenProduccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String codigo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoProduccion estado;

    // --- Relaciones con otras Entidades ---

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "taller_id")
    private Taller taller;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_responsable_id")
    private Usuario usuarioResponsable;

    /**
     * El almacén desde el cual se descontarán los insumos al iniciar la producción.
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "almacen_insumos_id")
    private Almacen almacenInsumos;

    /**
     * El almacén al cual ingresarán los productos terminados al finalizar la producción.
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "almacen_destino_id")
    private Almacen almacenDestino;

    // --- Timestamps del Ciclo de Vida ---

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private Instant fechaCreacion;

    @Column(name = "fecha_inicio_produccion")
    private Instant fechaInicioProduccion;

    @Column(name = "fecha_finalizacion")
    private Instant fechaFinalizacion;

    // --- Colecciones de Entidades Hijas (Cascada) ---

    @OneToMany(mappedBy = "ordenProduccion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrdenProduccionDetalle> detalles = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "orden_produccion_id")
    private Set<MovimientoInventario> movimientosGenerados = new HashSet<>();

    /**
     * Constructor por defecto requerido por JPA.
     */
    public OrdenProduccion() {}

    // --- Métodos de Ayuda (Helpers) ---

    /**
     * Añade un detalle a la orden, manteniendo la consistencia de la relación bidireccional.
     * @param detalle El {@link OrdenProduccionDetalle} a añadir.
     */
    public void addDetalle(OrdenProduccionDetalle detalle) {
        detalles.add(detalle);
        detalle.setOrdenProduccion(this);
    }

    /**
     * Asocia un movimiento de inventario a esta orden.
     * @param movimiento El {@link MovimientoInventario} a añadir.
     */
    public void addMovimiento(MovimientoInventario movimiento) {
        movimientosGenerados.add(movimiento);
    }

    // --- Getters y Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public EstadoProduccion getEstado() {
        return estado;
    }

    public void setEstado(EstadoProduccion estado) {
        this.estado = estado;
    }

    public Taller getTaller() {
        return taller;
    }

    public void setTaller(Taller taller) {
        this.taller = taller;
    }

    public Usuario getUsuarioResponsable() {
        return usuarioResponsable;
    }

    public void setUsuarioResponsable(Usuario usuarioResponsable) {
        this.usuarioResponsable = usuarioResponsable;
    }

    public Almacen getAlmacenInsumos() {
        return almacenInsumos;
    }

    public void setAlmacenInsumos(Almacen almacenInsumos) {
        this.almacenInsumos = almacenInsumos;
    }

    public Almacen getAlmacenDestino() {
        return almacenDestino;
    }

    public void setAlmacenDestino(Almacen almacenDestino) {
        this.almacenDestino = almacenDestino;
    }

    public Instant getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Instant fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Instant getFechaInicioProduccion() {
        return fechaInicioProduccion;
    }

    public void setFechaInicioProduccion(Instant fechaInicioProduccion) {
        this.fechaInicioProduccion = fechaInicioProduccion;
    }

    public Instant getFechaFinalizacion() {
        return fechaFinalizacion;
    }

    public void setFechaFinalizacion(Instant fechaFinalizacion) {
        this.fechaFinalizacion = fechaFinalizacion;
    }

    public List<OrdenProduccionDetalle> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<OrdenProduccionDetalle> detalles) {
        this.detalles = detalles;
    }

    public Set<MovimientoInventario> getMovimientosGenerados() {
        return movimientosGenerados;
    }

    public void setMovimientosGenerados(Set<MovimientoInventario> movimientosGenerados) {
        this.movimientosGenerados = movimientosGenerados;
    }
}