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

        setContentPane(panelFondo);
    }
}