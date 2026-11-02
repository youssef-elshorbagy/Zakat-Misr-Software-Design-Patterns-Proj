package volunteer;

import users.Volunteer;

/** MVC Model */
public class VolunteerAssignment {

    private int assignmentId;
    private double hoursWorked;
    private String status;
    private Volunteer volunteer; // association
    private VolunteerTask task;  // association

    public VolunteerAssignment(int assignmentId, Volunteer volunteer, VolunteerTask task) {
        this.assignmentId = assignmentId;
        this.volunteer = volunteer;
        this.task = task;
        this.status = "PENDING";
    }

    public void confirm() {
        if (!"CANCELLED".equals(status)) status = "CONFIRMED";
    }

    public void cancel() {
        if (!"CANCELLED".equals(status)) {
            status = "CANCELLED";
            task.releaseSlot();
        }
    }

    public int getAssignmentId() { return assignmentId; }
    public double getHoursWorked() { return hoursWorked; }
    public void setHoursWorked(double hoursWorked) { this.hoursWorked = hoursWorked; }
    public String getStatus() { return status; }
    public Volunteer getVolunteer() { return volunteer; }
    public VolunteerTask getTask() { return task; }
}
