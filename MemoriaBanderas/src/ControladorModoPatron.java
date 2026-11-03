import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.net.URL;

/**
 * Controlador del Modo Patrones.
 * Conecta la vista (VentanaModoPatron) con el modelo (LogicaPatron):
 * muestra el patrón, atiende los clics y maneja las barras de tiempo.
 */
public class ControladorModoPatron {

    private static final int INTERVALO_MOSTRAR_MS = 500;   // Duración de cada casilla al mostrar el patrón
    private static final int PAUSA_INICIAL_MS = 800;      // Pausa antes de mostrar un patrón
    private static final int TICK_MS = 100;               // Las barras se actualizan cada 0.1 s
    private static final int TIEMPO_TOTAL = 120;          // 12 segundos (en décimas)
    private static final int TIEMPO_ENTRE_CASILLAS = 20;  // 2 segundos (en décimas)

    private final VentanaModoPatron vista;
    private final LogicaPatron logica;
    private final JButton[][] casillas;

    private Timer timerMostrar;
    private Timer timerTotal;
    private Timer timerEntre;
    private Timer timerPausa;

    private int pasoMostrar;
    private int restanteTotal;
    private int restanteEntre;
    private boolean turnoJugador = false;
    private boolean juegoTerminado = false;

    public ControladorModoPatron(VentanaModoPatron vista) {
        this.vista = vista;
        this.casillas = vista.getBotonesCasillas();
        this.logica = new LogicaPatron(casillas.length, casillas[0].length);

        // Evento de cada casilla
        for (int i = 0; i < casillas.length; i++) {
            for (int j = 0; j < casillas[i].length; j++) {
                final int fila = i;
                final int columna = j;
                casillas[i][j].addActionListener(e -> alPresionarCasilla(fila, columna));
            }
        }

        // Si se cierra la ventana, se detienen los tiempos
        vista.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                detenerTimers();
            }
        });
    }

    // Arranca la partida
    public void iniciar() {
        logica.iniciar();
        mostrarPatron();
    }

    // ---------- Mostrar el patrón ----------

    private void mostrarPatron() {
        turnoJugador = false;
        vista.quitarTodosLosResaltados();
        vista.getLblCasillasPatron().setText("Casillas a recordar: " + logica.getLongitud());
        actualizarBarraTotal(TIEMPO_TOTAL);
        actualizarBarraEntre(TIEMPO_ENTRE_CASILLAS);

        pasoMostrar = 0;
        timerMostrar = new Timer(INTERVALO_MOSTRAR_MS, e -> pasoDeMuestra());
        timerMostrar.setInitialDelay(PAUSA_INICIAL_MS);
        timerMostrar.start();
    }

    // Cada paso par enciende una casilla del patrón y cada paso impar la apaga
    private void pasoDeMuestra() {
        int posicion = pasoMostrar / 2;
        int fila = logica.getFila(posicion);
        int columna = logica.getColumna(posicion);

        if (pasoMostrar % 2 == 0) {
            vista.resaltarCasilla(fila, columna);
        } else {
            vista.quitarResaltado(fila, columna);
        }

        pasoMostrar++;
        if (pasoMostrar >= logica.getLongitud() * 2) {
            timerMostrar.stop();
            comenzarTurnoJugador();
        }
    }

    // ---------- Turno del jugador ----------

    private void comenzarTurnoJugador() {
        logica.reiniciarIntento();
        turnoJugador = true;

        // Barra de 12 segundos para replicar todo el patrón
        restanteTotal = TIEMPO_TOTAL;
        actualizarBarraTotal(restanteTotal);
        actualizarBarraEntre(TIEMPO_ENTRE_CASILLAS);

        timerTotal = new Timer(TICK_MS, e -> {
            restanteTotal--;
            actualizarBarraTotal(restanteTotal);
            if (restanteTotal <= 0) {
                terminarJuego("¡Se acabó el tiempo total de 12 segundos!");
            }
        });
        timerTotal.start();
    }

    private void alPresionarCasilla(int fila, int columna) {
        if (!turnoJugador || juegoTerminado) {
            return;
        }

        // 🎵 Reproducir el sonido de clic al presionar cualquier casilla durante el turno del jugador
        reproducirEfectoSonido("Recursos/Cancion/Botones/Presiona1.wav");

        if (!logica.validarCasilla(fila, columna)) {
            terminarJuego("¡Casilla incorrecta!");
            return;
        }

        vista.resaltarCasilla(fila, columna);

        if (logica.patronCompleto()) {
            // Patrón correcto: se pasa al siguiente con 1 casilla más
            detenerTimers();
            turnoJugador = false;

            if (!logica.puedeAvanzar()) {
                terminarJuego("¡Completaste todos los patrones posibles!");
                return;
            }

            logica.avanzarNivel();
            timerPausa = new Timer(PAUSA_INICIAL_MS, e -> mostrarPatron());
            timerPausa.setRepeats(false);
            timerPausa.start();
        } else {
            reiniciarTimerEntreCasillas();
        }
    }

    // Método auxiliar para reproducir efectos de sonido
    private void reproducirEfectoSonido(String rutaAudio) {
        try {
            URL urlAudio = getClass().getClassLoader().getResource(rutaAudio);
            if (urlAudio != null) {
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(urlAudio);
                Clip clipClick = AudioSystem.getClip();
                clipClick.open(audioStream);
                clipClick.start();
            } else {
                System.err.println("No se encontró el archivo de audio: " + rutaAudio);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Reinicia la barra de 2 segundos máximo entre una casilla y la siguiente
    private void reiniciarTimerEntreCasillas() {
        if (timerEntre != null) {
            timerEntre.stop();
        }
        restanteEntre = TIEMPO_ENTRE_CASILLAS;
        actualizarBarraEntre(restanteEntre);

        timerEntre = new Timer(TICK_MS, e -> {
            restanteEntre--;
            actualizarBarraEntre(restanteEntre);
            if (restanteEntre <= 0) {
                terminarJuego("¡Pasaron más de 2 segundos entre casillas!");
            }
        });
        timerEntre.start();
    }

    // ---------- Fin de la partida ----------

    private void terminarJuego(String mensaje) {
        if (juegoTerminado) {
            return;
        }
        juegoTerminado = true;
        turnoJugador = false;
        detenerTimers();

        int puntajeObtenido = logica.getPatronesCompletados();
        Usuario actual = GestorUsuarios.getUsuarioSesionActual();

        if (actual != null) {
            actual.setPuntuacionMaxima(puntajeObtenido);
        }

        JOptionPane.showMessageDialog(vista,
                mensaje + "\nPatrones completados: " + puntajeObtenido,
                "Fin del juego", JOptionPane.INFORMATION_MESSAGE);
        vista.dispose();
    }

    private void detenerTimers() {
        if (timerMostrar != null) {
            timerMostrar.stop();
        }
        if (timerTotal != null) {
            timerTotal.stop();
        }
        if (timerEntre != null) {
            timerEntre.stop();
        }
        if (timerPausa != null) {
            timerPausa.stop();
        }
    }

    // ---------- Barras de tiempo ----------

    private void actualizarBarraTotal(int decimas) {
        decimas = Math.max(decimas, 0);
        vista.getBarraTiempoTotal().setValue(decimas);
        vista.getBarraTiempoTotal().setString(String.format("Tiempo total: %.1f s", decimas / 10.0));
    }

    private void actualizarBarraEntre(int decimas) {
        decimas = Math.max(decimas, 0);
        vista.getBarraTiempoEntreCasillas().setValue(decimas);
        vista.getBarraTiempoEntreCasillas().setString(String.format("Tiempo entre casillas: %.1f s", decimas / 10.0));
    }
}