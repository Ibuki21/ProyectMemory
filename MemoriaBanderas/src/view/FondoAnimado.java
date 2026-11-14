package view;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

// Fondo animado a partir de un GIF.
// El GIF se decodifica UNA sola vez (en un hilo aparte) y sus cuadros se guardan en memoria;
// después la animación es solo mostrar un cuadro tras otro con un Timer. Así no se vuelve a
// decodificar el GIF en bucle (que es lo que gastaba casi toda la CPU) y se puede pausar
// la animación cuando la ventana no se ve.
public class FondoAnimado {

    private static final Color COLOR_RESPALDO = new Color(20, 30, 50);
    private static final int ANCHO_MAXIMO = 1280;
    private static final int ALTO_MAXIMO = 720;
    private static final int MS_MINIMO_POR_CUADRO = 40;   // Máximo 25 cuadros por segundo

    private final Runnable alCambiarCuadro;
    private BufferedImage[] cuadros;     // Solo se toca desde el hilo de la interfaz
    private int[] duracionesMs;
    private int indice;
    private boolean activo;
    private Timer timer;

    // 'alCambiarCuadro' se llama (en el hilo de la interfaz) cada vez que hay que repintar
    public FondoAnimado(String ruta, Runnable alCambiarCuadro) {
        this.alCambiarCuadro = alCambiarCuadro;
        Thread hilo = new Thread(() -> cargarEnSegundoPlano(ruta), "carga-fondo-animado");
        hilo.setDaemon(true);
        hilo.setPriority(Thread.MIN_PRIORITY);
        hilo.start();
    }

    // Dibuja el cuadro actual estirado al tamaño indicado
    public void pintar(Graphics g, int ancho, int alto) {
        if (cuadros == null) {
            g.setColor(COLOR_RESPALDO);
            g.fillRect(0, 0, ancho, alto);
            return;
        }
        g.drawImage(cuadros[indice], 0, 0, ancho, alto, null);
    }

    // Inicia o continúa la animación
    public void reanudar() {
        activo = true;
        programarSiguiente();
    }

    // Detiene la animación sin perder el cuadro actual
    public void pausar() {
        activo = false;
        if (timer != null) {
            timer.stop();
        }
    }

    private void programarSiguiente() {
        if (!activo || cuadros == null || cuadros.length < 2) {
            return;
        }
        if (timer == null) {
            timer = new Timer(duracionesMs[indice], e -> {
                indice = (indice + 1) % cuadros.length;
                alCambiarCuadro.run();
                timer.setInitialDelay(duracionesMs[indice]);
                timer.restart();
            });
            timer.setRepeats(false);
        }
        timer.setInitialDelay(duracionesMs[indice]);
        timer.restart();
    }

    // ---------- Carga del GIF ----------

    private void cargarEnSegundoPlano(String ruta) {
        List<BufferedImage> lista = new ArrayList<>();
        List<Integer> duraciones = new ArrayList<>();
        try {
            leerCuadros(ruta, lista, duraciones);
        } catch (Exception | OutOfMemoryError e) {
            System.err.println("No se pudo cargar el fondo animado " + ruta + ": " + e);
            return;
        }
        if (lista.isEmpty()) {
            return;
        }
        BufferedImage[] arregloCuadros = lista.toArray(new BufferedImage[0]);
        int[] arregloDuraciones = new int[duraciones.size()];
        for (int i = 0; i < arregloDuraciones.length; i++) {
            arregloDuraciones[i] = duraciones.get(i);
        }
        SwingUtilities.invokeLater(() -> {
            cuadros = arregloCuadros;
            duracionesMs = arregloDuraciones;
            indice = 0;
            alCambiarCuadro.run();
            programarSiguiente();
        });
    }

