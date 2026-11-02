package donation;

import users.Donor;

public class ZakatDonation extends Donation {

    private boolean nisabVerified;

    public ZakatDonation(Donor donor, double amount) {
        super(donor, amount);
        this.nisabVerified = amount > 0;
    }

    @Override
    public double getAmount() {
        return amount;
    }

    @Override
    public String getDescription() {
        return "Zakat of " + amount + " EGP from " + donor.getFullName()
                + " (nisab verified: " + nisabVerified + ")";
    }

    public boolean isNisabVerified() { return nisabVerified; }
    public void setNisabVerified(boolean nisabVerified) { this.nisabVerified = nisabVerified; }
}
