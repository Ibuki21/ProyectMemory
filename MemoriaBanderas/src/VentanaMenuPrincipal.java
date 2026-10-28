import javax.swing.*;
import java.awt.*;

public class VentanaMenuPrincipal extends JFrame {

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

    private JButton btnLogin;
    private JButton btnBandera;
    private JButton btnPatron;

    public VentanaMenuPrincipal() {
        setTitle("Menú Principal - Juegos de Memoria");
        setSize(480, 540);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        PanelFondo panelFondo = new PanelFondo("Recursos/Fondo.png");
        panelFondo.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblImagen = new JLabel();
        ImageIcon iconoOriginal = new ImageIcon("Recursos/LogoMenu.png");
        Image imagenEscalada = iconoOriginal.getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH);
        lblImagen.setIcon(new ImageIcon(imagenEscalada));
        lblImagen.setHorizontalAlignment(SwingConstants.CENTER);

        gbc.gridy = 0;
        panelFondo.add(lblImagen, gbc);

        JLabel lblTitulo = new JLabel("=== MENÚ PRINCIPAL ===");
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setFont(new Font("Monospaced", Font.BOLD, 18));
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 1;
        panelFondo.add(lblTitulo, gbc);

        btnLogin = new JButton("🔐 Iniciar Sesión (Login)");
        btnBandera = new JButton("🚩 Encuentra la Bandera");
        btnPatron = new JButton("🧩 Siguiendo el Patrón");

        Font fuenteBotones = new Font("SansSerif", Font.BOLD, 14);
        btnLogin.setFont(fuenteBotones);
        btnBandera.setFont(fuenteBotones);
        btnPatron.setFont(fuenteBotones);

        gbc.gridy = 2;
        panelFondo.add(btnLogin, gbc);

        gbc.gridy = 3;
        panelFondo.add(btnBandera, gbc);

        gbc.gridy = 4;
        panelFondo.add(btnPatron, gbc);

        setContentPane(panelFondo);

        // --- Evento para abrir la ventana de Login ---
        btnLogin.addActionListener(e -> {
            VentanaLogin loginDialog = new VentanaLogin(this);
            loginDialog.setVisible(true);
        });
    }

    public JButton getBtnLogin() {
        return btnLogin;
    }

    public JButton getBtnBandera() {
        return btnBandera;
    }

    public JButton getBtnPatron() {
        return btnPatron;
    }
}