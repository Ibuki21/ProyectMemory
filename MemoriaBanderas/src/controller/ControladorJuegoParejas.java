package controller;

import model.GestorUsuarios;
import model.JuegoParejasLogica;
import model.Usuario;
import model.ServicioCambioDolar;
import view.JuegoParejasVisual;
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
import java.util.List;

public class ControladorJuegoParejas { // Coordina la vista, los dos modelos de tablero, timers, audio y puntajes

    private JuegoParejasVisual vista; // Referencia a la ventana del juego
    private JuegoParejasLogica logica1; // Lógica independiente para el tablero del Jugador 1
    private JuegoParejasLogica logica2; // Lógica independiente para el tablero del Jugador 2

    private static final String RUTA_PAREJAS = "Recursos/Parejas/";
    private static final String RUTA_AUDIO_CLICK = "Recursos/Cancion/Botones/Presiona1.wav";
    private static final String RUTA_MUSICA_ACDC = "Recursos/Cancion/AC-DC.wav";
    private static final String RUTA_MUSICA_MENU = "Recursos/Cancion/8 bits.wav";

    private static final Map<String, ImageIcon> CACHE_ICONOS = new HashMap<>(); // Caché para no reescalar las mismas imágenes repetidamente
    private static final Font FUENTE_CARTA_OCULTA = new Font("Segoe UI", Font.BOLD, 18);
    private static final Color COLOR_CARTA_OCULTA = new Color(30, 50, 80);

    private static final int TICK_MS = 100;
    private static final int TIEMPO_TURNO = 100;
    private static final int TIEMPO_EXTRA = 70;
    private static final int TIEMPO_MEMORIA = 50;
    private boolean bloqueandoClics = true; // Bloquea clics mientras se muestra memoria, pista o se evalúa un par
    private boolean faseDeMemoria = true; // Indica si está activa la fase inicial para memorizar cartas
    private boolean juegoTerminado = false;
    private int jugadorActual = 1; // Indica qué jugador tiene el turno activo (1 o 2)
    private int tiempoRestante;
    private int partida = 0; // Contador de partida para invalidar timers viejos tras reiniciar
    private Timer timerTurno;
    private Timer timerMemoria;

    // Banderas de control de pista única por jugador
    private boolean pistaUsada1 = false;
    private boolean pistaUsada2 = false;

    public ControladorJuegoParejas(JuegoParejasVisual vista) { // Constructor que enlaza vista y modelos e inicia la partida
        this.vista = vista;
        this.logica1 = new JuegoParejasLogica();
        this.logica2 = new JuegoParejasLogica();

        GestorMusica.reproducir(RUTA_MUSICA_ACDC);
        inicializarEventos(); // Registra los listeners de botones y casillas

        vista.addWindowListener(new WindowAdapter() { // Escucha eventos de la ventana
            @Override
            public void windowClosing(WindowEvent e) { // Se ejecuta al cerrar la ventana con la 'X'
                if (timerTurno != null) timerTurno.stop();
                if (timerMemoria != null) timerMemoria.stop();
                GestorMusica.reproducir(RUTA_MUSICA_MENU); // Restaura la música del menú principal
            }
        });

        iniciarFaseMemoria(); // Comienza el juego mostrando las cartas para memorizar
    }

