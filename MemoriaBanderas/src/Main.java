import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // Ejecutamos la creación de la interfaz gráfica de forma segura en el hilo EDT de Swing
        SwingUtilities.invokeLater(() -> {
            VentanaMenuPrincipal ventana = new VentanaMenuPrincipal();
            ventana.setVisible(true);
        });
    }
}