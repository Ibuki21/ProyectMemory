package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

// Ventana que muestra lo que ve la cámara (con un recuadro sobre el rostro detectado),
// un mensaje de estado y una barra de progreso. La usan tanto el registro como el inicio de sesión facial.
public class VentanaCamara extends JDialog {

    private static final int ANCHO_VISTA = 560;
    private static final int ALTO_VISTA = 420;

    private final JLabel lblVideo;
    private final JLabel lblMensaje;
    private final JProgressBar barra;
    private final JButton btnCancelar;
    private Runnable alCancelar;

    public VentanaCamara(Window padre, String titulo, String instruccion, int maximoProgreso) {
        super(padre, titulo, ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setResizable(false);

        PanelFondo panel = new PanelFondo("Recursos/Fondo1.jpg");
        panel.setLayout(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JLabel lblInstruccion = new JLabel(instruccion, SwingConstants.CENTER);
        lblInstruccion.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblInstruccion.setForeground(Color.WHITE);
        panel.add(lblInstruccion, BorderLayout.NORTH);

        lblVideo = new JLabel("Iniciando cámara...", SwingConstants.CENTER);
        lblVideo.setOpaque(true);
        lblVideo.setBackground(Color.BLACK);
        lblVideo.setForeground(Color.WHITE);
        lblVideo.setPreferredSize(new Dimension(ANCHO_VISTA, ALTO_VISTA));
        lblVideo.setBorder(BorderFactory.createLineBorder(new Color(255, 215, 0), 2));
        panel.add(lblVideo, BorderLayout.CENTER);

        lblMensaje = new JLabel(" ", SwingConstants.CENTER);
        lblMensaje.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblMensaje.setForeground(new Color(255, 215, 0));

        barra = new JProgressBar(0, Math.max(1, maximoProgreso));
        barra.setStringPainted(true);
        barra.setString(" ");

        btnCancelar = new JButton("Cancelar");
        btnCancelar.addActionListener(e -> cancelar());
        JPanel panelBoton = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelBoton.setOpaque(false);
        panelBoton.add(btnCancelar);

        JPanel panelSur = new JPanel(new GridLayout(3, 1, 0, 4));
        panelSur.setOpaque(false);
        panelSur.add(lblMensaje);
        panelSur.add(barra);
        panelSur.add(panelBoton);
        panel.add(panelSur, BorderLayout.SOUTH);

        setContentPane(panel);
        pack();
        setLocationRelativeTo(padre);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cancelar();
            }
        });
    }

    // Lo que se ejecuta al pulsar "Cancelar" o cerrar la ventana
    public void setAlCancelar(Runnable alCancelar) {
        this.alCancelar = alCancelar;
    }

    private void cancelar() {
        if (alCancelar != null) {
            alCancelar.run();
        } else {
            dispose();
        }
    }

    // Muestra un cuadro de la cámara con el recuadro del rostro (si lo hay). Debe llamarse desde el hilo de la interfaz.
    public void mostrar(BufferedImage imagen, Rectangle rostro, Color colorRecuadro,
                        String mensaje, int progreso, String textoBarra) {
        if (imagen != null) {
            double escala = Math.min((double) ANCHO_VISTA / imagen.getWidth(), (double) ALTO_VISTA / imagen.getHeight());
            int ancho = Math.max(1, (int) Math.round(imagen.getWidth() * escala));
            int alto = Math.max(1, (int) Math.round(imagen.getHeight() * escala));

            BufferedImage vista = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = vista.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(imagen, 0, 0, ancho, alto, null);
            if (rostro != null) {
                g2.setColor(colorRecuadro);
                g2.setStroke(new BasicStroke(3));
                g2.drawRect((int) Math.round(rostro.x * escala), (int) Math.round(rostro.y * escala),
                        (int) Math.round(rostro.width * escala), (int) Math.round(rostro.height * escala));
            }
            g2.dispose();
            lblVideo.setText(null);
            lblVideo.setIcon(new ImageIcon(vista));
        }
        lblMensaje.setText(mensaje);
        barra.setValue(progreso);
        barra.setString(textoBarra);
    }
}
