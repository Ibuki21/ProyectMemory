package model;

import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.RectVector;
import org.bytedeco.opencv.opencv_core.Size;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;
import static org.bytedeco.opencv.global.opencv_imgproc.COLOR_BGR2GRAY;
import static org.bytedeco.opencv.global.opencv_imgproc.cvtColor;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

// Cámara web con detección de rostros usando OpenCV mediante JavaCV.
// Es la ÚNICA clase del proyecto que depende de OpenCV.
//
// Funciona igual que el proyecto en Python: clasificador Haar de rostros frontales
// (detectMultiScale con escala 1.3 y 5 vecinos) y, de todos los rostros del cuadro, se queda con el más grande.
public class FuenteRostrosJavaCV implements FuenteRostros {

    private static final String NOMBRE_CASCADA = "haarcascade_frontalface_default.xml";
    private static final String RECURSO_CASCADA = "Recursos/Facial/" + NOMBRE_CASCADA;
    private static final String[] RUTAS_CASCADA = {
            "cascades/" + NOMBRE_CASCADA,
            "/usr/share/opencv4/haarcascades/" + NOMBRE_CASCADA,
            "/usr/share/opencv/haarcascades/" + NOMBRE_CASCADA,
            "/usr/local/share/opencv4/haarcascades/" + NOMBRE_CASCADA
    };

    private static final int ANCHO_CAMARA = 640;
    private static final int ALTO_CAMARA = 480;
    private static final int INTENTOS_SIN_IMAGEN = 100;   // Cuántas lecturas vacías seguidas se toleran (la cámara tarda en arrancar)

    private final OpenCVFrameGrabber camara;
    private final CascadeClassifier detector;
    private final OpenCVFrameConverter.ToMat aMat = new OpenCVFrameConverter.ToMat();
    private final Java2DFrameConverter aImagen = new Java2DFrameConverter();

    // Abre la cámara y carga el detector. Lanza IllegalStateException con un mensaje claro si algo falla.
    public FuenteRostrosJavaCV() {
        detector = new CascadeClassifier(localizarCascada());
        if (detector.empty()) {
            throw new IllegalStateException("No se pudo cargar el clasificador de rostros (" + NOMBRE_CASCADA + ").");
        }

        camara = new OpenCVFrameGrabber(0);
        camara.setImageWidth(ANCHO_CAMARA);
        camara.setImageHeight(ALTO_CAMARA);
        try {
            camara.start();
        } catch (FrameGrabber.Exception e) {
            try {
                camara.release();
            } catch (FrameGrabber.Exception ignorada) {
                // Ya falló al abrir; no hay nada más que liberar
            }
            throw new IllegalStateException("No se pudo abrir la cámara. Verifique que esté conectada y que otro programa no la esté usando.", e);
        }
    }

    @Override
    public Cuadro leer() throws IOException {
        try {
            Frame cuadro = null;
            for (int i = 0; i < INTENTOS_SIN_IMAGEN && (cuadro == null || cuadro.image == null); i++) {
                cuadro = camara.grab();
                if (cuadro == null || cuadro.image == null) {
                    Thread.sleep(20);
                }
            }
            if (cuadro == null || cuadro.image == null) {
                return null;
            }

            BufferedImage imagen = copiar(aImagen.convert(cuadro));
            Rectangle rostro = detectarRostroMasGrande(cuadro);
            float[] cara = (rostro == null) ? null : ProcesadorRostro.vectorDe(imagen, rostro);
            return new Cuadro(imagen, rostro, cara);
        } catch (FrameGrabber.Exception e) {
            throw new IOException("Error al leer la cámara: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    @Override
    public void close() {
        try {
            camara.stop();
            camara.release();
        } catch (FrameGrabber.Exception e) {
            System.err.println("Error al cerrar la cámara: " + e.getMessage());
        }
    }

    // ---------- Detección ----------

    private Rectangle detectarRostroMasGrande(Frame cuadro) {
        Mat color = aMat.convert(cuadro);   // Pertenece al convertidor: no se cierra aquí
        if (color == null || color.empty()) {
            return null;
        }
        Mat gris = new Mat();
        RectVector rostros = new RectVector();
        try {
            cvtColor(color, gris, COLOR_BGR2GRAY);
            detector.detectMultiScale(gris, rostros, 1.3, 5, 0, new Size(), new Size());

            Rectangle mejor = null;
            for (long i = 0; i < rostros.size(); i++) {
                Rect r = rostros.get(i);
                if (mejor == null || (long) r.width() * r.height() > (long) mejor.width * mejor.height) {
                    mejor = new Rectangle(r.x(), r.y(), r.width(), r.height());
                }
            }
            return mejor;
        } finally {
            gris.close();
            rostros.close();
        }
    }

    // El convertidor reutiliza la misma imagen en cada cuadro; se copia para poder mostrarla desde otro hilo
    private static BufferedImage copiar(BufferedImage origen) {
        BufferedImage copia = new BufferedImage(origen.getWidth(), origen.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = copia.createGraphics();
        g2.drawImage(origen, 0, 0, null);
        g2.dispose();
        return copia;
    }

    // ---------- Clasificador Haar ----------

    // Busca el archivo del clasificador: primero dentro del proyecto (Recursos/Facial) y luego en rutas comunes
    private static String localizarCascada() {
        try (InputStream is = FuenteRostrosJavaCV.class.getClassLoader().getResourceAsStream(RECURSO_CASCADA)) {
            if (is != null) {
                // OpenCV necesita una ruta de archivo real, así que se copia a un temporal
                File temporal = File.createTempFile("haarcascade", ".xml");
                temporal.deleteOnExit();
                Files.copy(is, temporal.toPath(), StandardCopyOption.REPLACE_EXISTING);
                return temporal.getAbsolutePath();
            }
        } catch (IOException e) {
            System.err.println("No se pudo leer el clasificador del proyecto: " + e.getMessage());
        }
        for (String ruta : RUTAS_CASCADA) {
            if (new File(ruta).isFile()) {
                return ruta;
            }
        }
        throw new IllegalStateException("No se encontró el clasificador de rostros. Copie " + NOMBRE_CASCADA
                + " a src/Recursos/Facial/ (o instale el paquete opencv-data).");
    }
}
