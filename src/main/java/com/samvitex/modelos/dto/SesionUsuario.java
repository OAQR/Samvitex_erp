package com.samvitex.modelos.dto;

/**
 * Data Transfer Object (DTO) que encapsula la información esencial y no sensible
 * del usuario que ha iniciado sesión.
 *
 * <p>Este objeto se crea después de una autenticación exitosa y se utiliza para
 * mantener el estado de la sesión en la capa de presentación (UI). Al ser un {@code record},
 * es inmutable por diseño, lo que garantiza que sus datos no puedan ser alterados
 * accidentalmente después de su creación, aportando seguridad y predictibilidad.</p>
 *
 * <p><b>Propósito:</b></p>
 * <ul>
 *   <li>Transportar de forma segura los datos del usuario entre la capa de servicio y la UI.</li>
 *   <li>Evitar exponer la entidad JPA {@link com.samvitex.modelos.entidades.Usuario} completa,
 *       que contiene información sensible como el hash de la contraseña, a la capa de presentación.</li>
 *   <li>Facilitar la personalización de la UI (ej. mostrar el nombre del usuario) y la
 *       lógica de autorización a nivel de vista (ej. mostrar/ocultar botones según el rol).</li>
 * </ul>
 *
 * @param nombreUsuario El nombre de usuario único utilizado para el login.
 * @param nombreCompleto El nombre completo del usuario, ideal para ser mostrado en la interfaz.
 * @param rol El nombre del rol del usuario (ej. "ADMINISTRADOR"), para controlar el acceso a funcionalidades.
 */
public record SesionUsuario(
        String nombreUsuario,
        String nombreCompleto,
        String rol
) {
}