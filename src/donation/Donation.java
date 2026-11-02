package donation;

import users.Donor;
import java.util.Date;

/**
 * FACTORY (abstract product) + DECORATOR (abstract component).
 */
public abstract class Donation {

    private static int counter = 1;

    protected int donationId;
    protected Donor donor;
    protected double amount;
    protected Date date;

    protected Donation(Donor donor, double amount) {
        this(donor, amount, counter++, new Date());
    }

    // Used by decorators so a wrapped donation keeps the SAME id and date.
    protected Donation(Donor donor, double amount, int donationId, Date date) {
        this.donor = donor;
        this.amount = amount;
        this.donationId = donationId;
        this.date = date;
    }

    public abstract double getAmount();

    public abstract String getDescription();

    public int getDonationId() { return donationId; }
    public Donor getDonor() { return donor; }
    public Date getDate() { return date; }
}
