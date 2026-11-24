package model;
import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GestorUsuarios {
    private static final String RUTA_ARCHIVO = "usuarios.txt";
    private static Usuario usuarioSesionActual = null;

    //Mínimo agregado: Lista para controlar hasta 2 jugadores activos simultáneos en los minijuegos
    private static final List<Usuario> usuariosSesion = new ArrayList<>();

    static { //Bloque que se ejecuta una sola vez al cargar la clase
        crearArchivoSiNoExiste();
    }

    private static void crearArchivoSiNoExiste() {
        File file = new File(RUTA_ARCHIVO);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) { //Captura errores si no se pudo crear el archivo
            System.err.println("Error al inicializar el archivo de usuarios: " + e.getMessage());
        }
    }

    public static void registrarLoginSesion(Usuario usuario) {
        if (usuario != null && !usuariosSesion.contains(usuario)) {
            if (usuariosSesion.size() < 2) {
                usuariosSesion.add(usuario);
            }
        }
    }

    public static List<Usuario> getUsuariosSesion() {
        return usuariosSesion;
    }

    public static List<Usuario> cargarUsuarios() {
        List<Usuario> lista = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(RUTA_ARCHIVO))) { // Abre el archivo de usuarios para leerlo
            String linea;
            while ((linea = reader.readLine()) != null) { // Lee línea por línea hasta llegar al final
                if (!linea.trim().isEmpty()) {
                    Usuario u = Usuario.fromFileString(linea);
                    if (u != null) { // Revisa que el usuario se haya leído correctamente
                        lista.add(u);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error al cargar usuarios desde archivo: " + e.getMessage());
        }
        return lista;
    }

    public static boolean registrarUsuario(Usuario nuevoUsuario) {
        List<Usuario> usuariosActu = cargarUsuarios();
        for (Usuario u : usuariosActu) {
            if (u.getCorreo().equalsIgnoreCase(nuevoUsuario.getCorreo()) ||
                    u.getUsuario().equalsIgnoreCase(nuevoUsuario.getUsuario())) {
                return false;
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(RUTA_ARCHIVO, true))) { // Abre el archivo en modo agregar al final
            writer.write(nuevoUsuario.toFileString()); // Escribe los datos del nuevo usuario en una línea
            writer.newLine();
            return true;
        } catch (IOException e) {
            System.err.println("Error al guardar usuario en archivo: " + e.getMessage());
            return false;
        }
    }

    public static Usuario validarCredenciales(String identificador, String password) {
        List<Usuario> usuarios = cargarUsuarios();
        for (Usuario u : usuarios) {
            if ((u.getCorreo().equalsIgnoreCase(identificador) || u.getUsuario().equalsIgnoreCase(identificador))
                    && u.getPassword().equals(password)) { //Revisa si coincide el correo o el usuario
                usuarioSesionActual = u;
                registrarLoginSesion(u);
                return u;
            }
        }
        return null;
    }

    //Alias de compatibilidad para que controladores antiguos llamen a iniciarSesion sin error
    public static Usuario iniciarSesion(String identificador, String password) {
        return validarCredenciales(identificador, password);
    }

    public static boolean existeUsuario(String identificador) {
        List<Usuario> usuarios = cargarUsuarios();
        for (Usuario u : usuarios) {
            if (u.getCorreo().equalsIgnoreCase(identificador) || u.getUsuario().equalsIgnoreCase(identificador)) {
                return true;
            }
        }
        return false;
    }

    public static void actualizarPuntuacionUsuario(Usuario usuarioActualizado) {
        List<Usuario> usuarios = cargarUsuarios();
        boolean actualizado = false;

        for (int i = 0; i < usuarios.size(); i++) {
            Usuario u = usuarios.get(i);
            if (u.getUsuario().equalsIgnoreCase(usuarioActualizado.getUsuario())) {
                usuarios.set(i, usuarioActualizado);
                actualizado = true;
                break;
            }
        }

        if (actualizado) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(RUTA_ARCHIVO, false))) {
                for (Usuario u : usuarios) {
                    writer.write(u.toFileString());
                    writer.newLine();
                }
            } catch (IOException e) {
                System.err.println("Error al reescribir puntuaciones en archivo: " + e.getMessage());
            }

            if (usuarioSesionActual != null && usuarioSesionActual.getUsuario().equalsIgnoreCase(usuarioActualizado.getUsuario())) {
                usuarioSesionActual = usuarioActualizado;
            }
        }
    }

    // Sobrecarga de compatibilidad para actualizar por entero directo
    public static void actualizarPuntuacionUsuario(Usuario usuario, int nuevaPuntuacion) {
        if (usuario != null) {
            usuario.agregarPuntos(nuevaPuntuacion);
            actualizarPuntuacionUsuario(usuario);
        }
    }

    // Busca un usuario por su nombre de usuario (sin distinguir mayúsculas). Devuelve null si no existe.
    public static Usuario buscarPorUsuario(String nombreUsuario) {
        for (Usuario u : cargarUsuarios()) {
            if (u.getUsuario().equalsIgnoreCase(nombreUsuario)) {
                return u;
            }
        }
        return null;
    }

    public static Usuario getUsuarioSesionActual() {
        return usuarioSesionActual;
    }

    public static void cerrarSesion() {
        usuarioSesionActual = null;
        usuariosSesion.clear();
    }

    public static void establecerSesion(Usuario usuario) {
        if (usuario == null) {
            usuarioSesionActual = null;
            return;
        }
        for (Usuario u : cargarUsuarios()) {
            if (u.getUsuario().equalsIgnoreCase(usuario.getUsuario())) {
                usuarioSesionActual = u;
                registrarLoginSesion(u);
                return;
            }
        }
        usuarioSesionActual = usuario;
        registrarLoginSesion(usuario);
    }

    public static List<Usuario> getMejoresPuntuaciones() {
        List<Usuario> listaOrdenada = cargarUsuarios();
        listaOrdenada.sort(Comparator.comparingInt(Usuario::getPuntuacionMaxima).reversed());
        return listaOrdenada;
    }
}