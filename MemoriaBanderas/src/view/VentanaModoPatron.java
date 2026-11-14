package view;

import controller.GestorMusica;
import javax.swing.*;
import java.awt.*;

public class VentanaModoPatron extends JFrame {

    private static final int FILAS = 6;
    private static final int COLUMNAS = 6;

    private JLabel lblTitulo;
    private JLabel lblCasillasPatron;
    private JProgressBar barraTiempoTotal;
    private JProgressBar barraTiempoEntreCasillas;
    private JButton[][] botonesCasillas;
    private JButton btnTerminarIntento;

    public VentanaModoPatron() {
        setTitle("Modo Patrones - Juegos de Memoria");
        setSize(1024, 576);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // 🎵 Cambiar a AC-DC.wav al abrir y restaurar 8 bits.wav al cerrar
        GestorMusica.reproducir("Recursos/Cancion/AC-DC.wav");
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                GestorMusica.reproducir("Recursos/Cancion/8 bits.wav");
            }
        });

        PanelFondo panelFondo = new PanelFondo("Recursos/Fondo1.png");
        panelFondo.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        lblTitulo = new JLabel("=== MODO PATRONES ===");
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setFont(new Font("Monospaced", Font.BOLD, 18));
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 0;
        panelFondo.add(lblTitulo, gbc);

        lblCasillasPatron = new JLabel("Casillas a recordar: 3");
        lblCasillasPatron.setForeground(Color.WHITE);
        lblCasillasPatron.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblCasillasPatron.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 1;
        panelFondo.add(lblCasillasPatron, gbc);

        barraTiempoTotal = new JProgressBar(0, 120);
        barraTiempoTotal.setValue(120);
        barraTiempoTotal.setStringPainted(true);
        barraTiempoTotal.setString("Tiempo total: 12 s");
        barraTiempoTotal.setFont(new Font("SansSerif", Font.BOLD, 12));
        gbc.gridy = 2;
        panelFondo.add(barraTiempoTotal, gbc);

        barraTiempoEntreCasillas = new JProgressBar(0, 20);
        barraTiempoEntreCasillas.setValue(20);
        barraTiempoEntreCasillas.setStringPainted(true);
        barraTiempoEntreCasillas.setString("Tiempo entre casillas: 2 s");
        barraTiempoEntreCasillas.setFont(new Font("SansSerif", Font.BOLD, 12));
        gbc.gridy = 3;
        panelFondo.add(barraTiempoEntreCasillas, gbc);

        JPanel panelTablero = new JPanel(new GridLayout(FILAS, COLUMNAS, 6, 6));
        panelTablero.setOpaque(false);
        botonesCasillas = ConstructorFichas.crearMatrizFichas(FILAS, COLUMNAS);

        for (int i = 0; i < FILAS; i++) {
            for (int j = 0; j < COLUMNAS; j++) {
                panelTablero.add(botonesCasillas[i][j]);
            }
        }

        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.NONE;
        panelFondo.add(panelTablero, gbc);

        btnTerminarIntento = new JButton("Terminar intento");
        btnTerminarIntento.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnTerminarIntento.setFocusPainted(false);
        btnTerminarIntento.setCursor(new Cursor(Cursor.HAND_CURSOR));
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.SOUTHWEST;
        panelFondo.add(btnTerminarIntento, gbc);

        setContentPane(panelFondo);
    }

    public void resaltarCasilla(int fila, int columna) {
        botonesCasillas[fila][columna].setBackground(Color.BLACK);
        botonesCasillas[fila][columna].setBorder(BorderFactory.createLineBorder(Color.BLACK, 4));
    }

    public void quitarResaltado(int fila, int columna) {
        botonesCasillas[fila][columna].setBackground(UIManager.getColor("Button.background"));
        botonesCasillas[fila][columna].setBorder(UIManager.getBorder("Button.border"));
    }

    public void quitarTodosLosResaltados() {
        for (int i = 0; i < FILAS; i++) {
            for (int j = 0; j < COLUMNAS; j++) {
                quitarResaltado(i, j);
            }
        }
    }

    public JLabel getLblCasillasPatron() { return lblCasillasPatron; }
    public JProgressBar getBarraTiempoTotal() { return barraTiempoTotal; }
    public JProgressBar getBarraTiempoEntreCasillas() { return barraTiempoEntreCasillas; }
    public JButton[][] getBotonesCasillas() { return botonesCasillas; }
    public JButton getBtnTerminarIntento() { return btnTerminarIntento; }
}