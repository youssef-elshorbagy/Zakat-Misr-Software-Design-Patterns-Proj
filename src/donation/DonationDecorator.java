package donation;

/**
 * DECORATOR PATTERN (base decorator)
 * It IS a Donation and it HAS a Donation (wrappedDonation).
 * By default everything is delegated to the wrapped donation.
 */
public abstract class DonationDecorator extends Donation {

    protected Donation wrappedDonation;

    public DonationDecorator(Donation d) {
        super(d.getDonor(), d.getAmount(), d.getDonationId(), d.getDate());
        this.wrappedDonation = d;
    }

    @Override
    public double getAmount() {
        return wrappedDonation.getAmount();
    }

    @Override
    public String getDescription() {
        return wrappedDonation.getDescription();
    }
}
