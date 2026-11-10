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

    public static double obtenerCambioDolar() {
        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        String hasta = LocalDate.now().format(f);
        String desde = LocalDate.now().minusDays(7).format(f);

        String url = "https://apim.bccr.fi.cr/SDDE/api/Bccr.GE.SDDE.Publico.Indicadores.API/"
                + "indicadoresEconomicos/318/series?fechaInicio=" + desde
                + "&fechaFin=" + hasta + "&idioma=ES";

        try {
            HttpURLConnection conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
            conn.setRequestMethod("GET");
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
            if (i == -1) return 500.0;

            int ini = i + clave.length();
            int fin = ini;
            while (fin < json.length() && "0123456789.".indexOf(json.charAt(fin)) >= 0) fin++;

            String valorStr = json.substring(ini, fin).trim();
            return Double.parseDouble(valorStr);
        } catch (Exception e) {
            System.err.println("No se pudo obtener el tipo de cambio del BCCR: " + e.getMessage());
            return 500.0;
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