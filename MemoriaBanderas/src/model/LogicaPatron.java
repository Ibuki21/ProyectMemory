package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LogicaPatron {
    private int filas;
    private int columnas;
    private List<int[]> secuencia;
    private int indicePasoUsuario;
    private int puntuacion;
    private int cantidadCasillas;

    // Constructor que recibe filas y columnas (6, 6)
    public LogicaPatron(int filas, int columnas) {
        this.filas = filas;
        this.columnas = columnas;
        this.secuencia = new ArrayList<>();
        this.puntuacion = 0;
        this.cantidadCasillas = 3; // Comienza pidiendo recordar 3 casillas
        generarNuevaSecuencia();
    }

    public void generarNuevaSecuencia() {
        secuencia.clear();
        Random rand = new Random();
        for (int i = 0; i < cantidadCasillas; i++) {
            int f = rand.nextInt(filas); // Elige una fila al azar
            int c = rand.nextInt(columnas);
            secuencia.add(new int[]{f, c});
        }
        indicePasoUsuario = 0;
    }

    public void reiniciarRonda() {
        indicePasoUsuario = 0;
    }

    public List<int[]> getSecuencia() {
        return secuencia;
    }

    public List<int[]> getSecuenciaActual() {
        return secuencia;
    }

    public boolean verificarPaso(int f, int c) {
        if (indicePasoUsuario < secuencia.size()) {
            int[] esperado = secuencia.get(indicePasoUsuario);
            if (esperado[0] == f && esperado[1] == c) {
                indicePasoUsuario++;
                return true;
            }
        }
        return false;
    }

    public boolean esRondaCompletada() {
        return indicePasoUsuario >= secuencia.size();
    }

    public void siguienteNivel() {
        cantidadCasillas++;
        puntuacion += 10; // Sumar puntos por cada nivel superado
        generarNuevaSecuencia();
    }

    public int getPuntuacion() {
        return puntuacion;
    }

    public int getCantidadCasillas() {
        return cantidadCasillas;
    }
}