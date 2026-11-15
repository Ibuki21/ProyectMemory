package model;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

// Datos faciales de los usuarios: guardado en disco y comparación de rostros.
// No usa cámara ni OpenCV, solo trabaja con el "vector" de un rostro:
// una imagen en gris de 100x100 píxeles (10 000 valores) ya recortada.
//
// Los vectores se guardan en el archivo rostros.dat, junto a usuarios.txt.
public class DatosFaciales {

    public static final int LADO = 100;                        // Cada rostro se reduce a 100x100 píxeles
    public static final int TAMANO_VECTOR = LADO * LADO;
    public static final double UMBRAL_COINCIDENCIA = 3000.0;   // Distancia máxima para aceptar un rostro

    private static final String RUTA_ARCHIVO = "rostros.dat";
    private static final int VERSION_FORMATO = 1;

    private DatosFaciales() {
    }

    // Resultado de comparar un rostro contra los registrados
    public static final class Coincidencia {
        private final String usuario;
        private final double distancia;

        Coincidencia(String usuario, double distancia) {
            this.usuario = usuario;
            this.distancia = distancia;
        }

        // Usuario (en minúsculas) del rostro más parecido
        public String getUsuario() {
            return usuario;
        }

        // Distancia al rostro más parecido: mientras más baja, más parecidos
        public double getDistancia() {
            return distancia;
        }

        // Verdadero si el parecido es suficiente para dar acceso
        public boolean esValida() {
            return distancia < UMBRAL_COINCIDENCIA;
        }
    }

    // Distancia euclidiana entre dos rostros
    public static double distancia(float[] a, float[] b) {
        double suma = 0;
        for (int i = 0; i < a.length; i++) {
            double d = a[i] - b[i];
            suma += d * d;
        }
        return Math.sqrt(suma);
    }

    // Promedio de varias muestras de un mismo rostro (reduce el ruido de cada captura)
    public static float[] promedio(List<float[]> muestras) {
        float[] resultado = new float[TAMANO_VECTOR];
        for (float[] muestra : muestras) {
            for (int i = 0; i < TAMANO_VECTOR; i++) {
                resultado[i] += muestra[i];
            }
        }
        for (int i = 0; i < TAMANO_VECTOR; i++) {
            resultado[i] /= muestras.size();
        }
        return resultado;
    }

    // Busca entre los rostros registrados el más parecido. Devuelve null si no hay ninguno registrado.
    public static Coincidencia mejorCoincidencia(Map<String, float[]> registrados, float[] rostro) {
        String mejorUsuario = null;
        double mejorDistancia = Double.MAX_VALUE;
        for (Map.Entry<String, float[]> entrada : registrados.entrySet()) {
            double d = distancia(rostro, entrada.getValue());
            if (d < mejorDistancia) {
                mejorDistancia = d;
                mejorUsuario = entrada.getKey();
            }
        }
        return (mejorUsuario == null) ? null : new Coincidencia(mejorUsuario, mejorDistancia);
    }

    // ---------- Archivo rostros.dat ----------

    public static synchronized boolean hayRostrosRegistrados() {
        return !cargarTodos().isEmpty();
    }

    public static synchronized boolean tieneRostro(String usuario) {
        return cargarTodos().containsKey(clave(usuario));
    }

    // Guarda (o reemplaza) el rostro de un usuario
    public static synchronized boolean guardar(String usuario, float[] vector) {
        if (usuario == null || usuario.trim().isEmpty() || vector == null || vector.length != TAMANO_VECTOR) {
            return false;
        }
        Map<String, float[]> todos = cargarTodos();
        todos.put(clave(usuario), vector);

        File destino = new File(RUTA_ARCHIVO);
        File temporal = new File(RUTA_ARCHIVO + ".tmp");
        try (DataOutputStream salida = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(temporal.toPath())))) {
            salida.writeInt(VERSION_FORMATO);
            salida.writeInt(todos.size());
            for (Map.Entry<String, float[]> entrada : todos.entrySet()) {
                salida.writeUTF(entrada.getKey());
                for (float valor : entrada.getValue()) {
                    salida.writeFloat(valor);
                }
            }
        } catch (IOException e) {
            System.err.println("Error al guardar los datos faciales: " + e.getMessage());
            return false;
        }
        try {
            // Se escribe a un archivo temporal y se reemplaza al final, para no dañar los datos si algo falla
            Files.move(temporal.toPath(), destino.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            System.err.println("Error al reemplazar rostros.dat: " + e.getMessage());
            return false;
        }
    }

    // Todos los rostros registrados, por usuario (en minúsculas)
    public static synchronized Map<String, float[]> cargarTodos() {
        Map<String, float[]> todos = new LinkedHashMap<>();
        File archivo = new File(RUTA_ARCHIVO);
        if (!archivo.exists()) {
            return todos;
        }
        try (DataInputStream entrada = new DataInputStream(new BufferedInputStream(Files.newInputStream(archivo.toPath())))) {
            if (entrada.readInt() != VERSION_FORMATO) {
                System.err.println("rostros.dat tiene un formato desconocido.");
                return todos;
            }
            int cantidad = entrada.readInt();
            for (int i = 0; i < cantidad; i++) {
                String usuario = entrada.readUTF();
                float[] vector = new float[TAMANO_VECTOR];
                for (int j = 0; j < TAMANO_VECTOR; j++) {
                    vector[j] = entrada.readFloat();
                }
                todos.put(usuario, vector);
            }
        } catch (IOException e) {
            System.err.println("Error al leer los datos faciales: " + e.getMessage());
        }
        return todos;
    }

    private static String clave(String usuario) {
        return usuario.trim().toLowerCase(Locale.ROOT);
    }
}
