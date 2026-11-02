package users;

/** Plain data holder for the registration form (no behaviour). */
public class Registration {
    public String role;            // Donor, Volunteer, Beneficiary, Admin
    public String fullName, email, phone, password;
    public String address, preferredChannel;               // Donor
    public String nationalId; public int familySize; public double monthlyIncome; // Beneficiary
    public int roleLevel = 1;                              // Admin
}
