package donation;

import users.Donor;

public class SadaqahDonationCreator extends DonationClient {
    @Override
    public Donation createDonation(Donor donor, double amount, String detail, int quantity) {
        SadaqahDonation donation = new SadaqahDonation(donor, amount);
        if (detail != null && !detail.isBlank()) donation.setCampaignName(detail.trim());
        return donation;
    }
}
