package model;
import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import model.Usuario;

public class GestorUsuarios {
    private static final String RUTA_ARCHIVO = "usuarios.txt";
    private static Usuario usuarioSesionActual = null;

    static {
        crearArchivoSiNoExiste();
    }

    private static void crearArchivoSiNoExiste() {
        File file = new File(RUTA_ARCHIVO);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
            System.err.println("Error al inicializar el archivo de usuarios: " + e.getMessage());
        }
    }

    public static List<Usuario> cargarUsuarios() {
        List<Usuario> lista = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(RUTA_ARCHIVO))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                if (!linea.trim().isEmpty()) {
                    Usuario u = Usuario.fromFileString(linea);
                    if (u != null) {
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
                return false; // Usuario o correo en uso
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(RUTA_ARCHIVO, true))) {
            writer.write(nuevoUsuario.toFileString());
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
                    && u.getPassword().equals(password)) {
                usuarioSesionActual = u;
                return u;
            }
        }
        return null;
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

            // Sincroniza la sesión activa en memoria para reflejar los puntos acumulados al instante
            if (usuarioSesionActual != null && usuarioSesionActual.getUsuario().equalsIgnoreCase(usuarioActualizado.getUsuario())) {
                usuarioSesionActual = usuarioActualizado;
            }
        }
    }

    public static Usuario getUsuarioSesionActual() {
        return usuarioSesionActual;
    }

    public static void cerrarSesion() {
        usuarioSesionActual = null;
    }

    public static void establecerSesion(Usuario usuario) {
        if (usuario == null) {
            usuarioSesionActual = null;
            return;
        }
        for (Usuario u : cargarUsuarios()) {
            if (u.getUsuario().equalsIgnoreCase(usuario.getUsuario())) {
                usuarioSesionActual = u;
                return;
            }
        }
        usuarioSesionActual = usuario;
    }

    public static List<Usuario> getMejoresPuntuaciones() {
        List<Usuario> listaOrdenada = cargarUsuarios();
        listaOrdenada.sort(Comparator.comparingInt(Usuario::getPuntuacionMaxima).reversed());
        return listaOrdenada;
    }
}