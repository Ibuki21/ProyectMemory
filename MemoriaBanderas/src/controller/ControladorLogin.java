package controller;

import model.GestorUsuarios;
import model.Usuario;
import view.VentanaLogin;
import view.VentanaRegistro;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class ControladorLogin {

    private VentanaLogin vista; // Guarda la ventana de inicio de sesión asociada
    private static int contadorLoginsExitosos = 0;

    public ControladorLogin(VentanaLogin vista) {
        this.vista = vista;
        initListeners(); // Conecta las acciones de los botones de la ventana
    }

    private void initListeners() { // Configura qué pasa cuando se presiona cada botón
        vista.getBtnVerPassword().addActionListener(e -> vista.alternarVisibilidadPassword());
        vista.getBtnCancelar().addActionListener(e -> vista.dispose()); // Cierra la ventana de login al cancelar

        vista.getBtnRegistrarse().addActionListener(e -> {
            GestorMusica.reproducirEfecto("Recursos/Cancion/Botones/Presiona1.wav");
            VentanaRegistro registro = new VentanaRegistro(vista); // Crea la ventana de registro
            new ControladorRegistro(registro); // Conecta la ventana de registro con su controlador
            registro.setVisible(true);
        });

        vista.getBtnIniciarSesion().addActionListener(e -> {
            GestorMusica.reproducirEfecto("Recursos/Cancion/Botones/Presiona1.wav");
            procesarLogin(); //Se llama al metodo procesar login
        });

        vista.getBtnRostro().addActionListener(e -> {
            GestorMusica.reproducirEfecto("Recursos/Cancion/Botones/Presiona1.wav");
            procesarLoginFacial(); //Se llama el metodo pra procesar el login facial
        });
    }

    // Inicio de sesión por reconocimiento facial: no hace falta escribir usuario ni contraseña
    private void procesarLoginFacial() {
        Usuario usuario = new ControladorFacial().iniciarSesionConRostro(vista);
        if (usuario != null) { //Revisa si se encontró un usuario válido
            GestorUsuarios.establecerSesion(usuario);
            completarLogin(usuario);
        }
    }

    // Lo que sigue a un inicio de sesión exitoso (con contraseña o con rostro)
    private void completarLogin(Usuario usuario) {
        contadorLoginsExitosos++;

        // Registrar usuario en la sesión para el Modo Patrones
        GestorUsuarios.registrarLoginSesion(usuario);

        String rutaImagen = (contadorLoginsExitosos == 1) ? "Recursos/Jugador1.png" : "Recursos/Jugador2.png"; // Revisa si se encontró un usuario válido

        mostrarImagenBienvenida(rutaImagen);
        vista.dispose(); // Cierra la ventana de inicio de sesión
    }
    // Valida usuario/correo y contraseña escritos por el usuario
    private void procesarLogin() {
        String identificador = vista.getTxtIdentificador().getText().trim(); // Lee el usuario o correo sin espacios extra
        String password = new String(vista.getTxtPassword().getPassword()); // Lee la contraseña escrita en el campo

        if (identificador.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(vista,
                    "Por favor, complete todos los campos.",
                    "Campos Vacíos",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Usuario usuarioAutenticado = GestorUsuarios.iniciarSesion(identificador, password); // Comprueba si los datos son correctos

        if (usuarioAutenticado != null) { // Revisa si las credenciales coincidieron con un usuario
            completarLogin(usuarioAutenticado); // Completa el inicio de sesión
        } else {
            JOptionPane.showMessageDialog(vista,
                    "Credenciales incorrectas o usuario no encontrado.",
                    "Error de Autenticación",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void mostrarImagenBienvenida(String rutaImagen) { //Muestra una ventana con la imagen del jugador que entró
        JDialog dialogo = new JDialog(vista, "", true);//Crea un cuadro modal bloqueando atrás mientras se muestra
        dialogo.setUndecorated(true);//Quita la barra superior y bordes de ventana normal
        dialogo.setBackground(new Color(0, 0, 0, 0)); //Hace transparente el fondo del diálogo

        JPanel panel = new JPanel(new BorderLayout()); //Crea un panel para acomodar la imagen
        panel.setOpaque(false); //Deja el panel transparente para ver solo la imagen

        JLabel lblImagen = new JLabel();
        lblImagen.setHorizontalAlignment(SwingConstants.CENTER); //Centra la imagen dentro de la etiqueta

        URL urlImg = getClass().getClassLoader().getResource(rutaImagen); //Busca la imagen dentro de los recursos del proyecto
        if (urlImg != null) {
            ImageIcon iconoOriginal = new ImageIcon(urlImg);
            int origW = iconoOriginal.getIconWidth();
            int origH = iconoOriginal.getIconHeight();

            if (origW > 0 && origH > 0) { //Revisa que la imagen tenga dimensiones válidas
                int maxW = 960;
                int maxH = 640;
                double escala = Math.min((double) maxW / origW, (double) maxH / origH); // Calcula la escala para no deformar la imagen
                int nuevoAncho = (int) (origW * escala);
                int nuevoAlto = (int) (origH * escala);

                Image imgEscalada = iconoOriginal.getImage().getScaledInstance(nuevoAncho, nuevoAlto, Image.SCALE_SMOOTH); //Cambia el tamaño de la imagen suavemente
                lblImagen.setIcon(new ImageIcon(imgEscalada));
            } else {
                lblImagen.setIcon(iconoOriginal);
            }
        } else {
            lblImagen.setText("No se encontró la imagen: " + rutaImagen);
            lblImagen.setForeground(Color.WHITE);
        }

        Timer timerCierre = new Timer(5000, e -> { // Crea un temporizador de 5 segundos para cerrar la imagen
            if (dialogo.isVisible()) {
                dialogo.dispose();
            }
        });
        timerCierre.setRepeats(false);
        timerCierre.start();

        lblImagen.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblImagen.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                timerCierre.stop();
                dialogo.dispose();
            }
        });

        panel.add(lblImagen, BorderLayout.CENTER);
        dialogo.setContentPane(panel);
        dialogo.pack();
        dialogo.setLocationRelativeTo(vista);
        dialogo.setVisible(true);
    }
}