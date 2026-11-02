package users;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/** MVC Model - base of all system users. */
public abstract class User {

    protected int userId;
    protected String fullName;
    protected String email;
    protected String phone;
    protected String passwordHash;

    protected User(int userId, String fullName, String email, String phone, String password) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.passwordHash = hash(password);
    }

    public boolean login(String email, String pass) {
        return this.email.equalsIgnoreCase(email) && this.passwordHash.equals(hash(pass));
    }

    public void updateProfile(String name, String phone) {
        if (name != null && !name.isBlank()) this.fullName = name;
        if (phone != null && !phone.isBlank()) this.phone = phone;
    }

    public void changePassword(String newPass) {
        if (newPass == null || newPass.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
        this.passwordHash = hash(newPass);
    }

    private static String hash(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public String getRole() { return getClass().getSimpleName(); }
    public int getUserId() { return userId; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
}
