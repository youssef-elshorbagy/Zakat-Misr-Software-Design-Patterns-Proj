package observer.Donation;

import donation.Donation;
import donation.SadaqahDonation;
import users.Donor;

public class SadaqahDonationService extends DonationService{
    
    @Override 
    protected Donation createDonation(Donor donor, double amount) {
        return new SadaqahDonation(donor, amount);
    }

}
