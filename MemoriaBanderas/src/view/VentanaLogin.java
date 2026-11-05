package view;
import model.Usuario;
import model.GestorUsuarios;
import controller.ControladorLogin; // Si la vista abre el controlador
import javax.swing.*;
import java.awt.*;
import java.net.URL;
import model.Usuario;
import model.GestorUsuarios;
import controller.ControladorLogin; // Si la vista abre el controlador
import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class VentanaLogin extends JDialog {

    static class PanelFondo extends JPanel {
        private Image imagenFondo;

        public PanelFondo(String ruta) {
            URL urlImagen = getClass().getClassLoader().getResource(ruta);
            if (urlImagen != null) {
                imagenFondo = new ImageIcon(urlImagen).getImage();
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

    private JTextField txtIdentificador;
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
        gbc.gridx = 2;
        panel.add(btnVerPassword, gbc);

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
    }

    public void alternarVisibilidadPassword() {
        if (passwordVisible) {
            txtPassword.setEchoChar('•');
            passwordVisible = false;
        } else {
            txtPassword.setEchoChar((char) 0);
            passwordVisible = true;
        }
    }

    // Getters para el Controlador
    public JTextField getTxtIdentificador() { return txtIdentificador; }
    public JPasswordField getTxtPassword() { return txtPassword; }
    public JButton getBtnVerPassword() { return btnVerPassword; }
    public JButton getBtnIniciarSesion() { return btnIniciarSesion; }
    public JButton getBtnRegistrarse() { return btnRegistrarse; }
    public JButton getBtnCancelar() { return btnCancelar; }
}