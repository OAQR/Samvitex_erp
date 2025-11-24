package com.samvitex.modelos.entidades;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "inventario_por_almacen",
        uniqueConstraints = @UniqueConstraint(columnNames = {"producto_id", "almacen_id"}))
public class InventarioPorAlmacen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "almacen_id")
    private Almacen almacen;

    @Column(nullable = false)
    private Integer cantidad;

    // Campos de auditoría
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_modificacion_id")
    private Usuario usuarioModificacion;

    @Column(name = "fecha_modificacion")
    private Instant fechaModificacion;

    public InventarioPorAlmacen() {
    }

    // Getters y Setters
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

    public Almacen getAlmacen() {
        return almacen;
    }

    public void setAlmacen(Almacen almacen) {
        this.almacen = almacen;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public Usuario getUsuarioModificacion() {
        return usuarioModificacion;
    }

    public void setUsuarioModificacion(Usuario usuarioModificacion) {
        this.usuarioModificacion = usuarioModificacion;
    }

    public Instant getFechaModificacion() {
        return fechaModificacion;
    }

    public void setFechaModificacion(Instant fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
    }
}