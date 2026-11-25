package model;

// Clases para hacer la conexión web y leer la respuesta.
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
// LocalDate y DateTimeFormatter sirven para manejar fechas y darles formato.
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

// Clase con métodos estáticos que consulta el tipo de cambio del dólar al BCCR y calcula el premio.
public class ServicioCambioDolar {

    // Clave (token) que da el BCCR para poder usar su servicio. Se manda en cada consulta.
    private static final String TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJCQ0NSLVNEREUiLCJzdWIiOiJqb3JzaGFucXVlc2FkYUBnbWFpbC5jb20iLCJhdWQiOiJTRERFLVNpdGlvRXh0ZXJubyIsImV4cCI6MjUzNDAyMzAwODAwLCJuYmYiOjE3OTMyODQ0NjAsImlhdCI6MTc5MzI4NDQ2MCwianRpIjoiMDlhYmMwODQtOWI5My00YzBlLWE0MTAtOTAxNTI4NTVlNTkwIiwiZW1haWwiOiJqb3JzaGFucXVlc2FkYUBnbWFpbC5jb20ifQ.VRmfKg0UjOxnZx-r7H9M4UYi_K8sI3v0og7SPNNPB6I";
    // Correo con el que se registró el token.
    private static final String EMAIL = "jorshanquesada@gmail.com";

    // Valor que se usa si no se puede consultar el BCCR y nunca se había obtenido uno.
    private static final double CAMBIO_POR_DEFECTO = 500.0;
    private static final int TIEMPO_LIMITE_MS = 4000;                 // Máximo de espera por la conexión y por la respuesta
    private static final long VIGENCIA_MS = 60L * 60 * 1000;           // El tipo de cambio se reutiliza durante 1 hora
    private static final long ESPERA_TRAS_FALLO_MS = 2L * 60 * 1000;   // Tras un fallo no se reintenta durante 2 minutos

    // Objeto que se usa como candado: solo un hilo a la vez puede consultar o cambiar los datos.
    private static final Object CANDADO = new Object();
    // Último tipo de cambio obtenido. Con -1 se indica que todavía no hay ninguno.
    private static double cambioGuardado = -1;
    // Momento (en milisegundos) en que se guardó ese valor.
    private static long instanteGuardado = 0;
    // Momento del último intento fallido. Con 0 se indica que no ha fallado.
    private static long instanteUltimoFallo = 0;

    // Consulta el tipo de cambio en segundo plano. Se llama al abrir el juego, para que el
    // valor ya esté listo cuando termine una partida y la interfaz nunca tenga que esperar la red.
    // Lanza la consulta en segundo plano para que el valor ya esté listo cuando haga falta.
    public static void precargar() {
        // Se crea un hilo nuevo que ejecuta obtenerCambioDolar.
        Thread hilo = new Thread(ServicioCambioDolar::obtenerCambioDolar, "precarga-cambio-dolar");
        // Hilo daemon: no impide que el programa cierre si todavía está corriendo.
        hilo.setDaemon(true);
        // Se inicia el hilo.
        hilo.start();
    }

    // Devuelve el tipo de cambio. Usa el guardado si todavía sirve y solo consulta al BCCR cuando hace falta.
    public static double obtenerCambioDolar() {
        // Solo un hilo a la vez puede entrar a este bloque.
        synchronized (CANDADO) {
            // Hora actual en milisegundos.
            long ahora = System.currentTimeMillis();
            // Si ya hay un valor y tiene menos de 1 hora de guardado...
            if (cambioGuardado > 0 && ahora - instanteGuardado < VIGENCIA_MS) {
                // ...se devuelve ese y no se consulta de nuevo.
                return cambioGuardado;
            }
            // Si la última consulta falló hace poco, no se vuelve a intentar con cada partida
            // Si hubo un fallo hace menos de 2 minutos, no se vuelve a intentar para no atrasar cada partida.
            if (instanteUltimoFallo != 0 && ahora - instanteUltimoFallo < ESPERA_TRAS_FALLO_MS) {
                // Se devuelve el último valor bueno, o el valor por defecto si nunca hubo uno.
                return (cambioGuardado > 0) ? cambioGuardado : CAMBIO_POR_DEFECTO;
            }

            // Se consulta al BCCR. Devuelve -1 si falla.
            double consultado = consultarBccr();
            // Si la consulta funcionó...
            if (consultado > 0) {
                // ...se guarda el valor...
                cambioGuardado = consultado;
                // ...y el momento en que se obtuvo...
                instanteGuardado = System.currentTimeMillis();
                // ...y se borra la marca del último fallo.
                instanteUltimoFallo = 0;
                // Se devuelve el valor nuevo.
                return consultado;
            }
            // Si falló, se guarda el momento del fallo.
            instanteUltimoFallo = System.currentTimeMillis();
            // Se devuelve el último valor bueno, o el valor por defecto si nunca hubo uno.
            return (cambioGuardado > 0) ? cambioGuardado : CAMBIO_POR_DEFECTO;
        }
    }

