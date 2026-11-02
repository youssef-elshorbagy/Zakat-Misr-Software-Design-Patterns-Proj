package beneficiary;

import donation.Donation;

import java.util.Date;

/** MVC Model */
public class AidDistribution {

    private int distributionId;
    private double amount;
    private String resourceType;
    private Date distributionDate;
    private String status;
    private Need need;         // association: satisfies
    private Donation donation; // association: funded by

    public AidDistribution(int distributionId, double amount, String resourceType) {
        this.distributionId = distributionId;
        this.amount = amount;
        this.resourceType = resourceType;
        this.distributionDate = new Date();
        this.status = "PENDING";
    }

    public void confirmReceipt() {
        this.status = "RECEIVED";
    }

    public void link(Donation donation, Need need) { this.donation = donation; this.need = need; }
    public Need getNeed() { return need; }
    public Donation getDonation() { return donation; }

    public int getDistributionId() { return distributionId; }
    public double getAmount() { return amount; }
    public String getResourceType() { return resourceType; }
    public Date getDistributionDate() { return distributionDate; }
    public String getStatus() { return status; }
}
