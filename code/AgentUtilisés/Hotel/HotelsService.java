import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * "ervice côté noeud Hôtels : retourne une liste de noms d'hôtels.
 */
public class HotelsService implements Serializable {
    private final Map<String, String[]> hotelsByCity = new HashMap<>();

    public HotelsService() {

        // Lyon -> 1 hôtel
        hotelsByCity.put("lyon", new String[] {
            "Bellecour Residence"
        });

        // Marseille -> 3 hôtels
        hotelsByCity.put("marseille", new String[] {
            "Vieux Port Hotel", "Calanques Retreat", "Prado Comfort"
        });

        // Bordeaux -> 5 hôtels
        hotelsByCity.put("bordeaux", new String[] {
            "Chartrons House", "Garonne Quays", "Vineyard Lodge",
            "Saint-Emilion Manor", "Atlantic Harbor Hotel"
        });

        // Paris -> 8 hôtels
        hotelsByCity.put("paris", new String[] {
            "Hotel Louvre", "Seine View", "Opera House", "Montmartre Boutique",
            "Eiffel Terrace", "Latin Quarter Hotel", "Champs Elysees Suites", "Gare de Lyon Lodge"
        });
    }


    public String[] listHotels(String city) {
        if (city == null) return new String[0];
        String[] res = hotelsByCity.get(city.toLowerCase());
        return (res == null) ? new String[0] : res;
    }
}
