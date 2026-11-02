package event;

import users.User;

/** MVC Model */
public class EventRegistration {

    private int registrationId;
    private String ticketNo;
    private boolean attended;
    private boolean cancelled; // implementation detail
    private Event event;       // association
    private User user;         // association

    public EventRegistration(int registrationId, String ticketNo, Event event, User user) {
        this.registrationId = registrationId;
        this.ticketNo = ticketNo;
        this.event = event;
        this.user = user;
    }

    public void markAttended() {
        if (cancelled) throw new IllegalStateException("Registration " + ticketNo + " was cancelled");
        attended = true;
    }

    public void cancelRegistration() {
        if (!cancelled) {
            cancelled = true;
            attended = false;
            event.removeRegistration();
        }
    }

    public int getRegistrationId() { return registrationId; }
    public String getTicketNo() { return ticketNo; }
    public boolean isAttended() { return attended; }
    public boolean isCancelled() { return cancelled; }
    public Event getEvent() { return event; }
    public User getUser() { return user; }
}