    private static void leerCuadros(String ruta, List<BufferedImage> salida, List<Integer> duracionesSalida) throws Exception {
        try (InputStream is = FondoAnimado.class.getClassLoader().getResourceAsStream(ruta)) {
            if (is == null) {
                System.err.println("No se encontró el fondo animado: " + ruta);
                return;
            }
            try (ImageInputStream entrada = ImageIO.createImageInputStream(is)) {
                Iterator<ImageReader> lectores = ImageIO.getImageReadersByFormatName("gif");
                if (!lectores.hasNext()) {
                    return;
                }
                ImageReader lector = lectores.next();
                try {
                    lector.setInput(entrada, false, false);
                    int total = lector.getNumImages(true);
                    if (total <= 0) {
                        return;
                    }

                    // Tamaño lógico del GIF y tamaño al que se guardan los cuadros
                    int anchoGif = lector.getWidth(0);
                    int altoGif = lector.getHeight(0);
                    int[] pantalla = leerPantallaLogica(lector);
                    if (pantalla != null) {
                        anchoGif = pantalla[0];
                        altoGif = pantalla[1];
                    }
                    double escala = Math.min(1.0, Math.min((double) ANCHO_MAXIMO / anchoGif, (double) ALTO_MAXIMO / altoGif));
                    int anchoSalida = Math.max(1, (int) Math.round(anchoGif * escala));
                    int altoSalida = Math.max(1, (int) Math.round(altoGif * escala));

                    // Límite de memoria: si hay demasiados cuadros, se guarda uno de cada 'salto'
                    long presupuesto = Math.min(256L * 1024 * 1024, Runtime.getRuntime().maxMemory() / 4);
                    long bytesPorCuadro = (long) anchoSalida * altoSalida * 4;
                    int salto = (int) Math.max(1, (total * bytesPorCuadro + presupuesto - 1) / presupuesto);

                    BufferedImage lienzo = new BufferedImage(anchoGif, altoGif, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D gl = lienzo.createGraphics();
                    int acumuladoMs = 0;

                    for (int i = 0; i < total; i++) {
                        BufferedImage cuadro = lector.read(i);
                        IIOMetadata meta = lector.getImageMetadata(i);
                        int izq = 0;
                        int arr = 0;
                        int retardo = 10;
                        String disposicion = "none";
                        if (meta != null) {
                            Node raiz = meta.getAsTree("javax_imageio_gif_image_1.0");
                            IIOMetadataNode descriptor = primerHijo(raiz, "ImageDescriptor");
                            if (descriptor != null) {
                                izq = entero(descriptor, "imageLeftPosition", 0);
                                arr = entero(descriptor, "imageTopPosition", 0);
                            }
                            IIOMetadataNode control = primerHijo(raiz, "GraphicControlExtension");
                            if (control != null) {
                                retardo = entero(control, "delayTime", 10);
                                String d = control.getAttribute("disposalMethod");
                                if (d != null && !d.isEmpty()) {
                                    disposicion = d;
                                }
                            }
                        }

                        // Copia del lienzo antes de dibujar, por si hay que restaurarlo
                        BufferedImage previo = null;
                        if (disposicion.equals("restoreToPrevious")) {
                            previo = copiar(lienzo);
                        }

                        gl.drawImage(cuadro, izq, arr, null);

                        // El retardo en GIF viene en centésimas; valores muy bajos se tratan como 100 ms
                        int ms = (retardo <= 1) ? 100 : retardo * 10;
                        acumuladoMs += ms;
                        if (i % salto == 0) {
                            salida.add(instantanea(lienzo, anchoSalida, altoSalida));
                            // Los cuadros omitidos suman su duración al cuadro guardado
                            duracionesSalida.add(Math.max(MS_MINIMO_POR_CUADRO, acumuladoMs));
                            acumuladoMs = 0;
                        } else {
                            int ultimo = duracionesSalida.size() - 1;
                            if (ultimo >= 0) {
                                duracionesSalida.set(ultimo, Math.max(MS_MINIMO_POR_CUADRO, duracionesSalida.get(ultimo) + ms));
                                acumuladoMs = 0;
                            }
                        }

                        // Aplicar el método de descarte para el cuadro siguiente
                        if (disposicion.equals("restoreToBackgroundColor")) {
                            gl.setComposite(java.awt.AlphaComposite.Clear);
                            gl.fillRect(izq, arr, cuadro.getWidth(), cuadro.getHeight());
                            gl.setComposite(java.awt.AlphaComposite.SrcOver);
                        } else if (previo != null) {
                            gl.setComposite(java.awt.AlphaComposite.Src);
                            gl.drawImage(previo, 0, 0, null);
                            gl.setComposite(java.awt.AlphaComposite.SrcOver);
                        }
                    }
                    gl.dispose();
                } finally {
                    lector.dispose();
                }
            }
        }
    }

    // Tamaño de pantalla lógica del GIF (ancho, alto), o null si no se puede leer
    private static int[] leerPantallaLogica(ImageReader lector) {
        try {
            IIOMetadata flujo = lector.getStreamMetadata();
            if (flujo == null) {
                return null;
            }
            Node raiz = flujo.getAsTree("javax_imageio_gif_stream_1.0");
            IIOMetadataNode pantalla = primerHijo(raiz, "LogicalScreenDescriptor");
            if (pantalla == null) {
                return null;
            }
            int ancho = entero(pantalla, "logicalScreenWidth", 0);
            int alto = entero(pantalla, "logicalScreenHeight", 0);
            return (ancho > 0 && alto > 0) ? new int[]{ancho, alto} : null;
        } catch (Exception e) {
            return null;
        }
    }

    private static IIOMetadataNode primerHijo(Node raiz, String nombre) {
        for (Node n = raiz.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n.getNodeName().equals(nombre) && n instanceof IIOMetadataNode) {
                return (IIOMetadataNode) n;
            }
        }
        return null;
    }

    private static int entero(IIOMetadataNode nodo, String atributo, int porDefecto) {
        NamedNodeMap atributos = nodo.getAttributes();
        Node valor = atributos.getNamedItem(atributo);
        if (valor == null) {
            return porDefecto;
        }
        try {
            return Integer.parseInt(valor.getNodeValue().trim());
        } catch (NumberFormatException e) {
            return porDefecto;
        }
    }

    private static BufferedImage copiar(BufferedImage origen) {
        BufferedImage copia = new BufferedImage(origen.getWidth(), origen.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = copia.createGraphics();
        g2.setComposite(java.awt.AlphaComposite.Src);
        g2.drawImage(origen, 0, 0, null);
        g2.dispose();
        return copia;
    }

    // Guarda el lienzo como imagen opaca (rápida de dibujar), ya reducida si el GIF es muy grande
    private static BufferedImage instantanea(BufferedImage lienzo, int ancho, int alto) {
        BufferedImage resultado = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = resultado.createGraphics();
        g2.setColor(COLOR_RESPALDO);
        g2.fillRect(0, 0, ancho, alto);
        if (ancho != lienzo.getWidth() || alto != lienzo.getHeight()) {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        }
        g2.drawImage(lienzo, 0, 0, ancho, alto, null);
        g2.dispose();
        return resultado;
    }
}
