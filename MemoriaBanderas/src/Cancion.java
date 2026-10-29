import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.net.URL;

public class Cancion {
    private Clip clip;
    private String rutaActual;

    public void cargarSonido(String rutaArchivo) {
        this.rutaActual = rutaArchivo;
        try {
            URL urlSonido = getClass().getClassLoader().getResource(rutaArchivo);
            if (urlSonido != null) {
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(urlSonido);
                clip = AudioSystem.getClip();
                clip.open(audioInput);
            } else {
                System.err.println("No se encontró el archivo de sonido en los recursos: " + rutaArchivo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void reproducir() {
        try {
            // Si ya está sonando, no hacemos nada
            if (clip != null && clip.isRunning()) {
                return;
            }

            // Si el clip es nulo o ya terminó, lo recargamos para asegurar un inicio limpio
            if (clip == null || !clip.isOpen()) {
                cargarSonido(rutaActual);
            }

            if (clip != null) {
                clip.setFramePosition(0);
                clip.loop(Clip.LOOP_CONTINUOUSLY); // Reproduce en bucle infinito
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void detener() {
        try {
            if (clip != null) {
                clip.stop();
                clip.close(); // Cerramos el clip por completo para liberar el recurso y evitar superposiciones
                clip = null;  // Lo ponemos en null para que se recargue limpio al encenderlo de nuevo
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}