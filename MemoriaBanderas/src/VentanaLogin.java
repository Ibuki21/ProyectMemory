import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VentanaLogin extends JDialog {

    static class PanelFondo extends JPanel {
        private Image imagenFondo;

        public PanelFondo(String ruta) {
            URL urlImagen = getClass().getClassLoader().getResource(ruta);
            if (urlImagen != null) {
                imagenFondo = new ImageIcon(urlImagen).getImage();
            } else {
                URL urlAlternativa = getClass().getResource("/" + ruta);
                if (urlAlternativa != null) {
                    imagenFondo = new ImageIcon(urlAlternativa).getImage();
                } else {
                    System.err.println("No se pudo cargar la imagen de fondo: " + ruta);
                }
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (imagenFondo != null) {
                g.drawImage(imagenFondo, 0, 0, getWidth(), getHeight(), this);
            }
        }
    }

    private JTextField txtIdentificador; // Correo o Usuario
    private JPasswordField txtPassword;
    private JButton btnVerPassword;
    private JButton btnIniciarSesion;
    private JButton btnRegistrarse;
    private JButton btnCancelar;
    private boolean passwordVisible = false;

    public VentanaLogin(Frame parent) {
        super(parent, "Iniciar Sesión", true);
        setSize(430, 330);
        setLocationRelativeTo(parent);
        setResizable(false);

        PanelFondo panel = new PanelFondo("Recursos/Fondo1.jpg");
        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblTitulo = new JLabel("ACCESO AL SISTEMA");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 3;
        panel.add(lblTitulo, gbc);

        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Usuario / Correo:"), gbc);

        txtIdentificador = new JTextField(18);
        gbc.gridx = 1; gbc.gridwidth = 2;
        panel.add(txtIdentificador, gbc);

        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Contraseña:"), gbc);

        txtPassword = new JPasswordField(14);
        gbc.gridx = 1;
        panel.add(txtPassword, gbc);

        btnVerPassword = new JButton("👁");
        btnVerPassword.setToolTipText("Mostrar/Ocultar contraseña");
        gbc.gridx = 2;
        panel.add(btnVerPassword, gbc);

        // Botones de acción
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        panelBotones.setOpaque(false);
        btnIniciarSesion = new JButton("Ingresar");
        btnRegistrarse = new JButton("Registrarse");
        btnCancelar = new JButton("Cancelar");

        panelBotones.add(btnIniciarSesion);
        panelBotones.add(btnRegistrarse);
        panelBotones.add(btnCancelar);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 3;
        panel.add(panelBotones, gbc);

        add(panel);

        btnVerPassword.addActionListener(e -> alternarVisibilidadPassword());
        btnIniciarSesion.addActionListener(e -> ejecutarLogin());
        btnRegistrarse.addActionListener(e -> abrirRegistro());
        btnCancelar.addActionListener(e -> dispose());
    }

    private void alternarVisibilidadPassword() {
        if (passwordVisible) {
            txtPassword.setEchoChar('•');
            passwordVisible = false;
        } else {
            txtPassword.setEchoChar((char) 0);
            passwordVisible = true;
        }
    }

    private void abrirRegistro() {
        VentanaRegistro registro = new VentanaRegistro(this);
        registro.setVisible(true);
    }

    private void ejecutarLogin() {
        String id = txtIdentificador.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (id.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor complete todos los campos.", "Campos vacíos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validación de existencia y credenciales
        Usuario usuarioLogueado = GestorUsuarios.validarCredenciales(id, password);

        if (usuarioLogueado != null) {
            JOptionPane.showMessageDialog(this, "¡Bienvenido/a, " + usuarioLogueado.getNombre() + "!", "Inicio de Sesión Exitoso", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            if (GestorUsuarios.existeUsuario(id)) {
                JOptionPane.showMessageDialog(this, "Contraseña incorrecta.", "Error de Autenticación", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "El usuario o correo no está registrado. Por favor regístrese primero.", "Usuario No Encontrado", JOptionPane.WARNING_MESSAGE);
            }
        }
    }
}