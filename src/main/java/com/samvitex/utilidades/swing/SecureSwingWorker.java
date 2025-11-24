package com.samvitex.utilidades.swing;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import javax.swing.SwingWorker;

/**
 * Un SwingWorker personalizado que propaga el contexto de seguridad de Spring
 * al hilo de fondo, asegurando que las llamadas a servicios protegidos con
 * @PreAuthorize funcionen correctamente.
 *
 * @param <T> el tipo de resultado del método doInBackground
 * @param <V> el tipo de resultado del método publish
 */
public abstract class SecureSwingWorker<T, V> extends SwingWorker<T, V> {

    private final Authentication authentication;

    protected SecureSwingWorker() {
        // Captura el contexto de seguridad del hilo que crea esta tarea (el EDT).
        this.authentication = SecurityContextHolder.getContext().getAuthentication();
    }

    @Override
    protected final T doInBackground() throws Exception {
        // Antes de ejecutar la tarea real, establece el contexto de seguridad en el hilo de fondo.
        SecurityContextHolder.getContext().setAuthentication(authentication);
        try {
            // Llama al método abstracto que las subclases implementarán.
            return doInBackgroundSecure();
        } finally {
            // Limpia el contexto de seguridad del hilo de fondo para evitar fugas.
            SecurityContextHolder.clearContext();
        }
    }

    /**
     * Reemplaza el método doInBackground() estándar. Las subclases deben implementar
     * este método para su lógica de fondo. El contexto de seguridad ya estará establecido.
     * @return el resultado de la computación de fondo.
     * @throws Exception si ocurre un error durante la computación.
     */
    protected abstract T doInBackgroundSecure() throws Exception;
}