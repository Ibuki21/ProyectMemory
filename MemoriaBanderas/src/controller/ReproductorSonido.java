package controller;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import java.net.URL;

// Reproduce efectos de sonido cortos (clics, aciertos, etc.).
// Es la unica copia de este codigo: las demas clases la llaman.
public class ReproductorSonido {

    private ReproductorSonido() {
    }

    // Reproduce el archivo una vez, en un hilo aparte para que la interfaz nunca se quede esperando al audio
    public static void reproducirEfecto(String rutaAudio) {
        Thread hilo = new Thread(() -> {
            URL urlAudio = ReproductorSonido.class.getClassLoader().getResource(rutaAudio);
            if (urlAudio == null) {
                System.err.println("No se encontró el archivo de audio: " + rutaAudio);
                return;
            }
            try (AudioInputStream audioStream = AudioSystem.getAudioInputStream(urlAudio)) {
                Clip clip = AudioSystem.getClip();
                // Al terminar el sonido se libera el clip. Sin esto cada clic deja una
                // linea de audio abierta y, tras muchos clics, el audio puede bloquearse.
                clip.addLineListener(evento -> {
                    if (evento.getType() == LineEvent.Type.STOP) {
                        evento.getLine().close();
                    }
                });
                clip.open(audioStream);
                clip.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "efecto-sonido");
        hilo.setDaemon(true);
        hilo.start();
    }
}
