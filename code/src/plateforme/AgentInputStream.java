import java.io.*;

/**
 * ObjectInputStream personnalisé pour désérialiser un agent avec un ClassLoader spécifique.
 */
public class AgentInputStream extends ObjectInputStream {

    // ClassLoader à utiliser pour résoudre les classes pendant readObject()
    private final ClassLoader loader;

    /**
     * Construit un AgentInputStream à partir d'un InputStream classique
     * et du ClassLoader qui contient les classes de l'agent (JarClassLoader).
     */
    public AgentInputStream(InputStream in, ClassLoader loader) throws IOException {
        super(in);
        this.loader = loader;
    }

    /**
     * Méthode appelée automatiquement pendant la désérialisation.
     * Elle sert à retrouver la classe correspondant à un nom.
     */
    @Override
    protected Class<?> resolveClass(ObjectStreamClass desc)
            throws IOException, ClassNotFoundException {

        String className = desc.getName();

        try {
            // 1) essayer avec le loader du jar (classes de l'agent)
            return Class.forName(className, false, loader);
        } catch (ClassNotFoundException e) {
            // 2) sinon, utiliser le mécanisme standard (path)
            return super.resolveClass(desc);
        }
    }
}
