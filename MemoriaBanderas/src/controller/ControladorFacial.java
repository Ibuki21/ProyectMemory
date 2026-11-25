// Paquete controller: aquí está la parte que conecta las ventanas con los datos.
package controller;

// Clases del modelo que usa este controlador: datos faciales, la cámara, los usuarios.
import model.DatosFaciales;
import model.FuenteRostros;
import model.FuenteRostrosJavaCV;
import model.GestorUsuarios;
import model.Usuario;
// VentanaCamara es la ventana donde se ve lo que graba la cámara.
import view.VentanaCamara;

// JOptionPane muestra cuadros de mensaje y SwingWorker permite hacer trabajo en un hilo aparte sin congelar la
// ventana.
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
// Clases de awt para colores, rectángulos, ventanas e imágenes.
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.image.BufferedImage;
// Listas y mapas.
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
// Errores que da el SwingWorker cuando se le pide el resultado y se canceló o falló.
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
// Supplier es una función que crea un objeto cuando se le pide. Se usa para crear la cámara.
import java.util.function.Supplier;

// Controlador del reconocimiento facial: captura el rostro al registrarse y reconoce al usuario al iniciar sesión.
// Funciona igual que el proyecto en Python: 10 muestras promediadas para registrar, y para entrar se compara el rostro
// con los registrados durante 10 segundos; tras 3 intentos fallidos se bloquea el acceso facial por 3 minutos.
// Clase que controla el reconocimiento facial, tanto al registrarse como al iniciar sesión.
public class ControladorFacial {

    // Cantidad de fotos del rostro que se toman al registrarse.
    private static final int MUESTRAS_REGISTRO = 10;
    // Tiempo máximo para el registro: 25 segundos (en milisegundos).
    private static final long TIEMPO_REGISTRO_MS = 25_000;
    // Pausa mínima entre una foto y la siguiente, para que no sean casi iguales.
    private static final long ESPERA_ENTRE_MUESTRAS_MS = 150;
    // Tiempo máximo para reconocer el rostro al iniciar sesión: 10 segundos.
    private static final long TIEMPO_LOGIN_MS = 10_000;
    // Cantidad de fallos seguidos antes de bloquear el acceso facial.
    private static final int INTENTOS_MAXIMOS = 3;
    // Duración del bloqueo: 3 minutos pasados a milisegundos.
    private static final long BLOQUEO_MS = 3L * 60 * 1000;
    private static final long PAUSA_FINAL_MS = 900;       // Se deja ver el resultado antes de cerrar la cámara
    // Cada cuánto se imprime en consola qué tan parecido es el rostro: cada 1 segundo.
    private static final long MS_ENTRE_REGISTROS_EN_CONSOLA = 1000;

    // Color del recuadro y el texto mientras no se reconoce un rostro (rojo).
    private static final Color COLOR_BUSCANDO = new Color(244, 21, 21);
    // Color cuando todo va bien (verde).
    private static final Color COLOR_CORRECTO = new Color(0, 200, 0);

    // El bloqueo es compartido por toda la aplicación
    // Fallos seguidos hasta ahora. Es static, por eso se mantiene aunque se cierre la ventana de login.
    private static int intentosFallidos = 0;
    // Momento hasta el cual el acceso facial está bloqueado (0 si no hay bloqueo).
    private static long bloqueadoHasta = 0;

    // Función que crea la fuente de imágenes (la cámara).
    private final Supplier<FuenteRostros> fabricaFuente;
    // Tiempo máximo de esta instancia para el registro.
    private final long tiempoRegistroMs;
    // Tiempo máximo de esta instancia para el login.
    private final long tiempoLoginMs;

    // Constructor normal: usa la cámara real (FuenteRostrosJavaCV) y los tiempos por defecto.
    public ControladorFacial() {
        // Llama al otro constructor con esos valores.
        this(() -> new FuenteRostrosJavaCV(), TIEMPO_REGISTRO_MS, TIEMPO_LOGIN_MS);
    }

    // Permite usar otra fuente de imágenes (por ejemplo, para pruebas sin cámara)
    // Constructor que permite pasar otra fuente de imágenes y otros tiempos.
    public ControladorFacial(Supplier<FuenteRostros> fabricaFuente, long tiempoRegistroMs, long tiempoLoginMs) {
        // Guarda la función que crea la fuente.
        this.fabricaFuente = fabricaFuente;
        // Guarda el tiempo de registro.
        this.tiempoRegistroMs = tiempoRegistroMs;
        // Guarda el tiempo de login.
        this.tiempoLoginMs = tiempoLoginMs;
    }

