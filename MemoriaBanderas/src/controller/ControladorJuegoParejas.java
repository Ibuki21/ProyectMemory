package controller;

import model.GestorUsuarios;
import model.JuegoParejasLogica;
import model.Usuario;
import model.ServicioCambioDolar;
import view.JuegoParejasVisual;
import view.VentanaLogin;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import javax.imageio.ImageIO;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ControladorJuegoParejas {

    private JuegoParejasVisual vista;
    private JuegoParejasLogica logica1;
    private JuegoParejasLogica logica2;

    private static final String RUTA_PAREJAS = "Recursos/Parejas/";
    private static final String RUTA_AUDIO_CLICK = "Recursos/Cancion/Botones/Presiona1.wav";

    // Las 18 imágenes ya leídas y escaladas: se cargan del disco una sola vez y se reutilizan
    private static final Map<String, ImageIcon> CACHE_ICONOS = new HashMap<>();
    private static final Font FUENTE_CARTA_OCULTA = new Font("Segoe UI", Font.BOLD, 18);
    private static final Color COLOR_CARTA_OCULTA = new Color(30, 50, 80);

    private static final int TICK_MS = 100;
    private static final int TIEMPO_TURNO = 100;
    private static final int TIEMPO_EXTRA = 70;
    private static final int TIEMPO_MEMORIA = 50;

    private boolean bloqueandoClics = true;
    private boolean faseDeMemoria = true;
    private boolean juegoTerminado = false;
    private int jugadorActual = 1;
    private int tiempoRestante;
    private int partida = 0;
    private Timer timerTurno;
    private Timer timerMemoria;

    public ControladorJuegoParejas(JuegoParejasVisual vista) {
        this.vista = vista;
        this.logica1 = new JuegoParejasLogica();
        this.logica2 = new JuegoParejasLogica();

        inicializarEventos();

        vista.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (timerTurno != null) timerTurno.stop();
                if (timerMemoria != null) timerMemoria.stop();
            }
        });

        iniciarFaseMemoria();
    }

    private void inicializarEventos() {
        int filas = vista.getFilas();
        int columnas = vista.getColumnas();

        JButton[][] botones1 = vista.getBotonesTablero1();
        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                final int indice = i * columnas + j;
                botones1[i][j].addActionListener(e -> manejarClicTablero(1, indice));
            }
        }

        JButton[][] botones2 = vista.getBotonesTablero2();
        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                final int indice = i * columnas + j;
                botones2[i][j].addActionListener(e -> manejarClicTablero(2, indice));
            }
        }

        // Botón Finalizar intento: detiene la partida y pregunta si comenzar de nuevo o volver al menú[cite: 12]
        vista.getBtnFinalizar().addActionListener(e -> {
            if (juegoTerminado) return;
            ReproductorSonido.reproducirEfecto(RUTA_AUDIO_CLICK);
            partida++;
            juegoTerminado = true;
            bloqueandoClics = true;
            if (timerTurno != null) timerTurno.stop();
            if (timerMemoria != null) timerMemoria.stop();
            preguntarSiguientePaso();
        });
    }

    private void preguntarSiguientePaso() {
        Object[] opciones = {"Comenzar de nuevo", "Volver al menú principal"};
        int eleccion = JOptionPane.showOptionDialog(vista,
                "¿Qué desea hacer ahora?",
                "Fin del juego",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                opciones,
                opciones[0]);

        if (eleccion == 0) {
            reiniciarPartida();
        } else {
            vista.dispose();
        }
    }

    private void reiniciarPartida() {
        partida++;
        if (timerTurno != null) timerTurno.stop();
        if (timerMemoria != null) timerMemoria.stop();

        juegoTerminado = false;
        logica1.reiniciarJuego();
        logica2.reiniciarJuego();

        vista.getLblIntentos1().setText("Intentos: 0");
        vista.getLblPares1().setText("Pares: 0 / 18");
        vista.getLblIntentos2().setText("Intentos: 0");
        vista.getLblPares2().setText("Pares: 0 / 18");

        vista.restaurarGridTablero(1);
        vista.restaurarGridTablero(2);

        iniciarFaseMemoria();
    }

    private void ofrecerRegistroPuntaje(int jugador, int intentos) {
        String nombreTablero = (jugador == 1) ? "Tablero 1 (Jugador 1)" : "Tablero 2 (Jugador 2)";

        int respuesta = JOptionPane.showConfirmDialog(vista,
                nombreTablero + " ha completado el juego con " + intentos + " intentos.\n¿Desea registrar este puntaje?",
                "Registrar puntaje - " + nombreTablero, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        Usuario sesionPrevia = GestorUsuarios.getUsuarioSesionActual();
        Usuario destino;

        if (jugador == 1 && sesionPrevia != null) {
            destino = sesionPrevia;
        } else {
            JOptionPane.showMessageDialog(vista,
                    "Por favor, inicie sesión con la cuenta correspondiente al " + nombreTablero + ".",
                    "Autenticación requerida", JOptionPane.INFORMATION_MESSAGE);

            VentanaLogin login = new VentanaLogin(vista);
            new ControladorLogin(login);
            login.setVisible(true);
            destino = GestorUsuarios.getUsuarioSesionActual();
        }

        if (destino == null) {
            JOptionPane.showMessageDialog(vista, "No se registró el puntaje porque no se inició sesión.",
                    "Puntaje no registrado", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        boolean mejoro = destino.registrarIntentosParejas(intentos);
        int puntosGanados = Math.max(10, 200 - (intentos * 2));
        destino.agregarPuntos(puntosGanados);
        GestorUsuarios.actualizarPuntuacionUsuario(destino);

        double premioColones = ServicioCambioDolar.calcularPremio(intentos);

        String mensaje = "¡Puntaje registrado para " + destino.getUsuario() + " (" + nombreTablero + ")!\n" +
                "• Intentos realizados: " + intentos + "\n" +
                "• Premio estimado (BCCR): ₡" + String.format("%.2f", premioColones);

        if (!mejoro) {
            mensaje += "\n(Su mejor marca histórica en este modo sigue siendo: " + destino.getMejorIntentosParejas() + " intentos)";
        }

        JOptionPane.showMessageDialog(vista, mensaje, "Puntaje y Premio Registrado", JOptionPane.INFORMATION_MESSAGE);

        if (jugador == 2 && sesionPrevia != null) {
            GestorUsuarios.establecerSesion(sesionPrevia);
        }
    }

    private void iniciarFaseMemoria() {
        faseDeMemoria = true;
        bloqueandoClics = true;
        vista.marcarTableroActivo(1);
        vista.getLblTurno().setText("¡MEMORICEN LAS CARTAS (5s)!");

        actualizarVistaTablero(1);
        actualizarVistaTablero(2);

        tiempoRestante = TIEMPO_MEMORIA;
        vista.getBarraTiempo().setMaximum(TIEMPO_MEMORIA);
        actualizarBarraTiempoMemoria();

        timerMemoria = new Timer(TICK_MS, e -> {
            tiempoRestante--;
            actualizarBarraTiempoMemoria();
            if (tiempoRestante <= 0) {
                timerMemoria.stop();
                faseDeMemoria = false;
                bloqueandoClics = false;
                actualizarVistaTablero(1);
                actualizarVistaTablero(2);
                iniciarTurno(1);
            }
        });
        timerMemoria.start();
    }

    private void actualizarBarraTiempoMemoria() {
        int decimas = Math.max(tiempoRestante, 0);
        vista.getBarraTiempo().setValue(decimas);
        vista.getBarraTiempo().setString(String.format("Memorizando: %.1f s", decimas / 10.0));
    }

    private JuegoParejasLogica logicaDe(int jugador) {
        return (jugador == 1) ? logica1 : logica2;
    }

    private void iniciarTurno(int jugador) {
        if (juegoTerminado) return;
        jugadorActual = jugador;
        tiempoRestante = TIEMPO_TURNO;
        vista.marcarTableroActivo(jugador);
        vista.getLblTurno().setText("Turno: Jugador " + jugador);
        vista.getBarraTiempo().setMaximum(TIEMPO_TURNO);
        actualizarBarraTiempoTurno();

        if (timerTurno == null) {
            timerTurno = new Timer(TICK_MS, e -> alPasarTiempo());
        }
        timerTurno.restart();
    }

    private void cambiarTurno() {
        if (juegoTerminado) return;
        int siguiente = (jugadorActual == 1) ? 2 : 1;
        if (logicaDe(siguiente).haGanado()) {
            siguiente = jugadorActual;
        }
        iniciarTurno(siguiente);
    }

    private void alPasarTiempo() {
        if (bloqueandoClics || faseDeMemoria || juegoTerminado) {
            return;
        }
        tiempoRestante--;
        actualizarBarraTiempoTurno();
        if (tiempoRestante <= 0) {
            logicaDe(jugadorActual).cancelarSeleccionPendiente();
            actualizarVistaTablero(jugadorActual);
            cambiarTurno();
        }
    }

    private void actualizarBarraTiempoTurno() {
        int decimas = Math.max(tiempoRestante, 0);
        if (decimas > vista.getBarraTiempo().getMaximum()) {
            vista.getBarraTiempo().setMaximum(decimas);
        }
        vista.getBarraTiempo().setValue(decimas);
        vista.getBarraTiempo().setString(String.format("Tiempo: %.1f s", decimas / 10.0));
    }

    private void manejarClicTablero(int numTablero, int indice) {
        if (juegoTerminado || bloqueandoClics || faseDeMemoria || numTablero != jugadorActual) return;

        JuegoParejasLogica logicaActiva = logicaDe(numTablero);
        if (logicaActiva.estaRevelada(indice)) return;

        ReproductorSonido.reproducirEfecto(RUTA_AUDIO_CLICK);

        if (!logicaActiva.seleccionarCarta(indice)) return;

        actualizarVistaTablero(numTablero);

        if (logicaActiva.getIndiceSegundaSeleccion() != -1) {
            bloqueandoClics = true;
            final int partidaActual = partida;

            Timer timer = new Timer(750, ae -> {
                if (partidaActual != partida || juegoTerminado) return;

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

    private void resolverJugada(int numTablero, boolean acierto) {
        if (logica1.haGanado() || logica2.haGanado()) {
            verificarFinPartidaDual();
        } else if (acierto) {
            tiempoRestante += TIEMPO_EXTRA;
            actualizarBarraTiempoTurno();
        } else {
            cambiarTurno();
        }
    }

    private void actualizarEstadisticas(int numTablero) {
        JuegoParejasLogica logicaActiva = logicaDe(numTablero);
        JLabel lblIntentos = (numTablero == 1) ? vista.getLblIntentos1() : vista.getLblIntentos2();
        JLabel lblPares = (numTablero == 1) ? vista.getLblPares1() : vista.getLblPares2();
        lblIntentos.setText("Intentos: " + logicaActiva.getIntentos());
        lblPares.setText("Pares: " + logicaActiva.getParesEncontrados() + " / 18");
    }

    private void verificarFinPartidaDual() {
        boolean gano1 = logica1.haGanado();
        boolean gano2 = logica2.haGanado();

        if (gano1 || gano2) {
            if (gano1 && gano2) {
                terminarJuegoCompleto();
            } else if (gano1) {
                juegoTerminado = true;
                if (timerTurno != null) timerTurno.stop();

                cargarGifEnTablero(1, true);  // Victoria.gif[cite: 1]
                cargarGifEnTablero(2, false); // Derrota.gif[cite: 1]

                vista.getLblTurno().setText("¡Jugador 1 completó su tablero primero!");
                JOptionPane.showMessageDialog(vista, "¡El Jugador 1 ganó al completar todas sus parejas primero!", "Fin del Juego", JOptionPane.INFORMATION_MESSAGE);
                ofrecerRegistroPuntaje(1, logica1.getIntentos());
                preguntarSiguientePaso();
            } else if (gano2) {
                juegoTerminado = true;
                if (timerTurno != null) timerTurno.stop();

                cargarGifEnTablero(2, true);  // Victoria.gif[cite: 1]
                cargarGifEnTablero(1, false); // Derrota.gif[cite: 1]

                vista.getLblTurno().setText("¡Jugador 2 completó su tablero primero!");
                JOptionPane.showMessageDialog(vista, "¡El Jugador 2 ganó al completar todas sus parejas primero!", "Fin del Juego", JOptionPane.INFORMATION_MESSAGE);
                ofrecerRegistroPuntaje(2, logica2.getIntentos());
                preguntarSiguientePaso();
            }
        }
    }

    private void terminarJuegoCompleto() {
        juegoTerminado = true;
        if (timerTurno != null) timerTurno.stop();

        int intentos1 = logica1.getIntentos();
        int intentos2 = logica2.getIntentos();

        if (intentos1 < intentos2) {
            cargarGifEnTablero(1, true);
            cargarGifEnTablero(2, false);
        } else if (intentos2 < intentos1) {
            cargarGifEnTablero(2, true);
            cargarGifEnTablero(1, false);
        } else {
            cargarGifEnTablero(1, true);
            cargarGifEnTablero(2, true);
        }

        vista.getLblTurno().setText("¡Partida Finalizada!");
        JOptionPane.showMessageDialog(vista,
                "¡Ambos tableros completados!\nJugador 1: " + intentos1 + " intentos\nJugador 2: " + intentos2 + " intentos",
                "Fin del juego", JOptionPane.INFORMATION_MESSAGE);

        if (intentos1 <= intentos2) {
            ofrecerRegistroPuntaje(1, intentos1);
        }
        if (intentos2 <= intentos1) {
            ofrecerRegistroPuntaje(2, intentos2);
        }
        preguntarSiguientePaso();
    }

    private void cargarGifEnTablero(int numTablero, boolean esVictoria) {
        String rutaGif = esVictoria ?
                "Recursos/Cancion/Ganaste/Victoria.gif" :
                "Recursos/Cancion/Perdiste/Derrota.gif";

        URL urlGif = getClass().getClassLoader().getResource(rutaGif);
        if (urlGif != null) {
            ImageIcon iconoGif = new ImageIcon(urlGif);
            vista.mostrarGifEnTablero(numTablero, iconoGif);
        } else {
            System.err.println("No se encontró el GIF en la ruta exacta: " + rutaGif);
        }
    }

    private void actualizarVistaTablero(int numTablero) {
        if (juegoTerminado) return;

        JuegoParejasLogica logicaActiva = (numTablero == 1) ? logica1 : logica2;
        JButton[][] botones = (numTablero == 1) ? vista.getBotonesTablero1() : vista.getBotonesTablero2();
        int filas = vista.getFilas();
        int columnas = vista.getColumnas();

        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                int indice = i * columnas + j;
                JButton btn = botones[i][j];

                if (faseDeMemoria || logicaActiva.estaRevelada(indice) || estaTemporalmenteVolteada(logicaActiva, indice)) {
                    String nombreArchivo = logicaActiva.getCodigoCarta(indice);
                    ImageIcon icono = obtenerIcono(nombreArchivo, 55, 55);
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
                    btn.setFont(FUENTE_CARTA_OCULTA);
                    btn.setForeground(Color.WHITE);
                    btn.setBackground(COLOR_CARTA_OCULTA);
                }
            }
        }
    }

    private boolean estaTemporalmenteVolteada(JuegoParejasLogica logica, int indice) {
        return indice == logica.getIndicePrimeraSeleccion() || indice == logica.getIndiceSegundaSeleccion();
    }

    private ImageIcon obtenerIcono(String nombreArchivo, int ancho, int alto) {
        String clave = nombreArchivo + "@" + ancho + "x" + alto;
        if (CACHE_ICONOS.containsKey(clave)) {
            return CACHE_ICONOS.get(clave);
        }
        ImageIcon icono = cargarImagenEscalada(nombreArchivo, ancho, alto);
        CACHE_ICONOS.put(clave, icono);
        return icono;
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