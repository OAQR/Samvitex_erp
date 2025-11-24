package com.samvitex.modelos.entidades;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * Representa un artículo físico en el inventario de la empresa.
 *
 * <p>Esta es una de las entidades más importantes del sistema, ya que centraliza toda
 * la información sobre un producto, incluyendo su identificación, descripción, costos,
 * precios, niveles de stock y sus relaciones con catálogos como {@link Categoria},
 * {@link Proveedor} y {@link Almacen}.</p>
 *
 * <p><b>Características Clave:</b></p>
 * <ul>
 *   <li><b>Identificación Única:</b> Utiliza un campo {@code sku} (Stock Keeping Unit) como
 *       identificador de negocio único, además del ID técnico de la base de datos.</li>
 *   <li><b>Gestión de Stock:</b> Contiene los campos {@code cantidad} para el stock actual y
 *       {@code stockMinimo} para alertas y gestión de reabastecimiento.</li>
 *   <li><b>Costos y Precios:</b> Almacena tanto el {@code precioCosto} como el {@code precioVenta},
 *       permitiendo el cálculo de márgenes de ganancia.</li>
 *   <li><b>Borrado Lógico:</b> Incluye un campo {@code activo} para "desactivar" productos
 *       en lugar de eliminarlos, preservando la integridad de las transacciones históricas.</li>
 *   <li><b>Relaciones LAZY:</b> Todas las relaciones {@code @ManyToOne} están configuradas con
 *       {@code FetchType.LAZY} para optimizar el rendimiento y evitar la carga innecesaria
 *       de datos relacionados.</li>
 * </ul>
 *
 * Mapea a la tabla 'productos' en la base de datos.
 */
@Entity
@Table(name = "productos")
public class Producto {

    /**
     * Identificador único del producto en la base de datos (clave primaria técnica).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * Stock Keeping Unit (SKU). Identificador de negocio único para el producto.
     * Es alfanumérico y definido por la empresa.
     */
    @Column(unique = true, nullable = false, length = 50)
    private String sku;

    /**
     * Nombre comercial o descriptivo del producto.
     */
    @Column(nullable = false)
    private String nombre;

    /**
     * Descripción detallada del producto, incluyendo características técnicas, composición, etc.
     */
    @Column(columnDefinition = "TEXT")
    private String descripcion;

    /**
     * El costo de adquisición del producto. Este valor puede ser actualizado por
     * el último costo de una transacción de compra.
     */
    @Column(name = "precio_costo", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioCosto;

    /**
     * El precio al que el producto se vende al cliente.
     */
    @Column(name = "precio_venta", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioVenta;

    /**
     * El nivel de stock por debajo del cual el producto se considera en "stock bajo".
     * Utilizado para generar alertas y reportes de reabastecimiento.
     */
    @Column(name = "stock_minimo", nullable = false)
    private Integer stockMinimo;

    /**
     * Indicador de borrado lógico. Si es {@code false}, el producto no aparecerá en
     * búsquedas para ventas y se considera descatalogado.
     */
    @Column(nullable = false)
    private boolean activo = true;

    /**
     * La categoría a la que pertenece este producto.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    /**
     * El proveedor principal asociado a este producto.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proveedor_id")
    private Proveedor proveedor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_creacion_id")
    private Usuario usuarioCreacion;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private Instant fechaCreacion = Instant.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_modificacion_id")
    private Usuario usuarioModificacion;

    @Column(name = "fecha_modificacion")
    private Instant fechaModificacion;

    /**
     * Constructor por defecto requerido por JPA.
     */
    public Producto() {}

    // --- Getters y Setters ---


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPrecioCosto() {
        return precioCosto;
    }

    public void setPrecioCosto(BigDecimal precioCosto) {
        this.precioCosto = precioCosto;
    }

    public BigDecimal getPrecioVenta() {
        return precioVenta;
    }

    public void setPrecioVenta(BigDecimal precioVenta) {
        this.precioVenta = precioVenta;
    }

    public Integer getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(Integer stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public Proveedor getProveedor() {
        return proveedor;
    }

    public void setProveedor(Proveedor proveedor) {
        this.proveedor = proveedor;
    }

    public Usuario getUsuarioCreacion() {
        return usuarioCreacion;
    }

    public void setUsuarioCreacion(Usuario usuarioCreacion) {
        this.usuarioCreacion = usuarioCreacion;
    }

    public Instant getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Instant fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
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

    /**
     * Representación en formato de cadena del objeto Producto.
     * Es útil para componentes de UI como {@link javax.swing.JList} donde se necesita
     * mostrar una representación textual del objeto.
     *
     * @return El nombre del producto.
     */
    @Override
    public String toString() {
        return nombre;
    }

    /**
     * Compara este producto con otro objeto para determinar si son iguales.
     * Dos productos se consideran iguales si tienen el mismo ID.
     * Esto es fundamental para el correcto funcionamiento de colecciones y
     * la selección de ítems en componentes de UI como {@link javax.swing.JComboBox}.
     *
     * @param o el objeto a comparar.
     * @return {@code true} si los objetos son el mismo producto, {@code false} de lo contrario.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Producto producto = (Producto) o;
        // Solo se compara por ID, asumiendo que si el ID no es nulo, es único.
        return id != null && Objects.equals(id, producto.id);
    }

    /**
     * Devuelve un código hash para el objeto.
     * Es consistente con el método {@code equals}. Si dos productos son iguales,
     * deben tener el mismo código hash.
     *
     * @return el código hash del producto, basado en su ID.
     */
    @Override
    public int hashCode() {
        // Se usa la clase Objects para manejar de forma segura el caso en que el ID es nulo.
        return Objects.hash(id);
    }
}