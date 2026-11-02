package users;

import beneficiary.Need;

import java.util.ArrayList;
import java.util.List;

public class Beneficiary extends User {

    private String nationalId;
    private int familySize;
    private double monthlyIncome;
    private final List<Need> needs = new ArrayList<>();

    public Beneficiary(int userId, String fullName, String email, String phone, String password,
                       String nationalId, int familySize, double monthlyIncome) {
        super(userId, fullName, email, phone, password);
        this.nationalId = nationalId;
        this.familySize = familySize;
        this.monthlyIncome = monthlyIncome;
    }

    public void submitNeed(Need need) {
        needs.add(need);
        System.out.println("[BENEFICIARY " + fullName + "] submitted need #" + need.getNeedId()
                + " (" + need.getCategory() + ", urgency " + need.getUrgency() + ")");
    }

    public String getNationalId() { return nationalId; }
    public int getFamilySize() { return familySize; }
    public double getMonthlyIncome() { return monthlyIncome; }
    public List<Need> getNeeds() { return needs; }
}
