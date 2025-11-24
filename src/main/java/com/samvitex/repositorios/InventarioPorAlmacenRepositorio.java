package com.samvitex.repositorios;

import com.samvitex.modelos.entidades.InventarioPorAlmacen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventarioPorAlmacenRepositorio extends JpaRepository<InventarioPorAlmacen, Long> {

    /**
     * Busca un registro de inventario específico por producto y almacén.
     * Esta será la consulta más utilizada para verificar y actualizar el stock.
     */
    Optional<InventarioPorAlmacen> findByProductoIdAndAlmacenId(Integer productoId, Integer almacenId);

    /**
     * Calcula la suma total del stock de un producto en todos los almacenes.
     * Útil para vistas generales de inventario.
     */
    @Query("SELECT SUM(i.cantidad) FROM InventarioPorAlmacen i WHERE i.producto.id = :productoId")
    Optional<Long> findStockTotalByProductoId(@Param("productoId") Integer productoId);

    @Query("SELECT ipa FROM InventarioPorAlmacen ipa JOIN FETCH ipa.almacen WHERE ipa.producto.id = :productoId ORDER BY ipa.almacen.nombre")
    List<InventarioPorAlmacen> findByProductoIdWithAlmacen(@Param("productoId") Integer productoId);


    /**
     * Verifica si existen registros de inventario para un almacén específico
     * donde la cantidad sea mayor a cero.
     * Es la forma más eficiente de saber si un almacén tiene stock.
     *
     * @param almacenId El ID del almacén a verificar.
     * @return true si el almacén tiene stock de al menos un producto, false en caso contrario.
     */
    boolean existsByAlmacenIdAndCantidadGreaterThan(Integer almacenId, int cantidad);

}