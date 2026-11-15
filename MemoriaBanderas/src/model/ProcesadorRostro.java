package model;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

// Convierte un rostro detectado en un cuadro de la cámara en el "vector" que se compara y se guarda:
// recorta el rostro, lo pasa a escala de grises y lo reduce a 100x100 píxeles.
// Es Java puro (sin OpenCV), así que se puede probar sin cámara.
public final class ProcesadorRostro {

    private ProcesadorRostro() {
    }

    // Devuelve los 10 000 valores de gris (0-255) del rostro, o null si el recuadro queda fuera de la imagen
    public static float[] vectorDe(BufferedImage imagen, Rectangle rostro) {
        int x0 = Math.max(0, rostro.x);
        int y0 = Math.max(0, rostro.y);
        int x1 = Math.min(imagen.getWidth(), rostro.x + rostro.width);
        int y1 = Math.min(imagen.getHeight(), rostro.y + rostro.height);
        int ancho = x1 - x0;
        int alto = y1 - y0;
        if (ancho <= 0 || alto <= 0) {
            return null;
        }

        int lado = DatosFaciales.LADO;
        float[] vector = new float[DatosFaciales.TAMANO_VECTOR];
        double escalaX = (double) ancho / lado;
        double escalaY = (double) alto / lado;

        // Reducción con interpolación bilineal (la misma que usa cv2.resize por defecto)
        for (int salidaY = 0; salidaY < lado; salidaY++) {
            double origenY = (salidaY + 0.5) * escalaY - 0.5;
            int arriba = (int) Math.floor(origenY);
            double pesoY = origenY - arriba;
            int abajo = limitar(arriba + 1, alto);
            arriba = limitar(arriba, alto);

            for (int salidaX = 0; salidaX < lado; salidaX++) {
                double origenX = (salidaX + 0.5) * escalaX - 0.5;
                int izquierda = (int) Math.floor(origenX);
                double pesoX = origenX - izquierda;
                int derecha = limitar(izquierda + 1, ancho);
                izquierda = limitar(izquierda, ancho);

                double superior = (1 - pesoX) * gris(imagen, x0 + izquierda, y0 + arriba)
                        + pesoX * gris(imagen, x0 + derecha, y0 + arriba);
                double inferior = (1 - pesoX) * gris(imagen, x0 + izquierda, y0 + abajo)
                        + pesoX * gris(imagen, x0 + derecha, y0 + abajo);
                vector[salidaY * lado + salidaX] = (float) ((1 - pesoY) * superior + pesoY * inferior);
            }
        }
        return vector;
    }

    private static int limitar(int valor, int tamano) {
        return Math.max(0, Math.min(tamano - 1, valor));
    }

    // Gris con los mismos pesos que OpenCV (BGR a gris)
    private static double gris(BufferedImage imagen, int x, int y) {
        int rgb = imagen.getRGB(x, y);
        int rojo = (rgb >> 16) & 0xFF;
        int verde = (rgb >> 8) & 0xFF;
        int azul = rgb & 0xFF;
        return 0.299 * rojo + 0.587 * verde + 0.114 * azul;
    }
}
