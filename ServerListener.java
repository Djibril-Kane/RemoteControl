import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Ouvre le port serveur et attend les connexions entrantes.
 * Utilise un ExecutorService (java.util.concurrent) pour gérer
 * les threads clients — plus propre que new Thread() à la main.
 */
public class ServerListener implements Runnable {

    private int port;
    private boolean running;
    private ServerSocket serverSocket;
    private List<ClientHandler> clients;
    private ServerGUI gui;

    // Pool de threads : 1 thread dédié par client, créés à la demande
    private ExecutorService threadPool;

    public ServerListener(int port, ServerGUI gui) {
        this.port       = port;
        this.gui        = gui;
        this.running    = false;
        this.clients    = Collections.synchronizedList(new ArrayList<>());
        this.threadPool = Executors.newCachedThreadPool(); // 1 thread par client
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            System.out.println("[SERVEUR] En écoute sur le port " + port);
            if (gui != null) gui.log("Serveur démarré sur le port " + port);

            while (running) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(clientSocket, gui);
                clients.add(handler);
                // submit() au lieu de new Thread().start()
                threadPool.submit(handler);
            }
        } catch (IOException e) {
            if (running) System.err.println("[SERVEUR] Erreur : " + e.getMessage());
        } finally {
            threadPool.shutdown(); // libère les ressources proprement
        }
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed())
                serverSocket.close();
        } catch (IOException e) {
            System.err.println("Erreur arrêt serveur : " + e.getMessage());
        }
        threadPool.shutdownNow(); // interrompt les threads en cours
        if (gui != null) gui.log("Serveur arrêté.");
    }

    public List<ClientHandler> getClients() { return clients; }
    public boolean isRunning()              { return running;  }
}
