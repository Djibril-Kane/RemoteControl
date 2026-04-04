import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Point d'entrée du SERVEUR.
 * Lance l'interface graphique ServerGUI.
 * Usage : java ServerApp [port]
 */
public class ServerApp {

    public static final int DEFAULT_PORT = 5000;

    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        if (args.length > 0) {
            try { port = Integer.parseInt(args[0]); }
            catch (NumberFormatException e) {
                System.out.println("Port invalide, utilisation de " + DEFAULT_PORT);
            }
        }
        final int finalPort = port;
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> new ServerGUI(finalPort).setVisible(true));
    }
}