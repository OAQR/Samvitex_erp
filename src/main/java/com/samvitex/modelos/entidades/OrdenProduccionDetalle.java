package com.samvitex.modelos.entidades;

import com.samvitex.modelos.enums.TipoDetalleProduccion;
import jakarta.persistence.*;

/**
 * Representa una línea de detalle dentro de una {@link OrdenProduccion}.
 *
 * <p>Esta entidad es polimórfica en su propósito, distinguido por el campo {@code tipoDetalle}.
 * Puede representar tanto una materia prima que será consumida ({@code TipoDetalleProduccion.INSUMO})
 * como un producto terminado que será generado ({@code TipoDetalleProduccion.PRODUCTO_FINAL}).
 * Esta flexibilidad permite modelar el "antes y después" de la producción en una única
 * estructura de detalles.</p>
 *
 * Mapea a la tabla 'ordenes_produccion_detalle'.
 */
@Entity
@Table(name = "ordenes_produccion_detalle")
public class OrdenProduccionDetalle {

    /**
     * Identificador único de la línea de detalle.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * La orden de producción maestra a la que pertenece este detalle.
     * Es la propietaria de la relación bidireccional.
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "orden_produccion_id")
    private OrdenProduccion ordenProduccion;

    /**
     * El producto al que se refiere esta línea, ya sea como insumo o como producto final.
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    /**
     * Indica si esta línea representa un INSUMO a consumir o un PRODUCTO_FINAL a generar.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_detalle", nullable = false)
    private TipoDetalleProduccion tipoDetalle;

    /**
     * La cantidad de unidades del producto. Si es un insumo, es la cantidad a consumir.
     * Si es un producto final, es la cantidad a fabricar.
     */
    @Column(nullable = false)
    private Integer cantidad;

    /**
     * Constructor por defecto requerido por JPA.
     */
    public OrdenProduccionDetalle() {}

    // --- Getters y Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public OrdenProduccion getOrdenProduccion() {
        return ordenProduccion;
    }

    public void setOrdenProduccion(OrdenProduccion ordenProduccion) {
        this.ordenProduccion = ordenProduccion;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public TipoDetalleProduccion getTipoDetalle() {
        return tipoDetalle;
    }

    public void setTipoDetalle(TipoDetalleProduccion tipoDetalle) {
        this.tipoDetalle = tipoDetalle;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }
}