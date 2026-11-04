import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.util.List;

public class VentanaMenuPrincipal extends JFrame {

    private static final int ANCHO_VENTANA = 800;
    private static final int ALTO_VENTANA = 600;

    private JPanel panelContenedor;
    private Timer timerTransicion;
    private Clip clipMusicaFondo;
    private boolean musicaActiva = true;

    private JButton btnLogin;
    private JButton btnRegistrar;
    private JButton btnBandera;
    private JButton btnPatron;
    private JButton btnMusica;
    private DefaultTableModel modeloTabla;
    private JTable tablaPuntuaciones;

    // Variables para controlar el estado de Pantalla Completa
    private boolean esPantallaCompleta = false;
    private Rectangle limitesVentanaAnterior;

    public VentanaMenuPrincipal() {
        setTitle("Menú Principal - Juegos de Memoria y Banderas");
        setSize(ANCHO_VENTANA, ALTO_VENTANA);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true); // Permitir redimensionar para habilitar el botón de maximizar

        // 🎵 Iniciar música de fondo al abrir
        reproducirMusicaFondo("Recursos/Cancion/8 bits.wav");

        // Panel principal con CardLayout para cambiar entre la Intro y el Menú Interactivo
        panelContenedor = new JPanel(new CardLayout());
        setContentPane(panelContenedor);

        // 1. Panel de Introducción (Muestra el GIF de presentación con proporción conservada)
        JPanel panelIntro = crearPanelConGifProporcional("Recursos/VideoJuego.gif");
        panelContenedor.add(panelIntro, "INTRO");

        // 2. Panel del Menú Interactivo (Fondo.gif con los botones flotantes y la tabla de puntuaciones)
        JPanel panelMenu = crearPanelMenuConBotones();
        panelContenedor.add(panelMenu, "MENU");

        // Mostrar la intro primero
        CardLayout cl = (CardLayout) panelContenedor.getLayout();
        cl.show(panelContenedor, "INTRO");

        // ⏱️ Duración de la intro en milisegundos (25 segundos)
        int duracionIntroMillis = 25000;

        timerTransicion = new Timer(duracionIntroMillis, e -> {
            cl.show(panelContenedor, "MENU");
            timerTransicion.stop();
        });
        timerTransicion.setRepeats(false);
        timerTransicion.start();

