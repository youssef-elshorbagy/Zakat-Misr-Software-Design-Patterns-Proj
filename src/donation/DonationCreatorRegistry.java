package donation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Maps the choices shown to donors to their donation creators. */
public class DonationCreatorRegistry {
    private final Map<String, DonationClient> creators = new LinkedHashMap<>();

    public void register(String type, DonationClient creator) {
        if (type == null || type.isBlank() || creator == null) {
            throw new IllegalArgumentException("Donation type and creator are required");
        }
        if (creators.putIfAbsent(type, creator) != null) {
            throw new IllegalArgumentException("Donation type already registered: " + type);
        }
    }

    public DonationClient getCreator(String type) {
        DonationClient creator = creators.get(type);
        if (creator == null) throw new IllegalArgumentException("Unknown donation type: " + type);
        return creator;
    }

    public List<String> getTypes() {
        return new ArrayList<>(creators.keySet());
    }
}
