package view;

import model.Usuario;
import model.GestorUsuarios;
import controller.ControladorLogin;
import controller.ControladorModoPatron;
import controller.GestorMusica;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.*;
import java.awt.image.BufferedImage;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.List;

public class VentanaMenuPrincipal extends JFrame {

    private static final int ANCHO_VENTANA = 1280;
    private static final int ALTO_VENTANA = 720;

    private static final int MARGEN = 20;
    private static final int ANCHO_BOTON = 260;
    private static final int ALTO_BOTON = 50;
    private static final int SEPARACION_BOTONES = 10;
    private static final int ANCHO_LEADERBOARD = 260;
    private static final int ALTO_LEADERBOARD = 260;

    private JPanel panelContenedor;
    private FondoAnimado fondoMenu;
    private ImageIcon iconoIntro;
    private static final double ESCALA_MINIMA = 0.5;

    private JPanel panelLeaderboard;
    private Dimension tamanoFijo;
    private Dimension baseContenedor;
    private double escalaTextoAplicada = -1;
    private Timer timerTransicion;
    private Timer timerParpadeo;
    private boolean introActiva = true;
    private boolean avisoSaltarVisible = true;

    private final KeyEventDispatcher despachadorTeclasIntro = e -> {
        if (introActiva && e.getID() == KeyEvent.KEY_PRESSED && isActive()) {
            SwingUtilities.invokeLater(this::terminarIntro);
        }
        return false;
    };

    private JButton btnLogin;
    private JButton btnRegistrar;
    private JButton btnBandera;
    private JButton btnPatron;
    private JButton btnMusica;
    private JMenuBar barraMenu;
    private JMenuItem itemAcercaDe;
    private JMenuItem itemCerrarSesion;
    private DefaultTableModel modeloTabla;
    private JTable tablaPuntuaciones;

    private boolean esPantallaCompleta = false;
    private Rectangle limitesVentanaAnterior;

    public VentanaMenuPrincipal() {
        setTitle("Menú Principal - Juegos de Memoria y Banderas");
        setSize(ANCHO_VENTANA, ALTO_VENTANA);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);

        crearBarraMenu();
        tamanoFijo = new Dimension(ANCHO_VENTANA, ALTO_VENTANA + barraMenu.getPreferredSize().height);
        setSize(tamanoFijo);
        setMinimumSize(new Dimension(
                (int) Math.round(tamanoFijo.width * ESCALA_MINIMA),
                (int) Math.round(tamanoFijo.height * ESCALA_MINIMA)));
        setLocationRelativeTo(null);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (esPantallaCompleta || estaMaximizada()) {
                    return;
                }
                double escala = Math.min((double) getWidth() / tamanoFijo.width,
                        (double) getHeight() / tamanoFijo.height);
                escala = Math.max(ESCALA_MINIMA, Math.min(1.0, escala));
                Dimension destino = new Dimension(
                        (int) Math.round(tamanoFijo.width * escala),
                        (int) Math.round(tamanoFijo.height * escala));

