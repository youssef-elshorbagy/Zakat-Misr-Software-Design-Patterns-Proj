package donation;

import users.Donor;

public class SadaqahDonation extends Donation {

    private String campaignName;

    public SadaqahDonation(Donor donor, double amount) {
        super(donor, amount);
        this.campaignName = "General Fund";
    }

    @Override
    public double getAmount() {
        return amount;
    }

    @Override
    public String getDescription() {
        return "Sadaqah of " + amount + " EGP from " + donor.getFullName()
                + " (campaign: " + campaignName + ")";
    }

    public String getCampaignName() { return campaignName; }
    public void setCampaignName(String campaignName) { this.campaignName = campaignName; }
}
