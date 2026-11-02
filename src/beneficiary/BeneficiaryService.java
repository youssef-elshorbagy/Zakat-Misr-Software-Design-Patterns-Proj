package beneficiary;

import core.DatabaseManager;
import donation.Donation;
import users.Beneficiary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BeneficiaryService {

    private static final double INCOME_PER_PERSON_LIMIT = 2000; // EGP per month
    private DatabaseManager db = DatabaseManager.getInstance();
    private int distributionCounter = 1;
    private int needCounter = 1;
    private final List<Beneficiary> beneficiaries = new ArrayList<>();
    private final Map<Need, Beneficiary> needOwners = new LinkedHashMap<>();
    private final List<AidDistribution> distributions = new ArrayList<>();
    private final Map<Integer, Double> allocatedPerDonation = new HashMap<>();

    public void registerBeneficiary(Beneficiary b) {
        if (beneficiaries.contains(b)) return;
        beneficiaries.add(b);
        db.executeQuery("INSERT INTO beneficiaries (id, name, national_id) VALUES (" + b.getUserId()
                + ", '" + b.getFullName() + "', '" + b.getNationalId() + "')");
    }

    public boolean verifyEligibility(Beneficiary b) {
        boolean validId = b.getNationalId() != null && b.getNationalId().matches("\\d{14}");
        boolean validFamily = b.getFamilySize() > 0;
        boolean lowIncome = validFamily && (b.getMonthlyIncome() / b.getFamilySize()) < INCOME_PER_PERSON_LIMIT;
        return validId && validFamily && lowIncome;
    }

    public int nextNeedId() { return needCounter++; }

    /** Records a need for an eligible beneficiary. */
    public void registerNeed(Beneficiary b, Need n) {
        if (!verifyEligibility(b)) {
            throw new IllegalStateException(b.getFullName() + " is not eligible for aid");
        }
        if (n.getAmountRequired() <= 0) throw new IllegalArgumentException("Amount required must be positive");
        b.submitNeed(n);
        needOwners.put(n, b);
        db.executeQuery("INSERT INTO needs (id, beneficiary_id, category, amount) VALUES (" + n.getNeedId() + ", "
                + b.getUserId() + ", '" + n.getCategory() + "', " + n.getAmountRequired() + ")");
    }

    /** How much of this donation has not been given to any need yet. */
    public double getAvailable(Donation d) {
        return d.getAmount() - allocatedPerDonation.getOrDefault(d.getDonationId(), 0.0);
    }

    public AidDistribution distributeAid(Donation d, Need need) {
        if (need.isFulfilled()) {
            throw new IllegalStateException("Need #" + need.getNeedId() + " is already fulfilled");
        }
        double available = getAvailable(d);
        if (available <= 0) {
            throw new IllegalStateException("Donation #" + d.getDonationId() + " is already fully distributed");
        }
        double amount = Math.min(available, need.getRemaining());
        AidDistribution aid = new AidDistribution(distributionCounter++, amount, need.getCategory());
        aid.link(d, need);
        need.fund(amount);
        allocatedPerDonation.merge(d.getDonationId(), amount, Double::sum);
        distributions.add(aid);
        db.executeQuery("INSERT INTO aid_distributions (id, donation_id, need_id, amount) VALUES ("
                + aid.getDistributionId() + ", " + d.getDonationId() + ", " + need.getNeedId() + ", " + amount + ")");
        return aid;
    }

    public String evaluateImpact(Beneficiary b) {
        int total = b.getNeeds().size();
        long fulfilled = b.getNeeds().stream().filter(Need::isFulfilled).count();
        return "IMPACT REPORT for " + b.getFullName() + " (family of " + b.getFamilySize() + "): "
                + fulfilled + " of " + total + " needs fulfilled"
                + (total > 0 ? " (" + (fulfilled * 100 / total) + "%)" : "");
    }

    // ---- lists used by the UI ----
    public List<Beneficiary> getBeneficiaries() { return new ArrayList<>(beneficiaries); }
    public List<Need> getAllNeeds() { return new ArrayList<>(needOwners.keySet()); }
    public Beneficiary getOwner(Need n) { return needOwners.get(n); }
    public List<AidDistribution> getDistributions() { return new ArrayList<>(distributions); }

    public List<AidDistribution> getDistributionsFor(Beneficiary b) {
        List<AidDistribution> r = new ArrayList<>();
        for (AidDistribution a : distributions) if (needOwners.get(a.getNeed()) == b) r.add(a);
        return r;
    }

    public void removeBeneficiary(Beneficiary b) {
        beneficiaries.remove(b);
        needOwners.entrySet().removeIf(e -> e.getValue() == b);
    }
}
