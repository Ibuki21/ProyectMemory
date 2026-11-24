package model;
import java.io.File;
import java.io.Serializable;

public class Usuario implements Serializable { // Entidad que representa a un jugador registrado
    private static final long serialVersionUID = 1L; // Identificador de versión para serialización

    private String nombre;
    private String apellidos;
    private String correo;
    private String usuario;
    private String password;
    private String fechaNacimiento;
    private File fotoPerfil;
    private int puntuacionMaxima;
    private int mejorIntentosParejas;

    public Usuario(String nombre, String apellidos, String correo, String usuario, String password, String fechaNacimiento, File fotoPerfil) { // Constructor inicial para nuevos registros
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

    public Usuario(String nombre, String apellidos, String correo, String usuario, String password, String fechaNacimiento, File fotoPerfil, int puntuacionMaxima, int mejorIntentosParejas) { // Constructor completo usado al cargar desde archivo
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

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getUsuario() { return usuario; } // Obtiene el nombre de usuario
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(String fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public File getFotoPerfil() { return fotoPerfil; }
    public void setFotoPerfil(File fotoPerfil) { this.fotoPerfil = fotoPerfil; }

    public int getPuntuacionMaxima() { return puntuacionMaxima; }
    public void setPuntuacionMaxima(int puntuacion) { this.puntuacionMaxima = puntuacion; }

    // Acumula los puntos nuevos sumándolos de forma persistente al total
    public void agregarPuntos(int puntosNuevos) { // Suma puntos ganados al total del usuario
        if (puntosNuevos > 0) { // Solo suma si la cantidad es positiva
            this.puntuacionMaxima += puntosNuevos; // Acumula los puntos
        }
    }

    // Alias para compatibilidad con la tabla del menú principal
    public int getPuntos() { // Devuelve el puntaje total bajo el nombre getPuntos
        return this.puntuacionMaxima;
    }

    public int getMejorIntentosParejas() { return mejorIntentosParejas; } // Devuelve el menor número de intentos en parejas

    public boolean registrarIntentosParejas(int intentos) { // Evalúa si se logró una nueva mejor marca de intentos
        if (intentos <= 0) {
            return false;
        }
        if (mejorIntentosParejas == 0 || intentos < mejorIntentosParejas) { // Si no había marca previa o se usaron menos intentos
            mejorIntentosParejas = intentos; // Actualiza el récord de intentos
            return true;
        }
        return false;
    }

    // --- Persistencia en Archivo de Texto ---

    public String toFileString() { // Convierte el objeto Usuario en una línea CSV separada por comas
        String rutaFoto = (fotoPerfil != null) ? fotoPerfil.getPath().replace(",", "_") : "null"; // Evita que comas en la ruta rompan el formato
        return nombre + "," + apellidos + "," + correo + "," + usuario + "," + password + "," + fechaNacimiento + "," + rutaFoto + "," + puntuacionMaxima + "," + mejorIntentosParejas; // Concatena los campos en orden
    }

    public static Usuario fromFileString(String line) { // Reconstruye un Usuario a partir de una línea de texto
        String[] parts = line.split(","); // Divide la línea por comas
        if (parts.length >= 9) { // Verifica que existan todos los campos esperados
            String nombre = parts[0].trim();
            String apellidos = parts[1].trim();
            String correo = parts[2].trim();
            String usuario = parts[3].trim();
            String password = parts[4].trim();
            String fechaNacimiento = parts[5].trim(); 

            String rutaFoto = parts[6].trim(); // Lee la ruta de la foto
            File foto = (rutaFoto.equals("null") || rutaFoto.isEmpty()) ? null : new File(rutaFoto);

            int puntuacion = 0; // Valor por defecto para puntuación
            try {
                puntuacion = Integer.parseInt(parts[parts.length - 2].trim()); // Convierte el penúltimo campo a entero
            } catch (NumberFormatException e) {
                puntuacion = 0; // Si hay error de formato, deja 0
            }

            int mejorIntentos = 0; // Valor por defecto para mejorIntentos
            try {
                mejorIntentos = Integer.parseInt(parts[parts.length - 1].trim()); // Convierte el último campo a entero
            } catch (NumberFormatException e) {
                mejorIntentos = 0; // Si hay error de formato, deja 0
            }

            return new Usuario(nombre, apellidos, correo, usuario, password, fechaNacimiento, foto, puntuacion, mejorIntentos); // Retorna el usuario reconstruido
        }
        return null; // Retorna null si la línea no tiene el formato esperado
    }
}