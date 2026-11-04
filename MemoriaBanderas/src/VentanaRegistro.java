import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.regex.Pattern;

public class VentanaRegistro extends JDialog {

    private JTextField txtNombre;
    private JTextField txtApellidos;
    private JTextField txtCorreo;
    private JTextField txtUsuario;
    private JPasswordField txtPassword;
    private JPasswordField txtConfirmarPassword;
    private JTextField txtFechaNacimiento;
    private JLabel lblVistaPreviaFoto;
    private File archivoFotoSeleccionado;

    public VentanaRegistro(Window parent) {
        super(parent, "Registro de Usuario", ModalityType.APPLICATION_MODAL);
        setSize(480, 560);
        setLocationRelativeTo(parent);
        setResizable(false);

        // Panel con fondo de imagen (heredado de VentanaLogin)
        VentanaLogin.PanelFondo panel = new VentanaLogin.PanelFondo("Recursos/Fondo1.jpg");
        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 🏷️ Título estilizado en blanco
        JLabel lblTitulo = new JLabel("REGISTRO DE NUEVO USUARIO");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(lblTitulo, gbc);

        gbc.gridwidth = 1;

        // Método auxiliar para crear etiquetas con texto blanco legible
        // (Reemplaza la creación repetitiva y asegura que no haya fondos blancos)
        gbc.gridx = 0; gbc.gridy = 1; panel.add(crearLabel("Nombre:"), gbc);
        txtNombre = new JTextField(16);
        gbc.gridx = 1; panel.add(txtNombre, gbc);

        gbc.gridx = 0; gbc.gridy = 2; panel.add(crearLabel("Apellidos:"), gbc);
        txtApellidos = new JTextField(16);
        gbc.gridx = 1; panel.add(txtApellidos, gbc);

        gbc.gridx = 0; gbc.gridy = 3; panel.add(crearLabel("Correo:"), gbc);
        txtCorreo = new JTextField(16);
        gbc.gridx = 1; panel.add(txtCorreo, gbc);

        gbc.gridx = 0; gbc.gridy = 4; panel.add(crearLabel("Usuario:"), gbc);
        txtUsuario = new JTextField(16);
        gbc.gridx = 1; panel.add(txtUsuario, gbc);

        gbc.gridx = 0; gbc.gridy = 5; panel.add(crearLabel("Contraseña:"), gbc);
        txtPassword = new JPasswordField(16);
        gbc.gridx = 1; panel.add(txtPassword, gbc);

        gbc.gridx = 0; gbc.gridy = 6; panel.add(crearLabel("Confirmar Contraseña:"), gbc);
        txtConfirmarPassword = new JPasswordField(16);
        gbc.gridx = 1; panel.add(txtConfirmarPassword, gbc);

        gbc.gridx = 0; gbc.gridy = 7; panel.add(crearLabel("Fecha Nac. (DD/MM/AAAA):"), gbc);
        txtFechaNacimiento = new JTextField(16);
        gbc.gridx = 1; panel.add(txtFechaNacimiento, gbc);

        // Subida de Foto de Perfil
        gbc.gridx = 0; gbc.gridy = 8; panel.add(crearLabel("Foto de Perfil:"), gbc);

        // 🛡️ Panel transparente para la foto y botón (Evita el recuadro blanco)
        JPanel panelFoto = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        panelFoto.setOpaque(false);

        JButton btnCargarFoto = new JButton("Seleccionar...");
        lblVistaPreviaFoto = new JLabel("Sin foto");
        lblVistaPreviaFoto.setForeground(Color.WHITE);
        lblVistaPreviaFoto.setPreferredSize(new Dimension(50, 50));
        lblVistaPreviaFoto.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        lblVistaPreviaFoto.setHorizontalAlignment(SwingConstants.CENTER);

        panelFoto.add(btnCargarFoto);
        panelFoto.add(lblVistaPreviaFoto);
        gbc.gridx = 1; panel.add(panelFoto, gbc);

        // 🛡️ Panel de botones transparente (Evita el recuadro blanco inferior)
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelBotones.setOpaque(false);

        JButton btnRegistrar = new JButton("Registrar");
        JButton btnCancelar = new JButton("Cancelar");
        panelBotones.add(btnRegistrar);
        panelBotones.add(btnCancelar);

        gbc.gridx = 0; gbc.gridy = 9; gbc.gridwidth = 2;
        panel.add(panelBotones, gbc);

        add(panel);

        // Eventos
        btnCargarFoto.addActionListener(e -> seleccionarFoto());
        btnRegistrar.addActionListener(e -> registrar());
        btnCancelar.addActionListener(e -> dispose());
    }

    /**
     * Método auxiliar para crear etiquetas con fuente clara y legible sobre el fondo.
     */
    private JLabel crearLabel(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return lbl;
    }

    private void seleccionarFoto() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Seleccionar Foto de Perfil");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Imágenes (JPG, PNG)", "jpg", "jpeg", "png"));
        int seleccion = fileChooser.showOpenDialog(this);
        if (seleccion == JFileChooser.APPROVE_OPTION) {
            archivoFotoSeleccionado = fileChooser.getSelectedFile();
            ImageIcon iconoOriginal = new ImageIcon(archivoFotoSeleccionado.getAbsolutePath());
            Image imgEscalada = iconoOriginal.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
            lblVistaPreviaFoto.setIcon(new ImageIcon(imgEscalada));
            lblVistaPreviaFoto.setText("");
        }
    }

    private void registrar() {
        String nombre = txtNombre.getText().trim();
        String apellidos = txtApellidos.getText().trim();
        String correo = txtCorreo.getText().trim();
        String usuario = txtUsuario.getText().trim();
        String password = new String(txtPassword.getPassword());
        String confirmarPass = new String(txtConfirmarPassword.getPassword());
        String fechaNac = txtFechaNacimiento.getText().trim();

        if (nombre.isEmpty() || apellidos.isEmpty() || correo.isEmpty() || usuario.isEmpty() || password.isEmpty() || fechaNac.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor complete todos los campos obligatorios.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!Pattern.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[a-zA-Z]{2,}$", correo)) {
            JOptionPane.showMessageDialog(this, "El correo electrónico no tiene un formato válido.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!password.equals(confirmarPass)) {
            JOptionPane.showMessageDialog(this, "Las contraseñas no coinciden.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Usuario nuevoUsuario = new Usuario(nombre, apellidos, correo, usuario, password, fechaNac, archivoFotoSeleccionado);
        if (GestorUsuarios.registrarUsuario(nuevoUsuario)) {
            JOptionPane.showMessageDialog(this, "¡Registro realizado con éxito! Ahora puede iniciar sesión.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "El correo o nombre de usuario ya se encuentra registrado.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}