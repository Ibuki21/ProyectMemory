import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class VentanaMenuPrincipal extends JFrame {

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

    private JButton btnLogin;
    private JButton btnBandera;
    private JButton btnPatron;
    private JButton btnMusica;

    // 🎵 Atributos para controlar el estado de la música
    private Cancion musicaFondo;
    private boolean musicaActiva = true;

    public VentanaMenuPrincipal() {
        setTitle("Menú Principal - Juegos de Memoria");
        setSize(480, 560);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Inicializar y arrancar la música de fondo al abrir el menú
        musicaFondo = new Cancion();
        musicaFondo.cargarSonido("Recursos/Cancion/8 bits.wav");
        musicaFondo.reproducir();

        PanelFondo panelFondo = new PanelFondo("Recursos/Fondo.png");
        panelFondo.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 🖼️ Carga del Logo
        JLabel lblImagen = new JLabel();
        URL urlLogo = getClass().getClassLoader().getResource("Recursos/LogoMenu.png");
        if (urlLogo != null) {
            ImageIcon iconoOriginal = new ImageIcon(urlLogo);
            Image imagenEscalada = iconoOriginal.getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH);
            lblImagen.setIcon(new ImageIcon(imagenEscalada));
        }
        lblImagen.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 0;
        panelFondo.add(lblImagen, gbc);

        // Título
        JLabel lblTitulo = new JLabel("=== MENÚ PRINCIPAL ===");
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setFont(new Font("Monospaced", Font.BOLD, 18));
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 1;
        panelFondo.add(lblTitulo, gbc);

        // Botones
        btnLogin = new JButton("🔐 Iniciar Sesión (Login)");
        btnBandera = new JButton("🚩 Encuentra la Bandera");
        btnPatron = new JButton("🧩 Siguiendo el Patrón");
        btnMusica = new JButton("🎵 Música: Activada");

        Font fuenteBotones = new Font("SansSerif", Font.BOLD, 14);
        btnLogin.setFont(fuenteBotones);
        btnBandera.setFont(fuenteBotones);
        btnPatron.setFont(fuenteBotones);
        btnMusica.setFont(fuenteBotones);

        gbc.gridy = 2;
        panelFondo.add(btnLogin, gbc);

        gbc.gridy = 3;
        panelFondo.add(btnBandera, gbc);

        gbc.gridy = 4;
        panelFondo.add(btnPatron, gbc);

        gbc.gridy = 5;
        panelFondo.add(btnMusica, gbc);

        setContentPane(panelFondo);

        // --- Evento para abrir la ventana de Login ---
        btnLogin.addActionListener(e -> {
            VentanaLogin loginDialog = new VentanaLogin(this);
            loginDialog.setVisible(true);
        });

        // --- Evento para abrir la ventana del Modo Patrones ---
        btnPatron.addActionListener(e -> {
            VentanaModoPatron ventanaPatron = new VentanaModoPatron();
            ventanaPatron.setVisible(true);
        });
        btnBandera.addActionListener(e -> {
            JuegoParejasVisual ventanaParejas = new JuegoParejasVisual();
            ventanaParejas.setVisible(true);
        });

        // --- 🎵 Evento para Encender / Apagar la Música (Independiente y ordenado) ---
        btnMusica.addActionListener(e -> {
            if (musicaActiva) {
                musicaFondo.detener();
                btnMusica.setText("🔇 Música: Silenciada");
                musicaActiva = false;
            } else {
                musicaFondo.reproducir();
                btnMusica.setText("🎵 Música: Activada");
                musicaActiva = true;
            }
        });
    }

    // --- Getters correctamente posicionados a nivel de clase ---
    public JButton getBtnLogin() {
        return btnLogin;
    }
    public JButton getBtnBandera() {
        return btnBandera;
    }
    public JButton getBtnPatron() {
        return btnPatron;
    }
    public JButton getBtnMusica() {
        return btnMusica;
    }
}