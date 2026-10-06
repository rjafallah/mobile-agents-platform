import java.lang.reflect.Method;
import java.util.*;

/**
 * - Va au noeud Hôtels : récupère la liste
 * - Va au noeud Annuaire : récupère les numéros localement
 * - Revient à l'origine et affiche le résultat
 */
public class HotelAgent extends AgentImpl {

    private boolean start = true;

    // Deux nœuds serveurs
    private final Node nHotels = new Node("localhost", 2001);
    private final Node nPhone  = new Node("localhost", 2002);

    // "place" sert à reprendre après migration
    private Node place = null;

    // Résultats transportés par l'agent
    private final String city = "paris";
    private List<String> hotels = new ArrayList<>();
    private Map<String, String> phoneByHotel = new LinkedHashMap<>();

    // Mesure de temps
    private long t0 = -1;        // début total
    private long startTrajet = -1;  // début du trajet courant
    private String label = "";  // nom du trajet courant

    private void printTime() {
        if (startTrajet > 0) {
            long dt = System.currentTimeMillis() - startTrajet;
            System.out.println("[HotelAgent] transmission " + label + " = " + dt + " ms");
            startTrajet = -1;
        }
    }

    @Override
    public void main() throws MoveException {
        try {
            // 1) Départ : aller sur le noeud hôtels
            if (start) {
                start = false;
                place = nHotels;
                System.out.println("[HotelAgent] before move -> nodeHotels (2001)");
                t0 = System.currentTimeMillis();
                label = "App -> Hotels";
                startTrajet = t0;
                move(nHotels);
                return;
            }

            // 2) Sur le noeud hôtels : récupérer la liste
            if (place == nHotels) {
                printTime();
                // Récupérer/créer le service sur ce noeud via nameServer
                Object svc = getNameServer().get("HotelsService");
                if (svc == null) {
                    svc = new HotelsService();
                    getNameServer().put("HotelsService", svc);
                }

                // Appel
                Method m = svc.getClass().getMethod("listHotels", String.class);
                String[] names = (String[]) m.invoke(svc, city);

                hotels = new ArrayList<>();
                for (String h : names) hotels.add(h);

                place = nPhone;
                System.out.println("[HotelAgent] got " + hotels.size() + " hotels, before move -> nodePhone (2002)");
                label = "Hotels -> PhoneBook";
                startTrajet = System.currentTimeMillis();
                move(nPhone);
                return;
            }

            // 3) Sur le noeud annuaire : récupérer les numéros
            if (place == nPhone) {
                printTime();
                Object svc = getNameServer().get("PhoneBookService");
                if (svc == null) {
                    svc = new PhoneBookService();
                    getNameServer().put("PhoneBookService", svc);
                }

                Method m = svc.getClass().getMethod("getPhone", String.class);

                phoneByHotel.clear();
                for (String h : hotels) {
                    String phone = (String) m.invoke(svc, h);
                    phoneByHotel.put(h, phone);
                }

                place = null;
                System.out.println("[HotelAgent] got phones, before back()");
                label = "PhoneBook -> App";
                startTrajet = System.currentTimeMillis();
                back();
                return;
            }

            printTime();
            System.out.println("[HotelAgent] TOTAL forward time = " + (System.currentTimeMillis() - t0) + " ms");
            // 4) Retour à l'origine : afficher et libérer le lock
            System.out.println("[HotelAgent] I am back. Results for city = " + city);
            for (Map.Entry<String, String> e : phoneByHotel.entrySet()) {
                System.out.println("  - " + e.getKey() + " -> " + e.getValue());
            }

            Object lock = getNameServer().get(this.getClass().getName() + "_lock");
            if (lock != null) {
                synchronized (lock) { lock.notify(); }
            }

        } catch (MoveException me) {
            throw me;
        } catch (Exception e) {
            throw new MoveException("HotelAgent error: " + e.getMessage());
        }
    }
}
