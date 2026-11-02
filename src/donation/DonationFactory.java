package donation;

import users.Donor;

/**
 * FACTORY PATTERN
 * Callers pass a String; the factory decides which Donation subclass to build.
 */
public class DonationFactory {

    public static Donation createDonation(String type, Donor donor, double amount) {
        if (type == null) {
            throw new IllegalArgumentException("Donation type is required");
        }
        switch (type.trim().toLowerCase()) {
            case "zakat":
                return new ZakatDonation(donor, amount);
            case "sadaqah":
                return new SadaqahDonation(donor, amount);
            case "inkind":
            case "in-kind":
                return new InKindDonation(donor, amount);
            default:
                throw new IllegalArgumentException("Unknown donation type: " + type);
        }
    }
}
