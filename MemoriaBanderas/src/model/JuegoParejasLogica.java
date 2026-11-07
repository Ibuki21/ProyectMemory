package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JuegoParejasLogica {
    private static final int FILAS = 6;
    private static final int COLUMNAS = 6;
    private static final int TOTAL_CASILLAS = FILAS * COLUMNAS; // 36 casillas
    private static final int TOTAL_PARES = 18; // 18 parejas

    private String[] codigosCartas; // Almacena el código/nombre de la imagen en cada posición
    private boolean[] reveladas;    // Indica si la casilla ya fue descubierta permanentemente

    private int indicePrimeraSeleccion = -1;
    private int indiceSegundaSeleccion = -1;

    private int intentos = 0;
    private int paresEncontrados = 0;

    // Las 18 imágenes distintas (las 8 nuevas + 10 base)
    private static final String[] IMAGENES_BASE = {
            "Dragon", "Rojo", "Navidad", "Rosa", "Calabaza", "Fantasma", "Halloween", "Otono",
            "Cafe", "Fresa", "Estrella", "Gato", "Honguitos", "Manzanas", "Moneda", "Reloj", "Sombrero", "Vela"
    };

    public JuegoParejasLogica() {
        codigosCartas = new String[TOTAL_CASILLAS];
        reveladas = new boolean[TOTAL_CASILLAS];
        reiniciarJuego();
    }

    public void reiniciarJuego() {
        intentos = 0;
        paresEncontrados = 0;
        indicePrimeraSeleccion = -1;
        indiceSegundaSeleccion = -1;

        for (int i = 0; i < TOTAL_CASILLAS; i++) {
            reveladas[i] = false;
        }

        generarTableroAleatorio();
    }

    private void generarTableroAleatorio() {
        List<String> listaCartas = new ArrayList<>();
        // Añadir cada imagen dos veces para formar los 18 pares
        for (int i = 0; i < TOTAL_PARES; i++) {
            String nombreImg = IMAGENES_BASE[i % IMAGENES_BASE.length];
            listaCartas.add(nombreImg);
            listaCartas.add(nombreImg);
        }
        // Mezclar de forma totalmente aleatoria
        Collections.shuffle(listaCartas);

        for (int i = 0; i < TOTAL_CASILLAS; i++) {
            codigosCartas[i] = listaCartas.get(i);
        }
    }

    public boolean seleccionarCarta(int indice) {
        if (indice < 0 || indice >= TOTAL_CASILLAS || reveladas[indice]) {
            return false;
        }
        if (indicePrimeraSeleccion == indice || indiceSegundaSeleccion == indice) {
            return false;
        }

        if (indicePrimeraSeleccion == -1) {
            indicePrimeraSeleccion = indice;
            return true;
        } else if (indiceSegundaSeleccion == -1) {
            indiceSegundaSeleccion = indice;
            intentos++;
            return true;
        }
        return false;
    }

    public boolean evaluarPareja() {
        if (indicePrimeraSeleccion != -1 && indiceSegundaSeleccion != -1) {
            String carta1 = codigosCartas[indicePrimeraSeleccion];
            String carta2 = codigosCartas[indiceSegundaSeleccion];

            if (carta1.equals(carta2)) {
                reveladas[indicePrimeraSeleccion] = true;
                reveladas[indiceSegundaSeleccion] = true;
                paresEncontrados++;
                limpiarSeleccionTemporal();
                return true;
            }
        }
        return false;
    }

    public void ocultarSeleccionFallida() {
        limpiarSeleccionTemporal();
    }

    public void cancelarSeleccionPendiente() {
        limpiarSeleccionTemporal();
    }

    private void limpiarSeleccionTemporal() {
        indicePrimeraSeleccion = -1;
        indiceSegundaSeleccion = -1;
    }

    public boolean haGanado() {
        return paresEncontrados == TOTAL_PARES;
    }

    public boolean estaRevelada(int indice) {
        return reveladas[indice];
    }

    public String getCodigoCarta(int indice) {
        return codigosCartas[indice];
    }

    public int getIndicePrimeraSeleccion() { return indicePrimeraSeleccion; }
    public int getIndiceSegundaSeleccion() { return indiceSegundaSeleccion; }
    public int getIntentos() { return intentos; }
    public int getParesEncontrados() { return paresEncontrados; }
    public int getFilas() { return FILAS; }
    public int getColumnas() { return COLUMNAS; }
}