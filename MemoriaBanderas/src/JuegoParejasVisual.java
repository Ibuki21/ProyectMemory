import javax.swing.*;
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

    private static final int FILAS = 4;
    private static final int COLUMNAS = 5;

    // Componentes Tablero 1
    private JButton[][] botonesTablero1;
    private JLabel lblIntentos1, lblPares1;

    // Componentes Tablero 2
    private JButton[][] botonesTablero2;
    private JLabel lblIntentos2, lblPares2;

    private JButton btnReiniciar;

    public JuegoParejasVisual() {
        setTitle("Doble Desafío: Encuentra la Pareja - Juegos de Memoria");
        setSize(1280, 720); // Ventana más ancha para albergar los dos tableros cómodamente
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        PanelFondo panelFondo = new PanelFondo("Recursos/Fondo1.jpg");
        panelFondo.setLayout(new BorderLayout(15, 15));
        panelFondo.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // --- Título Principal ---
        JLabel lblTitulo = new JLabel("MODO DOBLE TABLERO: ENCUENTRA LAS PAREJAS", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitulo.setForeground(Color.WHITE);
        panelFondo.add(lblTitulo, BorderLayout.NORTH);

        // --- Panel Central con los Dos Tableros Lado a Lado ---
        JPanel panelContenedorTableros = new JPanel(new GridLayout(1, 2, 25, 0));
        panelContenedorTableros.setOpaque(false);

        // Crear Tablero 1
        JPanel panelPanel1 = crearPanelSubTablero("Tablero 1", true);
        panelContenedorTableros.add(panelPanel1);

        // Crear Tablero 2
        JPanel panelPanel2 = crearPanelSubTablero("Tablero 2", false);
        panelContenedorTableros.add(panelPanel2);

        panelFondo.add(panelContenedorTableros, BorderLayout.CENTER);

        // --- Panel Inferior (Botón Reiniciar) ---
        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelInferior.setOpaque(false);
        btnReiniciar = new JButton("Reiniciar Ambos Tableros");
        btnReiniciar.setFont(new Font("Segoe UI", Font.BOLD, 15));
        panelInferior.add(btnReiniciar);
        panelFondo.add(panelInferior, BorderLayout.SOUTH);

        setContentPane(panelFondo);

        // Inicializamos el controlador para manejar ambos tableros
        new ControladorJuegoParejas(this);
    }

    private JPanel crearPanelSubTablero(String nombreTablero, boolean esTableroUno) {
        JPanel panelSub = new JPanel(new BorderLayout(5, 5));
        panelSub.setOpaque(false);
        panelSub.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(255, 215, 0), 2),
                " " + nombreTablero + " ",
                0, 0, new Font("Segoe UI", Font.BOLD, 15), Color.WHITE));

        // Estadísticas del subtablero
        JPanel panelStats = new JPanel(new GridLayout(1, 2));
        panelStats.setOpaque(false);
        JLabel lblIntentos = new JLabel("Intentos: 0", SwingConstants.CENTER);
        lblIntentos.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblIntentos.setForeground(new Color(220, 235, 255));

        JLabel lblPares = new JLabel("Pares: 0 / 10", SwingConstants.CENTER);
        lblPares.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblPares.setForeground(new Color(220, 235, 255));

        panelStats.add(lblIntentos);
        panelStats.add(lblPares);
        panelSub.add(panelStats, BorderLayout.NORTH);

        // Cuadrícula de botones (4x5)
        JPanel panelGrid = new JPanel(new GridLayout(FILAS, COLUMNAS, 8, 8));
        panelGrid.setOpaque(false);

        JButton[][] botones = new JButton[FILAS][COLUMNAS];
        for (int i = 0; i < FILAS; i++) {
            for (int j = 0; j < COLUMNAS; j++) {
                JButton btn = new JButton();
                btn.setPreferredSize(new Dimension(105, 95));
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
        } else {
            botonesTablero2 = botones;
            lblIntentos2 = lblIntentos;
            lblPares2 = lblPares;
        }

        return panelSub;
    }

    // --- Getters ---
    public JButton[][] getBotonesTablero1() { return botonesTablero1; }
    public JButton[][] getBotonesTablero2() { return botonesTablero2; }
    public JLabel getLblIntentos1() { return lblIntentos1; }
    public JLabel getLblPares1() { return lblPares1; }
    public JLabel getLblIntentos2() { return lblIntentos2; }
    public JLabel getLblPares2() { return lblPares2; }
    public JButton getBtnReiniciar() { return btnReiniciar; }
    public int getFilas() { return FILAS; }
    public int getColumnas() { return COLUMNAS; }
}