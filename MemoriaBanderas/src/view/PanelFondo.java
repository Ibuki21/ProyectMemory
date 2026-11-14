package view;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// Panel con imagen de fondo estática. Es la única copia de este código: todas las ventanas la usan.
// La imagen se lee del disco una sola vez y se guarda ya escalada al tamaño del panel,
// así cada repintado es solo copiar píxeles en vez de reescalar la imagen completa.
public class PanelFondo extends JPanel {

    private static final Color COLOR_RESPALDO = new Color(30, 60, 110);
    private static final BufferedImage SIN_IMAGEN = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);

    // Imágenes originales ya leídas, compartidas entre todas las ventanas
    private static final Map<String, BufferedImage> ORIGINALES = new ConcurrentHashMap<>();

    private final BufferedImage original;
    private BufferedImage escalada;

    public PanelFondo(String ruta) {
        this.original = ORIGINALES.computeIfAbsent(ruta, PanelFondo::cargar);
    }

    private static BufferedImage cargar(String ruta) {
        try (InputStream is = PanelFondo.class.getClassLoader().getResourceAsStream(ruta)) {
            if (is == null) {
                System.err.println("No se pudo cargar la imagen de fondo: " + ruta);
                return SIN_IMAGEN;
            }
            BufferedImage imagen = ImageIO.read(is);
            return (imagen != null) ? imagen : SIN_IMAGEN;
        } catch (IOException | RuntimeException e) {
            System.err.println("No se pudo procesar la imagen de fondo: " + ruta);
            return SIN_IMAGEN;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        int ancho = getWidth();
        int alto = getHeight();
        if (ancho <= 0 || alto <= 0) {
            return;
        }
        if (original == SIN_IMAGEN) {
            g.setColor(COLOR_RESPALDO);
            g.fillRect(0, 0, ancho, alto);
            return;
        }
        if (escalada == null || escalada.getWidth() != ancho || escalada.getHeight() != alto) {
            escalada = escalar(original, ancho, alto);
        }
        g.drawImage(escalada, 0, 0, null);
    }

    private static BufferedImage escalar(BufferedImage origen, int ancho, int alto) {
        int tipo = origen.getColorModel().hasAlpha() ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
        BufferedImage resultado = new BufferedImage(ancho, alto, tipo);
        Graphics2D g2 = resultado.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(origen, 0, 0, ancho, alto, null);
        g2.dispose();
        return resultado;
    }
}
