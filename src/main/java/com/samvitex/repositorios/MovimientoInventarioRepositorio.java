package com.samvitex.repositorios;

import com.samvitex.modelos.entidades.MovimientoInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio de Spring Data JPA para la entidad {@link MovimientoInventario}.
 *
 * <p>Este repositorio es fundamental para la auditoría y trazabilidad del inventario.
 * Proporciona métodos para consultar el historial de movimientos de stock (Kardex)
 * de los productos.</p>
 */
@Repository
public interface MovimientoInventarioRepositorio extends JpaRepository<MovimientoInventario, Long> {

    /**
     * Busca todos los movimientos de un producto específico dentro de un rango de fechas,
     * ordenados cronológicamente desde el más antiguo al más reciente.
     * <p>Esta consulta es la base para construir el reporte de Kardex de un producto.
     * Utiliza 'JOIN FETCH' para cargar las entidades relacionadas (Usuario) de forma
     * proactiva y evitar problemas de rendimiento y carga perezosa.</p>
     *
     * @param productoId El ID del producto para el cual se busca el historial.
     * @param fechaInicio El inicio del período de búsqueda (inclusivo).
     * @param fechaFin El fin del período de búsqueda (inclusivo).
     * @return Una lista ordenada de {@link MovimientoInventario} que representa el Kardex del producto en ese período.
     */
    @Query("SELECT m FROM MovimientoInventario m JOIN FETCH m.usuario WHERE m.producto.id = :productoId AND m.fechaMovimiento BETWEEN :fechaInicio AND :fechaFin ORDER BY m.fechaMovimiento ASC, m.id ASC")
    List<MovimientoInventario> findKardexPorProductoYPeriodo(
            @Param("productoId") Integer productoId,
            @Param("fechaInicio") Instant fechaInicio,
            @Param("fechaFin") Instant fechaFin
    );

    /**
     * Obtiene el último movimiento registrado para un producto específico antes de una fecha y hora determinadas.
     * <p>Esta consulta es crucial para calcular el "saldo anterior" al generar un reporte de Kardex.
     * Permite conocer cuál era el stock final del producto justo antes de que comenzara el período del reporte.</p>
     *
     * @param productoId El ID del producto.
     * @param fechaInicio La fecha y hora que actúa como límite superior (exclusivo) para la búsqueda.
     * @return Un {@link Optional} que contiene el último {@link MovimientoInventario} si se encuentra, o estará vacío
     *         si no existen movimientos para ese producto antes de la fecha especificada.
     */
    @Query("SELECT m FROM MovimientoInventario m WHERE m.producto.id = :productoId AND m.fechaMovimiento < :fechaInicio ORDER BY m.fechaMovimiento DESC, m.id DESC LIMIT 1")
    Optional<MovimientoInventario> findUltimoMovimientoAntesDeFecha(
            @Param("productoId") Integer productoId,
            @Param("fechaInicio") Instant fechaInicio
    );
}