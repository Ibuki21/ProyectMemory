package view;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class VentanaModoPatron extends JFrame {

    // Panel con imagen de fondo corregido para carga segura
    static class PanelFondo extends JPanel {
        private Image imagenFondo;

        public PanelFondo(String ruta) {
            URL urlImagen = getClass().getClassLoader().getResource(ruta);
            if (urlImagen != null) {
                imagenFondo = new ImageIcon(urlImagen).getImage();
            } else {
                URL urlAlternativa = getClass().getResource("/" + ruta);
                if (urlAlternativa != null) {
                    imagenFondo = new ImageIcon(urlAlternativa).getImage();
                } else {
                    System.err.println("No se pudo cargar la imagen de fondo: " + ruta);
                }
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (imagenFondo != null) {
                g.drawImage(imagenFondo, 0, 0, getWidth(), getHeight(), this);
            }
        }
    }

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

        PanelFondo panelFondo = new PanelFondo("Recursos/Fondo1.png");
        panelFondo.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Título
        lblTitulo = new JLabel("=== MODO PATRONES ===");
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setFont(new Font("Monospaced", Font.BOLD, 18));
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 0;
        panelFondo.add(lblTitulo, gbc);

        // Cantidad de casillas del patrón actual (inicia en 3)
        lblCasillasPatron = new JLabel("Casillas a recordar: 3");
        lblCasillasPatron.setForeground(Color.WHITE);
        lblCasillasPatron.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblCasillasPatron.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 1;
        panelFondo.add(lblCasillasPatron, gbc);

        // Barra de tiempo total para replicar el patrón (12 segundos = 120 décimas)
        barraTiempoTotal = new JProgressBar(0, 120);
        barraTiempoTotal.setValue(120);
        barraTiempoTotal.setStringPainted(true);
        barraTiempoTotal.setString("Tiempo total: 12 s");
        barraTiempoTotal.setFont(new Font("SansSerif", Font.BOLD, 12));
        gbc.gridy = 2;
        panelFondo.add(barraTiempoTotal, gbc);

        // Barra de tiempo entre casillas (2 segundos = 20 décimas)
        barraTiempoEntreCasillas = new JProgressBar(0, 20);
        barraTiempoEntreCasillas.setValue(20);
        barraTiempoEntreCasillas.setStringPainted(true);
        barraTiempoEntreCasillas.setString("Tiempo entre casillas: 2 s");
        barraTiempoEntreCasillas.setFont(new Font("SansSerif", Font.BOLD, 12));
        gbc.gridy = 3;
        panelFondo.add(barraTiempoEntreCasillas, gbc);

        // Matriz de casillas 6x6 (las fichas las construye ConstructorFichas)
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

        // Botón para terminar el intento en cualquier momento (a la derecha del tablero)
        btnTerminarIntento = new JButton("Terminar intento");
        btnTerminarIntento.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnTerminarIntento.setFocusPainted(false);
        btnTerminarIntento.setCursor(new Cursor(Cursor.HAND_CURSOR));
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.SOUTHWEST;
        panelFondo.add(btnTerminarIntento, gbc);

        setContentPane(panelFondo);
    }

    // Resalta una casilla (la usa el controlador al mostrar el patrón y al hacer clic)
    public void resaltarCasilla(int fila, int columna) {
        botonesCasillas[fila][columna].setBackground(Color.BLACK);
        botonesCasillas[fila][columna].setBorder(BorderFactory.createLineBorder(Color.BLACK, 4));
    }

    // Devuelve una casilla a su aspecto normal
    public void quitarResaltado(int fila, int columna) {
        botonesCasillas[fila][columna].setBackground(UIManager.getColor("Button.background"));
        botonesCasillas[fila][columna].setBorder(UIManager.getBorder("Button.border"));
    }

    // Devuelve todas las casillas a su aspecto normal
    public void quitarTodosLosResaltados() {
        for (int i = 0; i < FILAS; i++) {
            for (int j = 0; j < COLUMNAS; j++) {
                quitarResaltado(i, j);
            }
        }
    }

    public JLabel getLblCasillasPatron() {
        return lblCasillasPatron;
    }

    public JProgressBar getBarraTiempoTotal() {
        return barraTiempoTotal;
    }

    public JProgressBar getBarraTiempoEntreCasillas() {
        return barraTiempoEntreCasillas;
    }

    public JButton[][] getBotonesCasillas() {
        return botonesCasillas;
    }

    public JButton getBtnTerminarIntento() {
        return btnTerminarIntento;
    }
}