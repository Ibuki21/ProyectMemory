package controller;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Reproduce efectos de sonido cortos (clics, aciertos, etc.).
// Es la unica copia de este codigo: las demas clases la llaman.
//
// Cada sonido se abre UNA sola vez y se reutiliza. Antes se abria un Clip nuevo en cada clic, y eso
// significa leer el archivo, recorrer los dispositivos de audio y reconectarse a PulseAudio cada vez,
// que era muy costoso. Todo el audio corre en un unico hilo aparte para que la interfaz nunca espere.
public class ReproductorSonido {

    private static final ExecutorService HILO_AUDIO = Executors.newSingleThreadExecutor(r -> {
        Thread hilo = new Thread(r, "efecto-sonido");
        hilo.setDaemon(true);
        return hilo;
    });

    // Solo se usan desde HILO_AUDIO, por eso no necesitan sincronizarse
    private static final Map<String, Clip> CLIPS = new HashMap<>();
    private static final Set<String> NO_ENCONTRADOS = new HashSet<>();

    private ReproductorSonido() {
    }

    // Reproduce el archivo desde el inicio. Si ya estaba sonando, lo reinicia.
    public static void reproducirEfecto(String rutaAudio) {
        HILO_AUDIO.execute(() -> reproducir(rutaAudio));
    }

    // Abre los sonidos por adelantado, para que el primer clic no tenga retraso
    public static void precargar(String... rutasAudio) {
        for (String ruta : rutasAudio) {
            HILO_AUDIO.execute(() -> obtenerClip(ruta));
        }
    }

    private static void reproducir(String rutaAudio) {
        Clip clip = obtenerClip(rutaAudio);
        if (clip == null) {
            return;
        }
        if (clip.isRunning()) {
            clip.stop();
        }
        clip.setFramePosition(0);
        clip.start();
    }

    private static Clip obtenerClip(String rutaAudio) {
        Clip existente = CLIPS.get(rutaAudio);
        if (existente != null) {
            return existente;
        }
        if (NO_ENCONTRADOS.contains(rutaAudio)) {
            return null;
        }
        URL urlAudio = ReproductorSonido.class.getClassLoader().getResource(rutaAudio);
        if (urlAudio == null) {
            System.err.println("No se encontró el archivo de audio: " + rutaAudio);
            NO_ENCONTRADOS.add(rutaAudio);
            return null;
        }
        try (AudioInputStream audioStream = AudioSystem.getAudioInputStream(urlAudio)) {
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            CLIPS.put(rutaAudio, clip);
            return clip;
        } catch (Exception e) {
            // No se guarda el fallo: si el audio estaba ocupado, se reintenta en el siguiente clic
            e.printStackTrace();
            return null;
        }
    }
}
