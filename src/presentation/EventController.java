package presentation;

import event.Event;
import event.EventRegistration;
import event.EventService;
import users.Admin;
import users.User;

/** MVC Controller - event use cases (browse, register, cancel; admin: create, open, cancel, check-in, remind). */
public class EventController {

    private EventService service;
    private EventView view;
    private User currentUser;

    public EventController(EventService service, EventView view) {
        this.service = service;
        this.view = view;
    }

    public void start(User user) {
        this.currentUser = user;
        view.onRegister(e -> run(() -> registerUser(view.getSelectedEvent(), currentUser)));
        view.onCancelRegistration(e -> run(this::cancelSelectedRegistration));
        view.onCreate(e -> run(this::createFromForm));
        view.onOpenRegistration(e -> run(this::openSelected));
        view.onCancelEvent(e -> run(this::cancelSelectedEvent));
        view.onSendReminders(e -> run(this::remindSelected));
        view.onCheckIn(e -> run(() -> checkIn(view.getSelectedRegistration())));
        view.onEventSelected(this::refreshRegistrations);
        refresh();
    }

    /** Runs a use case and reports any business-rule error in the status bar. */
    private void run(Runnable r) {
        try {
            r.run();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            view.showError(ex.getMessage());
        }
    }

    private boolean isAdmin() { return currentUser instanceof Admin; }

    private void requireEvent(Event e) {
        if (e == null) throw new IllegalStateException("Select an event first");
    }

    public void refresh() {
        java.util.List<Event> visible = new java.util.ArrayList<>();
        for (Event e : service.getEvents()) {
            if (isAdmin() || !"DRAFT".equals(e.getStatus())) visible.add(e);   // drafts are only visible to admins
        }
        view.displayEvents(visible);
        refreshRegistrations();
    }

    private void refreshRegistrations() {
        if (isAdmin()) {
            Event sel = view.getSelectedEvent();
            view.displayRegistrations(sel == null ? java.util.Collections.emptyList() : service.getRegistrationsFor(sel));
        } else {
            view.displayRegistrations(service.getRegistrationsFor(currentUser));
        }
    }

    public void createEvent(Event e) {
        if (!isAdmin()) throw new IllegalStateException("Only an admin can create events");
        service.createEvent(e);
        view.renderEventPage(e);
        refresh();
        view.showStatusMessage("Event created (status DRAFT) - open registration when ready");
    }

    private void createFromForm() {
        Event e = new Event(service.nextEventId(), view.getTitleInput().trim(), view.getTypeInput(),
                Ui.parseDate(view.getWhenInput()), view.getWhereInput().trim(), view.getCapacityInput());
        if (e.getLocation().isEmpty()) throw new IllegalArgumentException("Location is required");
        createEvent(e);
        view.clearCreateForm();
    }

    public void registerUser(Event e, User u) {
        requireEvent(e);
        EventRegistration reg = service.registerAttendee(e, u);
        view.displayTicket(reg);
        refresh();
        view.showStatusMessage("Registered - ticket " + reg.getTicketNo());
    }

    private void cancelSelectedRegistration() {
        EventRegistration r = view.getSelectedRegistration();
        if (r == null) throw new IllegalStateException("Select a ticket first");
        if (r.isCancelled()) throw new IllegalStateException("This registration is already cancelled");
        if (!view.confirm("Cancel ticket " + r.getTicketNo() + "?")) return;
        service.cancelRegistration(r);
        refresh();
        view.showStatusMessage("Registration cancelled");
    }

    private void openSelected() {
        Event e = view.getSelectedEvent();
        requireEvent(e);
        service.openRegistration(e);
        refresh();
        view.showStatusMessage("Registration is now open for " + e.getTitle());
    }

    private void cancelSelectedEvent() {
        Event e = view.getSelectedEvent();
        requireEvent(e);
        if (!view.confirm("Cancel event '" + e.getTitle() + "'?")) return;
        service.cancelEvent(e);
        refresh();
        view.showStatusMessage("Event cancelled");
    }

    private void remindSelected() {
        Event e = view.getSelectedEvent();
        requireEvent(e);
        service.sendReminders(e);
        view.showStatusMessage("Reminders sent to " + service.getRegistrationsFor(e).stream().filter(r -> !r.isCancelled()).count() + " attendee(s)");
    }

    public void checkIn(EventRegistration reg) {
        if (reg == null) throw new IllegalStateException("Select an attendee first");
        service.checkInAttendee(reg);
        refreshRegistrations();
        view.showStatusMessage("Checked in: " + reg.getUser().getFullName());
    }
}
