package model;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ServicioCambioDolar {

    private static final String TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJCQ0NSLVNEREUiLCJzdWIiOiJqb3JzaGFucXVlc2FkYUBnbWFpbC5jb20iLCJhdWQiOiJTRERFLVNpdGlvRXh0ZXJubyIsImV4cCI6MjUzNDAyMzAwODAwLCJuYmYiOjE3OTMyODQ0NjAsImlhdCI6MTc5MzI4NDQ2MCwianRpIjoiMDlhYmMwODQtOWI5My00YzBlLWE0MTAtOTAxNTI4NTVlNTkwIiwiZW1haWwiOiJqb3JzaGFucXVlc2FkYUBnbWFpbC5jb20ifQ.VRmfKg0UjOxnZx-r7H9M4UYi_K8sI3v0og7SPNNPB6I";
    private static final String EMAIL = "jorshanquesada@gmail.com";

    private static final double CAMBIO_POR_DEFECTO = 500.0;
    private static final int TIEMPO_LIMITE_MS = 4000;                 // Máximo de espera por la conexión y por la respuesta
    private static final long VIGENCIA_MS = 60L * 60 * 1000;           // El tipo de cambio se reutiliza durante 1 hora
    private static final long ESPERA_TRAS_FALLO_MS = 2L * 60 * 1000;   // Tras un fallo no se reintenta durante 2 minutos

    private static final Object CANDADO = new Object();
    private static double cambioGuardado = -1;
    private static long instanteGuardado = 0;
    private static long instanteUltimoFallo = 0;

    // Consulta el tipo de cambio en segundo plano. Se llama al abrir el juego, para que el
    // valor ya esté listo cuando termine una partida y la interfaz nunca tenga que esperar la red.
    public static void precargar() {
        Thread hilo = new Thread(ServicioCambioDolar::obtenerCambioDolar, "precarga-cambio-dolar");
        hilo.setDaemon(true);
        hilo.start();
    }

    public static double obtenerCambioDolar() {
        synchronized (CANDADO) {
            long ahora = System.currentTimeMillis();
            if (cambioGuardado > 0 && ahora - instanteGuardado < VIGENCIA_MS) {
                return cambioGuardado;
            }
            // Si la última consulta falló hace poco, no se vuelve a intentar con cada partida
            if (instanteUltimoFallo != 0 && ahora - instanteUltimoFallo < ESPERA_TRAS_FALLO_MS) {
                return (cambioGuardado > 0) ? cambioGuardado : CAMBIO_POR_DEFECTO;
            }

            double consultado = consultarBccr();
            if (consultado > 0) {
                cambioGuardado = consultado;
                instanteGuardado = System.currentTimeMillis();
                instanteUltimoFallo = 0;
                return consultado;
            }
            instanteUltimoFallo = System.currentTimeMillis();
            return (cambioGuardado > 0) ? cambioGuardado : CAMBIO_POR_DEFECTO;
        }
    }

    // Devuelve el valor del BCCR, o -1 si no se pudo obtener
    private static double consultarBccr() {
        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        String hasta = LocalDate.now().format(f);
        String desde = LocalDate.now().minusDays(7).format(f);

        String url = "https://apim.bccr.fi.cr/SDDE/api/Bccr.GE.SDDE.Publico.Indicadores.API/"
                + "indicadoresEconomicos/318/series?fechaInicio=" + desde
                + "&fechaFin=" + hasta + "&idioma=ES";

        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(TIEMPO_LIMITE_MS);
            conn.setReadTimeout(TIEMPO_LIMITE_MS);
            conn.setRequestProperty("Authorization", "Bearer " + TOKEN);
            conn.setRequestProperty("X-Client-Email", EMAIL);
            conn.setRequestProperty("Accept", "application/json");

            StringBuilder sb = new StringBuilder();
            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                String line;
                while ((line = in.readLine()) != null) sb.append(line);
            }

            String json = sb.toString();
            String clave = "\"valorDatoPorPeriodo\":";
            int i = json.lastIndexOf(clave);
            if (i == -1) return -1;

            int ini = i + clave.length();
            int fin = ini;
            while (fin < json.length() && "0123456789.".indexOf(json.charAt(fin)) >= 0) fin++;

            return Double.parseDouble(json.substring(ini, fin).trim());
        } catch (Exception e) {
            System.err.println("No se pudo obtener el tipo de cambio del BCCR: " + e.getMessage());
            return -1;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    public static double calcularPremio(int puntaje) {
        if (puntaje <= 0) {
            return 0.0;
        }
        double cambioDolar = obtenerCambioDolar();
        return (1.0 / puntaje) * 100.0 * cambioDolar;
    }
}
