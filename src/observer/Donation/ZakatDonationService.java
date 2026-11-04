package observer.Donation;

import donation.Donation;
import donation.ZakatDonation;
import users.Donor;

public class ZakatDonationService extends DonationService {
    @Override 
    protected Donation createDonation(Donor donor, double amount) {
        return new ZakatDonation(donor, amount);
    }
}
