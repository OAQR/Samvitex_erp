package com.samvitex.servicios;

import com.samvitex.modelos.entidades.Usuario;
import com.samvitex.repositorios.UsuarioRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio de negocio para la gestión de usuarios.
 * Encapsula toda la lógica relacionada con el CRUD de usuarios,
 * asegurando la integridad de los datos y las reglas de negocio.
 * La seguridad a nivel de metodo se aplica aquí usando anotaciones @PreAuthorize.
 */
@Service
public class ServicioUsuario {

    private final UsuarioRepositorio usuarioRepositorio;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public ServicioUsuario(UsuarioRepositorio usuarioRepositorio, PasswordEncoder passwordEncoder) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Obtiene todos los usuarios del sistema.
     * Requiere que el usuario autenticado tenga el rol 'ADMINISTRADOR'.
     *
     * @return Una lista de todas las entidades Usuario.
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public List<Usuario> obtenerTodosLosUsuarios() {
        return usuarioRepositorio.findAll();
    }

    /**
     * Crea un nuevo usuario.
     * Requiere rol 'ADMINISTRADOR'.
     *
     * @param nuevoUsuario El objeto Usuario a crear.
     * @param passwordEnTextoPlano La contraseña inicial.
     * @return El usuario guardado.
     */
    @Transactional
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public Usuario crearUsuario(Usuario nuevoUsuario, String passwordEnTextoPlano) {
        if (usuarioRepositorio.existsByNombreUsuario(nuevoUsuario.getNombreUsuario())) {
            throw new IllegalArgumentException("El nombre de usuario ya está en uso.");
        }
        if (usuarioRepositorio.existsByEmail(nuevoUsuario.getEmail())) {
            throw new IllegalArgumentException("El correo electrónico ya está en uso.");
        }
        nuevoUsuario.setPasswordHash(passwordEncoder.encode(passwordEnTextoPlano));
        return usuarioRepositorio.save(nuevoUsuario);
    }

    /**
     * Busca un usuario por ID, asegurando que su rol sea cargado
     * para ser usado fuera de un contexto transaccional (ej. en la UI).
     *
     * @param id El ID del usuario.
     * @return un Optional conteniendo al Usuario si se encuentra.
     */
    @Transactional(readOnly = true)
    public Optional<Usuario> findByIdForEditing(Integer id) {
        return usuarioRepositorio.findByIdWithRol(id);
    }

    /**
     * Alterna el estado de un usuario (activo/inactivo).
     * Requiere rol 'ADMINISTRADOR'.
     *
     * @param idUsuario El ID del usuario a modificar.
     */
    @Transactional
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public void toggleEstadoUsuario(Integer idUsuario) {
        Usuario usuario = usuarioRepositorio.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + idUsuario));
        usuario.setActivo(!usuario.isActivo());
        usuarioRepositorio.save(usuario);
    }

    /**
     * Actualiza un usuario existente.
     * Requiere rol 'ADMINISTRADOR'.
     *
     * @param usuarioActualizado El objeto Usuario con los nuevos datos.
     * @param nuevaPassword La nueva contraseña (se ignora si es nula o vacía).
     * @return El usuario actualizado.
     */
    @Transactional
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public Usuario actualizarUsuario(Usuario usuarioActualizado, String nuevaPassword) {
        Usuario usuarioExistente = usuarioRepositorio.findById(usuarioActualizado.getId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        usuarioExistente.setNombreCompleto(usuarioActualizado.getNombreCompleto());
        usuarioExistente.setEmail(usuarioActualizado.getEmail());
        usuarioExistente.setRol(usuarioActualizado.getRol());
        usuarioExistente.setActivo(usuarioActualizado.isActivo());

        if (nuevaPassword != null && !nuevaPassword.isBlank()) {
            usuarioExistente.setPasswordHash(passwordEncoder.encode(nuevaPassword));
        }
        return usuarioRepositorio.save(usuarioExistente);
    }

    /**
     * Busca un usuario por su ID.
     *
     * @param id El ID del usuario.
     * @return un Optional conteniendo al Usuario si se encuentra.
     */
    @Transactional(readOnly = true)
    public Optional<Usuario> findById(Integer id) {
        return usuarioRepositorio.findById(id);
    }
}