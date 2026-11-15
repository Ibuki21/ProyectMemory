package controller;

import model.DatosFaciales;
import model.FuenteRostros;
import model.FuenteRostrosJavaCV;
import model.GestorUsuarios;
import model.Usuario;
import view.VentanaCamara;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

// Controlador del reconocimiento facial: captura el rostro al registrarse y reconoce al usuario al iniciar sesión.
// Funciona igual que el proyecto en Python: 10 muestras promediadas para registrar, y para entrar se compara el rostro
// con los registrados durante 10 segundos; tras 3 intentos fallidos se bloquea el acceso facial por 3 minutos.
public class ControladorFacial {

    private static final int MUESTRAS_REGISTRO = 10;
    private static final long TIEMPO_REGISTRO_MS = 25_000;
    private static final long ESPERA_ENTRE_MUESTRAS_MS = 150;
    private static final long TIEMPO_LOGIN_MS = 10_000;
    private static final int INTENTOS_MAXIMOS = 3;
    private static final long BLOQUEO_MS = 3L * 60 * 1000;
    private static final long PAUSA_FINAL_MS = 900;       // Se deja ver el resultado antes de cerrar la cámara
    private static final long MS_ENTRE_REGISTROS_EN_CONSOLA = 1000;

    private static final Color COLOR_BUSCANDO = new Color(244, 21, 21);
    private static final Color COLOR_CORRECTO = new Color(0, 200, 0);

    // El bloqueo es compartido por toda la aplicación
    private static int intentosFallidos = 0;
    private static long bloqueadoHasta = 0;

    private final Supplier<FuenteRostros> fabricaFuente;
    private final long tiempoRegistroMs;
    private final long tiempoLoginMs;

    public ControladorFacial() {
        this(() -> new FuenteRostrosJavaCV(), TIEMPO_REGISTRO_MS, TIEMPO_LOGIN_MS);
    }

    // Permite usar otra fuente de imágenes (por ejemplo, para pruebas sin cámara)
    public ControladorFacial(Supplier<FuenteRostros> fabricaFuente, long tiempoRegistroMs, long tiempoLoginMs) {
        this.fabricaFuente = fabricaFuente;
        this.tiempoRegistroMs = tiempoRegistroMs;
        this.tiempoLoginMs = tiempoLoginMs;
    }

    // ---------- Registro ----------

