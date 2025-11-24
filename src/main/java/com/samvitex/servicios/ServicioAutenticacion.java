package com.samvitex.servicios;

import com.samvitex.modelos.dto.SesionUsuario;
import com.samvitex.modelos.entidades.Usuario;
import com.samvitex.repositorios.UsuarioRepositorio;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;

/**
 * Servicio de negocio responsable del proceso de autenticación de usuarios.
 * Valida las credenciales proporcionadas contra la información almacenada en la base de datos
 * y, en caso de éxito, construye un objeto de sesión seguro.
 *
 * @author OAQR
 * @version 1.0
 */
@Service
public class ServicioAutenticacion {

    private final UsuarioRepositorio usuarioRepositorio;
    private final PasswordEncoder passwordEncoder;

    /**
     * Construye una nueva instancia del servicio de autenticación.
     *
     * @param usuarioRepositorio Repositorio para acceder a los datos de los usuarios.
     * @param passwordEncoder Codificador para comparar de forma segura las contraseñas.
     */
    public ServicioAutenticacion(UsuarioRepositorio usuarioRepositorio, PasswordEncoder passwordEncoder) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Autentica a un usuario y, si tiene éxito, establece su contexto de seguridad para toda la aplicación.     * Este es el paso clave para que las anotaciones @PreAuthorize funcionen.
     * Este es el paso clave para que las anotaciones @PreAuthorize funcionen.
     *
     * @param nombreUsuario El nombre de usuario proporcionado.
     * @param password La contraseña en formato de array de caracteres para mayor seguridad en memoria.
     * @return Un {@link Optional} que contiene un DTO {@link SesionUsuario} si la autenticación es exitosa,
     *         o un Optional vacío si las credenciales son incorrectas, el usuario no existe o está inactivo.
     */
    @Transactional(readOnly = true)
    public Optional<SesionUsuario> autenticarYEstablecerSesion(String nombreUsuario, char[] password) {

        Optional<Usuario> usuarioOpt = usuarioRepositorio.findByNombreUsuario(nombreUsuario);

        if (usuarioOpt.isEmpty() || !usuarioOpt.get().isActivo()) {
            return Optional.empty();
        }

        Usuario usuario = usuarioOpt.get();

        if (passwordEncoder.matches(new String(password), usuario.getPasswordHash())) {
            SimpleGrantedAuthority authority = new SimpleGrantedAuthority
                    ("ROLE_" + usuario.getRol().getNombre());

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    usuario.getNombreUsuario(),
                    null,
                    Collections.singletonList(authority)
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            SesionUsuario sesion = new SesionUsuario(
                    usuario.getNombreUsuario(),
                    usuario.getNombreCompleto(),
                    usuario.getRol().getNombre()
            );
            return Optional.of(sesion);

        }

        return Optional.empty();
    }
}