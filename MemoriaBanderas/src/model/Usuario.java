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

    // Getters y Setters
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(String fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public File getFotoPerfil() { return fotoPerfil; }
    public void setFotoPerfil(File fotoPerfil) { this.fotoPerfil = fotoPerfil; }

    public int getPuntuacionMaxima() { return puntuacionMaxima; }
    public void setPuntuacionMaxima(int puntuacion) {
        if (puntuacion > this.puntuacionMaxima) {
            this.puntuacionMaxima = puntuacion;
        }
    }

    // Métodos de Persistencia en TXT
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

            String rutaFoto = parts[6].trim();
            File foto = rutaFoto.equals("null") ? null : new File(rutaFoto);

            int puntuacion = Integer.parseInt(parts[7].trim());

            return new Usuario(nombre, apellidos, correo, usuario, password, fechaNacimiento, foto, puntuacion);
        }
        return null;
    }
}