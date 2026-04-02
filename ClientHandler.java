import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Gère la session d'un client côté serveur.
 * Tourne dans son propre Thread.
 */
public class ClientHandler implements Runnable {

    public static final String EXIT_COMMAND = "EXIT";
    public static final String END_MARKER   = "--END--";

    private Socket socket;
    private String clientIP;
    private BufferedReader in;
    private PrintWriter out;
    private CommandExecutor executor;

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
        } catch (IOException e) {
            System.err.println("Erreur init ClientHandler : " + e.getMessage());
        }
    }

    @Override
    public void run() {
        if (gui != null) gui.onClientConnected(this);
        System.out.println("[SERVEUR] Client connecté : " + clientIP);
        try {
            String command;
            while ((command = in.readLine()) != null) {
                command = command.trim();
                System.out.println("[" + clientIP + "] > " + command);
                if (gui != null) gui.log("[" + clientIP + "] > " + command);

                if (command.equalsIgnoreCase(EXIT_COMMAND)) {
                    sendResult("Session terminée.");
                    break;
                }
                String result = executor.execute(command);
                sendResult(result.isEmpty() ? "(pas de sortie)" : result);
            }
        } catch (IOException e) {
            System.err.println("Connexion perdue : " + clientIP);
        } finally {
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

    public void close() {
        try {
            System.out.println("[SERVEUR] Déconnexion : " + clientIP);
            if (in     != null) in.close();
            if (out    != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            System.err.println("Erreur fermeture : " + e.getMessage());
        }
    }

    public String getClientIP() { return clientIP; }
}

