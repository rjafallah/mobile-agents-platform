import java.io.*;
import java.util.*;
import java.util.jar.*;

/**
 * ClassLoader capable de charger des classes Java depuis un fichier .jar
 * représenté en mémoire par un tableau de bytes (byte[]).
 *
 */
public class JarClassLoader extends ClassLoader {

    // Stockage en mémoire : nom complet de la classe -> bytes du .class
    private final Map<String, byte[]> classes = new HashMap<>();

    /**
     * Construit un classloader à partir des bytes d'un .jar.
     * On parcourt le jar et on stocke toutes les classes (.class) dans une map.
     */
    public JarClassLoader(byte[] jarBytes) throws IOException {
        super(JarClassLoader.class.getClassLoader());
        loadJar(jarBytes);
    }

    /**
     * Charge le contenu du jar en mémoire.
     * Pour chaque entrée .class, on lit son contenu et on l'associe à son nom de classe.
     */
    private void loadJar(byte[] jarBytes) throws IOException {
        // ouvrir le jar
        try (JarInputStream jis = new JarInputStream(new ByteArrayInputStream(jarBytes))) {
            JarEntry entry;

            while ((entry = jis.getNextJarEntry()) != null) {
                // On ne garde que les fichiers .class
                if (!entry.getName().endsWith(".class")) {
                    continue;
                }

                // Lire tous les bytes de la classe
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int r;
                while ((r = jis.read(buffer)) != -1) {
                    baos.write(buffer, 0, r);
                }

                // Transformer 
                String className = entry.getName()
                        .replace("/", ".")
                        .replace(".class", "");
                
                // stocker dans la map
                classes.put(className, baos.toByteArray());
            }
        }
    }

    /**
     * On cherche la classe dans la map, puis on la définit via defineClass.
     */
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] code = classes.get(name);
        if (code == null) {
            throw new ClassNotFoundException(name);
        }
        return defineClass(name, code, 0, code.length);
    }
}
