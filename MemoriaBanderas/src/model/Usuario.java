package model;
import java.io.File;
import java.io.Serializable;

public class Usuario implements Serializable { // Entidad que representa a un jugador registrado
    private static final long serialVersionUID = 1L; // Identificador de versión para serialización

    private String nombre; // Nombre del usuario
    private String apellidos; // Apellidos del usuario
    private String correo; // Correo electrónico del usuario
    private String usuario; // Nombre de usuario (login)
    private String password; // Contraseña del usuario
    private String fechaNacimiento; // Fecha de nacimiento
    private File fotoPerfil; // Archivo asociado a la fotografía de perfil
    private int puntuacionMaxima; // Funciona como el acumulador total de puntos
    private int mejorIntentosParejas; // Menor cantidad de intentos lograda en el modo parejas

    public Usuario(String nombre, String apellidos, String correo, String usuario, String password, String fechaNacimiento, File fotoPerfil) { // Constructor inicial para nuevos registros
        this.nombre = nombre; // Asigna nombre
        this.apellidos = apellidos; // Asigna apellidos
        this.correo = correo; // Asigna correo
        this.usuario = usuario; // Asigna nombre de usuario
        this.password = password; // Asigna contraseña
        this.fechaNacimiento = fechaNacimiento; // Asigna fecha de nacimiento
        this.fotoPerfil = fotoPerfil; // Asigna archivo de foto
        this.puntuacionMaxima = 0; // Inicializa puntos acumulados en 0
        this.mejorIntentosParejas = 0; // Inicializa mejor marca de intentos en 0 (sin registro previo)
    }

    public Usuario(String nombre, String apellidos, String correo, String usuario, String password, String fechaNacimiento, File fotoPerfil, int puntuacionMaxima, int mejorIntentosParejas) { // Constructor completo usado al cargar desde archivo
        this.nombre = nombre; // Asigna nombre
        this.apellidos = apellidos; // Asigna apellidos
        this.correo = correo; // Asigna correo
        this.usuario = usuario; // Asigna nombre de usuario
        this.password = password; // Asigna contraseña
        this.fechaNacimiento = fechaNacimiento; // Asigna fecha de nacimiento
        this.fotoPerfil = fotoPerfil; // Asigna archivo de foto
        this.puntuacionMaxima = puntuacionMaxima; // Restaura puntaje acumulado
        this.mejorIntentosParejas = mejorIntentosParejas; // Restaura mejor cantidad de intentos
    }

    // --- Getters y Setters ---

    public String getNombre() { return nombre; } // Obtiene el nombre
    public void setNombre(String nombre) { this.nombre = nombre; } // Modifica el nombre

    public String getApellidos() { return apellidos; } // Obtiene los apellidos
    public void setApellidos(String apellidos) { this.apellidos = apellidos; } // Modifica los apellidos

    public String getCorreo() { return correo; } // Obtiene el correo
    public void setCorreo(String correo) { this.correo = correo; } // Modifica el correo

    public String getUsuario() { return usuario; } // Obtiene el nombre de usuario
    public void setUsuario(String usuario) { this.usuario = usuario; } // Modifica el nombre de usuario

    public String getPassword() { return password; } // Obtiene la contraseña
    public void setPassword(String password) { this.password = password; } // Modifica la contraseña

    public String getFechaNacimiento() { return fechaNacimiento; } // Obtiene la fecha de nacimiento
    public void setFechaNacimiento(String fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; } // Modifica la fecha de nacimiento

    public File getFotoPerfil() { return fotoPerfil; } // Obtiene el archivo de foto de perfil
    public void setFotoPerfil(File fotoPerfil) { this.fotoPerfil = fotoPerfil; } // Modifica la foto de perfil

    public int getPuntuacionMaxima() { return puntuacionMaxima; } // Obtiene los puntos acumulados
    public void setPuntuacionMaxima(int puntuacion) { this.puntuacionMaxima = puntuacion; } // Establece directamente la puntuación

    // Acumula los puntos nuevos sumándolos de forma persistente al total
    public void agregarPuntos(int puntosNuevos) { // Suma puntos ganados al total del usuario
        if (puntosNuevos > 0) { // Solo suma si la cantidad es positiva
            this.puntuacionMaxima += puntosNuevos; // Acumula los puntos
        }
    }

    // Alias para compatibilidad con la tabla del menú principal
    public int getPuntos() { // Devuelve el puntaje total bajo el nombre getPuntos
        return this.puntuacionMaxima; // Retorna puntuacionMaxima
    }

    public int getMejorIntentosParejas() { return mejorIntentosParejas; } // Devuelve el menor número de intentos en parejas

    public boolean registrarIntentosParejas(int intentos) { // Evalúa si se logró una nueva mejor marca de intentos
        if (intentos <= 0) { // Ignora valores inválidos menores o iguales a cero
            return false; // No registra marca
        }
        if (mejorIntentosParejas == 0 || intentos < mejorIntentosParejas) { // Si no había marca previa o se usaron menos intentos
            mejorIntentosParejas = intentos; // Actualiza el récord de intentos
            return true; // Indica que hubo mejora
        }
        return false; // Indica que no superó la marca anterior
    }

    // --- Persistencia en Archivo de Texto ---

    public String toFileString() { // Convierte el objeto Usuario en una línea CSV separada por comas
        String rutaFoto = (fotoPerfil != null) ? fotoPerfil.getPath().replace(",", "_") : "null"; // Evita que comas en la ruta rompan el formato
        return nombre + "," + apellidos + "," + correo + "," + usuario + "," + password + "," + fechaNacimiento + "," + rutaFoto + "," + puntuacionMaxima + "," + mejorIntentosParejas; // Concatena los campos en orden
    }

    public static Usuario fromFileString(String line) { // Reconstruye un Usuario a partir de una línea de texto
        String[] parts = line.split(","); // Divide la línea por comas
        if (parts.length >= 9) { // Verifica que existan todos los campos esperados
            String nombre = parts[0].trim(); // Lee y limpia el nombre
            String apellidos = parts[1].trim(); // Lee y limpia los apellidos
            String correo = parts[2].trim(); // Lee y limpia el correo
            String usuario = parts[3].trim(); // Lee y limpia el usuario
            String password = parts[4].trim(); // Lee y limpia la contraseña
            String fechaNacimiento = parts[5].trim(); // Lee y limpia la fecha de nacimiento

            String rutaFoto = parts[6].trim(); // Lee la ruta de la foto
            File foto = (rutaFoto.equals("null") || rutaFoto.isEmpty()) ? null : new File(rutaFoto); // Crea el File si hay ruta válida

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