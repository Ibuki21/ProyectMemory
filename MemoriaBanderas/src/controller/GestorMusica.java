package controller;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class GestorMusica {

    private static Clip clipMusicaFondo;
    private static String rutaActual;
    private static boolean musicaSilenciada = false;
    private static final Map<String, Clip> cacheEfectos = new HashMap<>();

    private GestorMusica() {
    }

    public static synchronized void reproducir(String rutaRecurso) {
        if (rutaRecurso == null || rutaRecurso.trim().isEmpty()) {
            return;
        }
        if (rutaRecurso.equals(rutaActual) && clipMusicaFondo != null && clipMusicaFondo.isOpen()) {
            aplicarEstadoSilencioMusica();
            if (!clipMusicaFondo.isRunning()) {
                clipMusicaFondo.loop(Clip.LOOP_CONTINUOUSLY);
            }
            return;
        }

        detener();
        rutaActual = rutaRecurso;

        AudioInputStream audioStream = abrirFlujoAudio(rutaRecurso);
        if (audioStream == null) {
            return;
        }

        try (AudioInputStream stream = audioStream) {
            Clip clip = AudioSystem.getClip();
            clip.open(stream);
            clipMusicaFondo = clip;
            aplicarEstadoSilencioMusica();
            clipMusicaFondo.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (LineUnavailableException | IOException e) {
            System.err.println("Error al reproducir música de fondo (" + rutaRecurso + "): " + e.getMessage());
        }
    }

    public static synchronized void detener() {
        if (clipMusicaFondo != null) {
            if (clipMusicaFondo.isRunning()) {
                clipMusicaFondo.stop();
            }
            clipMusicaFondo.close();
            clipMusicaFondo = null;
        }
        rutaActual = null;
    }

    public static synchronized void alternarSilencio() {
        musicaSilenciada = !musicaSilenciada;
        aplicarEstadoSilencioMusica();
    }

    public static synchronized boolean isMusicaSilenciada() {
        return musicaSilenciada;
    }

    private static void aplicarEstadoSilencioMusica() {
        if (clipMusicaFondo == null || !clipMusicaFondo.isOpen()) {
            return;
        }
        if (musicaSilenciada) {
            if (clipMusicaFondo.isRunning()) {
                clipMusicaFondo.stop();
            }
        } else {
            if (!clipMusicaFondo.isRunning()) {
                clipMusicaFondo.loop(Clip.LOOP_CONTINUOUSLY);
            }
        }
    }

    public static synchronized void precargar(String rutaEfecto) {
        precargarEfecto(rutaEfecto);
    }

    public static synchronized Clip precargarEfecto(String rutaEfecto) {
        if (rutaEfecto == null || rutaEfecto.trim().isEmpty()) {
            return null;
        }
        Clip existente = cacheEfectos.get(rutaEfecto);
        if (existente != null && existente.isOpen()) {
            return existente;
        }

        AudioInputStream audioStream = abrirFlujoAudio(rutaEfecto);
        if (audioStream == null) {
            return null;
        }

        try (AudioInputStream stream = audioStream) {
            Clip clip = AudioSystem.getClip();
            clip.open(stream);
            cacheEfectos.put(rutaEfecto, clip);
            return clip;
        } catch (LineUnavailableException | IOException e) {
            System.err.println("Error al precargar efecto de sonido (" + rutaEfecto + "): " + e.getMessage());
            return null;
        }
    }

    public static synchronized void reproducirEfecto(String rutaEfecto) {
        if (rutaEfecto == null || rutaEfecto.trim().isEmpty()) {
            return;
        }
        Clip clip = cacheEfectos.get(rutaEfecto);
        if (clip == null || !clip.isOpen()) {
            clip = precargarEfecto(rutaEfecto);
        }
        if (clip != null && clip.isOpen()) {
            if (clip.isRunning()) {
                clip.stop();
            }
            clip.setFramePosition(0);
            clip.start();
        }
    }

    public static synchronized void liberarTodosLosEfectos() {
        for (Clip clip : cacheEfectos.values()) {
            if (clip != null) {
                if (clip.isRunning()) {
                    clip.stop();
                }
                clip.close();
            }
        }
        cacheEfectos.clear();
    }

    private static AudioInputStream abrirFlujoAudio(String ruta) {
        try {
            URL url = GestorMusica.class.getClassLoader().getResource(ruta);
            if (url != null) {
                return AudioSystem.getAudioInputStream(url);
            }
            File archivo = new File(ruta);
            if (archivo.exists()) {
                return AudioSystem.getAudioInputStream(archivo);
            }
            System.err.println("No se encontró el archivo de audio: " + ruta);
            return null;
        } catch (UnsupportedAudioFileException | IOException e) {
            System.err.println("Formato o error al abrir audio (" + ruta + "): " + e.getMessage());
            return null;
        }
    }
}