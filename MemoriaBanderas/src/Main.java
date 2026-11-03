import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            VentanaMenuPrincipal menu = new VentanaMenuPrincipal();
            menu.setVisible(true);
        });
    }
}