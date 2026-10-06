import java.io.Serializable;
import java.util.Hashtable;

/**
 * Interface de base pour tous les agents mobiles
 * Un agent doit être sérialisable pour pouvoir migrer
 */
public interface Agent extends Serializable {
    
    /**
     * Initialise l'agent avec un nom et son nœud d'origine
     * @param name Le nom de l'agent
     * @param origin Le nœud d'origine de l'agent
     */
    public void init(String name, Node origin);
    
    /**
     * Définit le serveur de noms accessible à l'agent
     * Le serveur de noms contient les services disponibles
     * @param ns La table de hachage des services
     */
    public void setNameServer(Hashtable<String, Object> ns);
    
    /**
     * Récupère le serveur de noms
     * @return La table de hachage des services
     */
    public Hashtable<String, Object> getNameServer();
    
    /**
     * Déplace l'agent vers un nœud cible
     * @param target Le nœud de destination
     * @throws MoveException Si la migration échoue
     */
    public void move(Node target) throws MoveException;
    
    /**
     * Retourne l'agent à son nœud d'origine
     * @throws MoveException Si la migration échoue
     */
    public void back() throws MoveException;
    
    /**
     * Point d'entrée principal de l'agent
     * Cette méthode est appelée à chaque fois que l'agent arrive sur un nœud
     * @throws MoveException Si une migration échoue durant l'exécution
     */
    public void main() throws MoveException;
}