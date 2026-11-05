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

    // Constructor sin puntuación inicial (inicia en 0)
    public Usuario(String nombre, String apellidos, String correo, String usuario, String password, String fechaNacimiento, File fotoPerfil) {
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.correo = correo;
        this.usuario = usuario;
        this.password = password;
        this.fechaNacimiento = fechaNacimiento;
        this.fotoPerfil = fotoPerfil;
        this.puntuacionMaxima = 0;
    }

    // Constructor completo con puntuación
    public Usuario(String nombre, String apellidos, String correo, String usuario, String password, String fechaNacimiento, File fotoPerfil, int puntuacionMaxima) {
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.correo = correo;
        this.usuario = usuario;
        this.password = password;
        this.fechaNacimiento = fechaNacimiento;
        this.fotoPerfil = fotoPerfil;
        this.puntuacionMaxima = puntuacionMaxima;
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
        if (puntuacion > this.puntuacionMaxima) {
            this.puntuacionMaxima = puntuacion;
        }
    }

    // --- Métodos de Persistencia en Archivo de Texto ---

    public String toFileString() {
        String rutaFoto = (fotoPerfil != null) ? fotoPerfil.getPath() : "null";
        return nombre + "," + apellidos + "," + correo + "," + usuario + "," + password + "," + fechaNacimiento + "," + rutaFoto + "," + puntuacionMaxima;
    }

    public static Usuario fromFileString(String line) {
        String[] parts = line.split(",");
        if (parts.length >= 8) {
            String nombre = parts[0].trim();
            String apellidos = parts[1].trim();
            String correo = parts[2].trim();
            String usuario = parts[3].trim();
            String password = parts[4].trim();
            String fechaNacimiento = parts[5].trim();

            // La foto siempre se toma de la penúltima posición
            String rutaFoto = parts[parts.length - 2].trim();
            File foto = (rutaFoto.equals("null") || rutaFoto.isEmpty()) ? null : new File(rutaFoto);

            // La puntuación se toma de la última posición de la línea con manejo seguro de NumberFormatException
            int puntuacion = 0;
            try {
                puntuacion = Integer.parseInt(parts[parts.length - 1].trim());
            } catch (NumberFormatException e) {
                puntuacion = 0; // Si el dato del texto no es numérico, se asigna 0 sin que crashee la App
            }

            return new Usuario(nombre, apellidos, correo, usuario, password, fechaNacimiento, foto, puntuacion);
        }
        return null;
    }
}