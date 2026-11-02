package event;

import communication.CommunicationService;
import communication.EmailStrategy;
import core.DatabaseManager;
import donation.Donation;
import observer.Observer;
import observer.Subject;
import users.User;

import java.util.ArrayList;
import java.util.List;

public class EventService implements Subject {

    private List<Observer> observers = new ArrayList<>();
    private DatabaseManager db = DatabaseManager.getInstance();
    private final List<EventRegistration> registrations = new ArrayList<>(); // in-memory table
    private final List<Event> events = new ArrayList<>();
    private int registrationCounter = 1;
    private int eventCounter = 1;

    @Override
    public void attach(Observer o) {
        if (o != null && !observers.contains(o)) observers.add(o);
    }

    @Override
    public void detach(Observer o) {
        observers.remove(o);
    }

    @Override
    public void notifyObservers(Donation d) {
        for (Observer o : observers) o.update(d);
    }

    public int nextEventId() { return eventCounter++; }

    public Event createEvent(Event e) {
        if (e.getTitle() == null || e.getTitle().isBlank()) throw new IllegalArgumentException("Event title is required");
        if (e.getCapacity() <= 0) throw new IllegalArgumentException("Capacity must be at least 1");
        if (!events.contains(e)) events.add(e);
        db.executeQuery("INSERT INTO events (id, title, type, capacity) VALUES (" + e.getEventId()
                + ", '" + e.getTitle() + "', '" + e.getEventType() + "', " + e.getCapacity() + ")");
        return e;
    }

    public EventRegistration registerAttendee(Event e, User u) {
        if (!e.isRegistrationOpen()) {
            throw new IllegalStateException("Registration is not open for event: " + e.getTitle());
        }
        if (e.isFull()) {
            throw new IllegalStateException("Event is full: " + e.getTitle());
        }
        for (EventRegistration r : registrations) {
            if (r.getEvent() == e && r.getUser() == u && !r.isCancelled())
                throw new IllegalStateException(u.getFullName() + " is already registered for this event");
        }
        e.addRegistration();
        EventRegistration reg = new EventRegistration(registrationCounter,
                "TKT-" + e.getEventId() + "-" + String.format("%03d", registrationCounter), e, u);
        registrationCounter++;
        registrations.add(reg);
        db.executeQuery("INSERT INTO registrations (id, event_id, user_id, ticket) VALUES ("
                + reg.getRegistrationId() + ", " + e.getEventId() + ", " + u.getUserId() + ", '" + reg.getTicketNo() + "')");
        return reg;
    }

    public void checkInAttendee(EventRegistration reg) {
        reg.markAttended();
        db.executeQuery("UPDATE registrations SET attended=true WHERE ticket='" + reg.getTicketNo() + "'");
    }

    public void sendReminders(Event e) {
        CommunicationService comm = new CommunicationService();
        comm.setStrategy(new EmailStrategy()); // STRATEGY reused
        for (EventRegistration reg : registrations) {
            if (reg.getEvent() == e && !reg.isCancelled()) {
                comm.notifyUser(reg.getUser(), "Reminder: '" + e.getTitle() + "' at " + e.getLocation()
                        + " on " + e.getEventDate() + ". Your ticket: " + reg.getTicketNo());
            }
        }
    }

    // ---- extra operations used by the UI ----
    public void openRegistration(Event e) {
        if ("CANCELLED".equals(e.getStatus())) throw new IllegalStateException("Event is cancelled");
        e.openRegistration();
        db.executeQuery("UPDATE events SET status='OPEN' WHERE id=" + e.getEventId());
    }

    public void cancelEvent(Event e) {
        e.cancelEvent();
        db.executeQuery("UPDATE events SET status='CANCELLED' WHERE id=" + e.getEventId());
    }

    public void cancelRegistration(EventRegistration reg) {
        reg.cancelRegistration();
        db.executeQuery("UPDATE registrations SET cancelled=true WHERE ticket='" + reg.getTicketNo() + "'");
    }

    public List<Event> getEvents() { return new ArrayList<>(events); }

    public List<EventRegistration> getRegistrations() { return new ArrayList<>(registrations); }

    public List<EventRegistration> getRegistrationsFor(User u) {
        List<EventRegistration> r = new ArrayList<>();
        for (EventRegistration reg : registrations) if (reg.getUser() == u) r.add(reg);
        return r;
    }

    public List<EventRegistration> getRegistrationsFor(Event e) {
        List<EventRegistration> r = new ArrayList<>();
        for (EventRegistration reg : registrations) if (reg.getEvent() == e) r.add(reg);
        return r;
    }
}
