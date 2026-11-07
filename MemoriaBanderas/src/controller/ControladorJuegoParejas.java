package controller;
import model.JuegoParejasLogica;
import view.JuegoParejasVisual;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import javax.imageio.ImageIO;

public class ControladorJuegoParejas {

    private JuegoParejasVisual vista;
    private JuegoParejasLogica logica1;
    private JuegoParejasLogica logica2;

    private static final String RUTA_PAREJAS = "Recursos/Parejas/";
    private static final String RUTA_AUDIO_CLICK = "Recursos/Cancion/Botones/Presiona1.wav";

    private static final int TICK_MS = 100;      // El reloj se actualiza cada 0.1 s
    private static final int TIEMPO_TURNO = 100; // 10 segundos por jugada (en décimas)
    private static final int TIEMPO_EXTRA = 70;  // 7 segundos extra por cada acierto (en décimas)

    private boolean bloqueandoClics = false; // Verdadero mientras se revisa una pareja
    private boolean juegoTerminado = false;
    private int jugadorActual = 1;           // Jugador que tiene el turno (1 o 2)
    private int tiempoRestante;              // Tiempo que le queda al jugador actual (en décimas)
    private int partida = 0;                 // Cambia en cada reinicio para descartar revisiones pendientes
    private Timer timerTurno;

