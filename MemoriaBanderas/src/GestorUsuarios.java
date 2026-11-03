import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GestorUsuarios {
    private static final List<Usuario> usuariosRegistrados = new ArrayList<>();
    private static Usuario usuarioSesionActual = null; // Guardar el usuario que tiene la sesión activa

    public static boolean registrarUsuario(Usuario nuevoUsuario) {
        for (Usuario u : usuariosRegistrados) {
            if (u.getCorreo().equalsIgnoreCase(nuevoUsuario.getCorreo()) ||
                    u.getUsuario().equalsIgnoreCase(nuevoUsuario.getUsuario())) {
                return false;
            }
        }
        usuariosRegistrados.add(nuevoUsuario);
        return true;
    }

    public static Usuario validarCredenciales(String identificador, String password) {
        for (Usuario u : usuariosRegistrados) {
            if ((u.getCorreo().equalsIgnoreCase(identificador) || u.getUsuario().equalsIgnoreCase(identificador))
                    && u.getPassword().equals(password)) {
                usuarioSesionActual = u; // Se establece la sesión actual
                return u;
            }
        }
        return null;
    }

    public static boolean existeUsuario(String identificador) {
        for (Usuario u : usuariosRegistrados) {
            if (u.getCorreo().equalsIgnoreCase(identificador) || u.getUsuario().equalsIgnoreCase(identificador)) {
                return true;
            }
        }
        return false;
    }

    public static Usuario getUsuarioSesionActual() {
        return usuarioSesionActual;
    }

    public static void cerrarSesion() {
        usuarioSesionActual = null;
    }

    // Devuelve los usuarios ordenados por mayor puntuación
    public static List<Usuario> getMejoresPuntuaciones() {
        List<Usuario> listaOrdenada = new ArrayList<>(usuariosRegistrados);
        listaOrdenada.sort(Comparator.comparingInt(Usuario::getPuntuacionMaxima).reversed());
        return listaOrdenada;
    }
}