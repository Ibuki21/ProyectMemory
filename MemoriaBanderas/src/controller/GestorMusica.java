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

public class GestorMusica { // Administra música de fondo y caché de efectos cortos de sonido

    private static Clip clipMusicaFondo;
    private static String rutaActual;
    private static boolean musicaSilenciada = false;
    private static final Map<String, Clip> cacheEfectos = new HashMap<>();

    private GestorMusica() {
    }

    public static synchronized void reproducir(String rutaRecurso) { // Reproduce música de fondo en bucle
        if (rutaRecurso == null || rutaRecurso.trim().isEmpty()) { // Ignora rutas nulas o vacías
            return;
        }
        if (rutaRecurso.equals(rutaActual) && clipMusicaFondo != null && clipMusicaFondo.isOpen()) { // Si ya está cargada la misma pista y abierta
            aplicarEstadoSilencioMusica();
            if (!clipMusicaFondo.isRunning()) {
                clipMusicaFondo.loop(Clip.LOOP_CONTINUOUSLY); // Reproduce indefinidamente en bucle
            }
            return; // Evita recargar el mismo clip innecesariamente
        }

        detener();
        rutaActual = rutaRecurso;

        AudioInputStream audioStream = abrirFlujoAudio(rutaRecurso); // Intenta abrir el flujo del recurso de audio
        if (audioStream == null) { // Si no se pudo abrir el archivo/recurso
            return; // Termina de forma segura
        }

        try (AudioInputStream stream = audioStream) { // Cierra automáticamente el flujo al terminar el bloque
            Clip clip = AudioSystem.getClip();
            clip.open(stream);
            clipMusicaFondo = clip;
            aplicarEstadoSilencioMusica(); // Respeta si la música está silenciada
            clipMusicaFondo.loop(Clip.LOOP_CONTINUOUSLY); // Inicia reproducción continua en bucle
        } catch (LineUnavailableException | IOException e) { // Captura errores de hardware de audio o lectura
            System.err.println("Error al reproducir música de fondo (" + rutaRecurso + "): " + e.getMessage());
        }
    }

    public static synchronized void detener() { // Detiene y libera el clip de música de fondo actual
        if (clipMusicaFondo != null) {
            if (clipMusicaFondo.isRunning()) {
                clipMusicaFondo.stop();
            }
            clipMusicaFondo.close();
            clipMusicaFondo = null; // Limpia la referencia
        }
        rutaActual = null; // Reinicia la ruta actual
    }

    public static synchronized void alternarSilencio() { // Invierte el estado de silencio de la música de fondo
        musicaSilenciada = !musicaSilenciada;
        aplicarEstadoSilencioMusica();
    }

    public static synchronized boolean isMusicaSilenciada() { // Consulta si la música de fondo está silenciada
        return musicaSilenciada; // Retorna el estado actual
    }

    private static void aplicarEstadoSilencioMusica() { // Pausa o reanuda el clip de fondo según musicaSilenciada
        if (clipMusicaFondo == null || !clipMusicaFondo.isOpen()) { // Si no hay clip abierto, no hace nada
            return; // Sale del método
        }
        if (musicaSilenciada) { // Si está activado el silencio
            if (clipMusicaFondo.isRunning()) {
                clipMusicaFondo.stop();
            }
        } else { // Si no está silenciado
            if (!clipMusicaFondo.isRunning()) { // Si no está corriendo
                clipMusicaFondo.loop(Clip.LOOP_CONTINUOUSLY);
            }
        }
    }

    public static synchronized void precargar(String rutaEfecto) { // Metodo de conveniencia para precargar un efecto de sonido
        precargarEfecto(rutaEfecto); // Delega en precargarEfecto
    }

    public static synchronized Clip precargarEfecto(String rutaEfecto) { // Carga y guarda en caché un efecto corto para reutilizarlo
        if (rutaEfecto == null || rutaEfecto.trim().isEmpty()) {
            return null;
        }
        Clip existente = cacheEfectos.get(rutaEfecto); // Busca si ya estaba precargado en el mapa
        if (existente != null && existente.isOpen()) { // Si existe y sigue abierto
            return existente; // Reutiliza el clip ya cargado
        }

        AudioInputStream audioStream = abrirFlujoAudio(rutaEfecto); // Abre el flujo del archivo de efecto
        if (audioStream == null) {
            return null;
        }

        try (AudioInputStream stream = audioStream) { // Cierra el flujo al terminar
            Clip clip = AudioSystem.getClip(); // Obtiene un clip nuevo
            clip.open(stream); // Carga el efecto en memoria
            cacheEfectos.put(rutaEfecto, clip); // Guarda el clip en la caché
            return clip; // Devuelve el clip listo para reproducir
        } catch (LineUnavailableException | IOException e) { // Maneja fallos al abrir o cargar el clip
            System.err.println("Error al precargar efecto de sonido (" + rutaEfecto + "): " + e.getMessage());
            return null;
        }
    }

    public static synchronized void reproducirEfecto(String rutaEfecto) { // Reproduce un efecto corto desde el inicio usando la caché
        if (rutaEfecto == null || rutaEfecto.trim().isEmpty()) { // Ignora rutas vacías o nulas
            return;
        }
        Clip clip = cacheEfectos.get(rutaEfecto);
        if (clip == null || !clip.isOpen()) {
            clip = precargarEfecto(rutaEfecto); // Lo carga y guarda en caché
        }
        if (clip != null && clip.isOpen()) { // Si el clip está disponible
            if (clip.isRunning()) { // Si ya estaba sonando por un clic rápido anterior
                clip.stop(); // Lo detiene para reiniciarlo sin saturar
            }
            clip.setFramePosition(0); // Rebobina al frame 0 para reproducir desde el principio
            clip.start(); // Inicia la reproducción del efecto
        }
    }

    public static synchronized void liberarTodosLosEfectos() { // Cierra y limpia todos los clips almacenados en caché
        for (Clip clip : cacheEfectos.values()) { // Recorre cada clip en caché
            if (clip != null) { // Verifica que no sea nulo
                if (clip.isRunning()) { // Si está reproduciéndose
                    clip.stop(); // Detiene el clip
                }
                clip.close(); // Libera recursos del sistema operativo
            }
        }
        cacheEfectos.clear(); // Vacía el mapa de caché
    }

    private static AudioInputStream abrirFlujoAudio(String ruta) { // Busca el audio en recursos del proyecto o en disco y abre su flujo
        try {
            URL url = GestorMusica.class.getClassLoader().getResource(ruta); // Busca la ruta dentro del classpath/recursos
            if (url != null) { // Si se encontró como recurso
                return AudioSystem.getAudioInputStream(url); // Devuelve el flujo de audio
            }
            File archivo = new File(ruta); // Intenta tratar la ruta como archivo del sistema de archivos
            if (archivo.exists()) { // Si el archivo existe en esa ruta
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