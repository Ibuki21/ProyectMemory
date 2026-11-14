package model;

import java.io.Serializable;

public class Usuario implements Serializable {
    private static final long serialVersionUID = 1L;

    private String nombre;
    private String apellidos;
    private String correo;
    private String usuario;
    private String password;
    private String fechaNac;
    private String archivoFoto;

    private int puntuacionMaxima;
    private int mejorIntentosParejas;
    private int puntos;

    // 1. Constructor básico (3 parámetros)
    public Usuario(String usuario, String correo, String password) {
        this.nombre = "";
        this.apellidos = "";
        this.correo = correo;
        this.usuario = usuario;
        this.password = password;
        this.fechaNac = "";
        this.archivoFoto = "";
        this.puntuacionMaxima = 0;
        this.mejorIntentosParejas = 999;
        this.puntos = 0;
    }

    // 2. Constructor flexible (Acepta Object en fecha y foto para evitar errores de tipo File, Date o String)
    public Usuario(String nombre, String apellidos, String correo, String usuario, String password, Object fechaNac, Object archivoFoto) {
        this.nombre = nombre != null ? nombre.toString() : "";
        this.apellidos = apellidos != null ? apellidos.toString() : "";
        this.correo = correo != null ? correo.toString() : "";
        this.usuario = usuario != null ? usuario.toString() : "";
        this.password = password != null ? password.toString() : "";
        this.fechaNac = fechaNac != null ? fechaNac.toString() : "";
        this.archivoFoto = archivoFoto != null ? archivoFoto.toString() : "";

        this.puntuacionMaxima = 0;
        this.mejorIntentosParejas = 999;
        this.puntos = 0;
    }

    // Getters y Setters
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFechaNac() {
        return fechaNac;
    }

    public void setFechaNac(String fechaNac) {
        this.fechaNac = fechaNac;
    }

    public String getArchivoFoto() {
        return archivoFoto;
    }

    public void setArchivoFoto(String archivoFoto) {
        this.archivoFoto = archivoFoto;
    }

    public int getPuntuacionMaxima() {
        return puntuacionMaxima;
    }

    public void setPuntuacionMaxima(int puntuacionMaxima) {
        this.puntuacionMaxima = puntuacionMaxima;
    }

    public int getMejorIntentosParejas() {
        return mejorIntentosParejas;
    }

    public boolean registrarIntentosParejas(int intentos) {
        if (intentos < mejorIntentosParejas) {
            mejorIntentosParejas = intentos;
            return true;
        }
        return false;
    }

    public int getPuntos() {
        return puntos;
    }

    public void agregarPuntos(int puntosAdicionales) {
        this.puntos += puntosAdicionales;
    }
}