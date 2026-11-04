package observer.Donation;

import core.DatabaseManager;
import donation.AnonymousDonor;
import donation.Donation;
import donation.InKindDonation;
import donation.RecurringDonor;
import donation.SadaqahDonation;
import observer.Observer;
import observer.Subject;
import users.Donor;

import java.util.ArrayList;
import java.util.List;

/**
 * OBSERVER PATTERN (Subject) - uses FACTORY, DECORATOR and SINGLETON.
 */
public abstract class DonationService implements Subject {

    private List<Observer> observers = new ArrayList<>();
    private DatabaseManager db = DatabaseManager.getInstance();
    private final List<Donation> allDonations = new ArrayList<>();

    protected abstract Donation createDonation(Donor donor, double amount);

    @Override
    public void attach(Observer o) {
        if (o != null && !observers.contains(o)) {
            observers.add(o);
        }
    }

    @Override
    public void detach(Observer o) {
        observers.remove(o);
    }

    @Override
    public void notifyObservers(Donation d) {
        for (Observer o : observers) {
            o.update(d);
        }
    }

    public Donation processDonation(String type, Donor donor, double amount) {
        return processDonation(type, donor, amount, null, 1, false, 0);
    }

    /**
     * @param detail          sadaqah campaign name / in-kind item name (optional)
     * @param quantity        in-kind quantity
     * @param anonymous       wrap with AnonymousDonor (DECORATOR)
     * @param recurringMonths &gt; 0 wraps with RecurringDonor (DECORATOR)
     */
    public Donation processDonation(String type, Donor donor, double amount, String detail, int quantity,
                                    boolean anonymous, int recurringMonths) {
        if (donor == null) {
            throw new IllegalArgumentException("Donor is required");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Donation amount must be positive");
        }
        Donation donation = this.createDonation(donor, amount);     // FACTORY METHOD
        if (donation instanceof SadaqahDonation && detail != null && !detail.isBlank()) {
            ((SadaqahDonation) donation).setCampaignName(detail.trim());
        }
        if (donation instanceof InKindDonation) {
            if (quantity <= 0) throw new IllegalArgumentException("Quantity must be at least 1");
            if (detail != null && !detail.isBlank()) ((InKindDonation) donation).setItemName(detail.trim());
            ((InKindDonation) donation).setQuantity(quantity);
        }
        if (anonymous) donation = new AnonymousDonor(donation);                      // DECORATOR
        if (recurringMonths > 0) donation = new RecurringDonor(donation, recurringMonths); // DECORATOR

        db.executeQuery("INSERT INTO donations (id, donor_id, type, amount) VALUES ("
                + donation.getDonationId() + ", " + donor.getUserId() + ", '" + type + "', " + amount + ")");
        donor.addDonation(donation);
        allDonations.add(donation);
        notifyObservers(donation);                                                  // OBSERVER
        return donation;
    }

    public List<Donation> getAllDonations() {
        return new ArrayList<>(allDonations);
    }
}
