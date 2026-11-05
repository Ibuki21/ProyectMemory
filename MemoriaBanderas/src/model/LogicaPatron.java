package model;
import java.util.Random;

/**
 * Modelo del Modo Patrones (unijugador).
 * Genera el patrón, lo valida y lleva el nivel actual.
 * No contiene componentes gráficos ni manejo de tiempo.
 */
public class LogicaPatron {

    // El primer patrón siempre inicia con 3 casillas
    private static final int LONGITUD_INICIAL = 3;

    private final int filas;
    private final int columnas;
    private final Random aleatorio = new Random();

    // Matriz de n x 2: cada fila guarda [fila, columna] de una casilla del patrón
    private int[][] patron;
    private int longitudActual;
    private int indiceUsuario;

    public LogicaPatron(int filas, int columnas) {
        this.filas = filas;
        this.columnas = columnas;
    }

    // Inicia una partida nueva con un patrón de 3 casillas
    public void iniciar() {
        longitudActual = LONGITUD_INICIAL;
        generarPatron();
    }

    // Genera un patrón aleatorio sin repetir casillas
    private void generarPatron() {
        patron = new int[longitudActual][2];
        for (int k = 0; k < longitudActual; k++) {
            int fila;
            int columna;
            do {
                fila = aleatorio.nextInt(filas);
                columna = aleatorio.nextInt(columnas);
            } while (estaEnPatron(fila, columna, k));
            patron[k][0] = fila;
            patron[k][1] = columna;
        }
        indiceUsuario = 0;
    }

    // Revisa si una casilla ya está en las primeras 'hasta' posiciones del patrón
    private boolean estaEnPatron(int fila, int columna, int hasta) {
        for (int i = 0; i < hasta; i++) {
            if (patron[i][0] == fila && patron[i][1] == columna) {
                return true;
            }
        }
        return false;
    }

    // Verifica si la casilla presionada es la que sigue en el patrón
    public boolean validarCasilla(int fila, int columna) {
        if (patron[indiceUsuario][0] == fila && patron[indiceUsuario][1] == columna) {
            indiceUsuario++;
            return true;
        }
        return false;
    }

    // Indica si el usuario ya replicó todo el patrón
    public boolean patronCompleto() {
        return indiceUsuario == longitudActual;
    }

    // Indica si todavía caben más casillas en el tablero para el siguiente patrón
    public boolean puedeAvanzar() {
        return longitudActual < filas * columnas;
    }

    // Sube un nivel: genera un patrón nuevo con 1 casilla más
    public void avanzarNivel() {
        longitudActual++;
        generarPatron();
    }

    // Reinicia el avance del usuario sobre el patrón actual
    public void reiniciarIntento() {
        indiceUsuario = 0;
    }

    public int getLongitud() {
        return longitudActual;
    }

    public int getFila(int posicion) {
        return patron[posicion][0];
    }

    public int getColumna(int posicion) {
        return patron[posicion][1];
    }

    // Cantidad de patrones que el usuario logró completar
    public int getPatronesCompletados() {
        return longitudActual - LONGITUD_INICIAL;
    }
}
