import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientNetwork {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private boolean connected = false;

    //  prend l'IP et le port en paramètre
    public void connect(String ip, int port) throws IOException {
        socket = new Socket(ip, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        connected = true;
    }

    // Envoie la commande et attend le résultat ligne par ligne
    public String sendCommand(String command) throws IOException {
        out.println(command);

        StringBuilder result = new StringBuilder();
        String line;

        while ((line = in.readLine()) != null) {
            // Arrêt de la lecture au marqueur défini par le serveur
            if (line.equals("--END--")) {
                break;
            }
            result.append(line).append("\n");
        }

        return result.toString().trim();
    }

    // Déconnexion propre
    public void disconnect() {
        if (connected) {
            try {
                out.println("EXIT"); // Envoi du signal au serveur

                if (in != null) in.close();
                if (out != null) out.close();
                if (socket != null && !socket.isClosed()) socket.close();
            } catch (IOException e) {
                System.err.println("Erreur de déconnexion : " + e.getMessage());
            } finally {
                connected = false;
            }
        }
    }

    // Retourne l'état
    public boolean isConnected() {
        return connected;
    }
}