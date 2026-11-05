package controller;
import model.JuegoParejasLogica;
import view.JuegoParejasVisual;
import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URL;
import javax.imageio.ImageIO;

public class ControladorJuegoParejas {

    private JuegoParejasVisual vista;
    private JuegoParejasLogica logica1;
    private JuegoParejasLogica logica2;

    private static final String RUTA_PAREJAS = "Recursos/Parejas/";
    private static final String RUTA_AUDIO_CLICK = "Recursos/Cancion/Botones/Presiona1.wav";

    private boolean bloqueandoClics1 = false;
    private boolean bloqueandoClics2 = false;

    public ControladorJuegoParejas(JuegoParejasVisual vista) {
        this.vista = vista;
        this.logica1 = new JuegoParejasLogica(); // Lógica independiente para el Tablero 1
        this.logica2 = new JuegoParejasLogica(); // Lógica independiente para el Tablero 2 (patrón distinto)

        inicializarEventos();
        actualizarVistaTablero(1);
        actualizarVistaTablero(2);
    }

    private void inicializarEventos() {
        int filas = vista.getFilas();
        int columnas = vista.getColumnas();

        // Eventos Tablero 1
        JButton[][] botones1 = vista.getBotonesTablero1();
        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                final int indice = i * columnas + j;
                botones1[i][j].addActionListener(e -> {
                    if (bloqueandoClics1) return;
                    manejarClicTablero(1, indice);
                });
            }
        }

        // Eventos Tablero 2
        JButton[][] botones2 = vista.getBotonesTablero2();
        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                final int indice = i * columnas + j;
                botones2[i][j].addActionListener(e -> {
                    if (bloqueandoClics2) return;
                    manejarClicTablero(2, indice);
                });
            }
        }

        // Botón Reiniciar Ambos
        vista.getBtnReiniciar().addActionListener(e -> {
            reproducirSonido(RUTA_AUDIO_CLICK);
            logica1.reiniciarJuego();
            logica2.reiniciarJuego();
            actualizarVistaTablero(1);
            actualizarVistaTablero(2);
            vista.getLblIntentos1().setText("Intentos: 0");
            vista.getLblPares1().setText("Pares: 0 / 10");
            vista.getLblIntentos2().setText("Intentos: 0");
            vista.getLblPares2().setText("Pares: 0 / 10");
        });
    }

    private void manejarClicTablero(int numTablero, int indice) {
        JuegoParejasLogica logicaActiva = (numTablero == 1) ? logica1 : logica2;
        if (logicaActiva.estaRevelada(indice)) return;

        reproducirSonido(RUTA_AUDIO_CLICK);

        if (!logicaActiva.seleccionarCarta(indice)) return;

        actualizarVistaTablero(numTablero);

        if (logicaActiva.getIndiceSegundaSeleccion() != -1) {
            if (numTablero == 1) bloqueandoClics1 = true;
            else bloqueandoClics2 = true;

            Timer timer = new Timer(750, ae -> {
                if (logicaActiva.evaluarPareja()) {
                    if (logicaActiva.haGanado()) {
                        JOptionPane.showMessageDialog(vista,
                                "¡Felicidades! Has completado el Tablero " + numTablero + " en " + logicaActiva.getIntentos() + " intentos.",
                                "¡Victoria en Tablero " + numTablero + "!", JOptionPane.INFORMATION_MESSAGE);
                    }
                } else {
                    logicaActiva.ocultarSeleccionFallida();
                }

                actualizarVistaTablero(numTablero);
                if (numTablero == 1) {
                    vista.getLblIntentos1().setText("Intentos: " + logicaActiva.getIntentos());
                    vista.getLblPares1().setText("Pares: " + logicaActiva.getParesEncontrados() + " / 10");
                    bloqueandoClics1 = false;
                } else {
                    vista.getLblIntentos2().setText("Intentos: " + logicaActiva.getIntentos());
                    vista.getLblPares2().setText("Pares: " + logicaActiva.getParesEncontrados() + " / 10");
                    bloqueandoClics2 = false;
                }
            });
            timer.setRepeats(false);
            timer.start();
        }
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

    private void reproducirSonido(String rutaAudio) {
        new Thread(() -> {
            try {
                URL urlAudio = getClass().getClassLoader().getResource(rutaAudio);
                if (urlAudio != null) {
                    AudioInputStream audioStream = AudioSystem.getAudioInputStream(urlAudio);
                    Clip clip = AudioSystem.getClip();
                    clip.open(audioStream);
                    clip.start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}