    // Abre la cámara, toma 10 muestras del rostro y devuelve su vector promedio.
    // Devuelve null si el usuario cancela o si no se pudo capturar (ya se le explicó el motivo).
    public float[] capturarDatosFaciales(Window padre) {
        List<float[]> muestras = new ArrayList<>();
        long[] ultimaMuestra = {0};

        Resultado<float[]> resultado = ejecutar(padre, "Registro de datos faciales",
                "Mire fijamente a la cámara. Se tomarán " + MUESTRAS_REGISTRO + " muestras automáticamente.",
                MUESTRAS_REGISTRO, tiempoRegistroMs, (cuadro, salida) -> {
                    salida.progreso = muestras.size();
                    if (cuadro.cara == null) {
                        salida.color = COLOR_BUSCANDO;
                        salida.mensaje = "No se detecta ningún rostro. Mire a la cámara con buena iluminación.";
                        salida.textoBarra = muestras.size() + " / " + MUESTRAS_REGISTRO;
                        return null;
                    }
                    long ahora = System.currentTimeMillis();
                    if (ahora - ultimaMuestra[0] >= ESPERA_ENTRE_MUESTRAS_MS) {
                        muestras.add(cuadro.cara);
                        ultimaMuestra[0] = ahora;
                    }
                    salida.progreso = muestras.size();
                    salida.color = COLOR_CORRECTO;
                    salida.mensaje = "Captura " + muestras.size() + "/" + MUESTRAS_REGISTRO;
                    salida.textoBarra = muestras.size() + " / " + MUESTRAS_REGISTRO;
                    return (muestras.size() >= MUESTRAS_REGISTRO) ? DatosFaciales.promedio(muestras) : null;
                });

        if (resultado.cancelado) {
            return null;
        }
        if (resultado.error != null) {
            JOptionPane.showMessageDialog(padre, describirError(resultado.error),
                    "Cámara no disponible", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        if (resultado.valor == null) {
            JOptionPane.showMessageDialog(padre,
                    "No se detectó su rostro. Intente de nuevo con buena iluminación y mirando de frente a la cámara.",
                    "Escaneo fallido", JOptionPane.WARNING_MESSAGE);
        }
        return resultado.valor;
    }

    // ---------- Inicio de sesión ----------

    // Reconoce el rostro frente a la cámara y devuelve el usuario al que pertenece.
    // Devuelve null si se canceló, no se reconoció o no se pudo usar la cámara (ya se le explicó el motivo).
    public Usuario iniciarSesionConRostro(Window padre) {
        long ahora = System.currentTimeMillis();
        if (ahora < bloqueadoHasta) {
            long segundos = (bloqueadoHasta - ahora + 999) / 1000;
            JOptionPane.showMessageDialog(padre,
                    "El inicio de sesión facial está bloqueado. Intente de nuevo en " + segundos
                            + " segundos o ingrese con su usuario y contraseña.",
                    "Sistema bloqueado", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        Map<String, float[]> registrados = DatosFaciales.cargarTodos();
        if (registrados.isEmpty()) {
            JOptionPane.showMessageDialog(padre,
                    "Todavía no hay usuarios con datos faciales registrados.\n"
                            + "Puede registrarlos al crear su cuenta, en el botón \"Registrar Datos Faciales\".",
                    "Sin datos faciales", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }

        long[] ultimoRegistroConsola = {0};
        Resultado<String> resultado = ejecutar(padre, "Inicio de sesión facial",
                "Mire a la cámara para iniciar sesión.",
                (int) (tiempoLoginMs / 100), tiempoLoginMs, (cuadro, salida) -> {
                    long restante = Math.max(0, salida.restanteMs);
                    salida.progreso = (int) (restante / 100);
                    salida.textoBarra = String.format("Tiempo restante: %.1f s", restante / 1000.0);
                    if (cuadro.cara == null) {
                        salida.color = COLOR_BUSCANDO;
                        salida.mensaje = "Buscando rostro...";
                        return null;
                    }
                    DatosFaciales.Coincidencia mejor = DatosFaciales.mejorCoincidencia(registrados, cuadro.cara);

                    // Registro en consola de qué tan parecido es el rostro: sirve para ajustar el umbral
                    long ahora2 = System.currentTimeMillis();
                    if (ahora2 - ultimoRegistroConsola[0] >= MS_ENTRE_REGISTROS_EN_CONSOLA) {
                        ultimoRegistroConsola[0] = ahora2;
                        System.out.printf("[Facial] más parecido: %s, distancia %.0f (se acepta por debajo de %.0f)%n",
                                mejor.getUsuario(), mejor.getDistancia(), DatosFaciales.UMBRAL_COINCIDENCIA);
                    }

                    if (mejor.esValida()) {
                        salida.color = COLOR_CORRECTO;
                        salida.mensaje = "Acceso concedido: " + mejor.getUsuario();
                        return mejor.getUsuario();
                    }
                    salida.color = COLOR_BUSCANDO;
                    salida.mensaje = "Rostro desconocido";
                    return null;
                });

        if (resultado.cancelado) {
            return null;
        }
        if (resultado.error != null) {
            JOptionPane.showMessageDialog(padre, describirError(resultado.error),
                    "Cámara no disponible", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        if (resultado.valor == null) {
            registrarIntentoFallido(padre);
            return null;
        }

        intentosFallidos = 0;
        Usuario usuario = GestorUsuarios.buscarPorUsuario(resultado.valor);
        if (usuario == null) {
            JOptionPane.showMessageDialog(padre,
                    "Se reconoció su rostro, pero la cuenta \"" + resultado.valor + "\" ya no existe.",
                    "Usuario no encontrado", JOptionPane.ERROR_MESSAGE);
        }
        return usuario;
    }

    private void registrarIntentoFallido(Window padre) {
        intentosFallidos++;
        if (intentosFallidos >= INTENTOS_MAXIMOS) {
            bloqueadoHasta = System.currentTimeMillis() + BLOQUEO_MS;
            intentosFallidos = 0;
            JOptionPane.showMessageDialog(padre,
                    INTENTOS_MAXIMOS + " intentos fallidos. El inicio de sesión facial queda bloqueado por "
                            + (BLOQUEO_MS / 60000) + " minutos.\nMientras tanto puede ingresar con su usuario y contraseña.",
                    "Sistema bloqueado", JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(padre,
                    "No se pudo reconocer su rostro en " + (tiempoLoginMs / 1000) + " segundos. Intento "
                            + intentosFallidos + " de " + INTENTOS_MAXIMOS + ".",
                    "Tiempo excedido", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // ---------- Motor común: ventana de cámara + hilo que lee los cuadros ----------

    // Lo que el hilo de la cámara le pasa a la ventana para mostrar
    private static final class Actualizacion {
        BufferedImage imagen;
        Rectangle rostro;
        Color color = COLOR_BUSCANDO;
        String mensaje = " ";
        String textoBarra = " ";
        int progreso;
        long restanteMs;
    }

    private static final class Resultado<T> {
        T valor;
        boolean cancelado;
        Throwable error;
    }

    // Revisa un cuadro y devuelve el resultado final cuando ya terminó, o null para seguir leyendo
    private interface Procesador<T> {
        T procesar(FuenteRostros.Cuadro cuadro, Actualizacion salida);
    }

    private <T> Resultado<T> ejecutar(Window padre, String titulo, String instruccion,
                                      int maximoProgreso, long limiteMs, Procesador<T> procesador) {
        Resultado<T> resultado = new Resultado<>();
        VentanaCamara ventana = new VentanaCamara(padre, titulo, instruccion, maximoProgreso);

        // La cámara se lee en un hilo aparte para que la interfaz nunca se congele
        SwingWorker<T, Actualizacion> hilo = new SwingWorker<T, Actualizacion>() {
            @Override
            protected T doInBackground() throws Exception {
                try (FuenteRostros fuente = fabricaFuente.get()) {
                    long inicio = System.currentTimeMillis();
                    while (!isCancelled() && System.currentTimeMillis() - inicio < limiteMs) {
                        FuenteRostros.Cuadro cuadro = fuente.leer();
                        if (cuadro == null) {
                            throw new IllegalStateException("La cámara no está entregando imágenes.");
                        }
                        Actualizacion salida = new Actualizacion();
                        salida.imagen = cuadro.imagen;
                        salida.rostro = cuadro.rostro;
                        salida.restanteMs = limiteMs - (System.currentTimeMillis() - inicio);

                        T valor = procesador.procesar(cuadro, salida);
                        publish(salida);
                        if (valor != null) {
                            Thread.sleep(PAUSA_FINAL_MS);
                            return valor;
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return null;
            }

            @Override
            protected void process(List<Actualizacion> pendientes) {
                Actualizacion ultima = pendientes.get(pendientes.size() - 1);
                ventana.mostrar(ultima.imagen, ultima.rostro, ultima.color, ultima.mensaje,
                        ultima.progreso, ultima.textoBarra);
            }

            @Override
            protected void done() {
                ventana.dispose();
            }
        };

        ventana.setAlCancelar(() -> {
            resultado.cancelado = true;
            hilo.cancel(true);
            ventana.dispose();
        });

        hilo.execute();
        ventana.setVisible(true);   // Ventana modal: el código sigue aquí cuando se cierra

        if (!resultado.cancelado) {
            try {
                resultado.valor = hilo.get();
            } catch (ExecutionException e) {
                resultado.error = e.getCause();
            } catch (CancellationException e) {
                resultado.cancelado = true;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                resultado.cancelado = true;
            }
        }
        return resultado;
    }

    // Traduce los errores técnicos a un mensaje que el usuario entienda
    private static String describirError(Throwable error) {
        if (error instanceof NoClassDefFoundError || error instanceof ClassNotFoundException) {
            return "Faltan las librerías de OpenCV (JavaCV).\n"
                    + "Ejecute MemoriaBanderas/lib/descargar_librerias.sh y agregue los .jar de esa carpeta al proyecto.";
        }
        if (error instanceof UnsatisfiedLinkError) {
            return "No se pudieron cargar las librerías nativas de OpenCV:\n" + error.getMessage();
        }
        return (error.getMessage() != null) ? error.getMessage() : error.toString();
    }
}
