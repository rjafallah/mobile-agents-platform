import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Hashtable;

/**
 * Serveur de la plateforme d'agents mobiles.
 *
 * Deux modes d'exécution :
 * 1) Server-node
 * 2) App-node
 */
public class AgentServer extends Thread {

    /** Message spécial utilisé pour signaler une migration. */
    private static final String MOVED = "__MOVED__";

    /** Infos du noeuds */
    private static int port;
    private static Node node;

    /** Serveur de noms : services accessibles par les agents sur ce noeud. */
    private static final Hashtable<String, Object> nameServer = new Hashtable<>();

    /**
     * Cache de code : nom complet de la classe de l'agent -> bytes du .jar
     * Sert à renvoyer le code lors des move() depuis ce noeud.
     */
    private static final Hashtable<String, byte[]> codeBase = new Hashtable<>();

    /**
     * "Serveur local" utilisé par les agents .
     * On évite de donner aux agents le thread de connexion (this) qui sert juste à recevoir.
     */
    private static AgentServer localServer;

    // Attribut par thread de connexion
    private final Socket socket;

    /** Constructeur utilisé uniquement pour traiter une connexion entrante. */
    public AgentServer(Socket socket) {
        this.socket = socket;
    }

    /** Retourne le Node local (host,port) */
    public Node getNode() { 
        return node; 
    }

    /** Retourne le serveur de noms (services) */
    public Hashtable<String, Object> getNameServer() { 
        return nameServer; 
    }

    /**
     * Enregistre un service dans le nameServer. (lock)
     */
    public void registerService(String name, Object service) {
        nameServer.put(name, service);
    }

    /**
     * server-node ou app-node
     */
    public static void main(String[] args) throws Exception {
        if (args.length != 1 && args.length != 3) {
            System.out.println("Usage :");
            System.out.println(" Server-node : java AgentServer <port>");
            System.out.println(" App-node : java AgentServer <port> <agent.jar> <MainClass>");
            return;
        }

        port = Integer.parseInt(args[0]);
        node = new Node("localhost", port);

        // Crée la façade stable (utilisée par les agents pour moveAgent)
        localServer = new AgentServer(null);

        // Démarrer le serveur d'agents (boucle accept)
        final ServerSocket ss = new ServerSocket(port);
        System.out.println("[AgentServer] started on " + node);

        Thread acceptLoop = new Thread(() -> {
            try {
                while (true) {
                    Socket client = ss.accept();
                    new AgentServer(client).start();
                }
            } catch (IOException e) {
                // si c est fermé, on sort proprement
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // En mode app-node, on met l'acceptLoop en daemon pour que le process puisse finir
        // après le lock.notify().
        acceptLoop.setDaemon(args.length == 3);
        acceptLoop.start();

        // Mode app-node : charger jar + instancier agent + lancer localement + attendre la fin
        if (args.length == 3) {
            String jarPath = args[1];
            String mainClass = args[2];

            // 1) Lire le jar en bytes
            byte[] jarBytes = Files.readAllBytes(Path.of(jarPath));

            // 2) Charger la classe via le JarClassLoader
            JarClassLoader loader = new JarClassLoader(jarBytes);
            Class<?> c = loader.loadClass(mainClass);
            Agent agent = (Agent) c.getDeclaredConstructor().newInstance();

            // 3) Mettre le jar en cache sous le nom complet de la classe réellement chargée
            String className = agent.getClass().getName();
            codeBase.put(className, jarBytes);

            // 4) Créer un lock de fin et le publier dans le nameServer
            Object lock = new Object();
            nameServer.put(className + "_lock", lock);

            // 5) Initialiser et lancer l'agent localement
            agent.init("agent", node);
            localServer.launchAgent(agent);

            // 6) Attendre le retour (l'agent fera notify() sur className+"_lock")
            synchronized (lock) {
                lock.wait();
            }
            System.out.println("[App-node " + node + "] done.");
        } else {
            // Mode server-node : on laisse tourner indéfiniment
            acceptLoop.join();
        }
    }

    /**
    * Réception d'une migration : recevoir AgentMessage, charger jar, désérialiser agent, exécuter
    */
    @Override
    public void run() {
        try (ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {

            // 1) Lire le message (code + data)
            AgentMessage msg = (AgentMessage) ois.readObject();

            // 2) Fermer la socket de cette connexion
            try { socket.close(); } catch (IOException ignored) {}

            // 3) Mettre le jar en cache (pour les futurs move depuis ce nœud)
            codeBase.put(msg.getMainClass(), msg.getJarBytes());

            // 4) Préparer un loader avec le jar reçu
            JarClassLoader loader = new JarClassLoader(msg.getJarBytes());

            // 5) Désérialiser l'agent avec loader
            Agent agentArrive;
            try (AgentInputStream ais = new AgentInputStream(
                    new ByteArrayInputStream(msg.getAgentBytes()), loader)) {
                agentArrive = (Agent) ais.readObject();
            }

            System.out.println("[AgentServer " + node + "] received " + agentArrive.getClass().getName());

            // 6) Rebrancher les champs (server + nameServer)
            if (agentArrive instanceof AgentImpl) {
                AgentImpl ai = (AgentImpl) agentArrive;
                ai.setServer(localServer);
                ai.setNameServer(nameServer);
            }

            // 7) Exécuter l'agent
            try {
                agentArrive.main();
            } catch (MoveException me) {
                if (!MOVED.equals(me.getMessage())) {
                    // vraie erreur
                    me.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            try { if (socket != null) socket.close(); } catch (IOException ignored) {}
        }
    }

    /**
    * Envoi d'une migration : sérialiser agent + joindre le jar correspondant (codeBase)
    */
    public void moveAgent(Agent agent, Node target) throws MoveException {
        try {
            // 1) Récupérer le jar du code de l'agent
            String className = agent.getClass().getName();
            byte[] jarBytes = codeBase.get(className);
            if (jarBytes == null) {
                throw new MoveException("Jar introuvable pour " + className + " (codeBase vide ?)");
            }

            // 2) Sérialiser l'agent (état) en bytes
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ObjectOutputStream oosAgent = new ObjectOutputStream(baos)) {
                oosAgent.writeObject(agent);
            }
            byte[] agentBytes = baos.toByteArray();

            // 3) Construire le message (code + data)
            AgentMessage msg = new AgentMessage(className, jarBytes, agentBytes);

            System.out.println("[AgentServer " + node + "] move " + className + " -> " + target);

            // 4) Envoyer au serveur distant
            try (Socket s = new Socket(target.getHost(), target.getPort());
                 ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream())) {
                oos.writeObject(msg);
                oos.flush();
            }

        } catch (MoveException me) {
            throw me;
        } catch (Exception e) {
            throw new MoveException("Erreur pendant la migration : " + e.getMessage());
        }
    }

    /**
    * Lancement local d'un agent (sans migration réseau)
    */
    public void launchAgent(Agent agent) {
        // Configurer l'agent (transient)
        if (agent instanceof AgentImpl) {
            AgentImpl ai = (AgentImpl) agent;
            ai.setServer(localServer);
            ai.setNameServer(nameServer);
        }

        // Exécuter dans un thread (pour ne pas bloquer le serveur)
        new Thread(() -> {
            try {
                agent.main();
            } catch (MoveException me) {
                if (!MOVED.equals(me.getMessage())) {
                    me.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
