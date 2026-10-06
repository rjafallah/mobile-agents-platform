import java.util.Hashtable;

/**
 * Implémentation de base pour les agents mobiles
 * Les agents concrets doivent hériter de cette classe et implémenter la méthode main()
 */
public abstract class AgentImpl implements Agent {
    
    // Nom de l'agent
    private String name;
    
    // Nœud d'origine de l'agent
    private Node origin;
    
    // Serveur de noms contenant les services disponibles
    private transient Hashtable<String, Object> nameServer;
    
    // Référence au serveur d'agents local
    private transient AgentServer server;

    /**
     * Initialise l'agent avec un nom et son noeud d'origine
     */
    @Override
    public void init(String name, Node origin) {
        this.name = name;
        this.origin = origin;
    }
    
    /**
     * Définit le serveur de noms accessible à l'agent
     */
    @Override
    public void setNameServer(Hashtable<String, Object> ns) {
        this.nameServer = ns;
    }
    
    /**
     * Récupère le serveur de noms
     */
    @Override
    public Hashtable<String, Object> getNameServer() {
        return nameServer;
    }
    
    /**
     * Déplace l'agent vers un noeud cible
     * Utilise le serveur local pour effectuer la migration
     */
    @Override
    public void move(Node target) throws MoveException {
        if (server == null) {
            throw new MoveException("Agent pas lier à un serveur");
        }
        server.moveAgent(this, target);
    }
    
    /**
     * Retourne l'agent à son noeud d'origine
     */
    @Override
    public void back() throws MoveException {
        if (origin == null) {
            throw new MoveException("pas d'origine définie");
        }
        move(origin);
    }

    public void setServer(AgentServer server) {
        this.server = server;
    }

      
    /**
     * Point d'entrée principal de l'agent
     */
    @Override
    public abstract void main() throws MoveException;

}