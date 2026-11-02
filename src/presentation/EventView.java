package presentation;

import event.Event;
import event.EventRegistration;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/** MVC View - events. Normal users register for events; admins also create / open / cancel / check in. */
public class EventView extends JPanel {

    private final boolean admin;
    private final DefaultTableModel eventsModel = Ui.model("ID", "Title", "Type", "When", "Where", "Registered", "Status");
    private final JTable eventsTable = Ui.table(eventsModel);
    private final DefaultTableModel regModel;
    private final JTable regTable;
    private final JTextArea details = Ui.textArea(5);
    private final JLabel status = Ui.statusLabel();
    private List<Event> shownEvents = new ArrayList<>();
    private List<EventRegistration> shownRegs = new ArrayList<>();

    private final JButton registerBtn = Ui.button("Register for selected event");
    private final JButton cancelRegBtn = Ui.button("Cancel selected registration");
    private final JButton openBtn = Ui.button("Open registration"), cancelEventBtn = Ui.button("Cancel event"),
            remindBtn = Ui.button("Send reminders"), checkInBtn = Ui.button("Check in selected attendee"),
            createBtn = Ui.button("Create event");
    private final JTextField title = new JTextField(16), where = new JTextField(16), when = new JTextField(14);
    private final JComboBox<String> evType = new JComboBox<>(new String[]{"Fundraiser", "Workshop", "Awareness", "Distribution"});
    private final JSpinner capacity = new JSpinner(new SpinnerNumberModel(50, 1, 5000, 1));

