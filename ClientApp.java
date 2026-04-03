import javax.swing.SwingUtilities;

/**
 * Point d'entrée du client — lance la GUI.
 */
public class ClientApp {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            new ClientGUI().setVisible(true);
        });
    }
}
