package view;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

// Ventana "Acerca de": muestra los datos de los creadores del proyecto.
public class VentanaAcercaDe extends JDialog {

    private static final int TAM_FOTO = 150;

    // Datos de los creadores, de izquierda a derecha.
    // Por ahora la foto es una bandera; para usar una foto real basta con
    // cambiar la ruta correspondiente en RUTAS_FOTOS.
    private static final String[] NOMBRES = {
            "Ibuki Pérez",
            "Angel Fallas",
            "Jorshan Quesada Rojas"
    };
    private static final String[] FECHAS_NACIMIENTO = {
            "Febrero 18 de 2006",
            "Marzo 13 de 2007",
            "por definir"
    };
    private static final String[] RUTAS_FOTOS = {
            "Recursos/fotosAbout/ipa.png",   // Ibuki
            "Recursos/fotosAbout/afc.png",   // Angel
            "Recursos/fotosAbout/PD.jpg",   // Jorshan
    };

    public VentanaAcercaDe(Frame parent) {
        super(parent, "Acerca de", true);
        setSize(720, 370);
        setLocationRelativeTo(parent);
        setResizable(false);

        PanelFondo panel = new PanelFondo("Recursos/Fondo1.jpg");
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Título
        JLabel lblTitulo = new JLabel("CREADORES DEL PROYECTO", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitulo.setForeground(Color.WHITE);
        panel.add(lblTitulo, BorderLayout.NORTH);

        // Una tarjeta por creador, una al lado de la otra
        JPanel panelCreadores = new JPanel(new GridLayout(1, NOMBRES.length, 15, 0));
        panelCreadores.setOpaque(false);
        for (int i = 0; i < NOMBRES.length; i++) {
            panelCreadores.add(crearTarjeta(NOMBRES[i], FECHAS_NACIMIENTO[i], RUTAS_FOTOS[i]));
        }
        panel.add(panelCreadores, BorderLayout.CENTER);

        // Botón de cierre
        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.addActionListener(e -> dispose());
        JPanel panelBoton = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelBoton.setOpaque(false);
        panelBoton.add(btnCerrar);
        panel.add(panelBoton, BorderLayout.SOUTH);

        add(panel);
    }

    // Crea la tarjeta de un creador: foto arriba, nombre y fecha de nacimiento abajo
    private JPanel crearTarjeta(String nombre, String fechaNacimiento, String rutaFoto) {
        JPanel tarjeta = new JPanel();
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setOpaque(false);

        JLabel lblFoto = new JLabel();
        lblFoto.setHorizontalAlignment(SwingConstants.CENTER);
        lblFoto.setForeground(Color.WHITE);
        lblFoto.setPreferredSize(new Dimension(TAM_FOTO, TAM_FOTO));
        lblFoto.setMaximumSize(new Dimension(TAM_FOTO, TAM_FOTO));
        lblFoto.setBorder(BorderFactory.createLineBorder(new Color(255, 215, 0), 2));
        lblFoto.setAlignmentX(Component.CENTER_ALIGNMENT);
        ImageIcon foto = cargarFoto(rutaFoto);
        if (foto != null) {
            lblFoto.setIcon(foto);
        } else {
            lblFoto.setText("Sin foto");
        }

        JLabel lblNombre = new JLabel(nombre);
        lblNombre.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblNombre.setForeground(Color.WHITE);
        lblNombre.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblFecha = new JLabel("Fecha de nac.: " + fechaNacimiento);
        lblFecha.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblFecha.setForeground(new Color(220, 235, 255));
        lblFecha.setAlignmentX(Component.CENTER_ALIGNMENT);

        tarjeta.add(Box.createVerticalStrut(10));
        tarjeta.add(lblFoto);
        tarjeta.add(Box.createVerticalStrut(12));
        tarjeta.add(lblNombre);
        tarjeta.add(Box.createVerticalStrut(4));
        tarjeta.add(lblFecha);
        return tarjeta;
    }

    // Carga una imagen de los recursos y la ajusta al cuadro de la foto sin deformarla
    private ImageIcon cargarFoto(String ruta) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(ruta)) {
            if (is == null) {
                System.err.println("No se encontró la imagen: " + ruta);
                return null;
            }
            BufferedImage original = ImageIO.read(is);
            if (original == null) {
                return null;
            }
            double escala = Math.min((double) TAM_FOTO / original.getWidth(),
                    (double) TAM_FOTO / original.getHeight());
            int ancho = Math.max(1, (int) (original.getWidth() * escala));
            int alto = Math.max(1, (int) (original.getHeight() * escala));

            BufferedImage escalada = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = escalada.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(original, 0, 0, ancho, alto, null);
            g2.dispose();
            return new ImageIcon(escalada);
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("No se pudo procesar la imagen: " + ruta);
            return null;
        }
    }
}
