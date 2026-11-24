package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JuegoParejasLogica { // Contiene las reglas y el estado lógico de un tablero de parejas
    private static final int FILAS = 6;
    private static final int COLUMNAS = 6;
    private static final int TOTAL_CASILLAS = FILAS * COLUMNAS;
    private static final int TOTAL_PARES = 18;

    private String[] codigosCartas;
    private boolean[] reveladas;

    private int indicePrimeraSeleccion = -1; // Índice de la primera carta volteada en el turno (-1 si no hay)
    private int indiceSegundaSeleccion = -1;

    private int intentos = 0; // Contador de pares intentados
    private int paresEncontrados = 0;

    // Las 18 imágenes distintas (las 8 nuevas + 10 base)
    private static final String[] IMAGENES_BASE = {
            "Dragon", "Rojo", "Navidad", "Rosa", "Calabaza", "Fantasma", "Halloween", "Otono",
            "Cafe", "Fresa", "Estrella", "Gato", "Honguitos", "Manzanas", "Moneda", "Reloj", "Sombrero", "Vela"
    };

    public JuegoParejasLogica() { // Constructor de la lógica del juego
        codigosCartas = new String[TOTAL_CASILLAS]; // Inicializa arreglo de 36 códigos de carta
        reveladas = new boolean[TOTAL_CASILLAS]; // Inicializa arreglo de estados descubiertos
        reiniciarJuego(); // Prepara el primer tablero listo para jugar
    }

    public void reiniciarJuego() { // Reinicia contadores, selecciones y genera un nuevo tablero
        intentos = 0; // Reinicia intentos a 0
        paresEncontrados = 0; // Reinicia pares encontrados a 0
        indicePrimeraSeleccion = -1;
        indiceSegundaSeleccion = -1;

        for (int i = 0; i < TOTAL_CASILLAS; i++) { // Recorre todas las casillas
            reveladas[i] = false; // Marca cada casilla como no descubierta
        }

        generarTableroAleatorio(); // Reparte y mezcla las cartas aleatoriamente
    }

    private void generarTableroAleatorio() { // Crea los 18 pares y los mezcla en las 36 posiciones
        List<String> listaCartas = new ArrayList<>(); // Lista temporal para barajar
        // Añadir cada imagen dos veces para formar los 18 pares
        for (int i = 0; i < TOTAL_PARES; i++) {
            String nombreImg = IMAGENES_BASE[i % IMAGENES_BASE.length];
            listaCartas.add(nombreImg); // Agrega primera copia del par
            listaCartas.add(nombreImg); // Agrega segunda copia del par
        }
        // Mezclar de forma totalmente aleatoria
        Collections.shuffle(listaCartas);

        for (int i = 0; i < TOTAL_CASILLAS; i++) { // Vuelca la lista mezclada al arreglo fijo del tablero
            codigosCartas[i] = listaCartas.get(i); // Asigna la carta a la posición i
        }
    }

    public boolean seleccionarCarta(int indice) { // Registra la selección de una carta si es válida
        if (indice < 0 || indice >= TOTAL_CASILLAS || reveladas[indice]) {
            return false;
        }
        if (indicePrimeraSeleccion == indice || indiceSegundaSeleccion == indice) { // Evita seleccionar dos veces la misma casilla
            return false;
        }

        if (indicePrimeraSeleccion == -1) { // Si aún no se eligió la primera carta
            indicePrimeraSeleccion = indice; // Guarda la primera selección
            return true;
        } else if (indiceSegundaSeleccion == -1) { // Si ya hay primera carta pero falta la segunda
            indiceSegundaSeleccion = indice; // Guarda la segunda selección
            intentos++; // Incrementa el contador de intentos al completar el par elegido
            return true;
        }
        return false; // Si ya hay dos cartas seleccionadas pendientes de evaluar, rechaza
    }

    public boolean evaluarPareja() { // Compara las dos cartas seleccionadas para ver si coinciden
        if (indicePrimeraSeleccion != -1 && indiceSegundaSeleccion != -1) {
            String carta1 = codigosCartas[indicePrimeraSeleccion];
            String carta2 = codigosCartas[indiceSegundaSeleccion];

            if (carta1.equals(carta2)) { // Comprueba si ambas cartas tienen el mismo código
                reveladas[indicePrimeraSeleccion] = true; // Marca la primera carta como descubierta permanentemente
                reveladas[indiceSegundaSeleccion] = true; // Marca la segunda carta como descubierta permanentemente
                paresEncontrados++;
                limpiarSeleccionTemporal(); // Limpia los índices temporales de selección
                return true;
            }
        }
        return false; // Indica que no coincidieron o no había par completo
    }

    public void ocultarSeleccionFallida() { // Limpia la selección cuando las cartas no coinciden para volver a ocultarlas
        limpiarSeleccionTemporal();
    }

    public void cancelarSeleccionPendiente() { // Cancela una selección a medias (por ejemplo, al agotarse el tiempo del turno)
        limpiarSeleccionTemporal(); // Restablece los índices de selección a -1
    }

    private void limpiarSeleccionTemporal() { // Reinicia los índices de primera y segunda selección
        indicePrimeraSeleccion = -1;
        indiceSegundaSeleccion = -1;
    }

    public boolean haGanado() { // Comprueba si se encontraron todos los pares del tablero
        return paresEncontrados == TOTAL_PARES; // Verdadero cuando paresEncontrados llega a 18
    }

    public boolean estaRevelada(int indice) { // Consulta si una casilla ya está descubierta
        return reveladas[indice];
    }

    public String getCodigoCarta(int indice) { // Devuelve el código/nombre de carta en el índice dado
        return codigosCartas[indice];
    }

    public int getIndicePrimeraSeleccion() { return indicePrimeraSeleccion; } // Devuelve índice de la 1.ª carta elegida
    public int getIndiceSegundaSeleccion() { return indiceSegundaSeleccion; } // Devuelve índice de la 2.ª carta elegida
    public int getIntentos() { return intentos; }
    public int getParesEncontrados() { return paresEncontrados; }
    public int getFilas() { return FILAS; }
    public int getColumnas() { return COLUMNAS; }
}