import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Point d'entrée du CLIENT.
 * Lance d'abord LoginGUI (authentification obligatoire).
 * Si les identifiants sont corrects, LoginGUI crée et affiche ClientGUI.
 */
public class ClientApp {
    public static void main(String[] args) {
        // Appliquer le Look & Feel du système
        try { 
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); 
        } catch (Exception ignored) {}

        // Lancer LoginGUI dans le thread Swing
        SwingUtilities.invokeLater(() -> new LoginGUI());
    }
}
