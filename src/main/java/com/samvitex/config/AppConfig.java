package com.samvitex.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.annotation.PostConstruct;

/**
 * Configuración central de beans para la aplicación.
 * Define beans para la seguridad y configura el comportamiento del contexto de seguridad.
 */
@Configuration
@EnableMethodSecurity
public class AppConfig {

    /**
     * Define el bean para el codificador de contraseñas.
     * Se utiliza el algoritmo BCrypt, que es el estándar de la industria.
     *
     * @return una instancia de BCryptPasswordEncoder.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configura la estrategia del contexto de seguridad después de que el bean ha sido construido.
     * Esta configuración es CRÍTICA para aplicaciones de escritorio con hilos (como SwingWorker).
     *
     * {@link SecurityContextHolder#MODE_INHERITABLETHREADLOCAL} asegura que el contexto de
     * seguridad (la información del usuario autenticado) se propague desde el hilo principal
     * de Swing (EDT) a los hilos de fondo creados por SwingWorker. Sin esto, las
     * anotaciones @PreAuthorize fallarían en los hilos de fondo con un error
     * "An Authentication object was not found in the SecurityContext".
     */
    @PostConstruct
    public void configureSecurityContext() {
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
    }
}