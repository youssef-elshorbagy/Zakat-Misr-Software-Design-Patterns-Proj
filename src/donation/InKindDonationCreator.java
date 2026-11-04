package donation;

import users.Donor;

public class InKindDonationCreator extends DonationClient {
    @Override
    public Donation createDonation(Donor donor, double amount, String detail, int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be at least 1");
        InKindDonation donation = new InKindDonation(donor, amount);
        if (detail != null && !detail.isBlank()) donation.setItemName(detail.trim());
        donation.setQuantity(quantity);
        return donation;
    }
}
