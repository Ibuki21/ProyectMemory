import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class VentanaMenuPrincipal extends JFrame {

    private static final int ANCHO_VENTANA = 800;
    private static final int ALTO_VENTANA = 600;

    private JPanel panelContenedor;
    private Timer timerTransicion;
    private Clip clipMusicaFondo;
    private boolean musicaActiva = true;

    private JButton btnLogin;
    private JButton btnBandera;
    private JButton btnPatron;
    private JButton btnMusica;

    public VentanaMenuPrincipal() {
        setTitle("Menú Principal - Juegos de Memoria y Banderas");
        setSize(ANCHO_VENTANA, ALTO_VENTANA);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // 🎵 Iniciar música de fondo al abrir
        reproducirMusicaFondo("Recursos/Cancion/8 bits.wav");

        // Panel principal con CardLayout para cambiar entre la Intro y el Menú Interactivo
        panelContenedor = new JPanel(new CardLayout());
        setContentPane(panelContenedor);

        // 1. Panel de Introducción (Muestra el GIF de presentación "VideoJuego.gif")
        JPanel panelIntro = crearPanelConGifEscalado("Recursos/VideoJuego.gif", ANCHO_VENTANA, ANCHO_VENTANA);
        panelContenedor.add(panelIntro, "INTRO");

        // 2. Panel del Menú Interactivo (Fondo.gif con los botones flotantes)
        JPanel panelMenu = crearPanelMenuConBotones();
        panelContenedor.add(panelMenu, "MENU");

        // Mostrar la intro primero
        CardLayout cl = (CardLayout) panelContenedor.getLayout();
        cl.show(panelContenedor, "INTRO");

        // ⏱️ Duración de la intro en milisegundos (ej. 28000 ms = 28 segundos, ajústalo si dura más o menos)
        int duracionIntroMillis = 28000;

        timerTransicion = new Timer(duracionIntroMillis, e -> {
            cl.show(panelContenedor, "MENU");
            timerTransicion.stop();
        });
        timerTransicion.setRepeats(false);
        timerTransicion.start();

        // Detener la música al cerrar la ventana
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                detenerMusicaFondo();
            }
        });
    }

    // Panel que muestra el GIF de introducción escalado
    private JPanel crearPanelConGifEscalado(String ruta, int ancho, int alto) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.BLACK);

        JLabel lblGif = new JLabel();
        lblGif.setHorizontalAlignment(SwingConstants.CENTER);

        URL urlGif = getClass().getClassLoader().getResource(ruta);
        if (urlGif != null) {
            ImageIcon iconoOriginal = new ImageIcon(urlGif);
            Image imagenEscalada = iconoOriginal.getImage().getScaledInstance(ancho, alto, Image.SCALE_DEFAULT);
            lblGif.setIcon(new ImageIcon(imagenEscalada));
        } else {
            lblGif.setText("No se encontró el recurso: " + ruta);
            lblGif.setForeground(Color.WHITE);
        }

        panel.add(lblGif, BorderLayout.CENTER);
        return panel;
    }

    // Panel con capas para superponer los botones sobre el "Fondo.gif"
    private JPanel crearPanelMenuConBotones() {
        JPanel panelContenedorCapas = new JPanel(null);
        panelContenedorCapas.setBounds(0, 0, ANCHO_VENTANA, ALTO_VENTANA);

        // Capa inferior: El GIF de fondo en bucle
        JLabel lblGif = new JLabel();
        lblGif.setBounds(0, 0, ANCHO_VENTANA, ALTO_VENTANA);
        URL urlGif = getClass().getClassLoader().getResource("Recursos/Fondo.gif");
        if (urlGif != null) {
            ImageIcon iconoOriginal = new ImageIcon(urlGif);
            Image imagenEscalada = iconoOriginal.getImage().getScaledInstance(ANCHO_VENTANA, ALTO_VENTANA, Image.SCALE_DEFAULT);
            lblGif.setIcon(new ImageIcon(imagenEscalada));
        } else {
            System.err.println("No se pudo cargar el GIF de fondo: Recursos/Fondo.gif");
        }

        // Capa superior transparente para los botones flotantes
        JPanel panelBotonesFlotantes = new JPanel(null);
        panelBotonesFlotantes.setOpaque(false);
        panelBotonesFlotantes.setBounds(0, 0, ANCHO_VENTANA, ALTO_VENTANA);

        // 📌 Botón 1: Iniciar Sesión / Registro
        btnLogin = crearBotonConImagen("Recursos/Registro.png");
        btnLogin.setBounds(300, 310, 200, 40);
        btnLogin.addActionListener(e -> {
            reproducirEfectoSonido("Recursos/Cancion/Botones/Presiona1.wav");
            VentanaLogin loginDialog = new VentanaLogin(this);
            loginDialog.setVisible(true);
        });

        // 📌 Botón 2: Encuentra la Pareja / Banderas
        btnBandera = crearBotonConImagen("Recursos/Memoria.png");
        btnBandera.setBounds(300, 365, 200, 40);
        btnBandera.addActionListener(e -> {
            reproducirEfectoSonido("Recursos/Cancion/Botones/Presiona1.wav");
            JuegoParejasVisual ventanaParejas = new JuegoParejasVisual();
            ventanaParejas.setVisible(true);
        });

        // 📌 Botón 3: Siguiendo el Patrón
        btnPatron = crearBotonConImagen("Recursos/Patron.png");
        btnPatron.setBounds(300, 420, 200, 40);
        btnPatron.addActionListener(e -> {
            reproducirEfectoSonido("Recursos/Cancion/Botones/Presiona1.wav");
            VentanaModoPatron ventanaPatron = new VentanaModoPatron();
            ControladorModoPatron controlador = new ControladorModoPatron(ventanaPatron);
            ventanaPatron.setVisible(true);
            controlador.iniciar();
        });

        // 📌 Botón 4: Control de Música / Silenciar
        btnMusica = crearBotonConImagen("Recursos/Musica.png");
        btnMusica.setBounds(300, 485, 200, 40);
        btnMusica.addActionListener(e -> {
            reproducirEfectoSonido("Recursos/Cancion/Botones/Presiona1.wav");
            alternarMusicaFondo();
        });

        panelBotonesFlotantes.add(btnLogin);
        panelBotonesFlotantes.add(btnBandera);
        panelBotonesFlotantes.add(btnPatron);
        panelBotonesFlotantes.add(btnMusica);

        panelContenedorCapas.add(panelBotonesFlotantes);
        panelContenedorCapas.add(lblGif);

        return panelContenedorCapas;
    }

    private JButton crearBotonConImagen(String rutaImagen) {
        JButton boton = new JButton();

        URL urlImg = getClass().getClassLoader().getResource(rutaImagen);
        if (urlImg != null) {
            ImageIcon iconoOriginal = new ImageIcon(urlImg);
            if (iconoOriginal.getImage() != null) {
                Image imagenEscalada = iconoOriginal.getImage().getScaledInstance(200, 40, Image.SCALE_SMOOTH);
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
    public JButton getBtnBandera() { return btnBandera; }
    public JButton getBtnPatron() { return btnPatron; }
    public JButton getBtnMusica() { return btnMusica; }
}