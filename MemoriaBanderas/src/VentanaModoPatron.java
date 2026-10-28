import javax.swing.*;
import java.awt.*;

public class VentanaModoPatron extends JFrame {

    // Panel con imagen de fondo (misma estructura que en VentanaMenuPrincipal)
    static class PanelFondo extends JPanel {
        private Image imagenFondo;

        public PanelFondo(String ruta) {
            imagenFondo = new ImageIcon(ruta).getImage();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(imagenFondo, 0, 0, getWidth(), getHeight(), this);
        }
    }

    private JLabel lblTitulo;
    private JLabel lblCasillasPatron;
    private JProgressBar barraTiempoTotal;
    private JProgressBar barraTiempoEntreCasillas;

    public VentanaModoPatron() {
        setTitle("Modo Patrones - Juegos de Memoria");
        setSize(640, 780);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        PanelFondo panelFondo = new PanelFondo("Recursos/Fondo.png");
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

        // Barra de tiempo total para replicar el patrón (12 segundos)
        barraTiempoTotal = new JProgressBar(0, 12);
        barraTiempoTotal.setValue(12);
        barraTiempoTotal.setStringPainted(true);
        barraTiempoTotal.setString("Tiempo total: 12 s");
        barraTiempoTotal.setFont(new Font("SansSerif", Font.BOLD, 12));
        gbc.gridy = 2;
        panelFondo.add(barraTiempoTotal, gbc);

        // Barra de tiempo entre casillas (2 segundos)
        barraTiempoEntreCasillas = new JProgressBar(0, 2);
        barraTiempoEntreCasillas.setValue(2);
        barraTiempoEntreCasillas.setStringPainted(true);
        barraTiempoEntreCasillas.setString("Tiempo entre casillas: 2 s");
        barraTiempoEntreCasillas.setFont(new Font("SansSerif", Font.BOLD, 12));
        gbc.gridy = 3;
        panelFondo.add(barraTiempoEntreCasillas, gbc);

        setContentPane(panelFondo);
    }
}