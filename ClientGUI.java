import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Interface graphique côté CLIENT (Swing).
 *
 * Fonctionnalités (conformes au sujet) :
 *  - Champ IP + port pour se connecter au serveur
 *  - Champ de saisie des commandes
 *  - Bouton "Envoyer"
 *  - Zone d'affichage des résultats
 *  - Historique des commandes (touches ↑ / ↓)
 *  - Bouton Connexion / Déconnexion
 */
public class ClientGUI extends JFrame {

    // ── Réseau ───────────────────────────────────────────────────────────────
    private ClientNetwork network;

    // Thread unique pour les opérations réseau (évite les appels simultanés)
    private final ExecutorService networkExecutor = Executors.newSingleThreadExecutor();

    // ── Historique commandes ──────────────────────────────────────────────────
    private List<String> history  = new ArrayList<>();
    private int historyIndex      = -1;

    // ── Composants ───────────────────────────────────────────────────────────
    private JTextField    txtIP, txtPort, txtCommand;
    private JTextArea     txtOutput;
    private JButton       btnConnect, btnSend, btnClear;
    private JLabel        lblStatus;

    public ClientGUI() {
        buildUI();
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Construction de l'interface
    // ────────────────────────────────────────────────────────────────────────

    private void buildUI() {
        setTitle("Remote Control — Client");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(800, 580);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(6, 6));

        add(buildTopPanel(),    BorderLayout.NORTH);
        add(buildCenterPanel(), BorderLayout.CENTER);
        add(buildBottomBar(),   BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if (network != null && network.isConnected()) network.disconnect();
                networkExecutor.shutdownNow();
                System.exit(0);
            }
        });

        setCommandControlsEnabled(false); // désactivés avant connexion
    }

    /** Panneau du haut : IP, port, bouton connexion, statut */
    private JPanel buildTopPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        p.setBorder(BorderFactory.createTitledBorder("Connexion au serveur"));

        txtIP   = new JTextField("127.0.0.1", 12);
        txtPort = new JTextField("5000", 6);

        btnConnect = new JButton("Se connecter");
        btnConnect.setBackground(new Color(50, 120, 200));
        btnConnect.setForeground(Color.WHITE);
        btnConnect.setFocusPainted(false);
        btnConnect.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnConnect.addActionListener(e -> handleConnectToggle());

        lblStatus = new JLabel("  ● Déconnecté");
        lblStatus.setFont(new Font("Monospaced", Font.BOLD, 12));
        lblStatus.setForeground(Color.GRAY);

        p.add(new JLabel("IP :"));   p.add(txtIP);
        p.add(new JLabel("Port :")); p.add(txtPort);
        p.add(btnConnect);
        p.add(lblStatus);
        return p;
    }

    /** Zone centrale : terminal d'affichage + ligne de saisie */
    private JPanel buildCenterPanel() {
        JPanel p = new JPanel(new BorderLayout(4, 4));
        p.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));

        // Zone d'affichage des résultats (style terminal)
        txtOutput = new JTextArea();
        txtOutput.setEditable(false);
        txtOutput.setFont(new Font("Monospaced", Font.PLAIN, 13));
        txtOutput.setBackground(new Color(15, 15, 25));
        txtOutput.setForeground(new Color(200, 230, 200));
        txtOutput.setCaretColor(Color.WHITE);
        txtOutput.setLineWrap(true);
        txtOutput.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(txtOutput);
        scroll.setBorder(BorderFactory.createTitledBorder("Résultats"));
        p.add(scroll, BorderLayout.CENTER);

        // Ligne de saisie de commande
        JPanel inputLine = new JPanel(new BorderLayout(4, 0));
        inputLine.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));

        txtCommand = new JTextField();
        txtCommand.setFont(new Font("Monospaced", Font.PLAIN, 13));
        txtCommand.addActionListener(e -> sendCommand()); // Entrée = envoyer

        // Navigation dans l'historique avec ↑ et ↓
        txtCommand.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_UP)   navigateHistory(-1);
                if (e.getKeyCode() == KeyEvent.VK_DOWN) navigateHistory(+1);
            }
        });

        btnSend = new JButton("Envoyer");
        btnSend.setBackground(new Color(50, 155, 85));
        btnSend.setForeground(Color.WHITE);
        btnSend.setFocusPainted(false);
        btnSend.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnSend.addActionListener(e -> sendCommand());

        inputLine.add(new JLabel("  > "), BorderLayout.WEST);
        inputLine.add(txtCommand,         BorderLayout.CENTER);
        inputLine.add(btnSend,            BorderLayout.EAST);
        p.add(inputLine, BorderLayout.SOUTH);

        return p;
    }

    /** Barre du bas : bouton vider */
    private JPanel buildBottomBar() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        btnClear = new JButton("Vider l'affichage");
        btnClear.addActionListener(e -> txtOutput.setText(""));
        p.add(btnClear);
        return p;
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Actions
    // ────────────────────────────────────────────────────────────────────────

    /** Connexion ou déconnexion selon l'état actuel */
    private void handleConnectToggle() {
        if (network != null && network.isConnected()) {
            network.disconnect();
            network = null;
            onDisconnected();
            return;
        }

        String ip = txtIP.getText().trim();
        int port;
        try {
            port = Integer.parseInt(txtPort.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Port invalide.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        network = new ClientNetwork(ip, port);

        // Connexion dans un thread séparé pour ne pas bloquer l'UI
        networkExecutor.submit(() -> {
            try {
                network.connect();
                SwingUtilities.invokeLater(() -> {
                    onConnected(ip, port);
                    appendOutput("Connecté au serveur " + ip + ":" + port + "\n");
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(ClientGUI.this,
                        "Connexion impossible : " + ex.getMessage(),
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                    network = null;
                });
            }
        });
    }

    /** Envoie la commande saisie et affiche le résultat */
    private void sendCommand() {
        if (network == null || !network.isConnected()) return;
        String cmd = txtCommand.getText().trim();
        if (cmd.isEmpty()) return;

        // Ajouter à l'historique
        history.add(0, cmd);
        historyIndex = -1;

        appendOutput("\n> " + cmd);
        txtCommand.setText("");
        btnSend.setEnabled(false); // évite double-clic pendant l'attente

        networkExecutor.submit(() -> {
            try {
                String result = network.sendCommand(cmd);
                SwingUtilities.invokeLater(() -> {
                    appendOutput(result);
                    btnSend.setEnabled(true);
                    txtCommand.requestFocus();
                    if (cmd.equalsIgnoreCase(ClientHandler.EXIT_COMMAND)) {
                        network = null;
                        onDisconnected();
                    }
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    appendOutput("Erreur : " + ex.getMessage());
                    btnSend.setEnabled(true);
                    onDisconnected();
                });
            }
        });
    }

    /** Navigation dans l'historique des commandes (↑ = plus récent) */
    private void navigateHistory(int dir) {
        if (history.isEmpty()) return;
        historyIndex = Math.max(-1, Math.min(history.size() - 1, historyIndex + dir));
        txtCommand.setText(historyIndex >= 0 ? history.get(historyIndex) : "");
        txtCommand.setCaretPosition(txtCommand.getText().length());
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Helpers UI
    // ────────────────────────────────────────────────────────────────────────

    private void onConnected(String ip, int port) {
        setCommandControlsEnabled(true);
        txtIP.setEnabled(false);
        txtPort.setEnabled(false);
        btnConnect.setText("Se déconnecter");
        btnConnect.setBackground(new Color(200, 55, 55));
        lblStatus.setText("  ● Connecté à " + ip + ":" + port);
        lblStatus.setForeground(new Color(40, 160, 80));
        txtCommand.requestFocus();
    }

    private void onDisconnected() {
        setCommandControlsEnabled(false);
        txtIP.setEnabled(true);
        txtPort.setEnabled(true);
        btnConnect.setText("Se connecter");
        btnConnect.setBackground(new Color(50, 120, 200));
        lblStatus.setText("  ● Déconnecté");
        lblStatus.setForeground(Color.GRAY);
    }

    private void setCommandControlsEnabled(boolean enabled) {
        txtCommand.setEnabled(enabled);
        btnSend.setEnabled(enabled);
    }

    private void appendOutput(String text) {
        txtOutput.append(text + "\n");
        txtOutput.setCaretPosition(txtOutput.getDocument().getLength());
    }
}