import java.util.ArrayList;
import java.util.List;

public class GestorUsuarios {
    private static final List<Usuario> usuariosRegistrados = new ArrayList<>();

    public static boolean registrarUsuario(Usuario nuevoUsuario) {
        // Verificar si el correo o usuario ya existe
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
        // Permite iniciar sesión usando correo o nombre de usuario
        for (Usuario u : usuariosRegistrados) {
            if ((u.getCorreo().equalsIgnoreCase(identificador) || u.getUsuario().equalsIgnoreCase(identificador))
                    && u.getPassword().equals(password)) {
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
}