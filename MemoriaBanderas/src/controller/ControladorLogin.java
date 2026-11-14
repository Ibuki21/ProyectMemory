package controller;

import model.GestorUsuarios;
import model.Usuario;
import view.VentanaLogin;
import view.VentanaRegistro;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class ControladorLogin {

    private VentanaLogin vista;

    // Contador estático para alternar entre Jugador 1 y Jugador 2
    private static int contadorLoginsExitosos = 0;

    public ControladorLogin(VentanaLogin vista) {
        this.vista = vista;
        initListeners();
    }

    private void initListeners() {
        // Botón Ver / Ocultar contraseña
        vista.getBtnVerPassword().addActionListener(e -> vista.alternarVisibilidadPassword());

        // Botón Cancelar
        vista.getBtnCancelar().addActionListener(e -> vista.dispose());

        // Botón Abrir Ventana de Registro
        vista.getBtnRegistrarse().addActionListener(e -> {
            ReproductorSonido.reproducirEfecto("Recursos/Cancion/Botones/Presiona1.wav");
            VentanaRegistro registro = new VentanaRegistro(vista);
            new ControladorRegistro(registro);
            registro.setVisible(true);
        });

        // Botón Iniciar Sesión
        vista.getBtnIniciarSesion().addActionListener(e -> {
            ReproductorSonido.reproducirEfecto("Recursos/Cancion/Botones/Presiona1.wav");
            procesarLogin();
        });
    }

    private void procesarLogin() {
        String identificador = vista.getTxtIdentificador().getText().trim();
        String password = new String(vista.getTxtPassword().getPassword());

        if (identificador.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(vista,
                    "Por favor, complete todos los campos.",
                    "Campos Vacíos",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validar credenciales mediante GestorUsuarios
        Usuario usuarioAutenticado = GestorUsuarios.validarCredenciales(identificador, password);

        if (usuarioAutenticado != null) {
            // Incrementar contador de inicios de sesión exitosos
            contadorLoginsExitosos++;

            // Seleccionar la imagen correspondiente (Jugador1.png o Jugador2.png)
            String rutaImagen = (contadorLoginsExitosos == 1) ? "Recursos/Jugador1.png" : "Recursos/Jugador2.png";

            // Mostrar la imagen en tamaño grande y con temporizador de 5 segundos
            mostrarImagenBienvenida(rutaImagen);

            // Cerrar la ventana de login al ingresar exitosamente
            vista.dispose();

        } else {
            JOptionPane.showMessageDialog(vista,
                    "Credenciales incorrectas o usuario no encontrado.",
                    "Error de Autenticación",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void mostrarImagenBienvenida(String rutaImagen) {
        JDialog dialogo = new JDialog(vista, "", true);
        dialogo.setUndecorated(true); // Sin barra de título ni bordes
        dialogo.setBackground(new Color(0, 0, 0, 0)); // Fondo de ventana transparente

        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false); // Panel transparente

        JLabel lblImagen = new JLabel();
        lblImagen.setHorizontalAlignment(SwingConstants.CENTER);

        // Cargar y escalar la imagen a un tamaño más grande manteniendo su proporción
        URL urlImg = getClass().getClassLoader().getResource(rutaImagen);
        if (urlImg != null) {
            ImageIcon iconoOriginal = new ImageIcon(urlImg);
            int origW = iconoOriginal.getIconWidth();
            int origH = iconoOriginal.getIconHeight();

            if (origW > 0 && origH > 0) {
                // Dimensiones más grandes para la imagen de bienvenida
                int maxW = 960;
                int maxH = 640;
                double escala = Math.min((double) maxW / origW, (double) maxH / origH);
                int nuevoAncho = (int) (origW * escala);
                int nuevoAlto = (int) (origH * escala);

                Image imgEscalada = iconoOriginal.getImage().getScaledInstance(nuevoAncho, nuevoAlto, Image.SCALE_SMOOTH);
                lblImagen.setIcon(new ImageIcon(imgEscalada));
            } else {
                lblImagen.setIcon(iconoOriginal);
            }
        } else {
            lblImagen.setText("No se encontró la imagen: " + rutaImagen);
            lblImagen.setForeground(Color.WHITE);
        }

        // Timer para que la imagen desaparezca automáticamente después de 5 segundos (5000 ms)
        Timer timerCierre = new Timer(5000, e -> {
            if (dialogo.isVisible()) {
                dialogo.dispose();
            }
        });
        timerCierre.setRepeats(false);
        timerCierre.start();

        // Opcional: También permite cerrarla haciendo clic si el usuario desea omitir los 5 segundos
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