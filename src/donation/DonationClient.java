package donation;

import users.Donor;

/** Creator: subclasses decide which donation to construct. */
public abstract class DonationClient {
    public abstract Donation createDonation(Donor donor, double amount, String detail, int quantity);
}
