package donation;

public class RecurringDonor extends DonationDecorator {

    private int frequencyMonths;

    public RecurringDonor(Donation d, int frequencyMonths) {
        super(d);
        this.frequencyMonths = frequencyMonths;
    }

    @Override
    public double getAmount() {
        return wrappedDonation.getAmount(); // amount of EACH payment
    }

    @Override
    public String getDescription() {
        return wrappedDonation.getDescription()
                + " [Recurring every " + frequencyMonths + " month(s)]";
    }

    public int getFrequencyMonths() { return frequencyMonths; }
    public void setFrequencyMonths(int frequencyMonths) { this.frequencyMonths = frequencyMonths; }
}
