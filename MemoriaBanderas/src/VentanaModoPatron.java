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

        setContentPane(panelFondo);
    }
}