package lookup;

public class DonationCategory extends LookupItem {

    private boolean isTaxDeductible;

    public DonationCategory(int id, String code, String name, boolean isTaxDeductible) {
        super(id, code, name);
        this.isTaxDeductible = isTaxDeductible;
    }

    public String getCategoryDetails() {
        return "Category[" + code + "] " + name + " | tax deductible: " + isTaxDeductible
                + " | active: " + isActive;
    }

    public boolean isTaxDeductible() { return isTaxDeductible; }
    public void setTaxDeductible(boolean taxDeductible) { this.isTaxDeductible = taxDeductible; }
}
