package model;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class GestorUsuarios {
    private static final String ARCHIVO_DATOS = "usuarios.dat";
    private static List<Usuario> listaUsuarios = new ArrayList<>();
    private static Usuario usuarioSesionActual = null;

    // Lista de sesión para controlar hasta 2 jugadores activos en minijuegos
    private static final List<Usuario> usuariosSesion = new ArrayList<>();

    static {
        cargarUsuarios();
    }

    public static void registrarLoginSesion(Usuario usuario) {
        if (usuario != null && !usuariosSesion.contains(usuario)) {
            if (usuariosSesion.size() < 2) {
                usuariosSesion.add(usuario);
            }
        }
    }

    public static void establecerSesion(Usuario usuario) {
        usuarioSesionActual = usuario;
        registrarLoginSesion(usuario);
    }

    // Método para guardar cambios de puntaje o perfil
    public static void actualizarPuntuacionUsuario(Usuario usuario) {
        if (usuario != null) {
            guardarUsuarios();
        }
    }

    // Método para actualizar puntuación con valor explícito
    public static void actualizarPuntuacionUsuario(Usuario usuario, int nuevaPuntuacion) {
        if (usuario != null) {
            if (nuevaPuntuacion > usuario.getPuntuacionMaxima()) {
                usuario.setPuntuacionMaxima(nuevaPuntuacion);
            }
            guardarUsuarios();
        }
    }

    public static List<Usuario> getUsuariosSesion() {
        return usuariosSesion;
    }

    public static boolean registrarUsuario(Usuario nuevoUsuario) {
        for (Usuario u : listaUsuarios) {
            if (u.getUsuario().equalsIgnoreCase(nuevoUsuario.getUsuario()) ||
                    u.getCorreo().equalsIgnoreCase(nuevoUsuario.getCorreo())) {
                return false; // Ya existe
            }
        }
        listaUsuarios.add(nuevoUsuario);
        guardarUsuarios(); // Guarda inmediatamente en disco
        System.out.println("Usuario registrado y guardado en disco: " + nuevoUsuario.getUsuario());
        return true;
    }

    public static Usuario iniciarSesion(String identificador, String password) {
        for (Usuario u : listaUsuarios) {
            if ((u.getUsuario().equalsIgnoreCase(identificador) || u.getCorreo().equalsIgnoreCase(identificador)) &&
                    u.getPassword().equals(password)) {
                usuarioSesionActual = u;
                registrarLoginSesion(u);
                return u;
            }
        }
        return null;
    }

    public static void cerrarSesion() {
        usuarioSesionActual = null;
        usuariosSesion.clear();
    }

    public static Usuario getUsuarioSesionActual() {
        return usuarioSesionActual;
    }

    public static List<Usuario> getMejoresPuntuaciones() {
        List<Usuario> copia = new ArrayList<>(listaUsuarios);
        copia.sort((u1, u2) -> Integer.compare(u2.getPuntuacionMaxima(), u1.getPuntuacionMaxima()));
        return copia;
    }

    public static void guardarUsuarios() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ARCHIVO_DATOS))) {
            oos.writeObject(listaUsuarios);
            System.out.println("Archivo usuarios.dat guardado correctamente. Total usuarios: " + listaUsuarios.size());
        } catch (IOException e) {
            System.err.println("Error al guardar usuarios.dat: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public static void cargarUsuarios() {
        File archivo = new File(ARCHIVO_DATOS);
        if (archivo.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(archivo))) {
                listaUsuarios = (List<Usuario>) ois.readObject();
                System.out.println("¡Usuarios cargados con éxito desde usuarios.dat! Total: " + listaUsuarios.size());
            } catch (Exception e) {
                System.err.println("Advertencia al leer usuarios.dat, se inicializa lista vacía: " + e.getMessage());
                listaUsuarios = new ArrayList<>();
            }
        } else {
            System.out.println("El archivo usuarios.dat no existe todavía. Se creará al registrar el primer usuario.");
            listaUsuarios = new ArrayList<>();
        }
    }
}