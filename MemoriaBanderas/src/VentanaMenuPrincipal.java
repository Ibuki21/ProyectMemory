import javax.swing.*;
import java.awt.*;

public class VentanaMenuPrincipal extends JFrame {

    // Clase interna estática para mantener tu fondo de imagen personalizado
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

    // Declaración de los botones del menú principal
    private JButton btnLogin;
    private JButton btnBandera;
    private JButton btnPatron;

    public VentanaMenuPrincipal() {
        setTitle("Menú Principal - Juegos de Memoria");
        setSize(480, 540); // Ampliamos ligeramente el alto de la ventana para acomodar la imagen
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Creamos el panel con la imagen de fondo
        PanelFondo panelFondo = new PanelFondo("Recursos/Fondo.png");
        panelFondo.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Espaciado equilibrado entre componentes
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // --- NUEVO: Integración de la imagen PNG (Logo o Decoración) ---
        JLabel lblImagen = new JLabel();
        // Reemplaza "Recursos/LogoMenu.png" con la ruta exacta de tu archivo PNG
        ImageIcon iconoOriginal = new ImageIcon("Recursos/LogoMenu.png");

        // Reescalamos la imagen con alta calidad (por ejemplo, a 120x120 píxeles) para que no sea demasiado grande
        Image imagenEscalada = iconoOriginal.getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH);
        lblImagen.setIcon(new ImageIcon(imagenEscalada));
        lblImagen.setHorizontalAlignment(SwingConstants.CENTER); // Centramos la imagen

        gbc.gridy = 0;
        panelFondo.add(lblImagen, gbc);

        // Título del menú
        JLabel lblTitulo = new JLabel("=== MENÚ PRINCIPAL ===");
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setFont(new Font("Monospaced", Font.BOLD, 18));
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 1;
        panelFondo.add(lblTitulo, gbc);

        // Inicialización de los botones
        btnLogin = new JButton("🔐 Iniciar Sesión (Login)");
        btnBandera = new JButton("🚩 Encuentra la Bandera");
        btnPatron = new JButton("🧩 Siguiendo el Patrón");

        Font fuenteBotones = new Font("SansSerif", Font.BOLD, 14);
        btnLogin.setFont(fuenteBotones);
        btnBandera.setFont(fuenteBotones);
        btnPatron.setFont(fuenteBotones);

        // Agregamos los botones al panel principal en orden vertical
        gbc.gridy = 2;
        panelFondo.add(btnLogin, gbc);

        gbc.gridy = 3;
        panelFondo.add(btnBandera, gbc);

        gbc.gridy = 4;
        panelFondo.add(btnPatron, gbc);

        // Establecemos el panel con fondo como el contenido principal de la ventana
        setContentPane(panelFondo);
    }

    // Métodos "getters" para que el Controlador pueda escuchar los clics de cada botón
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