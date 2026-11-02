package donation;

import users.Donor;

public class InKindDonation extends Donation {

    private String itemName;
    private int quantity;

    public InKindDonation(Donor donor, double amount) {
        super(donor, amount);
        this.itemName = "Goods";
        this.quantity = 1;
    }

    @Override
    public double getAmount() {
        return amount; // estimated value of the items
    }

    @Override
    public String getDescription() {
        return "In-kind donation of " + quantity + " x " + itemName + " (value " + amount
                + " EGP) from " + donor.getFullName();
    }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
