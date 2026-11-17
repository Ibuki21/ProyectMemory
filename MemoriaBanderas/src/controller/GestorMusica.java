package controller;

import javax.sound.sampled.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class GestorMusica {

    private static Clip clipActual;
    private static String rutaActual = "";
    private static boolean musicaSilenciada = false;

    // 🔊 Caché en memoria para efectos de sonido cortos (botones, aciertos, errores)
    // Evita saturar las tarjetas de sonido y garantiza respuesta instantánea en cada clic.
    private static final Map<String, Clip> cacheEfectos = new HashMap<>();

    // Reproduce una pista en bucle continuo y fuerza el cambio de audio
    public static void reproducir(String rutaAudio) {
        if (musicaSilenciada) return;

        detener();

        try {
            URL urlAudio = GestorMusica.class.getClassLoader().getResource(rutaAudio);
            if (urlAudio != null) {
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(urlAudio);
                clipActual = AudioSystem.getClip();
                clipActual.open(audioStream);
                clipActual.loop(Clip.LOOP_CONTINUOUSLY);
                clipActual.start();
                rutaActual = rutaAudio;
            } else {
                System.err.println("No se encontró el archivo de música: " + rutaAudio);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ⚡ MÉTODO INFALIBLE PARA BOTONES Y EFECTOS CORTOS
     * No detiene la música de fondo, no satura líneas de audio y rebobina al instante
     * si el jugador hace clics muy rápidos.
     */
    public static void reproducirEfecto(String rutaEfecto) {
        if (musicaSilenciada) return;

        try {
            Clip clipEfecto = cacheEfectos.get(rutaEfecto);

            // Si es la primera vez que suena este efecto, lo cargamos en memoria
            if (clipEfecto == null || !clipEfecto.isOpen()) {
                URL urlEfecto = GestorMusica.class.getClassLoader().getResource(rutaEfecto);
                if (urlEfecto != null) {
                    AudioInputStream audioStream = AudioSystem.getAudioInputStream(urlEfecto);
                    clipEfecto = AudioSystem.getClip();
                    clipEfecto.open(audioStream);
                    cacheEfectos.put(rutaEfecto, clipEfecto);
                } else {
                    System.err.println("No se encontró el efecto de sonido: " + rutaEfecto);
                    return;
                }
            }

            // 🔥 CLAVE INFALIBLE: Si el sonido anterior aún estaba terminando, lo frenamos
            if (clipEfecto.isRunning()) {
                clipEfecto.stop();
            }

            // Rebobinamos exactamente al inicio (frame 0) y disparamos
            clipEfecto.setFramePosition(0);
            clipEfecto.start();

        } catch (Exception e) {
            // Si el dispositivo de audio falla momentáneamente, limpiamos esa entrada para recargarla
            cacheEfectos.remove(rutaEfecto);
            e.printStackTrace();
        }
    }

    /**
     * Precarga un efecto en memoria sin reproducirlo todavía (ideal al abrir una ventana
     * para evitar cualquier micro-retraso en el primer clic).
     */
    public static void precargarEfecto(String rutaEfecto) {
        try {
            Clip existente = cacheEfectos.get(rutaEfecto);
            if (existente == null || !existente.isOpen()) {
                URL urlEfecto = GestorMusica.class.getClassLoader().getResource(rutaEfecto);
                if (urlEfecto != null) {
                    AudioInputStream audioStream = AudioSystem.getAudioInputStream(urlEfecto);
                    Clip clipEfecto = AudioSystem.getClip();
                    clipEfecto.open(audioStream);
                    cacheEfectos.put(rutaEfecto, clipEfecto);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void detener() {
        try {
            if (clipActual != null) {
                if (clipActual.isRunning()) {
                    clipActual.stop();
                }
                clipActual.flush();
                clipActual.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            clipActual = null;
            rutaActual = "";
        }
    }

    // Libera también los efectos de memoria al cerrar completamente el programa si se desea
    public static void liberarTodosLosEfectos() {
        for (Clip clip : cacheEfectos.values()) {
            try {
                if (clip != null) {
                    clip.stop();
                    clip.close();
                }
            } catch (Exception ignored) {}
        }
        cacheEfectos.clear();
    }

    public static void alternarSilencio() {
        musicaSilenciada = !musicaSilenciada;
        if (musicaSilenciada) {
            detener();
        }
    }

    public static boolean isMusicaSilenciada() {
        return musicaSilenciada;
    }
}