    // Devuelve el valor del BCCR, o -1 si no se pudo obtener
    // Hace la consulta real al BCCR. Devuelve el valor, o -1 si no pudo.
    private static double consultarBccr() {
        // Formato de fecha que pide el BCCR: año/mes/día.
        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        // Fecha de hoy con ese formato.
        String hasta = LocalDate.now().format(f);
        // Fecha de hace 7 días. Se pide una semana porque en fines de semana y feriados no hay dato.
        String desde = LocalDate.now().minusDays(7).format(f);

        // Dirección de la API del BCCR. El 318 es el código del indicador del tipo de cambio del dólar.
        String url = "https://apim.bccr.fi.cr/SDDE/api/Bccr.GE.SDDE.Publico.Indicadores.API/"
                + "indicadoresEconomicos/318/series?fechaInicio=" + desde
                + "&fechaFin=" + hasta + "&idioma=ES";

        // Conexión web, empieza en null.
        HttpURLConnection conn = null;
        // El try captura cualquier error de conexión o de lectura.
        try {
            // Se abre la conexión con la dirección armada arriba.
            conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
            // Se pide con el método GET (solo leer datos).
            conn.setRequestMethod("GET");
            // Tiempo máximo para conectarse: 4 segundos.
            conn.setConnectTimeout(TIEMPO_LIMITE_MS);
            // Tiempo máximo para esperar la respuesta: 4 segundos.
            conn.setReadTimeout(TIEMPO_LIMITE_MS);
            // Authorization: se manda el token para identificarse ante el BCCR.
            conn.setRequestProperty("Authorization", "Bearer " + TOKEN);
            // Se manda también el correo registrado.
            conn.setRequestProperty("X-Client-Email", EMAIL);
            // Se pide la respuesta en formato JSON.
            conn.setRequestProperty("Accept", "application/json");

            // Aquí se va armando el texto completo de la respuesta.
            StringBuilder sb = new StringBuilder();
            // Se abre un lector sobre la respuesta, con codificación utf-8. El try lo cierra solo al terminar.
            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                // Variable para cada línea leída.
                String line;
                // Se lee línea por línea hasta que no haya más y se va juntando todo en un solo texto.
                while ((line = in.readLine()) != null) sb.append(line);
            }

            // Texto completo de la respuesta.
            String json = sb.toString();
            // Nombre del campo que trae el valor del tipo de cambio dentro del JSON.
            String clave = "\"valorDatoPorPeriodo\":";
            // lastIndexOf busca la última aparición de ese campo, que corresponde al dato más reciente de la
            // semana.
            int i = json.lastIndexOf(clave);
            // Si el campo no aparece, la respuesta no sirve y se devuelve -1.
            if (i == -1) return -1;

            // Posición donde empieza el número, justo después del nombre del campo.
            int ini = i + clave.length();
            // Posición donde termina el número. Empieza igual que ini y se va avanzando.
            int fin = ini;
            // Avanza mientras el carácter sea un dígito o un punto.
            while (fin < json.length() && "0123456789.".indexOf(json.charAt(fin)) >= 0) fin++;

            // Se saca el texto del número y se convierte a double.
            return Double.parseDouble(json.substring(ini, fin).trim());
        } catch (Exception e) {
            // Si algo falla (conexión, tiempo agotado, formato raro), se imprime el error en consola...
            System.err.println("No se pudo obtener el tipo de cambio del BCCR: " + e.getMessage());
            // ...y se devuelve -1 para indicar que no se pudo.
            return -1;
        } finally {
            // El finally se ejecuta siempre, haya error o no. Si se abrió una conexión...
            if (conn != null) {
                // ...se cierra.
                conn.disconnect();
            }
        }
    }

    // Calcula el premio de una partida según el puntaje, con la fórmula del enunciado.
    public static double calcularPremio(int puntaje) {
        // Si el puntaje es cero o negativo, el premio es 0 (así se evita dividir entre cero).
        if (puntaje <= 0) {
            return 0.0;
        }
        // Obtiene el tipo de cambio actual.
        double cambioDolar = obtenerCambioDolar();
        // Fórmula: 1 dividido entre el puntaje, por 100, por el tipo de cambio.
        return (1.0 / puntaje) * 100.0 * cambioDolar;
    }
}
