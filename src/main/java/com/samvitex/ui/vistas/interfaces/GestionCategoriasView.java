package com.samvitex.ui.vistas.interfaces;

import com.samvitex.modelos.entidades.Categoria;

import java.util.List;

/**
 * Define el contrato para la vista de gestión de categorías.
 * Cualquier clase que implemente esta interfaz actuará como la "Vista" en el patrón MVP
 * para la entidad {@link Categoria}.
 */
public interface GestionCategoriasView {

    /**
     * Muestra una lista de categorías en el componente visual principal.
     *
     * @param categorias La lista de entidades {@link Categoria} a mostrar.
     */
    void mostrarCategorias(List<Categoria> categorias);

    /**
     * Muestra un mensaje de error al usuario.
     *
     * @param mensaje El texto del error a mostrar.
     */
    void mostrarError(String mensaje);

    /**
     * Muestra un mensaje de éxito/informativo al usuario.
     *
     * @param mensaje El texto del mensaje a mostrar.
     */
    void mostrarMensajeExito(String mensaje);

    /**
     * Abre el diálogo para crear o editar una categoría.
     *
     * @param categoria La categoría a editar. Si es {@code null}, el diálogo se abrirá en modo de creación.
     */
    void mostrarDialogoCategoria(Categoria categoria);

    /**
     * Solicita a la vista que refresque sus datos, volviendo a cargar la lista de categorías.
     */
    void refrescarVista();

    /**
     * Muestra un diálogo de confirmación al usuario antes de una acción destructiva.
     *
     * @param mensaje La pregunta de confirmación a mostrar.
     * @param titulo El título de la ventana de confirmación.
     * @return {@code true} si el usuario confirma la acción, {@code false} en caso contrario.
     */
    boolean confirmarAccion(String mensaje, String titulo);
}