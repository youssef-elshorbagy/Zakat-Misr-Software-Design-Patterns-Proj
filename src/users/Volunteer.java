package users;

import lookup.Skill;
import volunteer.VolunteerAssignment;
import volunteer.VolunteerTask;

import java.util.ArrayList;
import java.util.List;

public class Volunteer extends User {

    private List<Skill> skills = new ArrayList<>();
    private List<String> availability = new ArrayList<>();
    private double totalHours;

    public Volunteer(int userId, String fullName, String email, String phone, String password) {
        super(userId, fullName, email, phone, password);
    }

    public void applyForTask(VolunteerTask task) {
        System.out.println("[VOLUNTEER " + fullName + "] applied for task: " + task.getTitle());
    }

    public void logHours(VolunteerAssignment assignment, double hrs) {
        if (hrs <= 0) {
            throw new IllegalArgumentException("Hours must be positive");
        }
        assignment.setHoursWorked(assignment.getHoursWorked() + hrs);
        this.totalHours += hrs;
    }

    public List<String> getCertificates() {
        List<String> certificates = new ArrayList<>();
        int earned = (int) (totalHours / 10); // one certificate per 10 hours
        for (int i = 1; i <= earned; i++) {
            certificates.add("Volunteer Certificate #" + i + " - " + fullName + " (" + (i * 10) + " hours)");
        }
        return certificates;
    }

    public void addSkill(Skill s) { if (!skills.contains(s)) skills.add(s); }
    public void addAvailability(String slot) { availability.add(slot); }
    public List<Skill> getSkills() { return skills; }
    public List<String> getAvailability() { return availability; }
    public double getTotalHours() { return totalHours; }
}