    public EventView(boolean admin) {
        super(new BorderLayout(10, 10));
        this.admin = admin;
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        regModel = admin ? Ui.model("Ticket", "Attendee", "Attended", "Cancelled")
                         : Ui.model("Ticket", "Event", "When", "Attended", "Cancelled");
        regTable = Ui.table(regModel);

        Ui.widths(eventsTable, 40, 170, 90, 130, 150, 80, 80);
        if (admin) Ui.widths(regTable, 110, 150, 70, 70); else Ui.widths(regTable, 110, 170, 130, 70, 70);

        JPanel evPanel = new JPanel(new BorderLayout());
        evPanel.add(Ui.scroll(eventsTable, 170), BorderLayout.CENTER);
        evPanel.add(admin ? Ui.flow(openBtn, cancelEventBtn, remindBtn) : Ui.flow(registerBtn), BorderLayout.SOUTH);

        JPanel regPanel = new JPanel(new BorderLayout());
        regPanel.add(Ui.scroll(regTable, 150), BorderLayout.CENTER);
        regPanel.add(admin ? Ui.flow(checkInBtn) : Ui.flow(cancelRegBtn), BorderLayout.SOUTH);
        JPanel detailsPanel = Ui.section("Event details", new JScrollPane(details));
        detailsPanel.setPreferredSize(new Dimension(300, 150));
        JPanel bottom = new JPanel(new BorderLayout(8, 0));
        bottom.add(Ui.section(admin ? "Attendees of the selected event" : "My tickets", regPanel), BorderLayout.CENTER);
        bottom.add(detailsPanel, BorderLayout.EAST);

        JPanel center = new JPanel(new GridLayout(2, 1, 0, 8));
        center.add(Ui.section("Events", evPanel));
        center.add(bottom);
        add(center, BorderLayout.CENTER);

        if (admin) {
            Ui.Form f = new Ui.Form();
            f.add("Title", title);
            f.add("Type", evType);
            f.add("When (" + Ui.DATE_HINT + ")", when);
            f.add("Location", where);
            f.add("Capacity", capacity);
            JPanel box = new JPanel(new BorderLayout());
            box.add(f, BorderLayout.NORTH);
            box.add(Ui.flow(createBtn), BorderLayout.CENTER);
            JPanel east = Ui.section("Create event", box);
            east.setPreferredSize(new Dimension(330, 300));
            add(east, BorderLayout.EAST);
        }
        add(status, BorderLayout.SOUTH);

        eventsTable.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            Event ev = getSelectedEvent();
            if (ev != null) renderEventPage(ev);
        });
    }

    // ---- diagram methods ----
    public void renderEventPage(Event e) {
        details.setText(e.getTitle() + " (" + e.getEventType() + ")\n"
                + "When     : " + Ui.fmt(e.getEventDate()) + "\n"
                + "Where    : " + e.getLocation() + "\n"
                + "Capacity : " + e.getRegisteredCount() + "/" + e.getCapacity() + "\n"
                + "Status   : " + e.getStatus());
    }

    public void displayTicket(EventRegistration reg) {
        Ui.info(this, "TICKET\n\nTicket No : " + reg.getTicketNo() + "\nEvent     : " + reg.getEvent().getTitle()
                + "\nWhen      : " + Ui.fmt(reg.getEvent().getEventDate())
                + "\nWhere     : " + reg.getEvent().getLocation()
                + "\nAttendee  : " + reg.getUser().getFullName() + "\nAttended  : " + reg.isAttended());
    }

    // ---- lists ----
    public void displayEvents(List<Event> events) {
        Event keep = getSelectedEvent();
        shownEvents = new ArrayList<>(events);
        eventsModel.setRowCount(0);
        for (Event e : events) {
            eventsModel.addRow(new Object[]{e.getEventId(), e.getTitle(), e.getEventType(), Ui.fmt(e.getEventDate()),
                    e.getLocation(), e.getRegisteredCount() + "/" + e.getCapacity(), e.getStatus()});
        }
        if (keep != null && shownEvents.contains(keep)) {
            int row = eventsTable.convertRowIndexToView(shownEvents.indexOf(keep));
            eventsTable.setRowSelectionInterval(row, row);
        }
    }

    public void displayRegistrations(List<EventRegistration> regs) {
        shownRegs = new ArrayList<>(regs);
        regModel.setRowCount(0);
        for (EventRegistration r : regs) {
            if (admin) {
                regModel.addRow(new Object[]{r.getTicketNo(), r.getUser().getFullName(), r.isAttended() ? "yes" : "no",
                        r.isCancelled() ? "yes" : "no"});
            } else {
                regModel.addRow(new Object[]{r.getTicketNo(), r.getEvent().getTitle(), Ui.fmt(r.getEvent().getEventDate()),
                        r.isAttended() ? "yes" : "no", r.isCancelled() ? "yes" : "no"});
            }
        }
    }

    public void showStatusMessage(String msg) { Ui.setStatus(status, msg, false); }
    public void showError(String msg) { Ui.setStatus(status, msg, true); }
    public boolean confirm(String msg) { return Ui.confirm(this, msg); }

    // ---- getters ----
    public Event getSelectedEvent() { int i = Ui.selected(eventsTable); return i < 0 || i >= shownEvents.size() ? null : shownEvents.get(i); }
    public EventRegistration getSelectedRegistration() { int i = Ui.selected(regTable); return i < 0 || i >= shownRegs.size() ? null : shownRegs.get(i); }
    public String getTitleInput() { return title.getText(); }
    public String getTypeInput() { return (String) evType.getSelectedItem(); }
    public String getWhenInput() { return when.getText(); }
    public String getWhereInput() { return where.getText(); }
    public int getCapacityInput() { return (Integer) capacity.getValue(); }
    public void clearCreateForm() { title.setText(""); when.setText(""); where.setText(""); }

    // ---- listeners ----
    public void onEventSelected(Runnable r) { eventsTable.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) r.run(); }); }
    public void onRegister(ActionListener l) { registerBtn.addActionListener(l); }
    public void onCancelRegistration(ActionListener l) { cancelRegBtn.addActionListener(l); }
    public void onOpenRegistration(ActionListener l) { openBtn.addActionListener(l); }
    public void onCancelEvent(ActionListener l) { cancelEventBtn.addActionListener(l); }
    public void onSendReminders(ActionListener l) { remindBtn.addActionListener(l); }
    public void onCheckIn(ActionListener l) { checkInBtn.addActionListener(l); }
    public void onCreate(ActionListener l) { createBtn.addActionListener(l); }
}
