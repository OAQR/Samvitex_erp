package com.samvitex.servicios;

import com.samvitex.modelos.dto.SearchResultDTO;
import com.samvitex.repositorios.ClienteRepositorio;
import com.samvitex.repositorios.ProductoRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Stream;

/**
 * Servicio de negocio para realizar búsquedas globales a través de múltiples entidades.
 * <p>
 * Este servicio centraliza la lógica para buscar en diferentes repositorios (Productos, Clientes, etc.)
 * y consolidar los resultados en una lista unificada de {@link SearchResultDTO}. Es utilizado
 * por la barra de búsqueda universal en la ventana principal de la aplicación.
 */
@Service
public class ServicioBusquedaUniversal {

    private final ProductoRepositorio productoRepositorio;
    private final ClienteRepositorio clienteRepositorio;

    public ServicioBusquedaUniversal(ProductoRepositorio productoRepositorio, ClienteRepositorio clienteRepositorio) {
        this.productoRepositorio = productoRepositorio;
        this.clienteRepositorio = clienteRepositorio;
    }

    /**
     * Busca productos y clientes que coincidan con el término de búsqueda y los devuelve como una lista unificada.
     * La búsqueda es insensible a mayúsculas/minúsculas y acentos.
     *
     * @param termino El texto a buscar.
     * @param limite El número máximo de resultados a devolver.
     * @return Una lista de {@link SearchResultDTO}.
     */
    @Transactional(readOnly = true)
    public List<SearchResultDTO> buscar(String termino, int limite) {
        if (termino == null || termino.isBlank()) {
            return List.of();
        }

        var resultadosProductos = productoRepositorio
                .findByNombreContainingIgnoreCaseOrSkuContainingIgnoreCase(termino)
                .stream()
                .map(p -> new SearchResultDTO(p.getId(), "PRODUCTO", p.getNombre(), "SKU: " + p.getSku()));

        var resultadosClientes = clienteRepositorio
                .findByNombreCompletoContainingIgnoreCase(termino)
                .stream()
                .map(c -> new SearchResultDTO(c.getId(), "CLIENTE", c.getNombreCompleto(), "DNI/RUC: " + c.getDniRuc()));

        return Stream.concat(resultadosProductos, resultadosClientes)
                .limit(limite)
                .toList();
    }
}