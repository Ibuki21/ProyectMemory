import java.io.File;

public class Usuario {
    private String nombre;
    private String apellidos;
    private String correo;
    private String usuario;
    private String password;
    private String fechaNacimiento;
    private File fotoPerfil;
    private int puntuacionMaxima; // Nueva variable para almacenar el récord

    public Usuario(String nombre, String apellidos, String correo, String usuario, String password, String fechaNacimiento, File fotoPerfil) {
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.correo = correo;
        this.usuario = usuario;
        this.password = password;
        this.fechaNacimiento = fechaNacimiento;
        this.fotoPerfil = fotoPerfil;
        this.puntuacionMaxima = 0; // Inicializa en 0
    }

    public String getNombre() { return nombre; }
    public String getApellidos() { return apellidos; }
    public String getCorreo() { return correo; }
    public String getUsuario() { return usuario; }
    public String getPassword() { return password; }
    public String getFechaNacimiento() { return fechaNacimiento; }
    public File getFotoPerfil() { return fotoPerfil; }

    public int getPuntuacionMaxima() { return puntuacionMaxima; }
    public void setPuntuacionMaxima(int puntuacion) {
        if (puntuacion > this.puntuacionMaxima) {
            this.puntuacionMaxima = puntuacion;
        }
    }
}