    private void inicializarEventos() { // Conecta las acciones de los botones de ambos tableros y de finalizar
        int filas = vista.getFilas();
        int columnas = vista.getColumnas();

        JButton[][] botones1 = vista.getBotonesTablero1(); // Matriz de botones del tablero 1
        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                final int indice = i * columnas + j; // Convierte coordenada (i, j) a índice lineal 0..35
                botones1[i][j].addActionListener(e -> manejarClicTablero(1, indice));
            }
        }

        JButton[][] botones2 = vista.getBotonesTablero2(); // Matriz de botones del tablero 2
        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                final int indice = i * columnas + j; // Convierte coordenada (i, j) a índice lineal 0..35
                botones2[i][j].addActionListener(e -> manejarClicTablero(2, indice));
            }
        }

        // Listeners para los botones de pista
        if (vista.getBtnPista1() != null) { // Verifica que exista botón de pista del jugador 1
            vista.getBtnPista1().addActionListener(e -> usarPista(1));
        }
        if (vista.getBtnPista2() != null) {
            vista.getBtnPista2().addActionListener(e -> usarPista(2));
        }

        vista.getBtnFinalizar().addActionListener(e -> { // Acción del botón para terminar la partida manualmente
            if (juegoTerminado) return; // Evita ejecutar dos veces si ya terminó
            GestorMusica.reproducirEfecto(RUTA_AUDIO_CLICK);
            partida++;
            juegoTerminado = true;
            bloqueandoClics = true; // Bloquea interacciones con el tablero
            if (timerTurno != null) timerTurno.stop();
            if (timerMemoria != null) timerMemoria.stop();
            preguntarSiguientePaso(); // Pregunta si desea reiniciar o volver al menú
        });
    }

    private void usarPista(int numTablero) { // Revela temporalmente todas las cartas del tablero del jugador en turno
        if (juegoTerminado || bloqueandoClics || faseDeMemoria || numTablero != jugadorActual) {
            return;
        }

        if (numTablero == 1 && pistaUsada1) {
            JOptionPane.showMessageDialog(vista, "El Jugador 1 ya usó su pista en esta partida.", "Pista agotada", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (numTablero == 2 && pistaUsada2) {
            JOptionPane.showMessageDialog(vista, "El Jugador 2 ya usó su pista en esta partida.", "Pista agotada", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (numTablero == 1) { // Marca pista como usada para jugador 1
            pistaUsada1 = true;
            if (vista.getBtnPista1() != null) vista.getBtnPista1().setEnabled(false); // Deshabilita su botón de pista
        } else {
            pistaUsada2 = true;
            if (vista.getBtnPista2() != null) vista.getBtnPista2().setEnabled(false);
        }

        GestorMusica.reproducirEfecto(RUTA_AUDIO_CLICK);
        bloqueandoClics = true; // Bloquea clics mientras observa las cartas

        JuegoParejasLogica logicaActiva = logicaDe(numTablero); // Obtiene la lógica del tablero correspondiente
        JButton[][] botones = (numTablero == 1) ? vista.getBotonesTablero1() : vista.getBotonesTablero2();
        int filas = vista.getFilas();
        int columnas = vista.getColumnas();

        // Revelar temporalmente todas las cartas de este tablero
        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                int indice = i * columnas + j; // Calcula índice lineal
                JButton btn = botones[i][j]; // Toma el botón de la casilla
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
            }
        }

        if (vista.getLblTurno() != null) {
            vista.getLblTurno().setText("¡PISTA ACTIVADA! Observa las cartas...");
        }

        // Temporizador de 3 segundos para volver a ocultar las no descubiertas
        Timer timerPista = new Timer(3000, e -> {
            actualizarVistaTablero(numTablero); // Redibuja el tablero ocultando las cartas no halladas
            bloqueandoClics = false; // Habilita nuevamente los clics
            if (vista.getLblTurno() != null) {
                vista.getLblTurno().setText("Turno: Jugador " + jugadorActual);
            }
        });
        timerPista.setRepeats(false);
        timerPista.start();
    }

    private void preguntarSiguientePaso() { // Muestra diálogo al finalizar para reiniciar o salir al menú
        Object[] opciones = {"Comenzar de nuevo", "Volver al menú principal"};
        int eleccion = JOptionPane.showOptionDialog(vista,
                "¿Qué desea hacer ahora?",
                "Fin del juego",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                opciones,
                opciones[0]);

        if (eleccion == 0) { // Si eligió "Comenzar de nuevo"
            GestorMusica.reproducir(RUTA_MUSICA_ACDC);
            reiniciarPartida();
        } else {
            GestorMusica.reproducir(RUTA_MUSICA_MENU);
            vista.dispose();
        }
    }

    private void reiniciarPartida() { // Restablece la partida completa a su estado inicial
        partida++; // Incrementa identificador de partida
        if (timerTurno != null) timerTurno.stop();
        if (timerMemoria != null) timerMemoria.stop();

        juegoTerminado = false; // Marca partida como activa
        logica1.reiniciarJuego();
        logica2.reiniciarJuego();

        pistaUsada1 = false;
        pistaUsada2 = false;
        if (vista.getBtnPista1() != null) vista.getBtnPista1().setEnabled(true);
        if (vista.getBtnPista2() != null) vista.getBtnPista2().setEnabled(true);

        vista.getLblIntentos1().setText("Intentos: 0");
        vista.getLblPares1().setText("Pares: 0 / 18");
        vista.getLblIntentos2().setText("Intentos: 0");
        vista.getLblPares2().setText("Pares: 0 / 18");

        vista.restaurarGridTablero(1);
        vista.restaurarGridTablero(2);

        iniciarFaseMemoria(); // Inicia nuevamente la fase de memorización
    }

    private void ofrecerRegistroPuntaje(int jugador, int intentos) { // Pregunta y guarda puntaje y premio calculado con BCCR
        String nombreTablero = (jugador == 1) ? "Tablero 1 (Jugador 1)" : "Tablero 2 (Jugador 2)";

        int respuesta = JOptionPane.showConfirmDialog(vista, // Pide confirmación para guardar el puntaje
                nombreTablero + " ha completado el juego con " + intentos + " intentos.\n¿Desea registrar este puntaje?",
                "Registrar puntaje - " + nombreTablero, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (respuesta != JOptionPane.YES_OPTION) { // Si el usuario responde que no
            return; // No guarda nada y regresa
        }

        //  Vinculación estricta: Tablero 1 = Jugador 1 (índice 0), Tablero 2 = Jugador 2 (índice 1)
        List<Usuario> listaJugadores = GestorUsuarios.getUsuariosSesion(); // Obtiene usuarios logueados en la sesión}
        Usuario destino = null; // Perfil de usuario donde se guardará el puntaje}

        if (jugador == 1 && !listaJugadores.isEmpty()) { // Si es jugador 1 y hay al menos un usuario en sesión}
            destino = listaJugadores.get(0); // Asigna el primer jugador de sesión}
        } else if (jugador == 2 && listaJugadores.size() >= 2) { // Si es jugador 2 y hay al menos dos jugadores en sesión
            destino = listaJugadores.get(1); // Asigna el segundo jugador de sesión
        } else {
            destino = GestorUsuarios.getUsuarioSesionActual(); // Toma el usuario actual de sesión
        }

        if (destino == null) {
            JOptionPane.showMessageDialog(vista, "No se encontró el perfil correspondiente para registrar el puntaje de este tablero.",
                    "Puntaje no registrado", JOptionPane.INFORMATION_MESSAGE);
            return; // Sale sin guardar
        }

        boolean mejoro = destino.registrarIntentosParejas(intentos); // Actualiza mejor marca si hubo menos intentos

        // Cálculo de puntos basado en intentos usando la fórmula BCCR
        double premioColones = ServicioCambioDolar.calcularPremio(Math.max(intentos, 1));
        int puntosGanados = (int) Math.round(premioColones); // Redondea a entero de puntos
        if (puntosGanados <= 0) { // Si el cálculo resulta menor o igual a 0, aplica el mínimo calculado por intentos
            puntosGanados = Math.max(10, 200 - (intentos * 2)); // Mínimo 10 puntos o 200 menos 2 por intento
        }

        destino.agregarPuntos(puntosGanados); // Suma los puntos ganados al perfil del usuario
        GestorUsuarios.actualizarPuntuacionUsuario(destino);

        String mensaje = "¡Puntaje registrado para " + destino.getUsuario() + " (" + nombreTablero + ")!\n" +
                "• Intentos realizados: " + intentos + "\n" +
                "• Puntos sumados (BCCR): +" + puntosGanados;

        if (!mejoro) {
            mensaje += "\n(Su mejor marca histórica en este modo sigue siendo: " + destino.getMejorIntentosParejas() + " intentos)";
        }

        JOptionPane.showMessageDialog(vista, mensaje, "Puntaje y Premio Registrado", JOptionPane.INFORMATION_MESSAGE);
    }

    private void iniciarFaseMemoria() { // Configura y arranca los 5 segundos iniciales para memorizar las cartas
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
            if (tiempoRestante <= 0) { // Cuando se agota el tiempo de memoria
                timerMemoria.stop(); // Detiene el timer de memoria
                faseDeMemoria = false; // Finaliza fase de memoria
                bloqueandoClics = false; // Habilita los clics de juego
                actualizarVistaTablero(1);
                actualizarVistaTablero(2);
                iniciarTurno(1); // Comienza el primer turno con el Jugador 1
            }
        });
        timerMemoria.start(); // Inicia el temporizador de memoria
    }

    private void actualizarBarraTiempoMemoria() { // Actualiza la barra de progreso durante la fase de memoria
        int decimas = Math.max(tiempoRestante, 0);
        vista.getBarraTiempo().setValue(decimas);
        vista.getBarraTiempo().setString(String.format("Memorizando: %.1f s", decimas / 10.0));
    }

    private JuegoParejasLogica logicaDe(int jugador) {
        return (jugador == 1) ? logica1 : logica2;
    }

    private void iniciarTurno(int jugador) { // Prepara e inicia el temporizador para el jugador indicado
        if (juegoTerminado) return;
        jugadorActual = jugador;
        tiempoRestante = TIEMPO_TURNO;
        vista.marcarTableroActivo(jugador);
        vista.getLblTurno().setText("Turno: Jugador " + jugador);
        vista.getBarraTiempo().setMaximum(TIEMPO_TURNO);
        actualizarBarraTiempoTurno();

        if (timerTurno == null) { // Crea el timer de turno solo la primera vez
            timerTurno = new Timer(TICK_MS, e -> alPasarTiempo());
        }
        timerTurno.restart(); // Reinicia y arranca el timer de turno
    }

    private void cambiarTurno() { // Pasa el turno al otro jugador (si aún no completó su tablero)
        if (juegoTerminado) return; // No cambia turno si el juego terminó
        int siguiente = (jugadorActual == 1) ? 2 : 1; // Alterna entre jugador 1 y 2
        if (logicaDe(siguiente).haGanado()) {
            siguiente = jugadorActual;
        }
        iniciarTurno(siguiente);
    }

    private void alPasarTiempo() { // Lógica ejecutada cada 100 ms durante un turno activo
        if (bloqueandoClics || faseDeMemoria || juegoTerminado) { // Si está bloqueado o fuera de turno normal
            return; // No descuenta tiempo
        }
        tiempoRestante--; // Resta una décima de segundo
        actualizarBarraTiempoTurno();
        if (tiempoRestante <= 0) {
            logicaDe(jugadorActual).cancelarSeleccionPendiente();
            actualizarVistaTablero(jugadorActual);
            cambiarTurno();
        }
    }

    private void actualizarBarraTiempoTurno() { // Actualiza valor y texto de la barra de tiempo en el turno
        int decimas = Math.max(tiempoRestante, 0);
        if (decimas > vista.getBarraTiempo().getMaximum()) {
            vista.getBarraTiempo().setMaximum(decimas);
        }
        vista.getBarraTiempo().setValue(decimas); // Actualiza el progreso visual
        vista.getBarraTiempo().setString(String.format("Tiempo: %.1f s", decimas / 10.0)); // Muestra el tiempo restante en segundos
    }

    private void manejarClicTablero(int numTablero, int indice) { // Procesa el clic del jugador sobre una casilla
        if (juegoTerminado || bloqueandoClics || faseDeMemoria || numTablero != jugadorActual) return;

        JuegoParejasLogica logicaActiva = logicaDe(numTablero);
        if (logicaActiva.estaRevelada(indice)) return;
        GestorMusica.reproducirEfecto(RUTA_AUDIO_CLICK);

        if (!logicaActiva.seleccionarCarta(indice)) return;
        actualizarVistaTablero(numTablero);

        if (logicaActiva.getIndiceSegundaSeleccion() != -1) { // Si ya se seleccionaron dos cartas en este intento
            bloqueandoClics = true; // Bloquea nuevos clics mientras se evalúa la pareja
            final int partidaActual = partida;

            Timer timer = new Timer(750, ae -> { // Pausa de 750 ms para que el jugador vea ambas cartas
                if (partidaActual != partida || juegoTerminado) return;

                boolean acierto = logicaActiva.evaluarPareja(); // Evalúa si las dos cartas forman par
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

    private void resolverJugada(int numTablero, boolean acierto) { // Decide qué ocurre tras evaluar un intento
        if (logica1.haGanado() || logica2.haGanado()) { // Si algún jugador completó sus 18 pares
            verificarFinPartidaDual();
        } else if (acierto) {
            tiempoRestante += TIEMPO_EXTRA; // Suma tiempo extra de bonificación (7.0 s)
            actualizarBarraTiempoTurno(); // Refleja el nuevo tiempo en la barra
        } else {
            cambiarTurno();
        }
    }

    private void actualizarEstadisticas(int numTablero) { // Refresca los textos de intentos y pares en la vista
        JuegoParejasLogica logicaActiva = logicaDe(numTablero); // Obtiene lógica del jugador
        JLabel lblIntentos = (numTablero == 1) ? vista.getLblIntentos1() : vista.getLblIntentos2(); // Selecciona label de intentos
        JLabel lblPares = (numTablero == 1) ? vista.getLblPares1() : vista.getLblPares2(); // Selecciona label de pares
        lblIntentos.setText("Intentos: " + logicaActiva.getIntentos()); // Muestra intentos actuales
        lblPares.setText("Pares: " + logicaActiva.getParesEncontrados() + " / 18"); // Muestra pares hallados sobre 18
    }

    private void verificarFinPartidaDual() { // Revisa si uno o ambos tableros están completos para cerrar la partida
        boolean gano1 = logica1.haGanado();
        boolean gano2 = logica2.haGanado();

        if (gano1 || gano2) { // Si al menos uno ganó
            if (gano1 && gano2) {
                terminarJuegoCompleto();
            } else if (gano1) {
                juegoTerminado = true; // Marca fin de partida
                if (timerTurno != null) timerTurno.stop();

                cargarGifEnTablero(1, true);
                cargarGifEnTablero(2, false);

                vista.getLblTurno().setText("¡Jugador 1 completó su tablero primero!");
                JOptionPane.showMessageDialog(vista, "¡El Jugador 1 ganó al completar todas sus parejas primero!", "Fin del Juego", JOptionPane.INFORMATION_MESSAGE);
                ofrecerRegistroPuntaje(1, logica1.getIntentos());
                preguntarSiguientePaso(); // Pregunta si desea reiniciar o salir
            } else if (gano2) { // Si solo ganó el Jugador 2
                juegoTerminado = true;
                if (timerTurno != null) timerTurno.stop();

                cargarGifEnTablero(2, true);
                cargarGifEnTablero(1, false);

                vista.getLblTurno().setText("¡Jugador 2 completó su tablero primero!");
                JOptionPane.showMessageDialog(vista, "¡El Jugador 2 ganó al completar todas sus parejas primero!", "Fin del Juego", JOptionPane.INFORMATION_MESSAGE);
                ofrecerRegistroPuntaje(2, logica2.getIntentos());
                preguntarSiguientePaso();
            }
        }
    }

    private void terminarJuegoCompleto() { // Resuelve el cierre cuando ambos jugadores terminan sus tableros
        juegoTerminado = true;
        if (timerTurno != null) timerTurno.stop();

        int intentos1 = logica1.getIntentos();
        int intentos2 = logica2.getIntentos();

        if (intentos1 < intentos2) { // Jugador 1 ganó por menor cantidad de intentos
            cargarGifEnTablero(1, true);
            cargarGifEnTablero(2, false);
        } else if (intentos2 < intentos1) { // Jugador 2 ganó por menor cantidad de intentos
            cargarGifEnTablero(2, true);
            cargarGifEnTablero(1, false);
        } else { // Empate en intentos
            cargarGifEnTablero(1, true);
            cargarGifEnTablero(2, true);
        }

        vista.getLblTurno().setText("¡Partida Finalizada!");
        JOptionPane.showMessageDialog(vista, // Muestra resumen de intentos de ambos jugadores
                "¡Ambos tableros completados!\nJugador 1: " + intentos1 + " intentos\nJugador 2: " + intentos2 + " intentos",
                "Fin del juego", JOptionPane.INFORMATION_MESSAGE);

        if (intentos1 <= intentos2) { // Si jugador 1 ganó o empató
            ofrecerRegistroPuntaje(1, intentos1); // Ofrece registrar puntaje de jugador 1
        }
        if (intentos2 <= intentos1) { // Si jugador 2 ganó o empató
            ofrecerRegistroPuntaje(2, intentos2); // Ofrece registrar puntaje de jugador 2
        }
        preguntarSiguientePaso(); // Pregunta por reinicio o retorno al menú
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

    private void actualizarVistaTablero(int numTablero) { // Sincroniza los botones de la vista con el estado lógico del tablero
        if (juegoTerminado) return; // No actualiza si el juego ya terminó

        JuegoParejasLogica logicaActiva = (numTablero == 1) ? logica1 : logica2; // Elige lógica 1 o 2
        JButton[][] botones = (numTablero == 1) ? vista.getBotonesTablero1() : vista.getBotonesTablero2(); // Elige matriz de botones 1 o 2
        int filas = vista.getFilas();
        int columnas = vista.getColumnas();

        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                int indice = i * columnas + j; // Calcula índice lineal de la casilla
                JButton btn = botones[i][j]; // Toma el botón correspondiente

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
                    btn.setIcon(null); // Quita ícono visible
                    btn.setText("❓"); // Muestra símbolo de interrogación
                    btn.setFont(FUENTE_CARTA_OCULTA);
                    btn.setForeground(Color.WHITE);
                    btn.setBackground(COLOR_CARTA_OCULTA);
                }
            }
        }
    }

    private boolean estaTemporalmenteVolteada(JuegoParejasLogica logica, int indice) { // Indica si el índice corresponde a una carta seleccionada en el turno actual
        return indice == logica.getIndicePrimeraSeleccion() || indice == logica.getIndiceSegundaSeleccion();
    }

    private ImageIcon obtenerIcono(String nombreArchivo, int ancho, int alto) { // Busca el ícono en caché o lo genera y almacena
        String clave = nombreArchivo + "@" + ancho + "x" + alto; // Clave única por nombre y tamaño
        if (CACHE_ICONOS.containsKey(clave)) { // Si ya está en caché
            return CACHE_ICONOS.get(clave); // Retorna el ícono guardado
        }
        ImageIcon icono = cargarImagenEscalada(nombreArchivo, ancho, alto); // Carga y escala la imagen
        CACHE_ICONOS.put(clave, icono); // Guarda en caché para futuras consultas
        return icono;
    }

    private ImageIcon cargarImagenEscalada(String nombreArchivo, int ancho, int alto) {
        String[] extensiones = {".jpg", ".png"};
        for (String ext : extensiones) {
            String rutaCompleta = RUTA_PAREJAS + nombreArchivo + ext;
            try (InputStream is = getClass().getClassLoader().getResourceAsStream(rutaCompleta)) {
                if (is == null) {
                    continue;
                }
                BufferedImage original = ImageIO.read(is);
                if (original == null) {
                    continue;
                }

                BufferedImage escalada = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = escalada.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.drawImage(original, 0, 0, ancho, alto, null);
                g2.dispose();
                return new ImageIcon(escalada);
            } catch (Exception e) {
                // Continúa con la siguiente extensión o retorna null al final
            }
        }
        return null;
    }
}