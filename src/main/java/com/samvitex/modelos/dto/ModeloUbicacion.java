package com.samvitex.modelos.dto;

/**
 * Clase de modelo para representar una ubicación o destino con un título,
 * una descripción y una ruta de video asociada.
 */
public class ModeloUbicacion {

    private String titulo;
    private String descripcion;
    private String rutaVideo;

    /**
     * Constructor para crear una nueva instancia de ModeloUbicacion.
     *
     * @param titulo      El título de la ubicación.
     * @param descripcion Una descripción detallada de la ubicación.
     * @param rutaVideo   La ruta al archivo de video asociado a esta ubicación.
     */
    public ModeloUbicacion(String titulo, String descripcion, String rutaVideo) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.rutaVideo = rutaVideo;
    }

    /**
     * Obtiene el título de la ubicación.
     *
     * @return El título de la ubicación.
     */
    public String getTitulo() {
        return titulo;
    }

    /**
     * Establece el título de la ubicación.
     *
     * @param titulo El nuevo título para la ubicación.
     */
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    /**
     * Obtiene la descripción de la ubicación.
     *
     * @return La descripción de la ubicación.
     */
    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Establece la descripción de la ubicación.
     *
     * @param descripcion La nueva descripción para la ubicación.
     */
    public void setDescription(String descripcion) {
        this.descripcion = descripcion;
    }

    /**
     * Obtiene la ruta del archivo de video asociado a la ubicación.
     *
     * @return La ruta del video.
     */
    public String getRutaVideo() {
        return rutaVideo;
    }

    /**
     * Establece la ruta del archivo de video asociado a la ubicación.
     *
     * @param rutaVideo La nueva ruta del video.
     */
    public void setRutaVideo(String rutaVideo) {
        this.rutaVideo = rutaVideo;
    }
}