import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

//Construye las fichas (casillas) del Modo Patrones.

public class ConstructorFichas {

    private static final String CARPETA_BANDERAS = "Recursos/w320/";

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

   //Crea una matriz de botones con una bandera en cada casilla.

    public static JButton[][] crearMatrizFichas(int filas, int columnas) {
        JButton[][] fichas = new JButton[filas][columnas];
        int indice = 0;
        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                fichas[i][j] = crearFicha(BANDERAS[indice]);
                indice++;
            }
        }
        return fichas;
    }

    private static JButton crearFicha(String codigoBandera) {
        JButton ficha = new JButton();
        ficha.setIcon(cargarBandera(codigoBandera, ANCHO_BANDERA, ALTO_BANDERA));
        ficha.setMargin(new Insets(0, 0, 0, 0));
        ficha.setPreferredSize(new Dimension(ANCHO_FICHA, ALTO_FICHA));
        ficha.setFocusPainted(false);
        return ficha;
    }

    //Carga una bandera desde la carpeta de recursos y la escala
    //al tamaño exacto indicado. Si no se encuentra, imprime la ruta
    //buscada en la consola y devuelve null.
    
    private static ImageIcon cargarBandera(String codigo, int ancho, int alto) {
        File archivo = new File(CARPETA_BANDERAS + codigo + ".png");
        try {
            BufferedImage original = ImageIO.read(archivo);
            BufferedImage escalada = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = escalada.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(original, 0, 0, ancho, alto, null);
            g2.dispose();
            return new ImageIcon(escalada);
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("No se pudo cargar la bandera: " + archivo.getAbsolutePath());
            return null;
        }
    }
}