    // ---------- Registro ----------

    // Abre la cámara, toma 10 muestras del rostro y devuelve su vector promedio.
    // Devuelve null si el usuario cancela o si no se pudo capturar (ya se le explicó el motivo).
    // Registro: abre la cámara y devuelve el rostro promedio, o null si se cancela o falla.
    public float[] capturarDatosFaciales(Window padre) {
        // Lista donde se van guardando las fotos del rostro.
        List<float[]> muestras = new ArrayList<>();
        // Momento de la última foto. Es un arreglo de un solo valor porque dentro de una lambda no se puede
        // cambiar una variable normal.
        long[] ultimaMuestra = {0};

        // Abre la ventana de la cámara y repite la función de abajo con cada imagen que llegue.
        Resultado<float[]> resultado = ejecutar(padre, "Registro de datos faciales",
                "Mire fijamente a la cámara. Se tomarán " + MUESTRAS_REGISTRO + " muestras automáticamente.",
                // cuadro es la imagen actual con su rostro. salida es lo que se muestra en la ventana (mensaje,
                // color, barra).
                MUESTRAS_REGISTRO, tiempoRegistroMs, (cuadro, salida) -> {
                    // La barra de progreso muestra cuántas fotos van.
                    salida.progreso = muestras.size();
                    // Si en esta imagen no se detectó rostro...
                    if (cuadro.cara == null) {
                        // ...el recuadro se pone rojo...
                        salida.color = COLOR_BUSCANDO;
                        // ...se le pide al usuario que mire a la cámara...
                        salida.mensaje = "No se detecta ningún rostro. Mire a la cámara con buena iluminación.";
                        // ...y se muestra cuántas fotos van.
                        salida.textoBarra = muestras.size() + " / " + MUESTRAS_REGISTRO;
                        // Se devuelve null para seguir leyendo la cámara.
                        return null;
                    }
                    // Hora actual en milisegundos.
                    long ahora = System.currentTimeMillis();
                    // Solo se toma una foto nueva si pasó la pausa mínima desde la anterior.
                    if (ahora - ultimaMuestra[0] >= ESPERA_ENTRE_MUESTRAS_MS) {
                        // Se agrega el vector del rostro a la lista.
                        muestras.add(cuadro.cara);
                        // Se guarda cuándo fue la última foto.
                        ultimaMuestra[0] = ahora;
                    }
                    // Se actualiza la barra con las fotos que van.
                    salida.progreso = muestras.size();
                    // El recuadro se pone verde.
                    salida.color = COLOR_CORRECTO;
                    // Se muestra el mensaje con el número de foto.
                    salida.mensaje = "Captura " + muestras.size() + "/" + MUESTRAS_REGISTRO;
                    // Se actualiza el texto de la barra.
                    salida.textoBarra = muestras.size() + " / " + MUESTRAS_REGISTRO;
                    // Cuando ya hay 10 fotos devuelve el promedio, que termina el proceso. Si no, devuelve null y
                    // sigue.
                    return (muestras.size() >= MUESTRAS_REGISTRO) ? DatosFaciales.promedio(muestras) : null;
                });

        // Si el usuario cerró la ventana, se cancela y no se devuelve nada.
        if (resultado.cancelado) {
            return null;
        }
        // Si hubo un error (por ejemplo la cámara no abre), se le explica al usuario.
        if (resultado.error != null) {
            JOptionPane.showMessageDialog(padre, describirError(resultado.error),
                    "Cámara no disponible", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        // Si terminó el tiempo sin completar las fotos, se avisa que no se detectó el rostro.
        if (resultado.valor == null) {
            JOptionPane.showMessageDialog(padre,
                    "No se detectó su rostro. Intente de nuevo con buena iluminación y mirando de frente a la cámara.",
                    "Escaneo fallido", JOptionPane.WARNING_MESSAGE);
        }
        // Se devuelve el rostro promedio, o null.
        return resultado.valor;
    }

    // ---------- Inicio de sesión ----------

    // Reconoce el rostro frente a la cámara y devuelve el usuario al que pertenece.
    // Devuelve null si se canceló, no se reconoció o no se pudo usar la cámara (ya se le explicó el motivo).
    // Login: reconoce el rostro y devuelve el usuario al que pertenece, o null.
    public Usuario iniciarSesionConRostro(Window padre) {
        // Hora actual.
        long ahora = System.currentTimeMillis();
        // Si todavía está dentro del tiempo de bloqueo...
        if (ahora < bloqueadoHasta) {
            // ...se calculan los segundos que faltan (se suma 999 para redondear hacia arriba)...
            long segundos = (bloqueadoHasta - ahora + 999) / 1000;
            JOptionPane.showMessageDialog(padre,
                    "El inicio de sesión facial está bloqueado. Intente de nuevo en " + segundos
                            + " segundos o ingrese con su usuario y contraseña.",
                    "Sistema bloqueado", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        // Se cargan todos los rostros registrados desde rostros.dat.
        Map<String, float[]> registrados = DatosFaciales.cargarTodos();
        // Si no hay ninguno, no se puede iniciar sesión con rostro.
        if (registrados.isEmpty()) {
            JOptionPane.showMessageDialog(padre,
                    "Todavía no hay usuarios con datos faciales registrados.\n"
                            + "Puede registrarlos al crear su cuenta, en el botón \"Registrar Datos Faciales\".",
                    "Sin datos faciales", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }

        // Momento de la última vez que se imprimió la distancia en consola.
        long[] ultimoRegistroConsola = {0};
        // Abre la ventana de la cámara y repite la función de abajo con cada imagen.
        Resultado<String> resultado = ejecutar(padre, "Inicio de sesión facial",
                "Mire a la cámara para iniciar sesión.",
                // El máximo de la barra es el tiempo total en décimas de segundo.
                (int) (tiempoLoginMs / 100), tiempoLoginMs, (cuadro, salida) -> {
                    // Tiempo que le queda al intento (nunca menor que 0).
                    long restante = Math.max(0, salida.restanteMs);
                    // La barra baja según el tiempo que queda.
                    salida.progreso = (int) (restante / 100);
                    // Texto con el tiempo restante.
                    salida.textoBarra = String.format("Tiempo restante: %.1f s", restante / 1000.0);
                    // Si no hay rostro en esta imagen...
                    if (cuadro.cara == null) {
                        salida.color = COLOR_BUSCANDO;
                        salida.mensaje = "Buscando rostro...";
                        return null;
                    }
                    // Se compara el rostro de la cámara con todos los registrados y se toma el más parecido.
                    DatosFaciales.Coincidencia mejor = DatosFaciales.mejorCoincidencia(registrados, cuadro.cara);

                    // Registro en consola de qué tan parecido es el rostro: sirve para ajustar el umbral
                    // Hora actual para controlar la impresión.
                    long ahora2 = System.currentTimeMillis();
                    // Solo se imprime una vez por segundo.
                    if (ahora2 - ultimoRegistroConsola[0] >= MS_ENTRE_REGISTROS_EN_CONSOLA) {
                        // Se guarda cuándo se imprimió.
                        ultimoRegistroConsola[0] = ahora2;
                        // Imprime el usuario más parecido, su distancia y el umbral. Sirve para ajustar el umbral.
                        System.out.printf("[Facial] más parecido: %s, distancia %.0f (se acepta por debajo de %.0f)%n",
                                mejor.getUsuario(), mejor.getDistancia(), DatosFaciales.UMBRAL_COINCIDENCIA);
                    }

                    // Si la distancia es menor al umbral...
                    if (mejor.esValida()) {
                        // ...el recuadro se pone verde...
                        salida.color = COLOR_CORRECTO;
                        // ...se muestra el mensaje de acceso concedido...
                        salida.mensaje = "Acceso concedido: " + mejor.getUsuario();
                        // ...y se devuelve el nombre del usuario, lo que termina el proceso.
                        return mejor.getUsuario();
                    }
                    // Si no es lo bastante parecido...
                    salida.color = COLOR_BUSCANDO;
                    // ...se muestra Rostro desconocido...
                    salida.mensaje = "Rostro desconocido";
                    // ...y se devuelve null para seguir buscando hasta que se acabe el tiempo.
                    return null;
                });

        // Si el usuario canceló, no se hace nada.
        if (resultado.cancelado) {
            return null;
        }
        // Si falló la cámara, se muestra el motivo.
        if (resultado.error != null) {
            JOptionPane.showMessageDialog(padre, describirError(resultado.error),
                    "Cámara no disponible", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        // Si pasó el tiempo y no se reconoció a nadie, cuenta como un intento fallido.
        if (resultado.valor == null) {
            registrarIntentoFallido(padre);
            return null;
        }

        // Si se reconoció, se reinicia el contador de fallos.
        intentosFallidos = 0;
        // Se busca la cuenta completa del usuario por su nombre.
        Usuario usuario = GestorUsuarios.buscarPorUsuario(resultado.valor);
        // Si la cuenta ya no existe (se pudo borrar), se avisa.
        if (usuario == null) {
            JOptionPane.showMessageDialog(padre,
                    "Se reconoció su rostro, pero la cuenta \"" + resultado.valor + "\" ya no existe.",
                    "Usuario no encontrado", JOptionPane.ERROR_MESSAGE);
        }
        // Se devuelve el usuario, o null.
        return usuario;
    }

    // Cuenta un intento fallido y bloquea si llegó al máximo.
    private void registrarIntentoFallido(Window padre) {
        // Suma un fallo.
        intentosFallidos++;
        // Si ya son 3 fallos seguidos...
        if (intentosFallidos >= INTENTOS_MAXIMOS) {
            // ...se guarda hasta cuándo dura el bloqueo...
            bloqueadoHasta = System.currentTimeMillis() + BLOQUEO_MS;
            // ...y se reinicia el contador.
            intentosFallidos = 0;
            // Se avisa que quedó bloqueado y que puede entrar con usuario y contraseña.
            JOptionPane.showMessageDialog(padre,
                    INTENTOS_MAXIMOS + " intentos fallidos. El inicio de sesión facial queda bloqueado por "
                            + (BLOQUEO_MS / 60000) + " minutos.\nMientras tanto puede ingresar con su usuario y contraseña.",
                    "Sistema bloqueado", JOptionPane.ERROR_MESSAGE);
        } else {
            // Si todavía no llega al máximo, solo se informa qué intento fue.
            JOptionPane.showMessageDialog(padre,
                    "No se pudo reconocer su rostro en " + (tiempoLoginMs / 1000) + " segundos. Intento "
                            + intentosFallidos + " de " + INTENTOS_MAXIMOS + ".",
                    "Tiempo excedido", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // ---------- Motor común: ventana de cámara + hilo que lee los cuadros ----------

    // Lo que el hilo de la cámara le pasa a la ventana para mostrar
    // Clase interna con lo que se muestra en la ventana en cada imagen.
    private static final class Actualizacion {
        // Imagen de la cámara.
        BufferedImage imagen;
        // Recuadro del rostro detectado.
        Rectangle rostro;
        // Color del recuadro y del mensaje.
        Color color = COLOR_BUSCANDO;
        // Mensaje que se muestra.
        String mensaje = " ";
        // Texto sobre la barra de progreso.
        String textoBarra = " ";
        // Valor de la barra de progreso.
        int progreso;
        // Tiempo que le queda al intento.
        long restanteMs;
    }

    // Clase interna con el resultado final del proceso. El tipo T es lo que devuelve (un vector en el registro, un
    // nombre en el login).
    private static final class Resultado<T> {
        // El valor obtenido.
        T valor;
        // Si el usuario canceló.
        boolean cancelado;
        // Si hubo error, cuál fue.
        Throwable error;
    }

    // Revisa un cuadro y devuelve el resultado final cuando ya terminó, o null para seguir leyendo
    // Interfaz con un solo método: revisar un cuadro de la cámara. El registro y el login lo implementan de forma
    // distinta.
    private interface Procesador<T> {
        // Devuelve el resultado final cuando ya terminó, o null para seguir leyendo.
        T procesar(FuenteRostros.Cuadro cuadro, Actualizacion salida);
    }

    // Método común para registro y login: abre la ventana y lee la cámara en un hilo aparte.
    private <T> Resultado<T> ejecutar(Window padre, String titulo, String instruccion,
                                      int maximoProgreso, long limiteMs, Procesador<T> procesador) {
        // Objeto donde se guarda cómo terminó.
        Resultado<T> resultado = new Resultado<>();
        // Ventana donde se ve la cámara.
        VentanaCamara ventana = new VentanaCamara(padre, titulo, instruccion, maximoProgreso);

        // La cámara se lee en un hilo aparte para que la interfaz nunca se congele
        // El SwingWorker hace el trabajo de la cámara en un hilo de fondo.
        SwingWorker<T, Actualizacion> hilo = new SwingWorker<T, Actualizacion>() {
            @Override
            // Este método corre en el hilo de fondo.
            protected T doInBackground() throws Exception {
                // Crea la cámara. El try la cierra sola al terminar.
                try (FuenteRostros fuente = fabricaFuente.get()) {
                    // Momento en que empezó.
                    long inicio = System.currentTimeMillis();
                    // Repite mientras no se cancele y no se acabe el tiempo.
                    while (!isCancelled() && System.currentTimeMillis() - inicio < limiteMs) {
                        // Lee una imagen de la cámara.
                        FuenteRostros.Cuadro cuadro = fuente.leer();
                        // Si no llega imagen, la cámara falla.
                        if (cuadro == null) {
                            // Se lanza un error que el controlador muestra al usuario.
                            throw new IllegalStateException("La cámara no está entregando imágenes.");
                        }
                        // Objeto donde se prepara lo que se va a mostrar.
                        Actualizacion salida = new Actualizacion();
                        // Se copia la imagen.
                        salida.imagen = cuadro.imagen;
                        // Se copia el recuadro del rostro.
                        salida.rostro = cuadro.rostro;
                        // Se calcula cuánto tiempo queda.
                        salida.restanteMs = limiteMs - (System.currentTimeMillis() - inicio);

                        // Se revisa la imagen con la función del registro o del login. Si ya terminó, devuelve el
                        // resultado.
                        T valor = procesador.procesar(cuadro, salida);
                        // publish manda la actualización al hilo de la ventana para que la dibuje.
                        publish(salida);
                        // Si ya hay resultado...
                        if (valor != null) {
                            // ...se espera un momento para que el usuario vea el mensaje de acceso...
                            Thread.sleep(PAUSA_FINAL_MS);
                            // ...y se devuelve el resultado.
                            return valor;
                        }
                    }
                } catch (InterruptedException e) {
                    // Si el hilo es interrumpido, se marca la interrupción y se sale.
                    Thread.currentThread().interrupt();
                }
                // Si se acabó el tiempo sin resultado, se devuelve null.
                return null;
            }

            // process corre en el hilo de la ventana y dibuja lo que mandó publish.
            @Override
            protected void process(List<Actualizacion> pendientes) {
                // Solo se dibuja la última actualización, porque las anteriores ya quedaron viejas.
                Actualizacion ultima = pendientes.get(pendientes.size() - 1);
                // Se muestra la imagen, el recuadro, el color, el mensaje y la barra.
                ventana.mostrar(ultima.imagen, ultima.rostro, ultima.color, ultima.mensaje,
                        ultima.progreso, ultima.textoBarra);
            }

            @Override
            // done se ejecuta cuando el hilo termina.
            protected void done() {
                // Se cierra la ventana de la cámara.
                ventana.dispose();
            }
        };

        // Qué hacer si el usuario cierra la ventana de la cámara.
        ventana.setAlCancelar(() -> {
            // Se marca como cancelado...
            resultado.cancelado = true;
            // ...se detiene el hilo...
            hilo.cancel(true);
            // ...y se cierra la ventana.
            ventana.dispose();
        });

        // Se inicia el hilo de la cámara.
        hilo.execute();
        // La ventana es modal: el programa se queda aquí hasta que se cierre.
        ventana.setVisible(true);   // Ventana modal: el código sigue aquí cuando se cierra

        // Si no se canceló, se recoge cómo terminó el hilo.
        if (!resultado.cancelado) {
            try {
                // get espera a que el hilo termine y devuelve su valor.
                resultado.valor = hilo.get();
            } catch (ExecutionException e) {
                // Si hubo un error dentro del hilo, se guarda la causa.
                resultado.error = e.getCause();
            } catch (CancellationException e) {
                // Si el hilo fue cancelado, se marca como cancelado.
                resultado.cancelado = true;
            } catch (InterruptedException e) {
                // Si el programa fue interrumpido mientras esperaba, se marca como cancelado.
                Thread.currentThread().interrupt();
                resultado.cancelado = true;
            }
        }
        // Se devuelve el resultado.
        return resultado;
    }

    // Traduce los errores técnicos a un mensaje que el usuario entienda
    // Convierte errores técnicos en mensajes que el usuario entienda.
    private static String describirError(Throwable error) {
        // Si faltan las clases de OpenCV/JavaCV...
        if (error instanceof NoClassDefFoundError || error instanceof ClassNotFoundException) {
            // ...se avisa que faltan las librerías.
            return "Faltan las librerías de OpenCV (JavaCV).\n"
                    + "Ejecute MemoriaBanderas/lib/descargar_librerias.sh y agregue los .jar de esa carpeta al proyecto.";
        }
        // Si no se pudieron cargar las librerías nativas de OpenCV...
        if (error instanceof UnsatisfiedLinkError) {
            // ...se muestra el mensaje del error.
            return "No se pudieron cargar las librerías nativas de OpenCV:\n" + error.getMessage();
        }
        // Para cualquier otro error, se muestra su mensaje o su descripción.
        return (error.getMessage() != null) ? error.getMessage() : error.toString();
    }
}
