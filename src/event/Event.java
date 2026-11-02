package event;

import java.util.Date;

/** MVC Model */
public class Event {

    private int eventId;
    private String title;
    private String eventType;
    private Date eventDate;
    private String location;
    private int capacity;
    private String status;         // DRAFT / OPEN / CANCELLED
    private int registeredCount;   // implementation detail used by isFull()

    public Event(int eventId, String title, String eventType, Date eventDate, String location, int capacity) {
        this.eventId = eventId;
        this.title = title;
        this.eventType = eventType;
        this.eventDate = eventDate;
        this.location = location;
        this.capacity = capacity;
        this.status = "DRAFT";
    }

    public void openRegistration() {
        if (!"CANCELLED".equals(status)) status = "OPEN";
    }

    public void cancelEvent() {
        status = "CANCELLED";
    }

    public boolean isFull() {
        return registeredCount >= capacity;
    }

    public boolean isRegistrationOpen() { return "OPEN".equals(status); }
    public void addRegistration() { registeredCount++; }
    public void removeRegistration() { if (registeredCount > 0) registeredCount--; }

    public int getEventId() { return eventId; }
    public String getTitle() { return title; }
    public String getEventType() { return eventType; }
    public Date getEventDate() { return eventDate; }
    public String getLocation() { return location; }
    public int getCapacity() { return capacity; }
    public int getRegisteredCount() { return registeredCount; }
    public String getStatus() { return status; }
}
