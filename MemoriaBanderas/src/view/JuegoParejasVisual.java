package view;

import controller.ControladorJuegoParejas;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.net.URL;

public class JuegoParejasVisual extends JFrame {

    static class PanelFondo extends JPanel {
        private Image imagenFondo;

        public PanelFondo(String ruta) {
            URL urlImagen = getClass().getClassLoader().getResource(ruta);
            if (urlImagen != null) {
                imagenFondo = new ImageIcon(urlImagen).getImage();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (imagenFondo != null) {
                g.drawImage(imagenFondo, 0, 0, getWidth(), getHeight(), this);
            } else {
                g.setColor(new Color(30, 60, 110));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        }
    }

    private static final int FILAS = 6;
    private static final int COLUMNAS = 6;

    // Componentes Tablero 1
    private JButton[][] botonesTablero1;
    private JLabel lblIntentos1, lblPares1;
    private JPanel panelSubTablero1;
    private JPanel panelGrid1;

    // Componentes Tablero 2
    private JButton[][] botonesTablero2;
    private JLabel lblIntentos2, lblPares2;
    private JPanel panelSubTablero2;
    private JPanel panelGrid2;

    // Turno y tiempo de la jugada
    private JLabel lblTurno;
    private JProgressBar barraTiempo;

    private JButton btnReiniciar;

    public JuegoParejasVisual() {
        setTitle("Doble Desafío: Encuentra la Pareja (Modo Clásico 6x6)");
        setSize(1280, 768);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        PanelFondo panelFondo = new PanelFondo("Recursos/Fondo1.jpg");
        panelFondo.setLayout(new BorderLayout(10, 10));
        panelFondo.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // --- Parte superior ---
        JPanel panelSuperior = new JPanel(new GridLayout(3, 1, 0, 4));
        panelSuperior.setOpaque(false);

        JLabel lblTitulo = new JLabel("MODO CLÁSICO: DOBLE TABLERO 6x6 (18 PARES)", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitulo.setForeground(Color.WHITE);
        panelSuperior.add(lblTitulo);

        lblTurno = new JLabel("Turno: Jugador 1", SwingConstants.CENTER);
        lblTurno.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTurno.setForeground(new Color(255, 215, 0));
        panelSuperior.add(lblTurno);

        barraTiempo = new JProgressBar(0, 100);
        barraTiempo.setValue(100);
        barraTiempo.setStringPainted(true);
        barraTiempo.setString("Tiempo: 10.0 s");
        barraTiempo.setFont(new Font("SansSerif", Font.BOLD, 12));
        barraTiempo.setPreferredSize(new Dimension(500, 20));
        JPanel panelBarra = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        panelBarra.setOpaque(false);
        panelBarra.add(barraTiempo);
        panelSuperior.add(panelBarra);

        panelFondo.add(panelSuperior, BorderLayout.NORTH);

        // --- Panel Central con los Dos Tableros Lado a Lado ---
        JPanel panelContenedorTableros = new JPanel(new GridLayout(1, 2, 20, 0));
        panelContenedorTableros.setOpaque(false);

        JPanel panelPanel1 = crearPanelSubTablero("Jugador 1", true);
        panelContenedorTableros.add(panelPanel1);

        JPanel panelPanel2 = crearPanelSubTablero("Jugador 2", false);
        panelContenedorTableros.add(panelPanel2);

        panelFondo.add(panelContenedorTableros, BorderLayout.CENTER);

        // --- Panel Inferior ---
        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelInferior.setOpaque(false);
        btnReiniciar = new JButton("Reiniciar Ambos Tableros");
        btnReiniciar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panelInferior.add(btnReiniciar);
        panelFondo.add(panelInferior, BorderLayout.SOUTH);

        setContentPane(panelFondo);
        new ControladorJuegoParejas(this);
    }

    private Border crearBordeTablero(String nombreTablero, boolean activo) {
        Color colorLinea = activo ? new Color(255, 215, 0) : new Color(120, 120, 120);
        return BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(colorLinea, 3),
                " " + nombreTablero + " ",
                0, 0, new Font("Segoe UI", Font.BOLD, 14), Color.WHITE);
    }

    public void marcarTableroActivo(int numTablero) {
        panelSubTablero1.setBorder(crearBordeTablero("Jugador 1", numTablero == 1));
        panelSubTablero2.setBorder(crearBordeTablero("Jugador 2", numTablero == 2));
    }

    private JPanel crearPanelSubTablero(String nombreTablero, boolean esTableroUno) {
        JPanel panelSub = new JPanel(new BorderLayout(4, 4));
        panelSub.setOpaque(false);
        panelSub.setBorder(crearBordeTablero(nombreTablero, esTableroUno));

        JPanel panelStats = new JPanel(new GridLayout(1, 2));
        panelStats.setOpaque(false);
        JLabel lblIntentos = new JLabel("Intentos: 0", SwingConstants.CENTER);
        lblIntentos.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblIntentos.setForeground(new Color(220, 235, 255));

        JLabel lblPares = new JLabel("Pares: 0 / 18", SwingConstants.CENTER);
        lblPares.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblPares.setForeground(new Color(220, 235, 255));

        panelStats.add(lblIntentos);
        panelStats.add(lblPares);
        panelSub.add(panelStats, BorderLayout.NORTH);

        // Matriz 6x6 adaptada al espacio visual
        JPanel panelGrid = new JPanel(new GridLayout(FILAS, COLUMNAS, 5, 5));
        panelGrid.setOpaque(false);

        JButton[][] botones = new JButton[FILAS][COLUMNAS];
        for (int i = 0; i < FILAS; i++) {
            for (int j = 0; j < COLUMNAS; j++) {
                JButton btn = new JButton();
                btn.setPreferredSize(new Dimension(55, 55));
                btn.setFocusPainted(false);
                btn.setBackground(new Color(30, 50, 80));
                btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
                botones[i][j] = btn;
                panelGrid.add(btn);
            }
        }
        panelSub.add(panelGrid, BorderLayout.CENTER);

        if (esTableroUno) {
            botonesTablero1 = botones;
            lblIntentos1 = lblIntentos;
            lblPares1 = lblPares;
            panelSubTablero1 = panelSub;
            panelGrid1 = panelGrid;
        } else {
            botonesTablero2 = botones;
            lblIntentos2 = lblIntentos;
            lblPares2 = lblPares;
            panelSubTablero2 = panelSub;
            panelGrid2 = panelGrid;
        }

        return panelSub;
    }

    public void mostrarGifEnTablero(int numTablero, ImageIcon gifIcono) {
        JPanel panelGrid = (numTablero == 1) ? panelGrid1 : panelGrid2;
        panelGrid.removeAll();
        panelGrid.setLayout(new BorderLayout());
        JLabel lblGif = new JLabel(gifIcono, SwingConstants.CENTER);
        lblGif.setOpaque(false);
        panelGrid.add(lblGif, BorderLayout.CENTER);
        panelGrid.revalidate();
        panelGrid.repaint();
    }

    public void restaurarGridTablero(int numTablero) {
        JPanel panelGrid = (numTablero == 1) ? panelGrid1 : panelGrid2;
        panelGrid.removeAll();
        panelGrid.setLayout(new GridLayout(FILAS, COLUMNAS, 5, 5));
        JButton[][] botones = (numTablero == 1) ? botonesTablero1 : botonesTablero2;
        for (int i = 0; i < FILAS; i++) {
            for (int j = 0; j < COLUMNAS; j++) {
                panelGrid.add(botones[i][j]);
            }
        }
        panelGrid.revalidate();
        panelGrid.repaint();
    }

    public JButton[][] getBotonesTablero1() { return botonesTablero1; }
    public JButton[][] getBotonesTablero2() { return botonesTablero2; }
    public JLabel getLblIntentos1() { return lblIntentos1; }
    public JLabel getLblPares1() { return lblPares1; }
    public JLabel getLblIntentos2() { return lblIntentos2; }
    public JLabel getLblPares2() { return lblPares2; }
    public JLabel getLblTurno() { return lblTurno; }
    public JProgressBar getBarraTiempo() { return barraTiempo; }
    public JButton getBtnReiniciar() { return btnReiniciar; }
    public int getFilas() { return FILAS; }
    public int getColumnas() { return COLUMNAS; }
}