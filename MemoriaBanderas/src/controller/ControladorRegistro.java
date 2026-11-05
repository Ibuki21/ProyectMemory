package controller;
import model.Usuario;
import model.GestorUsuarios;
import view.VentanaRegistro;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Image;
import java.io.File;
import java.util.regex.Pattern;

public class ControladorRegistro {
    private VentanaRegistro vista;

    public ControladorRegistro(VentanaRegistro vista) {
        this.vista = vista;

        this.vista.getBtnCargarFoto().addActionListener(e -> seleccionarFoto());
        this.vista.getBtnRegistrar().addActionListener(e -> registrar());
        this.vista.getBtnCancelar().addActionListener(e -> vista.dispose());
    }

    private void seleccionarFoto() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Seleccionar Foto de Perfil");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Imágenes (JPG, PNG)", "jpg", "jpeg", "png"));
        int seleccion = fileChooser.showOpenDialog(vista);
        if (seleccion == JFileChooser.APPROVE_OPTION) {
            File fotoSeleccionada = fileChooser.getSelectedFile();
            vista.setArchivoFotoSeleccionado(fotoSeleccionada);

            ImageIcon iconoOriginal = new ImageIcon(fotoSeleccionada.getAbsolutePath());
            Image imgEscalada = iconoOriginal.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
            vista.getLblVistaPreviaFoto().setIcon(new ImageIcon(imgEscalada));
            vista.getLblVistaPreviaFoto().setText("");
        }
    }

    private void registrar() {
        String nombre = vista.getTxtNombre().getText().trim();
        String apellidos = vista.getTxtApellidos().getText().trim();
        String correo = vista.getTxtCorreo().getText().trim();
        String usuario = vista.getTxtUsuario().getText().trim();
        String password = new String(vista.getTxtPassword().getPassword());
        String confirmarPass = new String(vista.getTxtConfirmarPassword().getPassword());
        String fechaNac = vista.getTxtFechaNacimiento().getText().trim();

        if (nombre.isEmpty() || apellidos.isEmpty() || correo.isEmpty() || usuario.isEmpty() || password.isEmpty() || fechaNac.isEmpty()) {
            JOptionPane.showMessageDialog(vista, "Por favor complete todos los campos obligatorios.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!Pattern.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[a-zA-Z]{2,}$", correo)) {
            JOptionPane.showMessageDialog(vista, "El correo electrónico no tiene un formato válido.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!password.equals(confirmarPass)) {
            JOptionPane.showMessageDialog(vista, "Las contraseñas no coinciden.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Usuario nuevoUsuario = new Usuario(nombre, apellidos, correo, usuario, password, fechaNac, vista.getArchivoFotoSeleccionado());
        if (GestorUsuarios.registrarUsuario(nuevoUsuario)) {
            JOptionPane.showMessageDialog(vista, "¡Registro realizado con éxito! Se guardó en el archivo usuarios.txt.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            vista.dispose();
        } else {
            JOptionPane.showMessageDialog(vista, "El correo o nombre de usuario ya se encuentra registrado.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
