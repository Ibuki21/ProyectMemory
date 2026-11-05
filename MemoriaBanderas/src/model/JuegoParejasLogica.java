package model;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JuegoParejasLogica {

    private static final int TOTAL_CARTAS = 20;
    private static final int TOTAL_PARES = 10;

    private String[] codigosBase = {
            "cafe", "Estrella", "Fresa", "Gato", "Honguitos",
            "Manzanas", "Moneda", "Reloj", "Sombrero", "Vela"
    };

    private List<String> baraja;
    private boolean[] cartasReveladas;
    private boolean[] cartasActualmenteVolteadas;

    private int indicePrimeraSeleccion = -1;
    private int indiceSegundaSeleccion = -1;
    private int intentos = 0;
    private int paresEncontrados = 0;

    public JuegoParejasLogica() {
        baraja = new ArrayList<>();
        cartasReveladas = new boolean[TOTAL_CARTAS];
        cartasActualmenteVolteadas = new boolean[TOTAL_CARTAS];
        reiniciarJuego();
    }

    public void reiniciarJuego() {
        baraja.clear();
        for (String codigo : codigosBase) {
            baraja.add(codigo);
            baraja.add(codigo);
        }
        // Cada tablero se mezcla de forma independiente, generando un patrón único
        Collections.shuffle(baraja);

        for (int i = 0; i < TOTAL_CARTAS; i++) {
            cartasReveladas[i] = false;
            cartasActualmenteVolteadas[i] = false;
        }
        indicePrimeraSeleccion = -1;
        indiceSegundaSeleccion = -1;
        intentos = 0;
        paresEncontrados = 0;
    }

    public boolean seleccionarCarta(int indice) {
        if (cartasReveladas[indice] || cartasActualmenteVolteadas[indice]) {
            return false;
        }

        if (indicePrimeraSeleccion == -1) {
            indicePrimeraSeleccion = indice;
            cartasActualmenteVolteadas[indice] = true;
            return true;
        } else if (indiceSegundaSeleccion == -1 && indice != indicePrimeraSeleccion) {
            indiceSegundaSeleccion = indice;
            cartasActualmenteVolteadas[indice] = true;
            intentos++;
            return true;
        }
        return false;
    }

    public boolean evaluarPareja() {
        if (indicePrimeraSeleccion != -1 && indiceSegundaSeleccion != -1) {
            String carta1 = baraja.get(indicePrimeraSeleccion);
            String carta2 = baraja.get(indiceSegundaSeleccion);

            if (carta1.equals(carta2)) {
                cartasReveladas[indicePrimeraSeleccion] = true;
                cartasReveladas[indiceSegundaSeleccion] = true;
                paresEncontrados++;
                limpiarSeleccionTemporal();
                return true;
            }
        }
        return false;
    }

    public void ocultarSeleccionFallida() {
        if (indicePrimeraSeleccion != -1 && indiceSegundaSeleccion != -1) {
            cartasActualmenteVolteadas[indicePrimeraSeleccion] = false;
            cartasActualmenteVolteadas[indiceSegundaSeleccion] = false;
            limpiarSeleccionTemporal();
        }
    }

    private void limpiarSeleccionTemporal() {
        indicePrimeraSeleccion = -1;
        indiceSegundaSeleccion = -1;
    }

    public boolean haGanado() {
        return paresEncontrados == TOTAL_PARES;
    }

    public String getCodigoCarta(int indice) { return baraja.get(indice); }
    public int getIntentos() { return intentos; }
    public int getParesEncontrados() { return paresEncontrados; }
    public int getIndicePrimeraSeleccion() { return indicePrimeraSeleccion; }
    public int getIndiceSegundaSeleccion() { return indiceSegundaSeleccion; }
    public boolean estaRevelada(int indice) { return cartasReveladas[indice]; }
}