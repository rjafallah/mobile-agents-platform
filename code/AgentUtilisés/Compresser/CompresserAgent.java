import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public class CompresserAgent extends AgentImpl {

    private int state = 0;

    // serveur (
    private final Node server = new Node("localhost", 2001);

    // données transportées
    private String originalText;
    private String compressedText;

    // Mesure de temps
    private long t0 = -1;          // début total
    private long startTrajet = -1; // début du trajet courant
    private String label = "";     // nom du trajet courant

    private void printTime() {
        if (startTrajet > 0) {
            long dt = System.currentTimeMillis() - startTrajet;
            System.out.println("[CompresserAgent] transmission " + label + " = " + dt + " ms");
            startTrajet = -1;
        }
    }

    @Override
    public void main() throws MoveException {

        // 0) Sur app-node : lire doc.in, puis migrer
        if (state == 0) {
            try {
                Path in = Path.of("AgentUtilisés/Compresser/doc.in");
                originalText = Files.readString(in, StandardCharsets.UTF_8);
                System.out.println("[CompresserAgent] read doc.in (" + originalText.length() + " octets) on app-node");
            } catch (IOException e) {
                throw new MoveException("Impossible de lire AgentUtilisés/Compresser/doc.in : " + e.getMessage());
            }

            state = 1;
            System.out.println("[CompresserAgent] move to server " + server);
            t0 = System.currentTimeMillis();
            label = "App -> Server";
            startTrajet = t0;
            move(server);
            return;
        }

        // 1) Sur server-node : compresser, puis back()
        if (state == 1) {
            printTime();
            compressedText = compresser(originalText);

            int before = originalText.length();
            int after = compressedText.length();
            System.out.println("[CompresserAgent] on server: before = " + before + " chars (octets), after = " + after + " chars (octets)");

            state = 2;
            System.out.println("[CompresserAgent] back() to app-node");
            label = "Server -> App";
            startTrajet = System.currentTimeMillis();
            back();
            return;
        }

        // 2) Retour app-node : écrire doc.out et notify lock
        if (state == 2) {
            printTime();
            try {
                Files.createDirectories(Path.of("src/apps/out"));
                Files.writeString(Path.of("src/apps/out/doc.out"), compressedText, StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new MoveException("Erreur écriture src/apps/out/doc.out : " + e.getMessage());
            }

            System.out.println("[CompresserAgent] write src/apps/out/doc.out (" + compressedText.length() + " octets)");

            Object lock = getNameServer().get(getClass().getName() + "_lock");
            System.out.println("[CompresserAgent] TOTAL compress time = " + (System.currentTimeMillis() - t0) + " ms");
            synchronized (lock) {
                lock.notify();
            }

            state = 3;
            return;
        }

        System.out.println("[CompresserAgent] done.");
    }

    // "Compression" : supprimer  les espaces et sauts de lignes
    private static String compresser(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c != ' ' && c != '\n') {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
