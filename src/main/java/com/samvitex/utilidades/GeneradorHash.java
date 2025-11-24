package com.samvitex.utilidades;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Clase de utilidad temporal para generar hashes BCrypt.
 * La ejecutaremos una vez para obtener el hash de la contraseña 'admin'
 * y luego podemos eliminarla o comentarla.
 */
public class GeneradorHash {

    public static void main(String[] args) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String passwordPlana = "vendedor123";
        String passwordHasheada = passwordEncoder.encode(passwordPlana);

        System.out.println("====================================================================");
        System.out.println("Utilidad para Generar Hash BCrypt");
        System.out.println("Contraseña en texto plano: " + passwordPlana);
        System.out.println("Hash BCrypt generado: " + passwordHasheada);
        System.out.println("====================================================================");
        System.out.println("COPIA Y PEGA EL HASH GENERADO EN TU SCRIPT V5__...SQL");
    }
}