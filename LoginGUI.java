import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Interface de login avant d'accéder au ClientGUI.
 * Utilisateur par défaut : admin / 1234
 */
public class LoginGUI extends JFrame {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JLabel lblError;

    // Identifiants par défaut
    private static final String DEFAULT_USER = "admin";
    private static final String DEFAULT_PASS = "passer1234";

    private ClientGUI clientGUI;

    public LoginGUI() {
        buildUI();
    }

    private void buildUI() {
        setTitle("Remote Control — Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(350, 250);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout(10, 10));

        // Panneau central
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Label titre
        JLabel lblTitle = new JLabel("Authentification");
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(lblTitle);
        centerPanel.add(Box.createVerticalStrut(20));

        // Username
        JPanel userPanel = new JPanel(new BorderLayout(10, 0));
        userPanel.add(new JLabel("Utilisateur :"), BorderLayout.WEST);
        txtUsername = new JTextField(15);
        txtUsername.setText("");
        userPanel.add(txtUsername, BorderLayout.CENTER);
        centerPanel.add(userPanel);
        centerPanel.add(Box.createVerticalStrut(10));

        // Password
        JPanel passPanel = new JPanel(new BorderLayout(10, 0));
        passPanel.add(new JLabel("Mot de passe :"), BorderLayout.WEST);
        txtPassword = new JPasswordField(15);
        txtPassword.setText("");
        passPanel.add(txtPassword, BorderLayout.CENTER);
        centerPanel.add(passPanel);
        centerPanel.add(Box.createVerticalStrut(15));

        // Bouton Login
        btnLogin = new JButton("Se connecter");
        btnLogin.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnLogin.setBackground(new Color(50, 120, 200));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.addActionListener(e -> handleLogin());
        
        // Entrée = bouton
        KeyListener enterListener = new KeyListener() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) handleLogin();
            }
            public void keyReleased(KeyEvent e) {}
            public void keyTyped(KeyEvent e) {}
        };
        txtUsername.addKeyListener(enterListener);
        txtPassword.addKeyListener(enterListener);

        centerPanel.add(btnLogin);
        centerPanel.add(Box.createVerticalStrut(10));

        // Label erreur
        lblError = new JLabel("");
        lblError.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblError.setForeground(new Color(200, 50, 50));
        lblError.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(lblError);

        add(centerPanel, BorderLayout.CENTER);
        setVisible(true);
    }

    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (authenticate(username, password)) {
            // Authentification réussie
            lblError.setText("");
            clientGUI = new ClientGUI();
            clientGUI.setVisible(true);
            dispose(); // Fermer la fenêtre login
        } else {
            // Authentification échouée
            lblError.setText("Identifiants incorrects !");
            txtPassword.setText("");
            txtUsername.requestFocus();
        }
    }

    /**
     * Valide les identifiants
     * À améliorer : vérifier dans une base de données
     */
    private boolean authenticate(String username, String password) {
        return username.equals(DEFAULT_USER) && password.equals(DEFAULT_PASS);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginGUI());
    }
}
