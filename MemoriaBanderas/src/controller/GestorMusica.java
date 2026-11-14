package controller;

import javax.sound.sampled.*;
import java.net.URL;

public class GestorMusica {

    private static Clip clipActual;
    private static String rutaActual = "";
    private static boolean musicaSilenciada = false;

    // Reproduce una pista en bucle continuo
    public static void reproducir(String rutaAudio) {
        if (musicaSilenciada) return;

        // Si ya está sonando exactamente esta misma pista, no la reiniciamos
        if (clipActual != null && rutaAudio.equals(rutaActual) && clipActual.isRunning()) {
            return;
        }

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
        if (clipActual != null) {
            if (clipActual.isRunning()) {
                clipActual.stop();
            }
            clipActual.close();
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