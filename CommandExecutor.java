import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Exécute une commande système en local.
 * Capture stdout + stderr et retourne le résultat sous forme de String.
 */
public class CommandExecutor {

    private String os;

    public CommandExecutor() {
        this.os = System.getProperty("os.name").toLowerCase();
    }

    public String execute(String command) {
        StringBuilder result = new StringBuilder();
        try {
            ProcessBuilder pb;
            if (os.contains("win")) {
                pb = new ProcessBuilder("cmd.exe", "/c", command);
            } else {
                pb = new ProcessBuilder("/bin/sh", "-c", command);
            }
            pb.redirectErrorStream(true);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line).append("\n");
            }
            process.waitFor();
        } catch (Exception e) {
            result.append("Erreur : ").append(e.getMessage());
        }
        return result.toString().trim();
    }
}
