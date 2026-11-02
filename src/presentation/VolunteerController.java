package presentation;

import lookup.LookupService;
import lookup.Skill;
import users.Admin;
import users.User;
import users.Volunteer;
import volunteer.VolunteerAssignment;
import volunteer.VolunteerService;
import volunteer.VolunteerTask;

/** MVC Controller - volunteer use cases (volunteer: apply / hours / certificate; admin: create tasks, match). */
public class VolunteerController {

    private VolunteerService service;
    private VolunteerView view;
    private LookupService lookupService;
    private User currentUser;

    public VolunteerController(VolunteerService service, VolunteerView view) {
        this.service = service;
        this.view = view;
    }

    public void start(User user, LookupService lookupService) {
        this.currentUser = user;
        this.lookupService = lookupService;
        view.onAddSkill(e -> run(this::addSkill));
        view.onAddAvailability(e -> run(this::addAvailability));
        view.onApply(e -> run(() -> applyForTask(volunteer(), requireTask())));
        view.onRecordHours(e -> run(() -> recordHours(requireAssignment(), view.getHours())));
        view.onCancelAssignment(e -> run(this::cancelSelectedAssignment));
        view.onCertificate(e -> run(() -> requestCertificate(volunteer())));
        view.onCreateTask(e -> run(this::createTask));
        view.onMatch(e -> run(() -> view.displayMatches(requireTask(), service.matchVolunteers(requireTask()))));
        refresh();
    }

    private void run(Runnable r) {
        try {
            r.run();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            view.showError(ex.getMessage());
        }
    }

    private boolean isAdmin() { return currentUser instanceof Admin; }

    private Volunteer volunteer() {
        if (!(currentUser instanceof Volunteer)) throw new IllegalStateException("Only volunteers can do this");
        return (Volunteer) currentUser;
    }

    private VolunteerTask requireTask() {
        VolunteerTask t = view.getSelectedTask();
        if (t == null) throw new IllegalStateException("Select a task first");
        return t;
    }

    private VolunteerAssignment requireAssignment() {
        VolunteerAssignment a = view.getSelectedAssignment();
        if (a == null) throw new IllegalStateException("Select an assignment first");
        return a;
    }

    public void refresh() {
        view.setSkillChoices(lookupService.getActiveSkills());
        view.displayAvailableTasks(service.getTasks());
        if (isAdmin()) {
            view.displayAssignments(service.getAssignments());
        } else {
            view.displayAssignments(service.getAssignmentsFor(volunteer()));
            view.displayProfile(volunteer());
        }
    }

    private void addSkill() {
        Skill s = view.getChosenSkill();
        if (s == null) throw new IllegalStateException("No skill selected");
        volunteer().addSkill(s);
        refresh();
        view.showStatusMessage("Skill added: " + s.getName());
    }

    private void addAvailability() {
        String slot = view.getAvailabilityInput();
        if (slot.isEmpty()) throw new IllegalArgumentException("Enter an availability slot first");
        volunteer().addAvailability(slot);
        view.clearAvailability();
        refresh();
        view.showStatusMessage("Availability added");
    }

    private void createTask() {
        if (!isAdmin()) throw new IllegalStateException("Only an admin can create tasks");
        VolunteerTask t = service.createTask(view.getTaskTitleInput(), view.getSelectedSkills(),
                Ui.parseDate(view.getTaskDateInput()), view.getSlotsInput());
        view.clearCreateForm();
        refresh();
        view.showStatusMessage("Task created: " + t.getTitle());
    }

    public void applyForTask(Volunteer v, VolunteerTask t) {
        v.applyForTask(t);
        VolunteerAssignment a = service.assign(v, t);
        a.confirm();
        refresh();
        view.showStatusMessage("Assignment #" + a.getAssignmentId() + " confirmed for '" + t.getTitle() + "'");
    }

    public void recordHours(VolunteerAssignment a, double hrs) {
        if (!"CONFIRMED".equals(a.getStatus())) throw new IllegalStateException("Only confirmed assignments can log hours");
        a.getVolunteer().logHours(a, hrs);
        refresh();
        view.showStatusMessage(hrs + " hours recorded (total " + a.getVolunteer().getTotalHours() + ")");
    }

    private void cancelSelectedAssignment() {
        VolunteerAssignment a = requireAssignment();
        if ("CANCELLED".equals(a.getStatus())) throw new IllegalStateException("Already cancelled");
        if (!isAdmin() && a.getVolunteer() != currentUser) throw new IllegalStateException("Not your assignment");
        if (!view.confirm("Cancel this assignment?")) return;
        a.cancel();
        refresh();
        view.showStatusMessage("Assignment cancelled - the slot is free again");
    }

    public void requestCertificate(Volunteer v) {
        String text = service.issueCertificate(v);
        if (!v.getCertificates().isEmpty()) {
            text += "\n\nMilestones:\n" + String.join("\n", v.getCertificates());
        }
        view.displayCertificate(text);
    }
}
