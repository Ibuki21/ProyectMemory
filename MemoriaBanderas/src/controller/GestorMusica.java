package controller;

import javax.sound.sampled.*;
import java.net.URL;

public class GestorMusica {

    private static Clip clipActual;
    private static String rutaActual = "";
    private static boolean musicaSilenciada = false;

    // Reproduce una pista en bucle continuo y fuerza el cambio de audio
    public static void reproducir(String rutaAudio) {
        if (musicaSilenciada) return;

        // 🔥 CORRECCIÓN CLAVE: Forzamos siempre la detención del clip actual
        // para garantizar que nunca se encimen canciones al cambiar de ventana.
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