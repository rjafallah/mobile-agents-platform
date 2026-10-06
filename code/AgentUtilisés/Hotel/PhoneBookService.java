import java.io.Serializable;
import java.util.*;

/**
 * Service côté noeud Annuaire : associe hôtel -> numéro.
 */
public class PhoneBookService implements Serializable {
    private final Map<String, String> phones = new HashMap<>();

    public PhoneBookService() {

        // Lyon -> 1 hôtel
        phones.put("Bellecour Residence", "04 72 00 00 01");

        // Marseille -> 3 hôtels
        phones.put("Vieux Port Hotel",     "04 91 00 00 01");
        phones.put("Calanques Retreat",    "04 91 00 00 02");
        phones.put("Prado Comfort",        "04 91 00 00 03");

        // Bordeaux -> 5 hôtels
        phones.put("Chartrons House",      "05 56 00 00 01");
        phones.put("Garonne Quays",        "05 56 00 00 02");
        phones.put("Vineyard Lodge",       "05 56 00 00 03");
        phones.put("Saint-Emilion Manor",  "05 56 00 00 04");
        phones.put("Atlantic Harbor Hotel","05 56 00 00 05");

        // Paris -> 8 hôtels
        phones.put("Hotel Louvre",         "01 40 00 00 01");
        phones.put("Seine View",           "01 40 00 00 02");
        phones.put("Opera House",          "01 40 00 00 03");
        phones.put("Montmartre Boutique",  "01 40 00 00 04");
        phones.put("Eiffel Terrace",       "01 40 00 00 05");
        phones.put("Latin Quarter Hotel",  "01 40 00 00 06");
        phones.put("Champs Elysees Suites","01 40 00 00 07");
        phones.put("Gare de Lyon Lodge",   "01 40 00 00 08");
    }

    public String getPhone(String hotelName) {
        if (hotelName == null) return null;
        return phones.get(hotelName);
    }
}
