import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Interface graphique côté SERVEUR (Swing).
 *
 * Fonctionnalités (conformes au sujet) :
 *  - Bouton Démarrer / Arrêter le serveur
 *  - Tableau des clients connectés avec leur adresse IP
 *  - Journal des commandes reçues et exécutées
 */
public class ServerGUI extends JFrame {

    // ── Composants ──────────────────────────────────────────────────────────
    private JButton     btnToggle;
    private JTextArea   txtLog;
    private JTable      clientTable;
    private DefaultTableModel tableModel;
    private JLabel      lblStatus;

    // ── Serveur ─────────────────────────────────────────────────────────────
    private ServerListener listener;
    private int port;

    // Thread unique dédié au ServerListener (boucle accept)
    private ExecutorService serverExecutor;

    public ServerGUI(int port) {
        this.port = port;
        buildUI();
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Construction de l'interface
    // ────────────────────────────────────────────────────────────────────────

    private void buildUI() {
        setTitle("Remote Control — Serveur");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(700, 520);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(6, 6));

        add(buildTopPanel(),    BorderLayout.NORTH);
        add(buildCenterPanel(), BorderLayout.CENTER);
        add(buildBottomPanel(), BorderLayout.SOUTH);

        // Confirmation à la fermeture
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                int r = JOptionPane.showConfirmDialog(ServerGUI.this,
                    "Arrêter le serveur et quitter ?", "Confirmation",
                    JOptionPane.YES_NO_OPTION);
                if (r == JOptionPane.YES_OPTION) {
                    if (listener != null) listener.stop();
                    if (serverExecutor != null) serverExecutor.shutdownNow();
                    System.exit(0);
                }
            }
        });
    }

    /** Barre du haut : statut + bouton démarrer/arrêter */
    private JPanel buildTopPanel() {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setBorder(BorderFactory.createEmptyBorder(8, 10, 4, 10));

        lblStatus = new JLabel("● Serveur arrêté  |  Port : " + port);
        lblStatus.setFont(new Font("Monospaced", Font.BOLD, 13));
        lblStatus.setForeground(new Color(180, 60, 60));
        p.add(lblStatus, BorderLayout.CENTER);

        btnToggle = new JButton("▶  Démarrer le serveur");
        btnToggle.setBackground(new Color(50, 155, 85));
        btnToggle.setForeground(Color.WHITE);
        btnToggle.setFocusPainted(false);
        btnToggle.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnToggle.addActionListener(e -> toggleServer());
        p.add(btnToggle, BorderLayout.EAST);

        return p;
    }

    /** Zone centrale : tableau clients (haut) + journal (bas) */
    private JComponent buildCenterPanel() {
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        split.setResizeWeight(0.35);
        split.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));

        // ── Tableau des clients connectés ────────────────────────────────
        String[] cols = {"Adresse IP", "Statut"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        clientTable = new JTable(tableModel);
        clientTable.setFont(new Font("Monospaced", Font.PLAIN, 13));
        clientTable.setRowHeight(24);
        clientTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        clientTable.setSelectionBackground(new Color(60, 120, 200));
        JScrollPane tableScroll = new JScrollPane(clientTable);
        tableScroll.setBorder(BorderFactory.createTitledBorder("Clients connectés"));
        split.setTopComponent(tableScroll);

        // ── Journal des commandes ────────────────────────────────────────
        txtLog = new JTextArea();
        txtLog.setEditable(false);
        txtLog.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtLog.setBackground(new Color(18, 18, 28));
        txtLog.setForeground(new Color(170, 220, 170));
        JScrollPane logScroll = new JScrollPane(txtLog);
        logScroll.setBorder(BorderFactory.createTitledBorder("Journal des commandes"));
        split.setBottomComponent(logScroll);

        return split;
    }

    /** Barre du bas : bouton vider le journal */
    private JPanel buildBottomPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        JButton btnClear = new JButton("Vider le journal");
        btnClear.addActionListener(e -> txtLog.setText(""));
        p.add(btnClear);
        return p;
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Actions
    // ────────────────────────────────────────────────────────────────────────

    private void toggleServer() {
        if (listener == null || !listener.isRunning()) {
            startServer();
        } else {
            stopServer();
        }
    }

    private void startServer() {
        listener = new ServerListener(port, this);
        serverExecutor = Executors.newSingleThreadExecutor();
        serverExecutor.submit(listener);

        btnToggle.setText("■  Arrêter le serveur");
        btnToggle.setBackground(new Color(200, 55, 55));
        lblStatus.setText("● Serveur actif  |  Port : " + port);
        lblStatus.setForeground(new Color(40, 160, 80));
    }

    private void stopServer() {
        if (listener != null) listener.stop();
        if (serverExecutor != null) serverExecutor.shutdownNow();
        listener = null;
        serverExecutor = null;

        btnToggle.setText("▶  Démarrer le serveur");
        btnToggle.setBackground(new Color(50, 155, 85));
        lblStatus.setText("● Serveur arrêté  |  Port : " + port);
        lblStatus.setForeground(new Color(180, 60, 60));
        tableModel.setRowCount(0);
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Callbacks appelés depuis ClientHandler (autres threads → SwingUtilities)
    // ────────────────────────────────────────────────────────────────────────

    /** Appelé quand un nouveau client se connecte */
    public void onClientConnected(ClientHandler handler) {
        SwingUtilities.invokeLater(() -> {
            tableModel.addRow(new Object[]{ handler.getClientIP(), "Connecté" });
            log("Connexion entrante : " + handler.getClientIP());
        });
    }

    /** Appelé quand un client se déconnecte : met à jour son statut */
    public void onClientDisconnected(ClientHandler handler) {
        SwingUtilities.invokeLater(() -> {
            String ip = handler.getClientIP();
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                if (ip.equals(tableModel.getValueAt(i, 0))) {
                    tableModel.setValueAt("Déconnecté", i, 1);
                    break;
                }
            }
            log("Déconnexion : " + ip);
        });
    }

    /** Ajoute une ligne dans le journal (thread-safe) */
    public void log(String message) {
        SwingUtilities.invokeLater(() -> {
            txtLog.append(message + "\n");
            txtLog.setCaretPosition(txtLog.getDocument().getLength());
        });
    }
}