        // ⌨️ Escucha global de teclado para alternar pantalla completa con F11 o salir con ESC
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_F11) {
                    alternarPantallaCompleta();
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE && esPantallaCompleta) {
                    alternarPantallaCompleta();
                }
            }
        });
        setFocusable(true);

        // Refrescar la tabla cuando la ventana recupera el foco o se reactiva
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowActivated(WindowEvent e) {
                actualizarTablaPuntuaciones();
            }

            @Override
            public void windowClosed(WindowEvent e) {
                detenerMusicaFondo();
            }
        });

        // Cargar puntuaciones iniciales
        actualizarTablaPuntuaciones();
    }

    /**
     * Alterna la ventana entre modo normal y pantalla completa real (F11 / ESC).
     */
    private void alternarPantallaCompleta() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice vc = ge.getDefaultScreenDevice();

        dispose(); // Ocultar temporalmente para modificar las propiedades de la ventana

        if (!esPantallaCompleta) {
            limitesVentanaAnterior = getBounds();
            setUndecorated(true); // Oculta barra de título y bordes para ocupar 100% de la pantalla
            vc.setFullScreenWindow(this);
            esPantallaCompleta = true;
        } else {
            vc.setFullScreenWindow(null);
            setUndecorated(false);
            setBounds(limitesVentanaAnterior);
            esPantallaCompleta = false;
        }

        setVisible(true);
        requestFocus(); // Mantiene el foco para capturar eventos de teclado
    }

    /**
     * Panel que muestra el GIF de introducción conservando estrictamente su proporción (Aspect Ratio)
     * y rellenando los espacios sobrantes con negro.
     */
    private JPanel crearPanelConGifProporcional(String ruta) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(Color.BLACK);
                g.fillRect(0, 0, getWidth(), getHeight());

                URL urlGif = getClass().getClassLoader().getResource(ruta);
                if (urlGif != null) {
                    ImageIcon iconoOriginal = new ImageIcon(urlGif);
                    Image imagen = iconoOriginal.getImage();

                    int anchoOriginal = iconoOriginal.getIconWidth();
                    int altoOriginal = iconoOriginal.getIconHeight();

                    if (anchoOriginal > 0 && altoOriginal > 0) {
                        int panelAncho = getWidth();
                        int panelAlto = getHeight();

                        // Calcular escala proporcional (Fit Inside sin deformar)
                        double escala = Math.min((double) panelAncho / anchoOriginal, (double) panelAlto / altoOriginal);

                        int nuevoAncho = (int) (anchoOriginal * escala);
                        int nuevoAlto = (int) (altoOriginal * escala);

                        // Centrar la imagen en el panel
                        int x = (panelAncho - nuevoAncho) / 2;
                        int y = (panelAlto - nuevoAlto) / 2;

                        g.drawImage(imagen, x, y, nuevoAncho, nuevoAlto, this);
                    }
                }
            }
        };
        panel.setBackground(Color.BLACK);
        return panel;
    }

    // Panel con capas para superponer los botones y la tabla sobre el "Fondo.gif"
    private JPanel crearPanelMenuConBotones() {
        JPanel panelContenedorCapas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                URL urlGif = getClass().getClassLoader().getResource("Recursos/Fondo.gif");
                if (urlGif != null) {
                    ImageIcon iconoOriginal = new ImageIcon(urlGif);
                    g.drawImage(iconoOriginal.getImage(), 0, 0, getWidth(), getHeight(), this);
                } else {
                    g.setColor(new Color(20, 30, 50));
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        panelContenedorCapas.setLayout(null);
        panelContenedorCapas.setBounds(0, 0, ANCHO_VENTANA, ALTO_VENTANA);

        // Capa superior transparente para los botones y la tabla flotante
        JPanel panelComponentesFlotantes = new JPanel(null);
        panelComponentesFlotantes.setOpaque(false);
        panelComponentesFlotantes.setBounds(0, 0, ANCHO_VENTANA, ALTO_VENTANA);

        // 🏆 Tabla de Mejores Puntuaciones (Leaderboard Flotante con fondo translúcido integrado)
        JPanel panelLeaderboard = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(new Color(15, 25, 45, 200)); // Azul oscuro translúcido
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                super.paintComponent(g);
            }
        };
        panelLeaderboard.setOpaque(false);
        panelLeaderboard.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(255, 215, 0), 2),
                " Top Jugadores ",
                0, 0, new Font("Segoe UI", Font.BOLD, 14), Color.WHITE));
        panelLeaderboard.setBounds(20, 270, 260, 260);

        String[] columnas = {"Pos.", "Usuario", "Puntos"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaPuntuaciones = new JTable(modeloTabla);
        tablaPuntuaciones.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tablaPuntuaciones.setRowHeight(22);
        tablaPuntuaciones.getTableHeader().setReorderingAllowed(false);
        tablaPuntuaciones.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        // Formato y centrado de columnas
        tablaPuntuaciones.getColumnModel().getColumn(0).setPreferredWidth(35);
        tablaPuntuaciones.getColumnModel().getColumn(1).setPreferredWidth(130);
        tablaPuntuaciones.getColumnModel().getColumn(2).setPreferredWidth(60);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        tablaPuntuaciones.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        tablaPuntuaciones.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);

        JScrollPane scrollTabla = new JScrollPane(tablaPuntuaciones);
        scrollTabla.getViewport().setOpaque(true);
        panelLeaderboard.add(scrollTabla, BorderLayout.CENTER);

        // 📌 Botón 1: Iniciar Sesión (Más grandes: 260x50)
        btnLogin = crearBotonConImagen("Recursos/Registro.png", 260, 50);
        btnLogin.setBounds(550, 290, 260, 50);
        btnLogin.addActionListener(e -> {
            reproducirEfectoSonido("Recursos/Cancion/Botones/Presiona1.wav");
            VentanaLogin loginDialog = new VentanaLogin(this);
            loginDialog.setVisible(true);
        });

        // 📌 Botón 2: Encuentra la Pareja / Banderas
        btnBandera = crearBotonConImagen("Recursos/Memoria.png", 260, 50);
        btnBandera.setBounds(550, 350, 260, 50);
        btnBandera.addActionListener(e -> {
            reproducirEfectoSonido("Recursos/Cancion/Botones/Presiona1.wav");
            JuegoParejasVisual ventanaParejas = new JuegoParejasVisual();
            ventanaParejas.setVisible(true);
        });

        // 📌 Botón 3: Siguiendo el Patrón
        btnPatron = crearBotonConImagen("Recursos/Patron.png", 260, 50);
        btnPatron.setBounds(550, 410, 260, 50);
        btnPatron.addActionListener(e -> {
            reproducirEfectoSonido("Recursos/Cancion/Botones/Presiona1.wav");
            VentanaModoPatron ventanaPatron = new VentanaModoPatron();
            ControladorModoPatron controlador = new ControladorModoPatron(ventanaPatron);
            ventanaPatron.setVisible(true);
            controlador.iniciar();
        });

        // 📌 Botón 4: Control de Música / Silenciar
        btnMusica = crearBotonConImagen("Recursos/Musica.png", 260, 50);
        btnMusica.setBounds(550, 470, 260, 50);
        btnMusica.addActionListener(e -> {
            reproducirEfectoSonido("Recursos/Cancion/Botones/Presiona1.wav");
            alternarMusicaFondo();
        });

        // Agregar componentes al panel flotante
        panelComponentesFlotantes.add(panelLeaderboard);
        panelComponentesFlotantes.add(btnLogin);
        panelComponentesFlotantes.add(btnBandera);
        panelComponentesFlotantes.add(btnPatron);
        panelComponentesFlotantes.add(btnMusica);

        panelContenedorCapas.add(panelComponentesFlotantes);

        return panelContenedorCapas;
    }

    /**
     * Refresca los datos expuestos en la tabla a partir de GestorUsuarios.
     */
    public void actualizarTablaPuntuaciones() {
        if (modeloTabla == null) {
            return;
        }
        modeloTabla.setRowCount(0);
        List<Usuario> ranking = GestorUsuarios.getMejoresPuntuaciones();

        int posicion = 1;
        for (Usuario u : ranking) {
            modeloTabla.addRow(new Object[]{
                    posicion++,
                    u.getUsuario(),
                    u.getPuntuacionMaxima()
            });
        }
    }

    private JButton crearBotonConImagen(String rutaImagen, int ancho, int alto) {
        JButton boton = new JButton();

        URL urlImg = getClass().getClassLoader().getResource(rutaImagen);
        if (urlImg != null) {
            ImageIcon iconoOriginal = new ImageIcon(urlImg);
            if (iconoOriginal.getImage() != null) {
                Image imagenEscalada = iconoOriginal.getImage().getScaledInstance(ancho, alto, Image.SCALE_SMOOTH);
                boton.setIcon(new ImageIcon(imagenEscalada));
            }
        } else {
            boton.setText("Botón");
            System.err.println("No se encontró la imagen del botón: " + rutaImagen);
        }

        boton.setContentAreaFilled(false);
        boton.setBorderPainted(false);
        boton.setFocusPainted(false);
        boton.setOpaque(false);
        boton.setBorder(BorderFactory.createEmptyBorder());
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return boton;
    }

    private void reproducirMusicaFondo(String rutaAudio) {
        try {
            URL urlAudio = getClass().getClassLoader().getResource(rutaAudio);
            if (urlAudio != null) {
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(urlAudio);
                clipMusicaFondo = AudioSystem.getClip();
                clipMusicaFondo.open(audioStream);
                clipMusicaFondo.loop(Clip.LOOP_CONTINUOUSLY);
                clipMusicaFondo.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void reproducirEfectoSonido(String rutaAudio) {
        try {
            URL urlAudio = getClass().getClassLoader().getResource(rutaAudio);
            if (urlAudio != null) {
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(urlAudio);
                Clip clipClick = AudioSystem.getClip();
                clipClick.open(audioStream);
                clipClick.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void alternarMusicaFondo() {
        if (musicaActiva) {
            if (clipMusicaFondo != null && clipMusicaFondo.isRunning()) {
                clipMusicaFondo.stop();
            }
            musicaActiva = false;
        } else {
            if (clipMusicaFondo != null) {
                clipMusicaFondo.start();
                clipMusicaFondo.loop(Clip.LOOP_CONTINUOUSLY);
            }
            musicaActiva = true;
        }
    }

    private void detenerMusicaFondo() {
        if (clipMusicaFondo != null && clipMusicaFondo.isRunning()) {
            clipMusicaFondo.stop();
            clipMusicaFondo.close();
        }
    }

    // --- Getters ---
    public JButton getBtnLogin() { return btnLogin; }
    public JButton getBtnRegistrar() { return btnRegistrar; }
    public JButton getBtnBandera() { return btnBandera; }
    public JButton getBtnPatron() { return btnPatron; }
    public JButton getBtnMusica() { return btnMusica; }
}