// Paquete model.
package model;

// Clases de JavaCV: Frame es una imagen que entrega la cámara y los convertidores pasan entre formatos de imagen.
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;
// Clases de OpenCV: Mat es una imagen como matriz, Rect y RectVector guardan los rostros detectados y Size es un
// tamaño.
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.RectVector;
import org.bytedeco.opencv.opencv_core.Size;
// CascadeClassifier es el detector de rostros de OpenCV.
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;
// Constante y función de OpenCV para pasar una imagen de color a gris.
import static org.bytedeco.opencv.global.opencv_imgproc.COLOR_BGR2GRAY;
import static org.bytedeco.opencv.global.opencv_imgproc.cvtColor;

// Clases de Java para copiar imágenes, leer archivos y copiar el clasificador a un archivo temporal.
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
// Implementa la interfaz FuenteRostros. El controlador solo conoce la interfaz, así que esta clase se puede
// cambiar por otra sin tocar el controlador.
public class FuenteRostrosJavaCV implements FuenteRostros {

    // Nombre del archivo XML del clasificador de rostros frontales (Haar Cascade).
    private static final String NOMBRE_CASCADA = "haarcascade_frontalface_default.xml";
    // Ruta de ese archivo dentro de la carpeta de recursos del proyecto.
    private static final String RECURSO_CASCADA = "Recursos/Facial/" + NOMBRE_CASCADA;
    // Rutas del sistema donde puede estar el clasificador si no se encuentra en el proyecto (pensadas para Linux).
    private static final String[] RUTAS_CASCADA = {
            "cascades/" + NOMBRE_CASCADA,
            "/usr/share/opencv4/haarcascades/" + NOMBRE_CASCADA,
            "/usr/share/opencv/haarcascades/" + NOMBRE_CASCADA,
            "/usr/local/share/opencv4/haarcascades/" + NOMBRE_CASCADA
    };

    // Ancho que se le pide a la cámara.
    private static final int ANCHO_CAMARA = 640;
    // Alto que se le pide a la cámara.
    private static final int ALTO_CAMARA = 480;
    // La cámara tarda en arrancar y al principio entrega imágenes vacías. Se tolera este número de lecturas vacías
    // seguidas.
    private static final int INTENTOS_SIN_IMAGEN = 100;   // Cuántas lecturas vacías seguidas se toleran (la cámara tarda en arrancar)

    // Objeto que controla la cámara web.
    private final OpenCVFrameGrabber camara;
    // Detector que encuentra los rostros dentro de una imagen.
    private final CascadeClassifier detector;
    // Convierte un Frame de JavaCV a Mat de OpenCV.
    private final OpenCVFrameConverter.ToMat aMat = new OpenCVFrameConverter.ToMat();
    // Convierte un Frame de JavaCV a BufferedImage de Java (para mostrarlo en la ventana).
    private final Java2DFrameConverter aImagen = new Java2DFrameConverter();

    // Abre la cámara y carga el detector. Lanza IllegalStateException con un mensaje claro si algo falla.
    // Constructor: carga el detector y enciende la cámara. Si algo falla lanza un error con un mensaje claro.
    public FuenteRostrosJavaCV() {
        // Se busca el archivo del clasificador y se carga.
        detector = new CascadeClassifier(localizarCascada());
        // Si el detector quedó vacío es que no se pudo leer el archivo.
        if (detector.empty()) {
            throw new IllegalStateException("No se pudo cargar el clasificador de rostros (" + NOMBRE_CASCADA + ").");
        }

        // Crea la conexión con la cámara. El 0 es la primera cámara del computador.
        camara = new OpenCVFrameGrabber(0);
        // Se configura el ancho de la imagen.
        camara.setImageWidth(ANCHO_CAMARA);
        // Se configura el alto de la imagen.
        camara.setImageHeight(ALTO_CAMARA);
        try {
            // Enciende la cámara.
            camara.start();
        } catch (FrameGrabber.Exception e) {
            // Si no se pudo abrir la cámara, se intenta liberar...
            try {
                camara.release();
            } catch (FrameGrabber.Exception ignorada) {
                // Ya falló al abrir; no hay nada más que liberar
            }
            // ...y se avisa con un mensaje que el usuario pueda entender.
            throw new IllegalStateException("No se pudo abrir la cámara. Verifique que esté conectada y que otro programa no la esté usando.", e);
        }
    }

