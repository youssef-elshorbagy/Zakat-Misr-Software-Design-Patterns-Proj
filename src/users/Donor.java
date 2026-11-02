package users;

import donation.Donation;

import java.util.ArrayList;
import java.util.List;

public class Donor extends User {

    private String address;
    private String preferredChannel; // "email", "sms" or "social"
    private final List<Donation> donationHistory = new ArrayList<>();

    public Donor(int userId, String fullName, String email, String phone, String password,
                 String address, String preferredChannel) {
        super(userId, fullName, email, phone, password);
        this.address = address;
        this.preferredChannel = preferredChannel;
    }

    public List<Donation> getDonationHistory() {
        return new ArrayList<>(donationHistory);
    }

    public void addDonation(Donation d) {
        donationHistory.add(d);
    }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPreferredChannel() { return preferredChannel; }
    public void setPreferredChannel(String preferredChannel) { this.preferredChannel = preferredChannel; }
}
