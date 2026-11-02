import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class BccrApiClient {
    public static void main(String[] args) {
        String token = "token";
        String email = "correo";

        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        String hasta = LocalDate.now().format(f);
        String desde = LocalDate.now().minusDays(7).format(f); // margen por fines de semana/feriados

        String url = "https://apim.bccr.fi.cr/SDDE/api/Bccr.GE.SDDE.Publico.Indicadores.API/"
                + "indicadoresEconomicos/318/series?fechaInicio=" + desde
                + "&fechaFin=" + hasta + "&idioma=ES";

        try {
            HttpURLConnection conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + token);
            conn.setRequestProperty("X-Client-Email", email);
            conn.setRequestProperty("Accept", "application/json");

            StringBuilder sb = new StringBuilder();
            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                String line;
                while ((line = in.readLine()) != null) sb.append(line);
            }

            // Toma el último valor de la serie (el más reciente)
            String json = sb.toString();
            int i = json.lastIndexOf("\"valorDatoPorPeriodo\":");
            int ini = i + "\"valorDatoPorPeriodo\":".length();
            int fin = ini;
            while (fin < json.length() && "0123456789.".indexOf(json.charAt(fin)) >= 0) fin++;

            System.out.println(json.substring(ini, fin));
        } catch (Exception e) {
            // Sin salida en consola
        }
    }
}