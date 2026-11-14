package model;

import controller.GestorMusica;

public class Cancion {
    private String rutaActual;

    public void cargarSonido(String rutaArchivo) {
        this.rutaActual = rutaArchivo;
    }

    public void reproducir() {
        if (rutaActual != null && !rutaActual.isEmpty()) {
            // Delega la reproducción al gestor global para evitar superposiciones
            GestorMusica.reproducir(rutaActual);
        }
    }

    public void detener() {
        // Detiene el audio global de forma centralizada
        GestorMusica.detener();
    }
}