    @Override
    // Lee un cuadro de la cámara y busca el rostro en él.
    public Cuadro leer() throws IOException {
        try {
            // Aquí se guarda lo que entregue la cámara.
            Frame cuadro = null;
            // Se repite la lectura hasta tener una imagen o hasta agotar los intentos.
            for (int i = 0; i < INTENTOS_SIN_IMAGEN && (cuadro == null || cuadro.image == null); i++) {
                // grab() pide una imagen a la cámara.
                cuadro = camara.grab();
                if (cuadro == null || cuadro.image == null) {
                    // Si vino vacía, se espera 20 milisegundos antes de volver a intentar.
                    Thread.sleep(20);
                }
            }
            // Si después de todos los intentos no hay imagen, se devuelve null y el controlador lo toma como error
            // de cámara.
            if (cuadro == null || cuadro.image == null) {
                return null;
            }

            // Se convierte a imagen de Java y se copia (ver el método copiar, más abajo).
            BufferedImage imagen = copiar(aImagen.convert(cuadro));
            // Se busca el rostro más grande de la imagen.
            Rectangle rostro = detectarRostroMasGrande(cuadro);
            // Si hay rostro, se convierte en el vector de 10 000 valores. Si no, queda null.
            float[] cara = (rostro == null) ? null : ProcesadorRostro.vectorDe(imagen, rostro);
            // Se devuelve el cuadro con la imagen, el recuadro del rostro y el vector.
            return new Cuadro(imagen, rostro, cara);
        } catch (FrameGrabber.Exception e) {
            // Si falla la lectura, se convierte en un error de entrada y salida con mensaje.
            throw new IOException("Error al leer la cámara: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            // Si el hilo es interrumpido se vuelve a marcar la interrupción y se devuelve null.
            Thread.currentThread().interrupt();
            return null;
        }
    }

    @Override
    // Apaga la cámara. Se llama sola al terminar porque la clase usa try-with-resources en el controlador.
    public void close() {
        try {
            // Detiene la captura.
            camara.stop();
            // Libera la cámara para que otros programas puedan usarla.
            camara.release();
        } catch (FrameGrabber.Exception e) {
            // Si falla al cerrar, solo se muestra el error.
            System.err.println("Error al cerrar la cámara: " + e.getMessage());
        }
    }

    // ---------- Detección ----------

    // Busca rostros en el cuadro y devuelve el más grande.
    private Rectangle detectarRostroMasGrande(Frame cuadro) {
        // Convierte el cuadro a una matriz de OpenCV. Esa matriz pertenece al convertidor, por eso no se cierra
        // aquí.
        Mat color = aMat.convert(cuadro);   // Pertenece al convertidor: no se cierra aquí
        // Si no hay imagen, no hay nada que buscar.
        if (color == null || color.empty()) {
            return null;
        }
        // Matriz donde se guarda la imagen en gris.
        Mat gris = new Mat();
        // Lista donde el detector deja los rostros que encuentre.
        RectVector rostros = new RectVector();
        try {
            // Pasa la imagen de color a gris, porque el detector trabaja con grises.
            cvtColor(color, gris, COLOR_BGR2GRAY);
            // Busca rostros. El 1.3 es cuánto se reduce la imagen en cada pasada y el 5 es cuántas detecciones
            // vecinas hacen falta para aceptar un rostro. Los dos Size vacíos significan sin tamaño mínimo ni
            // máximo.
            detector.detectMultiScale(gris, rostros, 1.3, 5, 0, new Size(), new Size());

            // Aquí se va guardando el rostro más grande.
            Rectangle mejor = null;
            // Recorre todos los rostros detectados.
            for (long i = 0; i < rostros.size(); i++) {
                // Rostro actual.
                Rect r = rostros.get(i);
                // Si es el primero o tiene más área (ancho por alto) que el mejor hasta ahora...
                if (mejor == null || (long) r.width() * r.height() > (long) mejor.width * mejor.height) {
                    // ...pasa a ser el mejor. Se convierte a Rectangle de Java.
                    mejor = new Rectangle(r.x(), r.y(), r.width(), r.height());
                }
            }
            // Se devuelve el rostro más grande, o null si no se encontró ninguno.
            return mejor;
        } finally {
            // El finally se ejecuta siempre, haya o no error. Aquí se libera la memoria de la imagen en gris.
            // OpenCV usa memoria fuera de Java y hay que cerrarla a mano.
            gris.close();
            // Libera también la lista de rostros.
            rostros.close();
        }
    }

    // El convertidor reutiliza la misma imagen en cada cuadro; se copia para poder mostrarla desde otro hilo
    // Hace una copia de la imagen. El convertidor reutiliza la misma imagen en cada cuadro, y como se muestra
    // desde otro hilo hay que copiarla.
    private static BufferedImage copiar(BufferedImage origen) {
        // Crea una imagen nueva vacía del mismo tamaño.
        BufferedImage copia = new BufferedImage(origen.getWidth(), origen.getHeight(), BufferedImage.TYPE_INT_RGB);
        // Obtiene el objeto para dibujar sobre esa imagen.
        Graphics2D g2 = copia.createGraphics();
        // Dibuja encima la imagen original, que la copia.
        g2.drawImage(origen, 0, 0, null);
        // Libera el objeto de dibujo.
        g2.dispose();
        // Devuelve la copia.
        return copia;
    }

    // ---------- Clasificador Haar ----------

    // Busca el archivo del clasificador: primero dentro del proyecto (Recursos/Facial) y luego en rutas comunes
    // Busca el archivo del clasificador. Primero dentro del proyecto y si no, en las rutas del sistema.
    private static String localizarCascada() {
        // Intenta abrir el archivo desde los recursos del proyecto.
        try (InputStream is = FuenteRostrosJavaCV.class.getClassLoader().getResourceAsStream(RECURSO_CASCADA)) {
            // Si existe dentro del proyecto...
            if (is != null) {
                // OpenCV necesita una ruta de archivo real, así que se copia a un temporal
                // ...OpenCV necesita una ruta de archivo real, así que se crea un archivo temporal...
                File temporal = File.createTempFile("haarcascade", ".xml");
                // ...que se borra cuando el programa termina...
                temporal.deleteOnExit();
                // ...y se copia el contenido del recurso a ese temporal.
                Files.copy(is, temporal.toPath(), StandardCopyOption.REPLACE_EXISTING);
                // Se devuelve la ruta del temporal.
                return temporal.getAbsolutePath();
            }
        } catch (IOException e) {
            // Si hubo error al leerlo, solo se muestra y se sigue con las otras rutas.
            System.err.println("No se pudo leer el clasificador del proyecto: " + e.getMessage());
        }
        // Prueba cada ruta del sistema.
        for (String ruta : RUTAS_CASCADA) {
            // La primera que sea un archivo existente es la que se usa.
            if (new File(ruta).isFile()) {
                return ruta;
            }
        }
        // Si no se encontró por ningún lado, se lanza un error que explica qué hacer.
        throw new IllegalStateException("No se encontró el clasificador de rostros. Copie " + NOMBRE_CASCADA
                + " a src/Recursos/Facial/ (o instale el paquete opencv-data).");
    }
}
