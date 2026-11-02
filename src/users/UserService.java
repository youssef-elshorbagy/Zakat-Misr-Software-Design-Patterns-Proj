package users;

import beneficiary.BeneficiaryService;
import core.DatabaseManager;
import volunteer.VolunteerService;

import java.util.ArrayList;
import java.util.List;

/** CRUD + authentication for all users (in-memory table + DatabaseManager singleton). */
public class UserService {

    private final DatabaseManager db = DatabaseManager.getInstance();
    private final List<User> users = new ArrayList<>();
    private final VolunteerService volunteerService;
    private final BeneficiaryService beneficiaryService;
    private int idCounter = 1;

    public UserService(VolunteerService volunteerService, BeneficiaryService beneficiaryService) {
        this.volunteerService = volunteerService;
        this.beneficiaryService = beneficiaryService;
    }

    // ---- Create ----
    public User createAccount(Registration r) {
        if (r == null || r.role == null) throw new IllegalArgumentException("Role is required");
        if (r.fullName == null || r.fullName.isBlank()) throw new IllegalArgumentException("Full name is required");
        if (r.email == null || !r.email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"))
            throw new IllegalArgumentException("Enter a valid e-mail address");
        validatePhone(r.phone);
        if (r.password == null || r.password.length() < 6)
            throw new IllegalArgumentException("Password must be at least 6 characters");
        if (findByEmail(r.email) != null) throw new IllegalArgumentException("E-mail is already registered");

        int id = idCounter++;
        User u;
        switch (r.role) {
            case "Donor":
                u = new Donor(id, r.fullName.trim(), r.email.trim(), r.phone.trim(), r.password,
                        r.address == null ? "" : r.address.trim(),
                        r.preferredChannel == null ? "email" : r.preferredChannel);
                break;
            case "Volunteer":
                Volunteer v = new Volunteer(id, r.fullName.trim(), r.email.trim(), r.phone.trim(), r.password);
                volunteerService.registerVolunteer(v);
                u = v;
                break;
            case "Beneficiary":
                if (r.nationalId == null || !r.nationalId.matches("\\d{14}"))
                    throw new IllegalArgumentException("National ID must be 14 digits");
                if (r.familySize <= 0) throw new IllegalArgumentException("Family size must be at least 1");
                if (r.monthlyIncome < 0) throw new IllegalArgumentException("Income cannot be negative");
                Beneficiary b = new Beneficiary(id, r.fullName.trim(), r.email.trim(), r.phone.trim(), r.password,
                        r.nationalId, r.familySize, r.monthlyIncome);
                beneficiaryService.registerBeneficiary(b);
                u = b;
                break;
            case "Admin":
                u = new Admin(id, r.fullName.trim(), r.email.trim(), r.phone.trim(), r.password, Math.max(1, r.roleLevel));
                break;
            default:
                throw new IllegalArgumentException("Unknown role: " + r.role);
        }
        users.add(u);
        db.executeQuery("INSERT INTO users (id, role, name, email) VALUES (" + id + ", '" + r.role + "', '"
                + u.getFullName() + "', '" + u.getEmail() + "')");
        return u;
    }

    /** Used for seeding demo data (accounts that already exist). */
    public int nextId() { return idCounter++; }

    // ---- Read ----
    public User authenticate(String email, String password) {
        if (email == null || password == null) return null;
        for (User u : users) if (u.login(email.trim(), password)) return u;
        return null;
    }

    public User findByEmail(String email) {
        if (email == null) return null;
        for (User u : users) if (u.getEmail().equalsIgnoreCase(email.trim())) return u;
        return null;
    }

    public List<User> getAll() { return new ArrayList<>(users); }

    public int countAdmins() {
        int n = 0;
        for (User u : users) if (u instanceof Admin) n++;
        return n;
    }

    // ---- Update ----
    public void update(User u) {
        if (!users.contains(u)) throw new IllegalArgumentException("User not found");
        db.executeQuery("UPDATE users SET name='" + u.getFullName() + "', phone='" + u.getPhone()
                + "' WHERE id=" + u.getUserId());
    }

    // ---- Delete ----
    public void delete(User u) {
        if (!users.remove(u)) throw new IllegalArgumentException("User not found");
        if (u instanceof Volunteer) volunteerService.removeVolunteer((Volunteer) u);
        if (u instanceof Beneficiary) beneficiaryService.removeBeneficiary((Beneficiary) u);
        db.executeQuery("DELETE FROM users WHERE id=" + u.getUserId());
    }

    public static void validatePhone(String phone) {
        if (phone == null || !phone.trim().matches("\\+?\\d{8,15}"))
            throw new IllegalArgumentException("Phone must be 8-15 digits");
    }
}
