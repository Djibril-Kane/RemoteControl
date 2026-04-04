import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Gère la session d'un client côté serveur.
 * Tourne dans son propre Thread.
 * 
 * Journalisation :
 *  - Sauvegarde les connexions et commandes dans serveur.log
 */
public class ClientHandler implements Runnable {

    public static final String EXIT_COMMAND = "EXIT";
    public static final String END_MARKER   = "--END--";
    public static final String LOG_FILE     = "serveur.log";
    
    private static final DateTimeFormatter DATE_FORMAT = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Socket socket;
    private String clientIP;
    private BufferedReader in;
    private PrintWriter out;
    private CommandExecutor executor;
    private FileWriter logFile;

    // Callback vers la GUI serveur pour mettre à jour l'affichage
    private ServerGUI gui;

    public ClientHandler(Socket socket, ServerGUI gui) {
        this.socket   = socket;
        this.clientIP = socket.getInetAddress().getHostAddress();
        this.executor = new CommandExecutor();
        this.gui      = gui;
        try {
            this.in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out = new PrintWriter(socket.getOutputStream(), true);
            // Ouvrir le fichier log en mode append (ajout à la fin)
            this.logFile = new FileWriter(LOG_FILE, true);
        } catch (IOException e) {
            System.err.println("Erreur init ClientHandler : " + e.getMessage());
        }
    }

    @Override
    public void run() {
        // Logger la connexion
        logConnection();
        
        if (gui != null) gui.onClientConnected(this);
        System.out.println("[SERVEUR] Client connecté : " + clientIP);
        try {
            String command;
            while ((command = in.readLine()) != null) {
                command = command.trim();
                System.out.println("[" + clientIP + "] > " + command);
                if (gui != null) gui.log("[" + clientIP + "] > " + command);

                if (command.equalsIgnoreCase(EXIT_COMMAND)) {
                    logCommand(command, "(session terminée)");
                    sendResult("Session terminée.");
                    break;
                }
                String result = executor.execute(command);
                logCommand(command, result.isEmpty() ? "(pas de sortie)" : result);
                sendResult(result.isEmpty() ? "(pas de sortie)" : result);
            }
        } catch (IOException e) {
            System.err.println("Connexion perdue : " + clientIP);
            logError("Connexion perdue : " + e.getMessage());
        } finally {
            logDisconnection();
            if (gui != null) gui.onClientDisconnected(this);
            close();
        }
    }

    /** Envoie le résultat au client suivi du marqueur de fin */
    public void sendResult(String result) {
        out.println(result);
        out.println(END_MARKER);
        out.flush();
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Journalisation
    // ────────────────────────────────────────────────────────────────────────

    /** Log la connexion d'un client */
    private synchronized void logConnection() {
        String message = String.format("[%s] CONNEXION : %s\n", 
            LocalDateTime.now().format(DATE_FORMAT), clientIP);
        try {
            logFile.write(message);
            logFile.flush();
        } catch (IOException e) {
            System.err.println("Erreur logging connexion : " + e.getMessage());
        }
    }

    /** Log une commande exécutée */
    private synchronized void logCommand(String command, String result) {
        String message = String.format("[%s] %s > %s\n", 
            LocalDateTime.now().format(DATE_FORMAT), clientIP, command);
        try {
            logFile.write(message);
            logFile.flush();
        } catch (IOException e) {
            System.err.println("Erreur logging commande : " + e.getMessage());
        }
    }

    /** Log la déconnexion d'un client */
    private synchronized void logDisconnection() {
        String message = String.format("[%s] DÉCONNEXION : %s\n", 
            LocalDateTime.now().format(DATE_FORMAT), clientIP);
        try {
            logFile.write(message);
            logFile.flush();
        } catch (IOException e) {
            System.err.println("Erreur logging déconnexion : " + e.getMessage());
        }
    }

    /** Log une erreur */
    private synchronized void logError(String errorMsg) {
        String message = String.format("[%s] ERREUR %s : %s\n", 
            LocalDateTime.now().format(DATE_FORMAT), clientIP, errorMsg);
        try {
            logFile.write(message);
            logFile.flush();
        } catch (IOException e) {
            System.err.println("Erreur logging erreur : " + e.getMessage());
        }
    }

    public void close() {
        try {
            System.out.println("[SERVEUR] Déconnexion : " + clientIP);
            if (in     != null) in.close();
            if (out    != null) out.close();
            if (logFile != null) logFile.close();  // ← Fermer le fichier log
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            System.err.println("Erreur fermeture : " + e.getMessage());
        }
    }

    public String getClientIP() { return clientIP; }
}

