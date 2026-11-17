package controller;

// Puente centralizado y seguro para todos los efectos de sonido cortos del proyecto
// (menús, botones, juego de memoria y juego de patrones).
// Delega directamente a GestorMusica para compartir el caché en memoria, respetar
// el botón de silencio global y garantizar respuesta instantánea en cada clic.
public class ReproductorSonido {

    private ReproductorSonido() {
    }

    /**
     * Reproduce el efecto de sonido desde el inicio (frame 0).
     * Si ya estaba sonando por un clic rápido anterior, lo frena, rebobina y dispara al instante.
     */
    public static void reproducirEfecto(String rutaAudio) {
        if (rutaAudio == null || rutaAudio.isEmpty()) {
            return;
        }
        GestorMusica.reproducirEfecto(rutaAudio);
    }

    /**
     * Precarga uno o varios efectos de sonido en memoria por adelantado,
     * evitando cualquier micro-retraso en el primer clic del jugador.
     */
    public static void precargar(String... rutasAudio) {
        if (rutasAudio == null) {
            return;
        }
        for (String ruta : rutasAudio) {
            if (ruta != null && !ruta.isEmpty()) {
                GestorMusica.precargarEfecto(ruta);
            }
        }
    }
}