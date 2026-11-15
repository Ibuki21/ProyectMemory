package view;
import model.Usuario;
import model.GestorUsuarios;
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
    private JButton btnCargarFoto;
    private JButton btnRegistrar;
    private JButton btnCancelar;
    private File archivoFotoSeleccionado;
    private JButton btnRegistrarRostro;
    private JLabel lblEstadoRostro;
    private float[] datosFaciales;   // Rostro capturado (null si el usuario no registró datos faciales)

    public VentanaRegistro(Window parent) {
        super(parent, "Registro de Usuario", ModalityType.APPLICATION_MODAL);
        setSize(480, 620);
        setLocationRelativeTo(parent);
        setResizable(false);

        PanelFondo panel = new PanelFondo("Recursos/Fondo1.jpg");
        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblTitulo = new JLabel("REGISTRO DE NUEVO USUARIO");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(lblTitulo, gbc);

        gbc.gridwidth = 1;

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

        gbc.gridx = 0; gbc.gridy = 8; panel.add(crearLabel("Foto de Perfil:"), gbc);

        JPanel panelFoto = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        panelFoto.setOpaque(false);

        btnCargarFoto = new JButton("Seleccionar...");
        lblVistaPreviaFoto = new JLabel("Sin foto");
        lblVistaPreviaFoto.setForeground(Color.WHITE);
        lblVistaPreviaFoto.setPreferredSize(new Dimension(50, 50));
        lblVistaPreviaFoto.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        lblVistaPreviaFoto.setHorizontalAlignment(SwingConstants.CENTER);

        panelFoto.add(btnCargarFoto);
        panelFoto.add(lblVistaPreviaFoto);
        gbc.gridx = 1; panel.add(panelFoto, gbc);

        // Datos faciales: opcionales, el usuario puede registrarse sin ellos
        gbc.gridx = 0; gbc.gridy = 9; panel.add(crearLabel("Inicio con Datos Faciales:"), gbc);

        JPanel panelRostro = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        panelRostro.setOpaque(false);
        btnRegistrarRostro = new JButton("Registrar Datos Faciales");
        lblEstadoRostro = new JLabel("Opcional");
        lblEstadoRostro.setForeground(Color.WHITE);
        panelRostro.add(btnRegistrarRostro);
        panelRostro.add(lblEstadoRostro);
        gbc.gridx = 1; panel.add(panelRostro, gbc);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelBotones.setOpaque(false);

        btnRegistrar = new JButton("Registrar");
        btnCancelar = new JButton("Cancelar");
        panelBotones.add(btnRegistrar);
        panelBotones.add(btnCancelar);

        gbc.gridx = 0; gbc.gridy = 10; gbc.gridwidth = 2;
        panel.add(panelBotones, gbc);

        add(panel);
    }

    private JLabel crearLabel(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return lbl;
    }

    // Getters y Setters para el Controlador
    public JTextField getTxtNombre() { return txtNombre; }
    public JTextField getTxtApellidos() { return txtApellidos; }
    public JTextField getTxtCorreo() { return txtCorreo; }
    public JTextField getTxtUsuario() { return txtUsuario; }
    public JPasswordField getTxtPassword() { return txtPassword; }
    public JPasswordField getTxtConfirmarPassword() { return txtConfirmarPassword; }
    public JTextField getTxtFechaNacimiento() { return txtFechaNacimiento; }
    public JLabel getLblVistaPreviaFoto() { return lblVistaPreviaFoto; }
    public JButton getBtnCargarFoto() { return btnCargarFoto; }
    public JButton getBtnRegistrar() { return btnRegistrar; }
    public JButton getBtnCancelar() { return btnCancelar; }
    public JButton getBtnRegistrarRostro() { return btnRegistrarRostro; }
    public float[] getDatosFaciales() { return datosFaciales; }

    // Guarda el rostro capturado y avisa en pantalla que ya está listo
    public void setDatosFaciales(float[] datosFaciales) {
        this.datosFaciales = datosFaciales;
        if (datosFaciales != null) {
            lblEstadoRostro.setText("\u2714 Capturados");
            lblEstadoRostro.setForeground(new Color(120, 230, 120));
            btnRegistrarRostro.setText("Volver a capturar");
        }
    }

    public File getArchivoFotoSeleccionado() { return archivoFotoSeleccionado; }
    public void setArchivoFotoSeleccionado(File archivoFotoSeleccionado) { this.archivoFotoSeleccionado = archivoFotoSeleccionado; }
}