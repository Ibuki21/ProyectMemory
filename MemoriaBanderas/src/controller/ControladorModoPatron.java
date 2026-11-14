package controller;

import model.GestorUsuarios;
import model.LogicaPatron;
import model.Usuario;
import view.DialogoSeleccionJugador;
import view.VentanaModoPatron;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class ControladorModoPatron {

    private VentanaModoPatron vista;
    private LogicaPatron modelo;
    private Usuario jugadorActivo;

    private Timer timerMostrarPatron;
    private Timer timerTotal;
    private Timer timerEntreCasillas;

    private int pasoActualMostrar = 0;
    private int segundosRestantesTotal = 120;
    private int segundosRestantesCasilla = 20;

    // Control de clics para evitar que se pongan grises al bloquear
    private boolean tableroHabilitado = false;

    private static final String RUTA_AUDIO_CLICK = "Recursos/Cancion/Botones/Presiona1.wav";

    public ControladorModoPatron(VentanaModoPatron vista) {
        this.vista = vista;
        this.modelo = new LogicaPatron(6, 6);

        // Evaluar jugadores para mostrar la selección si hay 2 cuentas activas
        seleccionarJugadorParaPartida();

        initListeners();
    }

    private void seleccionarJugadorParaPartida() {
        List<Usuario> listaJugadores = GestorUsuarios.getUsuariosSesion();

        if (listaJugadores.size() >= 2) {
            DialogoSeleccionJugador dialogo = new DialogoSeleccionJugador(vista, listaJugadores);
            dialogo.setVisible(true);

            jugadorActivo = dialogo.getJugadorSeleccionado();
            if (jugadorActivo == null) {
                jugadorActivo = listaJugadores.get(0);
            }
        } else if (listaJugadores.size() == 1) {
            jugadorActivo = listaJugadores.get(0);
        } else {
            jugadorActivo = GestorUsuarios.getUsuarioSesionActual();
        }
    }

    public void iniciar() {
        if (modelo != null && vista != null) {
            modelo.reiniciarRonda();
            actualizarEtiquetas();
            bloquearTablero(true);
            reproducirPatron();
        }
    }

    private void initListeners() {
        if (vista == null || vista.getBotonesCasillas() == null) return;

        JButton[][] botones = vista.getBotonesCasillas();
        for (int i = 0; i < botones.length; i++) {
            for (int j = 0; j < botones[i].length; j++) {
                final int f = i;
                final int c = j;
                if (botones[i][j] != null) {
                    botones[i][j].addActionListener(e -> {
                        // Solo permite hacer clic si el tablero está habilitado para el jugador
                        if (!tableroHabilitado) return;
                        ReproductorSonido.reproducirEfecto(RUTA_AUDIO_CLICK);
                        verificarSeleccion(f, c);
                    });
                }
            }
        }

        if (vista.getBtnTerminarIntento() != null) {
            vista.getBtnTerminarIntento().addActionListener(e -> {
                ReproductorSonido.reproducirEfecto(RUTA_AUDIO_CLICK);
                detenerTimers();
                vista.dispose();
            });
        }
    }

    private void reproducirPatron() {
        if (vista == null || modelo == null) return;

        vista.quitarTodosLosResaltados();
        pasoActualMostrar = 0;

        List<int[]> secuencia = modelo.getSecuencia();

        if (secuencia == null || secuencia.isEmpty()) {
            bloquearTablero(false);
            iniciarTimersJuego();
            return;
        }

        final List<int[]> secuenciaFinal = secuencia;
        timerMostrarPatron = new Timer(800, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                vista.quitarTodosLosResaltados();
                if (pasoActualMostrar < secuenciaFinal.size()) {
                    int[] coordenada = secuenciaFinal.get(pasoActualMostrar);
                    if (coordenada != null && coordenada.length >= 2) {
                        // 1. Mostrar la lucecita negra / resaltado en la vista
                        vista.resaltarCasilla(coordenada[0], coordenada[1]);
                        // 2. Reproducir el sonido de presionar botón en cada paso del patrón
                        ReproductorSonido.reproducirEfecto(RUTA_AUDIO_CLICK);
                    }
                    pasoActualMostrar++;
                } else {
                    timerMostrarPatron.stop();
                    vista.quitarTodosLosResaltados();
                    bloquearTablero(false); // Habilita el tablero manteniendo los colores reales
                    iniciarTimersJuego();
                }
            }
        });
        timerMostrarPatron.start();
    }

    private void iniciarTimersJuego() {
        segundosRestantesTotal = 120;
        segundosRestantesCasilla = 20;

        timerTotal = new Timer(1000, e -> {
            segundosRestantesTotal--;
            if (vista != null && vista.getBarraTiempoTotal() != null) {
                vista.getBarraTiempoTotal().setValue(segundosRestantesTotal);
                vista.getBarraTiempoTotal().setString("Tiempo total: " + (segundosRestantesTotal / 10) + " s");
            }
            if (segundosRestantesTotal <= 0) {
                finalizarJuego();
            }
        });
        timerTotal.start();

        timerEntreCasillas = new Timer(1000, e -> {
            segundosRestantesCasilla--;
            if (vista != null && vista.getBarraTiempoEntreCasillas() != null) {
                vista.getBarraTiempoEntreCasillas().setValue(segundosRestantesCasilla);
                vista.getBarraTiempoEntreCasillas().setString("Tiempo casilla: " + segundosRestantesCasilla + " s");
            }
            if (segundosRestantesCasilla <= 0) {
                finalizarJuego();
            }
        });
        timerEntreCasillas.start();
    }

    private void verificarSeleccion(int f, int c) {
        if (modelo == null || vista == null) return;

        boolean acierto = modelo.verificarPaso(f, c);
        if (acierto) {
            segundosRestantesCasilla = 20;
            if (modelo.esRondaCompletada()) {
                detenerTimers();
                JOptionPane.showMessageDialog(vista, "¡Patrón completado con éxito!", "Acierto", JOptionPane.INFORMATION_MESSAGE);
                modelo.siguienteNivel();
                iniciar();
            }
        } else {
            detenerTimers();
            JOptionPane.showMessageDialog(vista, "¡Fallaste el patrón!", "Game Over", JOptionPane.ERROR_MESSAGE);
            finalizarJuego();
        }
    }

    private void finalizarJuego() {
        detenerTimers();

        if (jugadorActivo != null && modelo != null) {
            int puntuacionObtenida = modelo.getPuntuacion();
            if (puntuacionObtenida > jugadorActivo.getPuntuacionMaxima()) {
                jugadorActivo.setPuntuacionMaxima(puntuacionObtenida);
                GestorUsuarios.guardarUsuarios();
            }
        }
        if (vista != null) {
            vista.dispose();
        }
    }

    private void bloquearTablero(boolean bloquear) {
        // En lugar de usar setEnabled(false) que pone los botones grises,
        // usamos una bandera booleana para conservar los colores vivos de las banderas.
        tableroHabilitado = !bloquear;
    }

    private void detenerTimers() {
        if (timerMostrarPatron != null) timerMostrarPatron.stop();
        if (timerTotal != null) timerTotal.stop();
        if (timerEntreCasillas != null) timerEntreCasillas.stop();
    }

    private void actualizarEtiquetas() {
        if (vista != null && vista.getLblCasillasPatron() != null && modelo != null) {
            vista.getLblCasillasPatron().setText("Casillas a recordar: " + modelo.getCantidadCasillas());
        }
    }
}