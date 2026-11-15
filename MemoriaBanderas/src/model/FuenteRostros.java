package model;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;

// Origen de imágenes con rostros, normalmente la cámara web.
// El controlador solo conoce esta interfaz; la única clase que usa OpenCV es FuenteRostrosJavaCV.
public interface FuenteRostros extends AutoCloseable {

    // Un cuadro de la cámara con el rostro que se detectó en él (si hay alguno)
    final class Cuadro {
        public final BufferedImage imagen;   // Imagen completa, lista para mostrar
        public final Rectangle rostro;       // Zona del rostro, o null si no se detectó ninguno
        public final float[] cara;           // Rostro en gris de 100x100 (ver ProcesadorRostro), o null

        public Cuadro(BufferedImage imagen, Rectangle rostro, float[] cara) {
            this.imagen = imagen;
            this.rostro = rostro;
            this.cara = cara;
        }
    }

    // Siguiente cuadro de la cámara, o null si la cámara dejó de entregar imágenes
    Cuadro leer() throws IOException;

    @Override
    void close();
}
