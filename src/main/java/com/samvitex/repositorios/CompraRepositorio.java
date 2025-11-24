package com.samvitex.repositorios;

import com.samvitex.modelos.entidades.Compra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio de Spring Data JPA para la entidad {@link Compra}.
 *
 * <p>Esta interfaz hereda de {@link JpaRepository}, lo que le proporciona un conjunto
 * completo de métodos CRUD (Crear, Leer, Actualizar, Eliminar) para la entidad {@code Compra}
 * sin necesidad de implementación explícita. Spring Data JPA genera automáticamente
 * las consultas necesarias en tiempo de ejecución.</p>
 *
 * <p>Métodos principales heredados:</p>
 * <ul>
 *   <li>{@code save(Compra entidad)}: Guarda o actualiza una compra.</li>
 *   <li>{@code findById(Long id)}: Busca una compra por su identificador único.</li>
 *   <li>{@code findAll()}: Devuelve todas las compras registradas.</li>
 *   <li>{@code deleteById(Long id)}: Elimina una compra (no recomendado para transacciones).</li>
 * </ul>
 *
 * <p>En el futuro, se podrían añadir aquí consultas personalizadas (JPQL) para
 * generar reportes específicos de compras, como "total comprado a un proveedor en un
 * período de tiempo".</p>
 */
@Repository
public interface CompraRepositorio extends JpaRepository<Compra, Long> {
    // Por el momento, no se requieren métodos de consulta personalizados.
    // Los métodos CRUD básicos proporcionados por JpaRepository son suficientes.
}