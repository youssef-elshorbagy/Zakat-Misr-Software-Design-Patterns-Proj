package observer.Donation;

import donation.Donation;
import donation.InKindDonation;
import users.Donor;

public class InKindDonationService extends DonationService{
    
    @Override 
    protected Donation createDonation(Donor donor, double amount) {
        return new InKindDonation(donor, amount);
    }
}
