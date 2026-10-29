import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;

// Construye las fichas (casillas) del Modo Patrones.
public class ConstructorFichas {

    private static final String RUTA_BANDERAS = "Recursos/w320/";

    // Tamaño de la casilla y de la bandera (proporción 3:2, ajustada a la casilla)
    private static final int ANCHO_FICHA = 80;
    private static final int ALTO_FICHA = 56;
    private static final int ANCHO_BANDERA = 69;
    private static final int ALTO_BANDERA = 46;

    // Banderas fijas: siempre salen en el mismo orden en cada partida
    private static final String[] BANDERAS = {
            "cr", "us", "mx", "br", "ar", "cl",
            "co", "pe", "ve", "ec", "uy", "py",
            "bo", "pa", "gt", "hn", "sv", "ni",
            "cu", "do", "es", "fr", "de", "it",
            "pt", "gb", "jp", "kr", "cn", "ca",
            "au", "nz", "za", "eg", "ma", "ng"
    };

    // Crea una matriz de botones con una bandera en cada casilla.
    public static JButton[][] crearMatrizFichas(int filas, int columnas) {
        JButton[][] fichas = new JButton[filas][columnas];
        int indice = 0;
        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                if (indice < BANDERAS.length) {
                    fichas[i][j] = crearFicha(BANDERAS[indice]);
                    indice++;
                }
            }
        }
        return fichas;
    }

    private static JButton crearFicha(String codigoBandera) {
        JButton ficha = new JButton();
        ImageIcon icono = cargarBandera(codigoBandera, ANCHO_BANDERA, ALTO_BANDERA);
        if (icono != null) {
            ficha.setIcon(icono);
        } else {
            ficha.setText(codigoBandera.toUpperCase()); // Texto de respaldo por si falla
        }
        ficha.setMargin(new Insets(0, 0, 0, 0));
        ficha.setPreferredSize(new Dimension(ANCHO_FICHA, ALTO_FICHA));
        ficha.setFocusPainted(false);
        return ficha;
    }

    // Carga una bandera desde los recursos internos usando InputStream
    private static ImageIcon cargarBandera(String codigo, int ancho, int alto) {
        String rutaCompleta = RUTA_BANDERAS + codigo + ".png";
        try (InputStream is = ConstructorFichas.class.getClassLoader().getResourceAsStream(rutaCompleta)) {
            if (is == null) {
                System.err.println("No se encontró el archivo de bandera en los recursos: " + rutaCompleta);
                return null;
            }
            BufferedImage original = ImageIO.read(is);
            if (original == null) {
                return null;
            }
            BufferedImage escalada = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = escalada.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(original, 0, 0, ancho, alto, null);
            g2.dispose();
            return new ImageIcon(escalada);
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("No se pudo procesar la bandera: " + rutaCompleta);
            return null;
        }
    }
}