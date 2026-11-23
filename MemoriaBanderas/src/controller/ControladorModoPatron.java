package controller;

import model.GestorUsuarios;
import model.LogicaPatron;
import model.Usuario;
import model.ServicioCambioDolar;
import view.DialogoSeleccionJugador;
import view.VentanaModoPatron;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
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

    private boolean tableroHabilitado = false;

    private static final String RUTA_AUDIO_CLICK = "Recursos/Cancion/Botones/Presiona1.wav";
    private static final String RUTA_MUSICA_ACDC = "Recursos/Cancion/AC-DC.wav";
    private static final String RUTA_MUSICA_MENU = "Recursos/Cancion/8 bits.wav";
    private static final String RUTA_GIF_DERROTA = "Recursos/Cancion/Perdiste/Derrota.gif";
    private static final int RETRASO_MENSAJE_DERROTA_MS = 5000;

    public ControladorModoPatron(VentanaModoPatron vista) {
        this.vista = vista;
        this.modelo = new LogicaPatron(6, 6);

        GestorMusica.reproducir(RUTA_MUSICA_ACDC);

        seleccionarJugadorParaPartida();
        initListeners();

        vista.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                detenerTimers();
                GestorMusica.reproducir(RUTA_MUSICA_MENU);
            }
        });
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
                        if (!tableroHabilitado) return;

                        GestorMusica.reproducirEfecto(RUTA_AUDIO_CLICK);

                        vista.resaltarCasilla(f, c);
                        Timer timerApagar = new Timer(300, ev -> vista.quitarResaltado(f, c));
                        timerApagar.setRepeats(false);
                        timerApagar.start();

                        verificarSeleccion(f, c);
                    });
                }
            }
        }

        if (vista.getBtnTerminarIntento() != null) {
            vista.getBtnTerminarIntento().addActionListener(e -> {
                GestorMusica.reproducirEfecto(RUTA_AUDIO_CLICK);
                detenerTimers();
                GestorMusica.reproducir(RUTA_MUSICA_MENU);
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
                        vista.resaltarCasilla(coordenada[0], coordenada[1]);
                        GestorMusica.reproducirEfecto(RUTA_AUDIO_CLICK);
                    }
                    pasoActualMostrar++;
                } else {
                    timerMostrarPatron.stop();
                    vista.quitarTodosLosResaltados();
                    bloquearTablero(false);
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
            bloquearTablero(true);
            mostrarGifDerrota();

            Timer timerMensaje = new Timer(RETRASO_MENSAJE_DERROTA_MS, ev -> {
                JOptionPane.showMessageDialog(vista, "¡Fallaste el patrón!", "Game Over", JOptionPane.ERROR_MESSAGE);
                finalizarJuego();
            });
            timerMensaje.setRepeats(false);
            timerMensaje.start();
        }
    }

    private void mostrarGifDerrota() {
        URL urlGif = getClass().getClassLoader().getResource(RUTA_GIF_DERROTA);
        if (urlGif != null && vista != null) {
            vista.mostrarGifDerrota(new ImageIcon(urlGif));
        } else {
            System.err.println("No se encontró el GIF en la ruta: " + RUTA_GIF_DERROTA);
        }
    }

    private void finalizarJuego() {
        detenerTimers();
        GestorMusica.reproducir(RUTA_MUSICA_MENU);

        if (jugadorActivo != null && modelo != null) {
            int puntuacionObtenida = modelo.getPuntuacion();
            int puntajeParaFormula = Math.max(puntuacionObtenida, 1);

            // Aplicar fórmula del BCCR
            double premioColones = ServicioCambioDolar.calcularPremio(puntajeParaFormula);
            int puntosRedondeados = (int) Math.round(premioColones);
            if (puntosRedondeados <= 0) {
                puntosRedondeados = 50; // Puntos mínimos por participar
            }

            // Sumar puntos EXCLUSIVAMENTE al jugador activo de esta partida
            jugadorActivo.agregarPuntos(puntosRedondeados);
            if (puntosRedondeados > jugadorActivo.getPuntuacionMaxima()) {
                jugadorActivo.setPuntuacionMaxima(puntosRedondeados);
            }

            GestorUsuarios.actualizarPuntuacionUsuario(jugadorActivo);

            JOptionPane.showMessageDialog(vista,
                    "¡Partida de Patrón finalizada para " + jugadorActivo.getUsuario() + "!\n" +
                            "Puntos sumados a su cuenta: +" + puntosRedondeados,
                    "Puntaje Registrado", JOptionPane.INFORMATION_MESSAGE);
        }
        if (vista != null) {
            vista.dispose();
        }
    }

    private void bloquearTablero(boolean bloquear) {
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