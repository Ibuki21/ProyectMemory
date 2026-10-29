import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class JuegoParejasVisual extends JFrame {

    public JuegoParejasVisual() {
        setTitle("Encuentra la Pareja - Banderas");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Cambiado a DISPOSE para no cerrar toda la app al salir del juego
        setSize(700, 620);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(new Color(30, 60, 110));
        setLayout(new BorderLayout());

        JLabel lblTitulo = new JLabel("ENCUENTRA LA PAREJA", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(15, 0, 10, 0));

        JLabel lblIntentos = new JLabel("Intentos: 0", SwingConstants.CENTER);
        lblIntentos.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblIntentos.setForeground(new Color(200, 220, 255));

        JLabel lblPares = new JLabel("Pares encontrados: 0 / 10", SwingConstants.CENTER);
        lblPares.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblPares.setForeground(new Color(200, 220, 255));

        JPanel panelInfo = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 5));
        panelInfo.setOpaque(false);
        panelInfo.add(lblIntentos);
        panelInfo.add(lblPares);

        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.setOpaque(false);
        panelSuperior.add(lblTitulo, BorderLayout.NORTH);
        panelSuperior.add(panelInfo, BorderLayout.CENTER);
        add(panelSuperior, BorderLayout.NORTH);

        JPanel panelCartas = new JPanel(new GridLayout(4, 5, 12, 12));
        panelCartas.setBackground(new Color(30, 60, 110));
        panelCartas.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));

        BufferedImage img = new BufferedImage(100, 70, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        GradientPaint gp = new GradientPaint(0, 0, new Color(40, 80, 160), 0, 70, new Color(20, 50, 110));
        g.setPaint(gp);
        g.fillRect(0, 0, 100, 70);
        g.setColor(new Color(150, 190, 255));
        g.setStroke(new BasicStroke(3));
        g.drawRect(2, 2, 95, 65);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Segoe UI", Font.BOLD, 36));
        FontMetrics fm = g.getFontMetrics();

        // Líneas corregidas y unidas correctamente:
        int x = (100 - fm.stringWidth("?")) / 2;
        int y = (70 - fm.getHeight()) / 2 + fm.getAscent();

        g.drawString("?", x, y);
        g.dispose();
        ImageIcon dorso = new ImageIcon(img);

        for (int i = 0; i < 20; i++) {
            JButton carta = new JButton();
            carta.setPreferredSize(new Dimension(100, 70));
            carta.setIcon(dorso);
            carta.setBackground(new Color(20, 40, 80));
            carta.setBorder(BorderFactory.createLineBorder(new Color(100, 150, 220), 2));
            carta.setFocusPainted(false);
            carta.setCursor(new Cursor(Cursor.HAND_CURSOR));
            panelCartas.add(carta);
        }

        add(panelCartas, BorderLayout.CENTER);

        JButton btnReiniciar = new JButton("Reiniciar");
        btnReiniciar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnReiniciar.setBackground(new Color(70, 130, 180));
        btnReiniciar.setForeground(Color.WHITE);
        btnReiniciar.setFocusPainted(false);
        btnReiniciar.setPreferredSize(new Dimension(120, 35));

        JButton btnSalir = new JButton("Salir");
        btnSalir.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSalir.setBackground(new Color(180, 70, 70));
        btnSalir.setForeground(Color.WHITE);
        btnSalir.setFocusPainted(false);
        btnSalir.setPreferredSize(new Dimension(120, 35));

        // Evento para el botón Salir de la ventana del juego
        btnSalir.addActionListener(e -> dispose());

        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 15));
        panelInferior.setOpaque(false);
        panelInferior.add(btnReiniciar);
        panelInferior.add(btnSalir);
        add(panelInferior, BorderLayout.SOUTH);
    }
}