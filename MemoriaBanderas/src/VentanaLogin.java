import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VentanaLogin extends JDialog {

    // Panel con imagen de fondo seguro para recursos
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

    private JTextField txtCorreo;
    private JPasswordField txtPassword;
    private JButton btnVerPassword;
    private JButton btnIniciarSesion;
    private JButton btnCancelar;
    private boolean passwordVisible = false;

    public VentanaLogin(Frame parent) {
        super(parent, "Iniciar Sesión", true);
        setSize(430, 310);
        setLocationRelativeTo(parent);
        setResizable(false);

        // Panel principal con imagen de fondo y margen interno
        PanelFondo panel = new PanelFondo("Recursos/Fondo1.jpg");
        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Etiqueta Título
        JLabel lblTitulo = new JLabel("ACCESO AL SISTEMA");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        panel.add(lblTitulo, gbc);

        // Campo de Correo Electrónico
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Correo:"), gbc);

        txtCorreo = new JTextField(18);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        panel.add(txtCorreo, gbc);

        // Campo de Contraseña
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Contraseña:"), gbc);

        txtPassword = new JPasswordField(14);
        gbc.gridx = 1;
        panel.add(txtPassword, gbc);

        // Botón para mostrar / ocultar contraseña
        btnVerPassword = new JButton("👁");
        btnVerPassword.setToolTipText("Mostrar/Ocultar contraseña");
        gbc.gridx = 2;
        panel.add(btnVerPassword, gbc);

        // Panel de botones (Aceptar / Cancelar)
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBotones.setOpaque(false); // Para que se vea el fondo detrás de los botones
        btnIniciarSesion = new JButton("Ingresar");
        btnCancelar = new JButton("Cancelar");
        panelBotones.add(btnIniciarSesion);
        panelBotones.add(btnCancelar);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 3;
        panel.add(panelBotones, gbc);

        add(panel);

        // Lógica para alternar visibilidad de la contraseña
        btnVerPassword.addActionListener(e -> alternarVisibilidadPassword());

        // Evento de inicio de sesión
        btnIniciarSesion.addActionListener(e -> ejecutarLogin());

        // Evento cancelar
        btnCancelar.addActionListener(e -> dispose());
    }

    private void alternarVisibilidadPassword() {
        if (passwordVisible) {
            // Ocultar caracteres
            txtPassword.setEchoChar('•');
            passwordVisible = false;
        } else {
            // Mostrar texto plano
            txtPassword.setEchoChar((char) 0);
            passwordVisible = true;
        }
    }

    private boolean esCorreoValido(String correo) {
        String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[a-zA-Z]{2,}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(correo);
        return matcher.matches();
    }

    private void ejecutarLogin() {
        String correo = txtCorreo.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (correo.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor complete todos los campos.", "Campos vacíos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!esCorreoValido(correo)) {
            JOptionPane.showMessageDialog(this, "El correo electrónico no tiene un formato válido.", "Correo Inválido", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Aquí se agrega la verificación con base de datos o lógica del sistema
        JOptionPane.showMessageDialog(this, "¡Inicio de sesión exitoso!", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }
}