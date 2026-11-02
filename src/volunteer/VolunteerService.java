package volunteer;

import core.DatabaseManager;
import users.Volunteer;

import lookup.Skill;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class VolunteerService {

    private DatabaseManager db = DatabaseManager.getInstance();
    private final List<Volunteer> volunteers = new ArrayList<>(); // in-memory table
    private final List<VolunteerTask> tasks = new ArrayList<>();
    private final List<VolunteerAssignment> assignments = new ArrayList<>();
    private int assignmentCounter = 1;
    private int taskCounter = 1;

    public void registerVolunteer(Volunteer v) {
        if (volunteers.contains(v)) return;
        volunteers.add(v);
        db.executeQuery("INSERT INTO volunteers (id, name) VALUES (" + v.getUserId() + ", '" + v.getFullName() + "')");
    }

    public List<Volunteer> matchVolunteers(VolunteerTask task) {
        List<Volunteer> matched = new ArrayList<>();
        for (Volunteer v : volunteers) {
            if (v.getSkills().containsAll(task.getRequiredSkills())) {
                matched.add(v);
            }
        }
        return matched;
    }

    public VolunteerAssignment assign(Volunteer v, VolunteerTask task) {
        if (task.isFull()) {
            throw new IllegalStateException("Cannot assign: task '" + task.getTitle() + "' is full");
        }
        if (!v.getSkills().containsAll(task.getRequiredSkills())) {
            throw new IllegalStateException(v.getFullName() + " does not have the required skills");
        }
        for (VolunteerAssignment existing : assignments) {
            if (existing.getVolunteer() == v && existing.getTask() == task && !"CANCELLED".equals(existing.getStatus()))
                throw new IllegalStateException(v.getFullName() + " already applied for this task");
        }
        task.reserveSlot();
        VolunteerAssignment a = new VolunteerAssignment(assignmentCounter++, v, task);
        assignments.add(a);
        db.executeQuery("INSERT INTO assignments (id, volunteer_id, task_id) VALUES ("
                + a.getAssignmentId() + ", " + v.getUserId() + ", " + task.getTaskId() + ")");
        return a;
    }

    public String issueCertificate(Volunteer v) {
        if (v.getTotalHours() < 1) {
            return "No certificate: " + v.getFullName() + " has no logged volunteer hours yet";
        }
        return "CERTIFICATE OF APPRECIATION - Awarded to " + v.getFullName()
                + " for " + v.getTotalHours() + " volunteer hours at Zakat Misr";
    }

    // ---- tasks & lists used by the UI ----
    public VolunteerTask createTask(String title, List<Skill> requiredSkills, Date date, int maxSlots) {
        if (title == null || title.isBlank()) throw new IllegalArgumentException("Task title is required");
        if (maxSlots <= 0) throw new IllegalArgumentException("Slots must be at least 1");
        if (date == null) throw new IllegalArgumentException("Task date is required");
        VolunteerTask t = new VolunteerTask(taskCounter++, title.trim(), new ArrayList<>(requiredSkills), date, maxSlots);
        tasks.add(t);
        db.executeQuery("INSERT INTO tasks (id, title, slots) VALUES (" + t.getTaskId() + ", '" + t.getTitle() + "', " + maxSlots + ")");
        return t;
    }

    public List<VolunteerTask> getTasks() { return new ArrayList<>(tasks); }
    public List<VolunteerAssignment> getAssignments() { return new ArrayList<>(assignments); }
    public List<Volunteer> getVolunteers() { return new ArrayList<>(volunteers); }

    public List<VolunteerAssignment> getAssignmentsFor(Volunteer v) {
        List<VolunteerAssignment> r = new ArrayList<>();
        for (VolunteerAssignment a : assignments) if (a.getVolunteer() == v) r.add(a);
        return r;
    }

    public void removeVolunteer(Volunteer v) {
        volunteers.remove(v);
        for (VolunteerAssignment a : assignments) if (a.getVolunteer() == v) a.cancel();
        assignments.removeIf(a -> a.getVolunteer() == v);
    }
}
