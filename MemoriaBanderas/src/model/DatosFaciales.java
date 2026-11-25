// Paquete model: lógica y datos.
package model;

// Imports de entrada y salida: sirven para leer y escribir el archivo binario rostros.dat.
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
// LinkedHashMap es un mapa que recuerda el orden en que se agregaron los usuarios.
import java.util.LinkedHashMap;
import java.util.List;
// Locale se usa para pasar a minúsculas sin depender del idioma del computador.
import java.util.Locale;
import java.util.Map;

// Datos faciales de los usuarios: guardado en disco y comparación de rostros.
// No usa cámara ni OpenCV, solo trabaja con el "vector" de un rostro:
// una imagen en gris de 100x100 píxeles (10 000 valores) ya recortada.
//
// Los vectores se guardan en el archivo rostros.dat, junto a usuarios.txt.
// Clase con solo métodos estáticos para guardar, leer y comparar rostros.
public class DatosFaciales {

    // Lado en píxeles de cada rostro ya reducido (100 x 100).
    public static final int LADO = 100;                        // Cada rostro se reduce a 100x100 píxeles
    // Cantidad de valores por rostro: 100 x 100 = 10 000.
    public static final int TAMANO_VECTOR = LADO * LADO;
    // Distancia máxima para aceptar que dos rostros son de la misma persona. Si es muy baja cuesta que lo
    // reconozca, si es muy alta podría aceptar a otra persona.
    public static final double UMBRAL_COINCIDENCIA = 3000.0;   // Distancia máxima para aceptar un rostro

    // Nombre del archivo donde se guardan los rostros. Queda en la carpeta desde donde se ejecuta el programa.
    private static final String RUTA_ARCHIVO = "rostros.dat";
    // Número de versión del formato del archivo. Se escribe al inicio para saber si el archivo se puede leer.
    private static final int VERSION_FORMATO = 1;

    // Constructor privado: la clase no se instancia.
    private DatosFaciales() {
    }

    // Resultado de comparar un rostro contra los registrados
    // Clase interna que guarda el resultado de comparar un rostro: el usuario más parecido y qué tan distinto es.
    public static final class Coincidencia {
        // Usuario cuyo rostro es el más parecido.
        private final String usuario;
        // Distancia a ese rostro (menor distancia, más parecido).
        private final double distancia;

        // Constructor sin public: solo lo puede usar esta misma clase y las de su paquete.
        Coincidencia(String usuario, double distancia) {
            this.usuario = usuario;
            this.distancia = distancia;
        }

        // Usuario (en minúsculas) del rostro más parecido
        // Devuelve el nombre del usuario más parecido.
        public String getUsuario() {
            return usuario;
        }

        // Distancia al rostro más parecido: mientras más baja, más parecidos
        // Devuelve la distancia al rostro más parecido.
        public double getDistancia() {
            return distancia;
        }

        // Verdadero si el parecido es suficiente para dar acceso
        // Dice si el rostro es lo bastante parecido como para dar acceso.
        public boolean esValida() {
            // Es válido solo si la distancia es menor que el umbral.
            return distancia < UMBRAL_COINCIDENCIA;
        }
    }

    // Distancia euclidiana entre dos rostros
    // Distancia euclidiana: qué tan diferentes son dos rostros, comparando los 10 000 valores uno por uno.
    public static double distancia(float[] a, float[] b) {
        // Aquí se va sumando la diferencia de cada posición, elevada al cuadrado.
        double suma = 0;
        // Recorre todas las posiciones de los dos arreglos.
        for (int i = 0; i < a.length; i++) {
            // Diferencia entre el valor de gris de un rostro y el del otro en esa posición.
            double d = a[i] - b[i];
            // Se eleva al cuadrado para que no importe el signo y se suma.
            suma += d * d;
        }
        // Raíz cuadrada de la suma: ese es el resultado de la distancia.
        return Math.sqrt(suma);
    }

    // Promedio de varias muestras de un mismo rostro (reduce el ruido de cada captura)
    // Saca el promedio de varias fotos del mismo rostro. Así el ruido de cada foto pesa menos.
    public static float[] promedio(List<float[]> muestras) {
        // Arreglo donde se guarda el promedio (empieza en ceros).
        float[] resultado = new float[TAMANO_VECTOR];
        // Recorre cada foto.
        for (float[] muestra : muestras) {
            // Recorre cada posición de la foto.
            for (int i = 0; i < TAMANO_VECTOR; i++) {
                // Va sumando el valor de esa posición de todas las fotos.
                resultado[i] += muestra[i];
            }
        }
        // Cuando ya se sumaron todas las fotos, cada posición se divide entre la cantidad de fotos.
        for (int i = 0; i < TAMANO_VECTOR; i++) {
            // Eso da el promedio.
            resultado[i] /= muestras.size();
        }
        return resultado;
    }

    // Busca entre los rostros registrados el más parecido. Devuelve null si no hay ninguno registrado.
    // Compara un rostro contra todos los registrados y devuelve el que más se parece.
    public static Coincidencia mejorCoincidencia(Map<String, float[]> registrados, float[] rostro) {
        // Usuario más parecido hasta ahora (todavía ninguno).
        String mejorUsuario = null;
        // Menor distancia encontrada hasta ahora. Empieza en el valor más grande posible para que cualquier
        // distancia sea menor.
        double mejorDistancia = Double.MAX_VALUE;
        // Recorre todos los usuarios registrados (cada entrada tiene el nombre y su vector).
        for (Map.Entry<String, float[]> entrada : registrados.entrySet()) {
            // Distancia entre el rostro de la cámara y el de este usuario.
            double d = distancia(rostro, entrada.getValue());
            // Si es la menor hasta ahora...
            if (d < mejorDistancia) {
                // ...se guarda esa distancia...
                mejorDistancia = d;
                // ...y se guarda el nombre de ese usuario.
                mejorUsuario = entrada.getKey();
            }
        }
        // Si no había ningún usuario registrado devuelve null. Si no, devuelve el resultado con el más parecido.
        return (mejorUsuario == null) ? null : new Coincidencia(mejorUsuario, mejorDistancia);
    }

