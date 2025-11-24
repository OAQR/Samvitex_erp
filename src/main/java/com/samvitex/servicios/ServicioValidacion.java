package com.samvitex.servicios;

/**
 * `ServicioValidacion` contiene la lógica de negocio para validar diferentes
 * tipos de datos de la aplicación, como correos electrónicos, DNI, contraseñas, etc.
 * Los métodos lanzan excepciones si la validación falla.
 */
public final class ServicioValidacion {

    private ServicioValidacion() {
    }

    /**
     * Valida que un texto no contenga números.
     *
     * @param texto El texto a validar.
     * @param nombreCampo El nombre del campo para el mensaje de error.
     * @throws IllegalArgumentException si el texto contiene números.
     */
    public static void validarSoloLetras(String texto, String nombreCampo) {
        for (char c : texto.toCharArray()) {
            if (Character.isDigit(c)) {
                throw new IllegalArgumentException("El campo '" + nombreCampo + "' no puede contener números.");
            }
        }
    }

    /**
     * Valida que un texto no contenga letras.
     *
     * @param texto El texto a validar.
     * @param nombreCampo El nombre del campo para el mensaje de error.
     * @throws IllegalArgumentException si el texto contiene letras.
     */
    public static void validarSoloNumeros(String texto, String nombreCampo) {
        for (char c : texto.toCharArray()) {
            if (Character.isAlphabetic(c)) {
                throw new IllegalArgumentException("El campo '" + nombreCampo + "' no puede contener letras.");
            }
        }
    }

    /**
     * Valida la longitud mínima de un texto.
     *
     * @param texto El texto a validar.
     * @param longitudMinima La longitud mínima requerida.
     * @param nombreCampo El nombre del campo para el mensaje de error.
     * @throws IllegalArgumentException si el texto es más corto que la longitud mínima.
     */
    public static void validarLongitudMinima(String texto, int longitudMinima, String nombreCampo) {
        if (texto.length() < longitudMinima) {
            throw new IllegalArgumentException("El campo '" + nombreCampo + "' debe tener al menos " + longitudMinima + " caracteres.");
        }
    }

    /**
     * Valida el formato de una dirección de correo electrónico.
     *
     * @param email El correo electrónico a validar.
     * @throws IllegalArgumentException si el formato del correo no es válido.
     */
    public static void validarEmail(String email) {
        // Expresión regular más robusta que la original
        String regex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";
        if (!email.matches(regex)) {
            throw new IllegalArgumentException("El formato del correo electrónico es inválido.");
        }
    }

    /**
     * Valida que un valor numérico sea mayor o igual a un mínimo.
     *
     * @param valor El valor a validar.
     * @param valorMinimo El valor mínimo permitido.
     * @param nombreCampo El nombre del campo para el mensaje de error.
     * @throws IllegalArgumentException si el valor es menor al mínimo.
     */
    public static void validarValorMinimo(int valor, int valorMinimo, String nombreCampo) {
        if (valor < valorMinimo) {
            throw new IllegalArgumentException("El campo '" + nombreCampo + "' no puede ser menor a " + valorMinimo + ".");
        }
    }
}