import java.io.Serializable;

/**
 * Représente un noeud (serveur d'agents) dans le réseau
 */
public class Node implements Serializable {
    
    private String host;
    private int port;
    
    /* constructeur pour un Noeud */
    public Node(String host, int port) {
        this.host = host;
        this.port = port;
    }
    /* ge */
    public String getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
    }
    
    @Override
    public String toString() {
        return host + ":" + port;
    }
}
