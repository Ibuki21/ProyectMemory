package controller;
import model.Usuario;
import model.GestorUsuarios;
import view.VentanaLogin;
import view.VentanaRegistro;
import javax.swing.JOptionPane;

public class ControladorLogin {
    private VentanaLogin vista;

    public ControladorLogin(VentanaLogin vista) {
        this.vista = vista;

        // Eventos delegados desde la Vista
        this.vista.getBtnVerPassword().addActionListener(e -> vista.alternarVisibilidadPassword());
        this.vista.getBtnIniciarSesion().addActionListener(e -> ejecutarLogin());
        this.vista.getBtnRegistrarse().addActionListener(e -> abrirRegistro());
        this.vista.getBtnCancelar().addActionListener(e -> vista.dispose());
    }

    private void ejecutarLogin() {
        String id = vista.getTxtIdentificador().getText().trim();
        String password = new String(vista.getTxtPassword().getPassword());

        if (id.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(vista, "Por favor complete todos los campos.", "Campos vacíos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Usuario usuarioLogueado = GestorUsuarios.validarCredenciales(id, password);

        if (usuarioLogueado != null) {
            JOptionPane.showMessageDialog(vista, "¡Bienvenido/a, " + usuarioLogueado.getNombre() + "!", "Inicio de Sesión Exitoso", JOptionPane.INFORMATION_MESSAGE);
            vista.dispose();
        } else {
            if (GestorUsuarios.existeUsuario(id)) {
                JOptionPane.showMessageDialog(vista, "Contraseña incorrecta.", "Error de Autenticación", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(vista, "El usuario o correo no está registrado. Por favor regístrese primero.", "Usuario No Encontrado", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void abrirRegistro() {
        Usuario sesionAntes = GestorUsuarios.getUsuarioSesionActual();
        VentanaRegistro registroView = new VentanaRegistro(vista);
        new ControladorRegistro(registroView);
        registroView.setVisible(true);

        // Si el registro inició sesión automáticamente, ya no hace falta el login
        Usuario sesionDespues = GestorUsuarios.getUsuarioSesionActual();
        if (sesionDespues != null && sesionDespues != sesionAntes) {
            vista.dispose();
        }
    }
}