    // ---------- Archivo rostros.dat ----------

    // synchronized hace que solo un hilo a la vez use el método, para que dos hilos no lean o escriban el archivo
    // al mismo tiempo.
    public static synchronized boolean hayRostrosRegistrados() {
        // Hay rostros registrados si el mapa que se carga del archivo no está vacío.
        return !cargarTodos().isEmpty();
    }

    // Dice si un usuario específico tiene rostro registrado.
    public static synchronized boolean tieneRostro(String usuario) {
        // Busca el nombre (en minúsculas y sin espacios) dentro de los rostros cargados.
        return cargarTodos().containsKey(clave(usuario));
    }

    // Guarda (o reemplaza) el rostro de un usuario
    // Guarda el rostro de un usuario. Si ya tenía uno, lo reemplaza.
    public static synchronized boolean guardar(String usuario, float[] vector) {
        // No guarda si el usuario está vacío, si el vector es null o si no tiene los 10 000 valores.
        if (usuario == null || usuario.trim().isEmpty() || vector == null || vector.length != TAMANO_VECTOR) {
            // Devuelve false para avisar que no se guardó.
            return false;
        }
        // Primero se leen todos los rostros que ya estaban guardados.
        Map<String, float[]> todos = cargarTodos();
        // Se agrega o reemplaza el de este usuario.
        todos.put(clave(usuario), vector);

        // Archivo final: rostros.dat.
        File destino = new File(RUTA_ARCHIVO);
        // Archivo temporal donde se escribe primero: rostros.dat.tmp.
        File temporal = new File(RUTA_ARCHIVO + ".tmp");
        // Abre el archivo temporal para escribir datos binarios. El try cierra el archivo solo al terminar.
        try (DataOutputStream salida = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(temporal.toPath())))) {
            // Primero se escribe la versión del formato.
            salida.writeInt(VERSION_FORMATO);
            // Después la cantidad de usuarios.
            salida.writeInt(todos.size());
            // Por cada usuario...
            for (Map.Entry<String, float[]> entrada : todos.entrySet()) {
                // ...se escribe su nombre...
                salida.writeUTF(entrada.getKey());
                // ...y luego cada uno de sus 10 000 valores como número decimal (float).
                for (float valor : entrada.getValue()) {
                    salida.writeFloat(valor);
                }
            }
        } catch (IOException e) {
            // Si algo falla al escribir se muestra el error y se devuelve false.
            System.err.println("Error al guardar los datos faciales: " + e.getMessage());
            return false;
        }
        // Cuando el temporal quedó completo, se mueve encima de rostros.dat. Así, si el programa falla a la mitad,
        // el archivo anterior no se daña.
        try {
            // Se escribe a un archivo temporal y se reemplaza al final, para no dañar los datos si algo falla
            // REPLACE_EXISTING permite reemplazar el archivo viejo.
            Files.move(temporal.toPath(), destino.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            // Si no se pudo reemplazar el archivo se muestra el error y se devuelve false.
            System.err.println("Error al reemplazar rostros.dat: " + e.getMessage());
            return false;
        }
    }

    // Todos los rostros registrados, por usuario (en minúsculas)
    // Lee todos los rostros del archivo y los devuelve en un mapa (nombre de usuario y su vector).
    public static synchronized Map<String, float[]> cargarTodos() {
        // Mapa donde se van guardando los rostros leídos.
        Map<String, float[]> todos = new LinkedHashMap<>();
        // Objeto que representa el archivo rostros.dat.
        File archivo = new File(RUTA_ARCHIVO);
        // Si el archivo no existe todavía, se devuelve el mapa vacío.
        if (!archivo.exists()) {
            return todos;
        }
        // Abre el archivo para leer datos binarios. El try lo cierra solo al terminar.
        try (DataInputStream entrada = new DataInputStream(new BufferedInputStream(Files.newInputStream(archivo.toPath())))) {
            // Lo primero que se lee es la versión. Si no es la que se espera, no se puede leer el archivo.
            if (entrada.readInt() != VERSION_FORMATO) {
                System.err.println("rostros.dat tiene un formato desconocido.");
                return todos;
            }
            // Después se lee cuántos usuarios hay guardados.
            int cantidad = entrada.readInt();
            // Por cada usuario...
            for (int i = 0; i < cantidad; i++) {
                // ...se lee su nombre...
                String usuario = entrada.readUTF();
                // ...se crea el arreglo para su rostro...
                float[] vector = new float[TAMANO_VECTOR];
                // ...y se leen sus 10 000 valores en el mismo orden en que se escribieron.
                for (int j = 0; j < TAMANO_VECTOR; j++) {
                    vector[j] = entrada.readFloat();
                }
                // Se agrega el usuario con su rostro al mapa.
                todos.put(usuario, vector);
            }
        } catch (IOException e) {
            // Si hay un error de lectura, se muestra y se devuelve lo que se alcanzó a leer.
            System.err.println("Error al leer los datos faciales: " + e.getMessage());
        }
        // Devuelve el mapa con todos los rostros.
        return todos;
    }

    // Deja el nombre de usuario en un formato único: sin espacios en los lados y en minúsculas.
    private static String clave(String usuario) {
        // Así 'Angel' y 'angel ' se tratan como el mismo usuario.
        return usuario.trim().toLowerCase(Locale.ROOT);
    }
}
