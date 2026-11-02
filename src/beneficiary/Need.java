package beneficiary;

/** MVC Model */
public class Need {

    private int needId;
    private String category;
    private String urgency;
    private double amountRequired;
    private boolean fulfilled; // implementation detail
    private double fundedAmount; // implementation detail (aid received so far)

    public Need(int needId, String category, String urgency, double amountRequired) {
        this.needId = needId;
        this.category = category;
        this.urgency = urgency;
        this.amountRequired = amountRequired;
    }

    public void markFulfilled() {
        this.fulfilled = true;
    }

    /** Adds aid to this need; marks it fulfilled when fully covered. */
    public void fund(double amount) {
        fundedAmount += amount;
        if (fundedAmount >= amountRequired) fulfilled = true;
    }

    public double getRemaining() {
        return fulfilled ? 0 : Math.max(0, amountRequired - fundedAmount);
    }

    public String getStatus() {
        if (fulfilled) return "FULFILLED";
        return fundedAmount > 0 ? "PARTIAL" : "OPEN";
    }

    public int getNeedId() { return needId; }
    public String getCategory() { return category; }
    public String getUrgency() { return urgency; }
    public double getAmountRequired() { return amountRequired; }
    public double getFundedAmount() { return fundedAmount; }
    public boolean isFulfilled() { return fulfilled; }
}
