package view;

import model.Usuario;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class DialogoSeleccionJugador extends JDialog {

    private Usuario jugadorSeleccionado = null;

    public DialogoSeleccionJugador(Window parent, List<Usuario> jugadores) {
        super(parent, "Selección de Jugador", ModalityType.APPLICATION_MODAL);
        setSize(850, 520);
        setLocationRelativeTo(parent);
        setResizable(false);

        // Imagen de fondo corregida con la ruta exacta que indicaste
        PanelFondo panelFondo = new PanelFondo("Recursos/PantallaEleccion.png");
        panelFondo.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;

        JPanel panelBotones = new JPanel(new GridLayout(2, 1, 15, 20));
        panelBotones.setOpaque(false);

        String nombre1 = (jugadores.size() > 0) ? jugadores.get(0).getUsuario() : "Jugador 1";
        JButton btnJugador1 = new JButton("<html><center><b>JUGADOR 1</b><br><span style='font-size:11px;'>" + nombre1 + "</span></center></html>");
        estilizarBotonJugador(btnJugador1);
        btnJugador1.addActionListener(e -> {
            jugadorSeleccionado = jugadores.get(0);
            dispose();
        });

        String nombre2 = (jugadores.size() > 1) ? jugadores.get(1).getUsuario() : "Jugador 2";
        JButton btnJugador2 = new JButton("<html><center><b>JUGADOR 2</b><br><span style='font-size:11px;'>" + nombre2 + "</span></center></html>");
        estilizarBotonJugador(btnJugador2);
        btnJugador2.addActionListener(e -> {
            jugadorSeleccionado = jugadores.get(1);
            dispose();
        });

        panelBotones.add(btnJugador1);
        panelBotones.add(btnJugador2);

        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        // Margen ajustado para ubicar los botones en la zona despejada de la pantalla de elección
        panelFondo.setBorder(BorderFactory.createEmptyBorder(80, 45, 40, 420));
        panelFondo.add(panelBotones, gbc);

        setContentPane(panelFondo);
    }

    private void estilizarBotonJugador(JButton boton) {
        boton.setFont(new Font("Segoe UI", Font.BOLD, 15));
        boton.setForeground(Color.WHITE);
        boton.setBackground(new Color(20, 35, 60, 220));
        boton.setFocusPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.setPreferredSize(new Dimension(240, 65));
    }

    public Usuario getJugadorSeleccionado() {
        return jugadorSeleccionado;
    }
}