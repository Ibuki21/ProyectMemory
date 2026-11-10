package model;
import java.io.File;

public class Usuario {
    private String nombre;
    private String apellidos;
    private String correo;
    private String usuario;
    private String password;
    private String fechaNacimiento;
    private File fotoPerfil;
    private int puntuacionMaxima;
    private int mejorIntentosParejas;

    public Usuario(String nombre, String apellidos, String correo, String usuario, String password, String fechaNacimiento, File fotoPerfil) {
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.correo = correo;
        this.usuario = usuario;
        this.password = password;
        this.fechaNacimiento = fechaNacimiento;
        this.fotoPerfil = fotoPerfil;
        this.puntuacionMaxima = 0;
        this.mejorIntentosParejas = 0;
    }

    public Usuario(String nombre, String apellidos, String correo, String usuario, String password, String fechaNacimiento, File fotoPerfil, int puntuacionMaxima) {
        this(nombre, apellidos, correo, usuario, password, fechaNacimiento, fotoPerfil, puntuacionMaxima, 0);
    }

    public Usuario(String nombre, String apellidos, String correo, String usuario, String password, String fechaNacimiento, File fotoPerfil, int puntuacionMaxima, int mejorIntentosParejas) {
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.correo = correo;
        this.usuario = usuario;
        this.password = password;
        this.fechaNacimiento = fechaNacimiento;
        this.fotoPerfil = fotoPerfil;
        this.puntuacionMaxima = puntuacionMaxima;
        this.mejorIntentosParejas = mejorIntentosParejas;
    }

    // --- Getters y Setters ---

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

    public String getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(String fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public File getFotoPerfil() {
        return fotoPerfil;
    }

    public void setFotoPerfil(File fotoPerfil) {
        this.fotoPerfil = fotoPerfil;
    }

    public int getPuntuacionMaxima() {
        return puntuacionMaxima;
    }

    public void setPuntuacionMaxima(int puntuacion) {
        this.puntuacionMaxima = puntuacion;
    }

    // Acumula los puntos nuevos sumándolos al total actual de forma persistente
    public void agregarPuntos(int puntosNuevos) {
        this.puntuacionMaxima += puntosNuevos;
    }

    public int getMejorIntentosParejas() {
        return mejorIntentosParejas;
    }

    public boolean registrarIntentosParejas(int intentos) {
        if (intentos <= 0) {
            return false;
        }
        if (mejorIntentosParejas == 0 || intentos < mejorIntentosParejas) {
            mejorIntentosParejas = intentos;
            return true;
        }
        return false;
    }

    // --- Métodos de Persistencia en Archivo de Texto ---

    public String toFileString() {
        String rutaFoto = (fotoPerfil != null) ? fotoPerfil.getPath().replace(",", "_") : "null";
        return nombre + "," + apellidos + "," + correo + "," + usuario + "," + password + "," + fechaNacimiento + "," + rutaFoto + "," + puntuacionMaxima + "," + mejorIntentosParejas;
    }

    public static Usuario fromFileString(String line) {
        String[] parts = line.split(",");
        if (parts.length >= 9) {
            String nombre = parts[0].trim();
            String apellidos = parts[1].trim();
            String correo = parts[2].trim();
            String usuario = parts[3].trim();
            String password = parts[4].trim();
            String fechaNacimiento = parts[5].trim();

            String rutaFoto = parts[6].trim();
            File foto = (rutaFoto.equals("null") || rutaFoto.isEmpty()) ? null : new File(rutaFoto);

            int puntuacion = 0;
            try {
                puntuacion = Integer.parseInt(parts[parts.length - 2].trim());
            } catch (NumberFormatException e) {
                puntuacion = 0;
            }

            int mejorIntentos = 0;
            try {
                mejorIntentos = Integer.parseInt(parts[parts.length - 1].trim());
            } catch (NumberFormatException e) {
                mejorIntentos = 0;
            }

            return new Usuario(nombre, apellidos, correo, usuario, password, fechaNacimiento, foto, puntuacion, mejorIntentos);
        }
        return null;
    }
}
