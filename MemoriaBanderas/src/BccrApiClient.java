// Estos imports son para leer la respuesta de internet y para manejar fechas.
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

// Clase de prueba, independiente del juego. Sirve para comprobar que la consulta al BCCR funciona. El juego usa
// ServicioCambioDolar.
public class BccrApiClient {
    // main: punto de entrada de esta clase. Se puede ejecutar sola.
    public static void main(String[] args) {
        // Token (clave) que da el BCCR para usar su servicio.
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJCQ0NSLVNEREUiLCJzdWIiOiJqb3JzaGFucXVlc2FkYUBnbWFpbC5jb20iLCJhdWQiOiJTRERFLVNpdGlvRXh0ZXJubyIsImV4cCI6MjUzNDAyMzAwODAwLCJuYmYiOjE3OTMyODQ0NjAsImlhdCI6MTc5MzI4NDQ2MCwianRpIjoiMDlhYmMwODQtOWI5My00YzBlLWE0MTAtOTAxNTI4NTVlNTkwIiwiZW1haWwiOiJqb3JzaGFucXVlc2FkYUBnbWFpbC5jb20ifQ.VRmfKg0UjOxnZx-r7H9M4UYi_K8sI3v0og7SPNNPB6I";
        // Correo con el que se registró el token.
        String email = "jorshanquesada@gmail.com";

        // Formato de fecha que pide el BCCR: año/mes/día.
        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        // Fecha de hoy.
        String hasta = LocalDate.now().format(f);
        String desde = LocalDate.now().minusDays(7).format(f); // margen por fines de semana/feriados

        // Se arma la dirección de la API. El 318 es el código del indicador del tipo de cambio del dólar. Se pide
        // desde hace 7 días hasta hoy.
        String url = "https://apim.bccr.fi.cr/SDDE/api/Bccr.GE.SDDE.Publico.Indicadores.API/"
                + "indicadoresEconomicos/318/series?fechaInicio=" + desde
                + "&fechaFin=" + hasta + "&idioma=ES";

        // El try captura cualquier error de conexión o lectura.
        try {
            // Se abre la conexión con esa dirección.
            HttpURLConnection conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
            // Se pide con GET (solo leer datos).
            conn.setRequestMethod("GET");
            // Se manda el token para identificarse.
            conn.setRequestProperty("Authorization", "Bearer " + token);
            // Se manda el correo registrado.
            conn.setRequestProperty("X-Client-Email", email);
            // Se pide la respuesta en formato JSON.
            conn.setRequestProperty("Accept", "application/json");

            // Aquí se va armando el texto completo de la respuesta.
            StringBuilder sb = new StringBuilder();
            // Se abre un lector sobre la respuesta. El try lo cierra solo al terminar.
            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                // Variable para cada línea leída.
                String line;
                // Se lee línea por línea y se junta todo en un solo texto.
                while ((line = in.readLine()) != null) sb.append(line);
            }

            // Toma el último valor de la serie (el más reciente)
            // Texto completo de la respuesta.
            String json = sb.toString();
            // Posición de la última aparición del campo con el valor (la más reciente).
            int i = json.lastIndexOf("\"valorDatoPorPeriodo\":");
            // Posición donde empieza el número, justo después del nombre del campo.
            int ini = i + "\"valorDatoPorPeriodo\":".length();
            // Posición donde termina el número, se va avanzando.
            int fin = ini;
            // Avanza mientras el carácter sea un dígito o un punto.
            while (fin < json.length() && "0123456789.".indexOf(json.charAt(fin)) >= 0) fin++;

            // Imprime en consola solo el número del tipo de cambio.
            System.out.println(json.substring(ini, fin));
        } catch (Exception e) {
            // Sin salida en consola
        }
    }
}