                if (!getSize().equals(destino)) {
                    setSize(destino);
                }
            }
        });

        GestorMusica.reproducir("Recursos/Cancion/8 bits.wav");

        fondoMenu = new FondoAnimado("Recursos/Fondo.gif", () -> panelContenedor.repaint());
        URL urlIntro = getClass().getClassLoader().getResource("Recursos/VideoJuego.gif");
        iconoIntro = (urlIntro != null) ? new ImageIcon(urlIntro) : null;

        panelContenedor = new JPanel(new CardLayout());
        setContentPane(panelContenedor);

        JPanel panelIntro = crearPanelConGifProporcional("Recursos/VideoJuego.gif");
        panelContenedor.add(panelIntro, "INTRO");

        JPanel panelMenu = crearPanelMenuConBotones();
        panelContenedor.add(panelMenu, "MENU");

        CardLayout cl = (CardLayout) panelContenedor.getLayout();
        cl.show(panelContenedor, "INTRO");

        int duracionIntroMillis = 25000;

        timerTransicion = new Timer(duracionIntroMillis, e -> terminarIntro());
        timerTransicion.setRepeats(false);
        timerTransicion.start();

        timerParpadeo = new Timer(600, e -> {
            avisoSaltarVisible = !avisoSaltarVisible;
            panelContenedor.repaint();
        });
        timerParpadeo.start();
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(despachadorTeclasIntro);

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

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowActivated(WindowEvent e) {
                actualizarTablaPuntuaciones();
                if (!introActiva) {
                    fondoMenu.reanudar();
                }
            }

            @Override
            public void windowDeactivated(WindowEvent e) {
                fondoMenu.pausar();
            }

            @Override
            public void windowIconified(WindowEvent e) {
                fondoMenu.pausar();
            }

            @Override
            public void windowClosed(WindowEvent e) {
                fondoMenu.pausar();
                GestorMusica.detener();
            }
        });

        actualizarTablaPuntuaciones();

        GestorMusica.precargar("Recursos/Cancion/Botones/Presiona1.wav");
        model.ServicioCambioDolar.precargar();
    }

    private boolean estaMaximizada() {
        if ((getExtendedState() & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH) {
            return true;
        }
        Rectangle area = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        return getWidth() >= area.width - 4 && getHeight() >= area.height - 4;
    }

    private void terminarIntro() {
        if (!introActiva) {
            return;
        }
        introActiva = false;
        timerTransicion.stop();
        timerParpadeo.stop();
        KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(despachadorTeclasIntro);
        ((CardLayout) panelContenedor.getLayout()).show(panelContenedor, "MENU");

        if (iconoIntro != null) {
            iconoIntro.getImage().flush();
            iconoIntro = null;
        }
        fondoMenu.reanudar();
        requestFocus();
    }

    private void crearBarraMenu() {
        barraMenu = new JMenuBar();

        JMenu menuAyuda = new JMenu("Ayuda");
        itemAcercaDe = new JMenuItem("Acerca de");
        itemAcercaDe.addActionListener(e -> {
            VentanaAcercaDe acercaDe = new VentanaAcercaDe(this);
            acercaDe.setVisible(true);
        });
        menuAyuda.add(itemAcercaDe);

        itemCerrarSesion = new JMenuItem("Cerrar sesión");
        itemCerrarSesion.addActionListener(e -> cerrarSesion());
        itemCerrarSesion.setMaximumSize(itemCerrarSesion.getPreferredSize());

        barraMenu.add(menuAyuda);
        barraMenu.add(Box.createHorizontalGlue());
        barraMenu.add(itemCerrarSesion);

        setJMenuBar(barraMenu);
    }

    private void cerrarSesion() {
        Usuario actual = GestorUsuarios.getUsuarioSesionActual();
        if (actual == null) {
            JOptionPane.showMessageDialog(this, "No hay ninguna sesión abierta.",
                    "Cerrar sesión", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        GestorUsuarios.cerrarSesion();
        JOptionPane.showMessageDialog(this, "Sesión cerrada. Hasta pronto, " + actual.getNombre() + ".",
                "Cerrar sesión", JOptionPane.INFORMATION_MESSAGE);
    }

    private void alternarPantallaCompleta() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice vc = ge.getDefaultScreenDevice();

        dispose();

        if (!esPantallaCompleta) {
            limitesVentanaAnterior = getBounds();
            setUndecorated(true);
            vc.setFullScreenWindow(this);
            esPantallaCompleta = true;
        } else {
            vc.setFullScreenWindow(null);
            setUndecorated(false);
            setBounds(limitesVentanaAnterior);
            esPantallaCompleta = false;
        }

        setVisible(true);
        requestFocus();
    }

    private JPanel crearPanelConGifProporcional(String ruta) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(Color.BLACK);
                g.fillRect(0, 0, getWidth(), getHeight());

                ImageIcon iconoOriginal = iconoIntro;
                if (iconoOriginal != null) {
                    Image imagen = iconoOriginal.getImage();

                    int anchoOriginal = iconoOriginal.getIconWidth();
                    int altoOriginal = iconoOriginal.getIconHeight();

                    if (anchoOriginal > 0 && altoOriginal > 0) {
                        int panelAncho = getWidth();
                        int panelAlto = getHeight();

                        double escala = Math.min((double) panelAncho / anchoOriginal, (double) panelAlto / altoOriginal);

                        int nuevoAncho = (int) (anchoOriginal * escala);
                        int nuevoAlto = (int) (altoOriginal * escala);

                        int x = (panelAncho - nuevoAncho) / 2;
                        int y = (panelAlto - nuevoAlto) / 2;

                        g.drawImage(imagen, x, y, nuevoAncho, nuevoAlto, this);
                    }
                }

                if (introActiva && avisoSaltarVisible) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 20));
                    String texto = "Presiona cualquier tecla para saltar";
                    FontMetrics fm = g2.getFontMetrics();
                    int textoX = (getWidth() - fm.stringWidth(texto)) / 2;
                    int textoY = 40;
                    g2.setColor(new Color(0, 0, 0, 170));
                    g2.drawString(texto, textoX + 2, textoY + 2);
                    g2.setColor(Color.WHITE);
                    g2.drawString(texto, textoX, textoY);
                    g2.dispose();
                }
            }
        };
        panel.setBackground(Color.BLACK);
        return panel;
    }

    private JPanel crearPanelMenuConBotones() {
        JPanel panelContenedorCapas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                fondoMenu.pintar(g, getWidth(), getHeight());
            }
        };
        panelContenedorCapas.setLayout(new BorderLayout());

        JPanel panelComponentesFlotantes = new JPanel(null) {
            @Override
            public void doLayout() {
                int ancho = getWidth();
                int alto = getHeight();
                if (ancho <= 0 || alto <= 0) {
                    return;
                }
                if (baseContenedor == null) {
                    baseContenedor = new Dimension(ancho, alto);
                }

                double escala = Math.min((double) ancho / baseContenedor.width,
                        (double) alto / baseContenedor.height);
                int margen = (int) Math.round(MARGEN * escala);

                int anchoBoton = (int) Math.round(ANCHO_BOTON * escala);
                int altoBoton = (int) Math.round(ALTO_BOTON * escala);
                int separacion = (int) Math.round(SEPARACION_BOTONES * escala);

                int anchoTabla = (int) Math.round(ANCHO_LEADERBOARD * escala);
                int altoTabla = (int) Math.round(ALTO_LEADERBOARD * escala);

                if (panelLeaderboard != null) {
                    panelLeaderboard.setBounds(margen, alto - altoTabla - margen, anchoTabla, altoTabla);
                    aplicarEscalaTabla(escala);
                }

                JButton[] botones = {btnLogin, btnBandera, btnPatron, btnMusica};
                int altoColumna = botones.length * altoBoton + (botones.length - 1) * separacion;

                int x = (ancho - anchoBoton) / 2;
                int y = (alto - altoColumna) / 2 + 30;

                for (JButton boton : botones) {
                    if (boton != null) {
                        boton.setBounds(x, y, anchoBoton, altoBoton);
                        actualizarIconoBoton(boton, anchoBoton, altoBoton);
                    }
                    y += altoBoton + separacion;
                }
            }
        };
        panelComponentesFlotantes.setOpaque(false);

        panelLeaderboard = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(new Color(15, 25, 45, 180));
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                super.paintComponent(g);
            }
        };
        panelLeaderboard.setOpaque(false);
        panelLeaderboard.setBorder(crearBordeLeaderboard(1.0));

        String[] columnas = {"Pos.", "Usuario", "Colones"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaPuntuaciones = new JTable(modeloTabla);
        tablaPuntuaciones.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tablaPuntuaciones.setRowHeight(24);
        tablaPuntuaciones.getTableHeader().setReorderingAllowed(false);

        tablaPuntuaciones.setOpaque(false);
        tablaPuntuaciones.setForeground(Color.WHITE);
        tablaPuntuaciones.setGridColor(new Color(50, 80, 120, 100));
        tablaPuntuaciones.setSelectionBackground(new Color(70, 130, 180, 150));
        tablaPuntuaciones.setSelectionForeground(Color.WHITE);

        tablaPuntuaciones.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tablaPuntuaciones.getTableHeader().setBackground(new Color(30, 50, 80));
        tablaPuntuaciones.getTableHeader().setForeground(Color.WHITE);

        tablaPuntuaciones.getColumnModel().getColumn(0).setPreferredWidth(35);
        tablaPuntuaciones.getColumnModel().getColumn(1).setPreferredWidth(130);
        tablaPuntuaciones.getColumnModel().getColumn(2).setPreferredWidth(60);

        DefaultTableCellRenderer renderizadorTransparente = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (c instanceof JLabel) {
                    ((JLabel) c).setOpaque(false);
                    c.setForeground(Color.WHITE);
                }
                return c;
            }
        };
        renderizadorTransparente.setHorizontalAlignment(SwingConstants.CENTER);

        for (int i = 0; i < tablaPuntuaciones.getColumnCount(); i++) {
            tablaPuntuaciones.getColumnModel().getColumn(i).setCellRenderer(renderizadorTransparente);
        }

        JScrollPane scrollTabla = new JScrollPane(tablaPuntuaciones);
        scrollTabla.setOpaque(false);
        scrollTabla.getViewport().setOpaque(false);
        scrollTabla.setBorder(BorderFactory.createEmptyBorder());
        panelLeaderboard.add(scrollTabla, BorderLayout.CENTER);

        btnLogin = crearBotonConImagen("Recursos/Registro.png", 260, 50);
        btnLogin.addActionListener(e -> {
           GestorMusica.reproducirEfecto("Recursos/Cancion/Botones/Presiona1.wav");
            VentanaLogin loginDialog = new VentanaLogin(this);
            new ControladorLogin(loginDialog);
            loginDialog.setVisible(true);
            actualizarTablaPuntuaciones();
        });

        btnBandera = crearBotonConImagen("Recursos/Memoria.png", 260, 50);
        btnBandera.addActionListener(e -> {
            GestorMusica.reproducirEfecto("Recursos/Cancion/Botones/Presiona1.wav");
            JuegoParejasVisual ventanaParejas = new JuegoParejasVisual();
            ventanaParejas.setVisible(true);
        });

        btnPatron = crearBotonConImagen("Recursos/Patron.png", 260, 50);
        btnPatron.addActionListener(e -> {
            GestorMusica.reproducirEfecto("Recursos/Cancion/Botones/Presiona1.wav");
            VentanaModoPatron ventanaPatron = new VentanaModoPatron();
            ventanaPatron.setVisible(true);
            ControladorModoPatron controlador = new ControladorModoPatron(ventanaPatron);
            controlador.iniciar();
            actualizarTablaPuntuaciones();
        });

        btnMusica = crearBotonConImagen("Recursos/Musica.png", 260, 50);
        btnMusica.addActionListener(e -> {
            GestorMusica.reproducirEfecto("Recursos/Cancion/Botones/Presiona1.wav");
            GestorMusica.alternarSilencio();
        });

        panelComponentesFlotantes.add(panelLeaderboard);
        panelComponentesFlotantes.add(btnLogin);
        panelComponentesFlotantes.add(btnBandera);
        panelComponentesFlotantes.add(btnPatron);
        panelComponentesFlotantes.add(btnMusica);

        panelContenedorCapas.add(panelComponentesFlotantes, BorderLayout.CENTER);

        return panelContenedorCapas;
    }

    private Border crearBordeLeaderboard(double escala) {
        int tamanoLetra = Math.max(8, (int) Math.round(14 * escala));

        return BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(255, 215, 0), 2),
                " Top Jugadores ",
                0, 0, new Font("Segoe UI", Font.BOLD, tamanoLetra), Color.WHITE);
    }

    private void aplicarEscalaTabla(double escala) {
        if (Math.abs(escala - escalaTextoAplicada) < 0.01) {
            return;
        }
        escalaTextoAplicada = escala;
        int tamanoLetra = Math.max(7, (int) Math.round(12 * escala));

        tablaPuntuaciones.setFont(new Font("Segoe UI", Font.PLAIN, tamanoLetra));
        tablaPuntuaciones.setRowHeight(Math.max(10, (int) Math.round(24 * escala)));

        tablaPuntuaciones.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, tamanoLetra));
        panelLeaderboard.setBorder(crearBordeLeaderboard(escala));
    }

    private void actualizarIconoBoton(JButton boton, int ancho, int alto) {
        Image original = (Image) boton.getClientProperty("imagenOriginal");
        if (original == null || ancho <= 0 || alto <= 0) {
            return;
        }
        Icon actual = boton.getIcon();
        if (actual != null && actual.getIconWidth() == ancho && actual.getIconHeight() == alto) {
            return;
        }
        BufferedImage escalada = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = escalada.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(original, 0, 0, ancho, alto, null);
        g2.dispose();
        boton.setIcon(new ImageIcon(escalada));
    }

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
                    "₡" +
                    u.getPuntuacionMaxima() // 📈 Muestra el total acumulado de puntos de cada cuenta leída del archivo
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
                boton.putClientProperty("imagenOriginal", iconoOriginal.getImage());
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

    // --- Getters ---
    public JButton getBtnLogin() { return btnLogin; }
    public JButton getBtnRegistrar() { return btnRegistrar; }
    public JButton getBtnBandera() { return btnBandera; }
    public JButton getBtnPatron() { return btnPatron; }
    public JButton getBtnMusica() { return btnMusica; }
    public JMenuItem getItemAcercaDe() { return itemAcercaDe; }
    public JMenuItem getItemCerrarSesion() { return itemCerrarSesion; }
}