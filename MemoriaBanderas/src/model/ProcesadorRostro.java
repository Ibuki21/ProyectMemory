// Esta clase está en el paquete model, que es la parte de lógica y datos (sin ventanas).
package model;

// Rectangle guarda la esquina, el ancho y el alto de la zona donde se detectó el rostro.
import java.awt.Rectangle;
// BufferedImage es una imagen en memoria de la que se pueden leer los píxeles uno por uno.
import java.awt.image.BufferedImage;

// Convierte un rostro detectado en un cuadro de la cámara en el "vector" que se compara y se guarda:
// recorta el rostro, lo pasa a escala de grises y lo reduce a 100x100 píxeles.
// Es Java puro (sin OpenCV), así que se puede probar sin cámara.
// Clase solo con métodos estáticos: no se crean objetos de ella, se usa directo como
// ProcesadorRostro.vectorDe(...).
public final class ProcesadorRostro {

    // Constructor privado para que nadie pueda hacer new ProcesadorRostro().
    private ProcesadorRostro() {
    }

    // Devuelve los 10 000 valores de gris (0-255) del rostro, o null si el recuadro queda fuera de la imagen
    // Recibe la foto completa y el recuadro del rostro. Devuelve el rostro como una lista de 10 000 números (uno
    // por píxel).
    public static float[] vectorDe(BufferedImage imagen, Rectangle rostro) {
        // x0 y y0 son la esquina de arriba a la izquierda del recorte. Math.max evita que sean negativas.
        int x0 = Math.max(0, rostro.x);
        int y0 = Math.max(0, rostro.y);
        // x1 y y1 son la esquina de abajo a la derecha. Math.min evita pasarse del borde de la imagen.
        int x1 = Math.min(imagen.getWidth(), rostro.x + rostro.width);
        int y1 = Math.min(imagen.getHeight(), rostro.y + rostro.height);
        // Ancho y alto reales del recorte, ya ajustados a los bordes de la imagen.
        int ancho = x1 - x0;
        int alto = y1 - y0;
        // Si el recuadro quedó fuera de la imagen no hay nada que recortar.
        if (ancho <= 0 || alto <= 0) {
            // Se devuelve null para avisar que no se pudo sacar el rostro.
            return null;
        }

        // Lado final del rostro: 100 píxeles (viene de DatosFaciales).
        int lado = DatosFaciales.LADO;
        // Arreglo donde se va guardando el gris de cada píxel del resultado (100 x 100 = 10 000 posiciones).
        float[] vector = new float[DatosFaciales.TAMANO_VECTOR];
        // Cuántos píxeles del recorte original equivalen a un píxel del resultado, en horizontal.
        double escalaX = (double) ancho / lado;
        // Lo mismo pero en vertical.
        double escalaY = (double) alto / lado;

        // Reducción con interpolación bilineal (la misma que usa cv2.resize por defecto)
        // Recorre cada fila del resultado (de 0 a 99).
        for (int salidaY = 0; salidaY < lado; salidaY++) {
            // Posición en el recorte original que le toca a esta fila. El 0.5 es para tomar el centro del píxel.
            double origenY = (salidaY + 0.5) * escalaY - 0.5;
            // Fila de arriba (la parte entera de la posición).
            int arriba = (int) Math.floor(origenY);
            // Qué tan lejos está de la fila de arriba, entre 0 y 1. Sirve para mezclar las dos filas vecinas.
            double pesoY = origenY - arriba;
            // Fila de abajo: la siguiente a la de arriba, sin pasarse del recorte.
            int abajo = limitar(arriba + 1, alto);
            // Se asegura que la fila de arriba tampoco se salga del recorte.
            arriba = limitar(arriba, alto);

            // Recorre cada columna del resultado (de 0 a 99).
            for (int salidaX = 0; salidaX < lado; salidaX++) {
                // Lo mismo que origenY pero para la columna.
                double origenX = (salidaX + 0.5) * escalaX - 0.5;
                // Columna de la izquierda.
                int izquierda = (int) Math.floor(origenX);
                // Peso horizontal para mezclar las columnas vecinas.
                double pesoX = origenX - izquierda;
                // Columna de la derecha.
                int derecha = limitar(izquierda + 1, ancho);
                // Se asegura que la columna de la izquierda no se salga del recorte.
                izquierda = limitar(izquierda, ancho);

                // Mezcla el gris de los dos píxeles vecinos de la fila de arriba, según qué tan cerca está de cada
                // uno.
                double superior = (1 - pesoX) * gris(imagen, x0 + izquierda, y0 + arriba)
                        + pesoX * gris(imagen, x0 + derecha, y0 + arriba);
                // Hace lo mismo con los dos píxeles de la fila de abajo.
                double inferior = (1 - pesoX) * gris(imagen, x0 + izquierda, y0 + abajo)
                        + pesoX * gris(imagen, x0 + derecha, y0 + abajo);
                // Mezcla la fila de arriba con la de abajo y guarda el resultado en la posición fila * 100 +
                // columna.
                vector[salidaY * lado + salidaX] = (float) ((1 - pesoY) * superior + pesoY * inferior);
            }
        }
        // Se devuelve el arreglo con los 10 000 valores de gris.
        return vector;
    }

    // Mantiene el número entre 0 y tamano - 1 para no salirse del recorte.
    private static int limitar(int valor, int tamano) {
        return Math.max(0, Math.min(tamano - 1, valor));
    }

    // Gris con los mismos pesos que OpenCV (BGR a gris)
    // Convierte el color de un píxel a un solo valor de gris.
    private static double gris(BufferedImage imagen, int x, int y) {
        // getRGB devuelve el color del píxel en un solo número entero.
        int rgb = imagen.getRGB(x, y);
        // Con desplazamiento de bits y una máscara se saca la cantidad de rojo (0 a 255).
        int rojo = (rgb >> 16) & 0xFF;
        // Lo mismo para el verde.
        int verde = (rgb >> 8) & 0xFF;
        // Lo mismo para el azul.
        int azul = rgb & 0xFF;
        // Los pesos 0.299, 0.587 y 0.114 son los que usa OpenCV para pasar de color a gris, porque el ojo ve más
        // el verde.
        return 0.299 * rojo + 0.587 * verde + 0.114 * azul;
    }
}
