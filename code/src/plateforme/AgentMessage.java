import java.io.Serializable;

/**
 * Message de migration d'un agent mobile.
 *
 * Cette classe sert à transporter :
 *  - le code de l'agent (un fichier .jar sous forme de bytes)
 *  - l'état de l'agent (l'objet Agent sérialisé sous forme de bytes)
 *
 */
public class AgentMessage implements Serializable {

    // Nom de la classe principale de l'agent
    private final String mainClass;

    // Contenu du .jar de l'agent (code) sous forme de bytes
    private final byte[] jarBytes;

    // Contenu de l'agent sérialisé (état) sous forme de bytes
    private final byte[] agentBytes;

    /**
     * Construit un message contenant le code (.jar) et l'état (objet sérialisé) de l'agent.
     */
    public AgentMessage(String mainClass, byte[] jarBytes, byte[] agentBytes) {
        this.mainClass = mainClass;
        this.jarBytes = jarBytes;
        this.agentBytes = agentBytes;
    }

    /**
     * Récupère le nom de la classe principale de l'agent.
     */
    public String getMainClass() {
        return mainClass;
    }

    /**
     * Récupère le contenu du fichier .jar associé à l'agent.
     */
    public byte[] getJarBytes() {
        return jarBytes;
    }

    /**
     * Récupère le contenu sérialisé de l'agent.
     */
    public byte[] getAgentBytes() {
        return agentBytes;
    }
}
