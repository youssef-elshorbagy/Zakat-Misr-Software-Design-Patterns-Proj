package volunteer;

import lookup.Skill;

import java.util.Date;
import java.util.List;

/** MVC Model */
public class VolunteerTask {

    private int taskId;
    private String title;
    private List<Skill> requiredSkills;
    private Date taskDate;
    private int maxSlots;
    private String status;
    private int filledSlots; // implementation detail used by isFull()

    public VolunteerTask(int taskId, String title, List<Skill> requiredSkills, Date taskDate, int maxSlots) {
        this.taskId = taskId;
        this.title = title;
        this.requiredSkills = requiredSkills;
        this.taskDate = taskDate;
        this.maxSlots = maxSlots;
        this.status = "OPEN";
    }

    public boolean isFull() {
        return filledSlots >= maxSlots;
    }

    public void reserveSlot() {
        if (isFull()) throw new IllegalStateException("Task '" + title + "' is full");
        filledSlots++;
        if (isFull()) status = "FULL";
    }

    public void releaseSlot() {
        if (filledSlots > 0) filledSlots--;
        status = "OPEN";
    }

    public int getTaskId() { return taskId; }
    public String getTitle() { return title; }
    public List<Skill> getRequiredSkills() { return requiredSkills; }
    public Date getTaskDate() { return taskDate; }
    public int getMaxSlots() { return maxSlots; }
    public int getFilledSlots() { return filledSlots; }
    public String getStatus() { return status; }
}