    public ControladorJuegoParejas(JuegoParejasVisual vista) {
        this.vista = vista;
        this.logica1 = new JuegoParejasLogica(); // Lógica independiente para el Tablero 1
        this.logica2 = new JuegoParejasLogica(); // Lógica independiente para el Tablero 2 (patrón distinto)

        inicializarEventos();
        actualizarVistaTablero(1);
        actualizarVistaTablero(2);

        timerTurno = new Timer(TICK_MS, e -> alPasarTiempo());

        // Si se cierra la ventana, se detiene el reloj
        vista.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                timerTurno.stop();
            }
        });

        iniciarTurno(1);
    }

    private void inicializarEventos() {
        int filas = vista.getFilas();
        int columnas = vista.getColumnas();

        // Eventos Tablero 1
        JButton[][] botones1 = vista.getBotonesTablero1();
        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                final int indice = i * columnas + j;
                botones1[i][j].addActionListener(e -> manejarClicTablero(1, indice));
            }
        }

        // Eventos Tablero 2
        JButton[][] botones2 = vista.getBotonesTablero2();
        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                final int indice = i * columnas + j;
                botones2[i][j].addActionListener(e -> manejarClicTablero(2, indice));
            }
        }

        // Botón Reiniciar Ambos
        vista.getBtnReiniciar().addActionListener(e -> {
            ReproductorSonido.reproducirEfecto(RUTA_AUDIO_CLICK);
            partida++;
            bloqueandoClics = false;
            juegoTerminado = false;
            logica1.reiniciarJuego();
            logica2.reiniciarJuego();
            actualizarVistaTablero(1);
            actualizarVistaTablero(2);
            vista.getLblIntentos1().setText("Intentos: 0");
            vista.getLblPares1().setText("Pares: 0 / 10");
            vista.getLblIntentos2().setText("Intentos: 0");
            vista.getLblPares2().setText("Pares: 0 / 10");
            iniciarTurno(1);
        });
    }

    // ---------- Turnos y tiempo ----------

    private JuegoParejasLogica logicaDe(int jugador) {
        return (jugador == 1) ? logica1 : logica2;
    }

    // Da el turno a un jugador con sus 10 segundos completos
    private void iniciarTurno(int jugador) {
        jugadorActual = jugador;
        tiempoRestante = TIEMPO_TURNO;
        vista.marcarTableroActivo(jugador);
        vista.getLblTurno().setText("Turno: Jugador " + jugador);
        vista.getBarraTiempo().setMaximum(TIEMPO_TURNO);
        actualizarBarraTiempo();
        timerTurno.restart();
    }

    // Pasa el turno al contrincante
    private void cambiarTurno() {
        int siguiente = (jugadorActual == 1) ? 2 : 1;
        // Si el contrincante ya completó su tablero, el turno sigue siendo del jugador actual
        if (logicaDe(siguiente).haGanado()) {
            siguiente = jugadorActual;
        }
        iniciarTurno(siguiente);
    }

    // Se ejecuta cada 0.1 s mientras el jugador tiene el turno
    private void alPasarTiempo() {
        if (bloqueandoClics) {
            return; // El reloj espera mientras se revisa la pareja elegida
        }
        tiempoRestante--;
        actualizarBarraTiempo();
        if (tiempoRestante <= 0) {
            // Se acabó el tiempo: la carta volteada sin pareja se oculta y el turno pasa al contrincante
            logicaDe(jugadorActual).cancelarSeleccionPendiente();
            actualizarVistaTablero(jugadorActual);
            cambiarTurno();
        }
    }

    private void actualizarBarraTiempo() {
        int decimas = Math.max(tiempoRestante, 0);
        // Con los segundos extra el tiempo puede superar los 10 segundos iniciales
        if (decimas > vista.getBarraTiempo().getMaximum()) {
            vista.getBarraTiempo().setMaximum(decimas);
        }
        vista.getBarraTiempo().setValue(decimas);
        vista.getBarraTiempo().setString(String.format("Tiempo: %.1f s", decimas / 10.0));
    }

    // ---------- Jugadas ----------

    private void manejarClicTablero(int numTablero, int indice) {
        // Solo juega el jugador que tiene el turno, en su propio tablero
        if (juegoTerminado || bloqueandoClics || numTablero != jugadorActual) return;

        JuegoParejasLogica logicaActiva = logicaDe(numTablero);
        if (logicaActiva.estaRevelada(indice)) return;

        ReproductorSonido.reproducirEfecto(RUTA_AUDIO_CLICK);

        if (!logicaActiva.seleccionarCarta(indice)) return;

        actualizarVistaTablero(numTablero);

        if (logicaActiva.getIndiceSegundaSeleccion() != -1) {
            bloqueandoClics = true;
            final int partidaActual = partida;

            Timer timer = new Timer(750, ae -> {
                if (partidaActual != partida) return; // Se reinició la partida mientras se revisaba

                boolean acierto = logicaActiva.evaluarPareja();
                if (!acierto) {
                    logicaActiva.ocultarSeleccionFallida();
                }

                actualizarVistaTablero(numTablero);
                actualizarEstadisticas(numTablero);
                bloqueandoClics = false;
                resolverJugada(numTablero, acierto);
            });
            timer.setRepeats(false);
            timer.start();
        }
    }

    // Decide qué pasa después de revisar la pareja: fin del juego, tiempo extra o cambio de turno
    private void resolverJugada(int numTablero, boolean acierto) {
        if (logica1.haGanado() && logica2.haGanado()) {
            terminarJuego();
        } else if (acierto && !logicaDe(numTablero).haGanado()) {
            // Acierto: el jugador conserva el turno y se le suman 7 segundos
            tiempoRestante += TIEMPO_EXTRA;
            actualizarBarraTiempo();
        } else {
            // Falla (o ya completó su tablero): el turno pasa al contrincante
            cambiarTurno();
        }
    }

    private void actualizarEstadisticas(int numTablero) {
        JuegoParejasLogica logicaActiva = logicaDe(numTablero);
        JLabel lblIntentos = (numTablero == 1) ? vista.getLblIntentos1() : vista.getLblIntentos2();
        JLabel lblPares = (numTablero == 1) ? vista.getLblPares1() : vista.getLblPares2();
        lblIntentos.setText("Intentos: " + logicaActiva.getIntentos());
        lblPares.setText("Pares: " + logicaActiva.getParesEncontrados() + " / 10");
    }

    // ---------- Fin de la partida ----------

    // Gana el jugador que usó menos intentos para encontrar todas las parejas
    private void terminarJuego() {
        juegoTerminado = true;
        timerTurno.stop();

        int intentos1 = logica1.getIntentos();
        int intentos2 = logica2.getIntentos();

        String resultado;
        if (intentos1 < intentos2) {
            resultado = "¡Gana el Jugador 1!";
        } else if (intentos2 < intentos1) {
            resultado = "¡Gana el Jugador 2!";
        } else {
            resultado = "¡Empate!";
        }

        JOptionPane.showMessageDialog(vista,
                resultado + "\nJugador 1: " + intentos1 + " intentos"
                        + "\nJugador 2: " + intentos2 + " intentos",
                "Fin del juego", JOptionPane.INFORMATION_MESSAGE);
    }

    private void actualizarVistaTablero(int numTablero) {
        JuegoParejasLogica logicaActiva = (numTablero == 1) ? logica1 : logica2;
        JButton[][] botones = (numTablero == 1) ? vista.getBotonesTablero1() : vista.getBotonesTablero2();
        int filas = vista.getFilas();
        int columnas = vista.getColumnas();

        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                int indice = i * columnas + j;
                JButton btn = botones[i][j];

                if (logicaActiva.estaRevelada(indice) || estaTemporalmenteVolteada(logicaActiva, indice)) {
                    String nombreArchivo = logicaActiva.getCodigoCarta(indice);
                    ImageIcon icono = cargarImagenEscalada(nombreArchivo, 85, 75);
                    if (icono != null) {
                        btn.setIcon(icono);
                        btn.setText("");
                    } else {
                        btn.setIcon(null);
                        btn.setText(nombreArchivo);
                    }
                    btn.setBackground(Color.WHITE);
                } else {
                    btn.setIcon(null);
                    btn.setText("❓");
                    btn.setFont(new Font("Segoe UI", Font.BOLD, 22));
                    btn.setForeground(Color.WHITE);
                    btn.setBackground(new Color(30, 50, 80));
                }
            }
        }
    }

    private boolean estaTemporalmenteVolteada(JuegoParejasLogica logica, int indice) {
        return indice == logica.getIndicePrimeraSeleccion() || indice == logica.getIndiceSegundaSeleccion();
    }

    private ImageIcon cargarImagenEscalada(String nombreArchivo, int ancho, int alto) {
        String rutaCompleta = RUTA_PAREJAS + nombreArchivo + ".jpg";
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(rutaCompleta)) {
            if (is == null) {
                return cargarImagenPng(nombreArchivo, ancho, alto);
            }
            BufferedImage original = ImageIO.read(is);
            if (original == null) return null;

            BufferedImage escalada = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = escalada.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(original, 0, 0, ancho, alto, null);
            g2.dispose();
            return new ImageIcon(escalada);
        } catch (Exception e) {
            return null;
        }
    }

    private ImageIcon cargarImagenPng(String nombreArchivo, int ancho, int alto) {
        String rutaCompleta = RUTA_PAREJAS + nombreArchivo + ".png";
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(rutaCompleta)) {
            if (is == null) return null;
            BufferedImage original = ImageIO.read(is);
            if (original == null) return null;

            BufferedImage escalada = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = escalada.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(original, 0, 0, ancho, alto, null);
            g2.dispose();
            return new ImageIcon(escalada);
        } catch (Exception e) {
            return null;
        }
    }
}
