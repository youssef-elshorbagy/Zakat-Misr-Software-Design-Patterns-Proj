package donation;

import users.Donor;

public class ZakatDonationCreator extends DonationClient {
    @Override
    public Donation createDonation(Donor donor, double amount, String detail, int quantity) {
        return new ZakatDonation(donor, amount);
    }
}
