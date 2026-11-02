package presentation;

import lookup.Skill;
import users.Volunteer;
import volunteer.VolunteerAssignment;
import volunteer.VolunteerTask;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** MVC View - volunteers apply for tasks and log hours; admins create tasks and match volunteers. */
public class VolunteerView extends JPanel {

    private final boolean admin;
    private final DefaultTableModel tasksModel = Ui.model("ID", "Task", "Required skills", "Date", "Slots", "Status");
    private final JTable tasksTable = Ui.table(tasksModel);
    private final DefaultTableModel asgModel;
    private final JTable asgTable;
    private final JTextArea output = Ui.textArea(3);
    private final JLabel status = Ui.statusLabel();
    private List<VolunteerTask> shownTasks = new ArrayList<>();
    private List<VolunteerAssignment> shownAsg = new ArrayList<>();

    // volunteer mode
    private final JLabel skillsLbl = new JLabel(" ");
    private final JComboBox<Skill> skillChoice = new JComboBox<>();
    private final JTextField availability = new JTextField(16);
    private final JButton addSkillBtn = Ui.button("Add skill"), addAvailBtn = Ui.button("Add availability"),
            applyBtn = Ui.button("Apply for selected task"), hoursBtn = Ui.button("Record hours"),
            cancelAsgBtn = Ui.button("Cancel selected assignment"), certBtn = Ui.button("Request certificate");
    private final JSpinner hours = new JSpinner(new SpinnerNumberModel(1.0, 0.5, 24.0, 0.5));

    // admin mode
    private final JTextField taskTitle = new JTextField(16), taskDate = new JTextField(14);
    private final JSpinner slots = new JSpinner(new SpinnerNumberModel(3, 1, 500, 1));
    private final DefaultListModel<Skill> skillListModel = new DefaultListModel<>();
    private final JList<Skill> skillList = new JList<>(skillListModel);
    private final JButton createTaskBtn = Ui.button("Create task"), matchBtn = Ui.button("Show matching volunteers");

    public VolunteerView(boolean admin) {
        super(new BorderLayout(10, 10));
        this.admin = admin;
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        asgModel = admin ? Ui.model("ID", "Volunteer", "Task", "Status", "Hours")
                         : Ui.model("ID", "Task", "Status", "Hours");
        asgTable = Ui.table(asgModel);
        Ui.widths(tasksTable, 40, 200, 150, 130, 60, 70);
        if (admin) Ui.widths(asgTable, 40, 130, 200, 90, 60); else Ui.widths(asgTable, 40, 260, 90, 60);
        skillList.setVisibleRowCount(4);
        skillChoice.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean f) {
                return super.getListCellRendererComponent(l, v instanceof Skill ? ((Skill) v).getName() : v, i, s, f);
            }
        });
        skillList.setCellRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean f) {
                return super.getListCellRendererComponent(l, v instanceof Skill ? ((Skill) v).getName() : v, i, s, f);
            }
        });

        JPanel north = new JPanel(new BorderLayout());
        if (admin) {
            Ui.Form f = new Ui.Form();
            f.add("Task title", taskTitle);
            f.add("Date (" + Ui.DATE_HINT + ")", taskDate);
            f.add("Slots", slots);
            f.add("Required skills", new JScrollPane(skillList));
            north.add(f, BorderLayout.CENTER);
            north.add(Ui.flow(createTaskBtn), BorderLayout.SOUTH);
            north.setPreferredSize(new Dimension(500, 215));
            add(Ui.section("Create volunteer task", north), BorderLayout.WEST);
        } else {
            JPanel p = new JPanel(new GridLayout(3, 1));
            p.add(skillsLbl);
            p.add(Ui.flow(new JLabel("Skill:"), skillChoice, addSkillBtn));
            p.add(Ui.flow(new JLabel("Available (e.g. Saturday 10:00-16:00):"), availability, addAvailBtn));
            add(Ui.section("My skills and availability", p), BorderLayout.NORTH);
        }

        JPanel tasks = new JPanel(new BorderLayout());
        tasks.add(Ui.scroll(tasksTable, 110), BorderLayout.CENTER);
        tasks.add(admin ? Ui.flow(matchBtn) : Ui.flow(applyBtn), BorderLayout.SOUTH);
        JPanel asg = new JPanel(new BorderLayout());
        asg.add(Ui.scroll(asgTable, 90), BorderLayout.CENTER);
        asg.add(admin ? Ui.flow(cancelAsgBtn)
                      : Ui.flow(new JLabel("Hours:"), hours, hoursBtn, cancelAsgBtn, certBtn), BorderLayout.SOUTH);

        JPanel center = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0; gc.fill = GridBagConstraints.BOTH; gc.weightx = 1; gc.insets = new Insets(0, 0, 6, 0);
        gc.gridy = 0; gc.weighty = 0.45; center.add(Ui.section("Volunteer tasks", tasks), gc);
        gc.gridy = 1; gc.weighty = 0.35; center.add(Ui.section(admin ? "All assignments" : "My assignments", asg), gc);
        gc.gridy = 2; gc.weighty = 0.2;  center.add(Ui.section(admin ? "Matching volunteers" : "Certificates", new JScrollPane(output)), gc);
        add(center, BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);
    }

    // ---- diagram methods ----
    public void displayAvailableTasks(List<VolunteerTask> tasks) {
        shownTasks = new ArrayList<>(tasks);
        tasksModel.setRowCount(0);
        for (VolunteerTask t : tasks) {
            String skills = t.getRequiredSkills().isEmpty() ? "-"
                    : t.getRequiredSkills().stream().map(Skill::getName).collect(Collectors.joining(", "));
            tasksModel.addRow(new Object[]{t.getTaskId(), t.getTitle(), skills, Ui.fmt(t.getTaskDate()),
                    t.getFilledSlots() + "/" + t.getMaxSlots(), t.getStatus()});
        }
    }

    public void displayCertificate(String cert) {
        output.setText("*********************************************\n" + cert
                + "\n*********************************************");
        output.setCaretPosition(0);
    }

    // ---- extra ----
    public void displayAssignments(List<VolunteerAssignment> list) {
        shownAsg = new ArrayList<>(list);
        asgModel.setRowCount(0);
        for (VolunteerAssignment a : list) {
            if (admin) asgModel.addRow(new Object[]{a.getAssignmentId(), a.getVolunteer().getFullName(), a.getTask().getTitle(),
                    a.getStatus(), a.getHoursWorked()});
            else asgModel.addRow(new Object[]{a.getAssignmentId(), a.getTask().getTitle(), a.getStatus(), a.getHoursWorked()});
        }
    }

    public void displayProfile(Volunteer v) {
        String skills = v.getSkills().isEmpty() ? "none yet"
                : v.getSkills().stream().map(Skill::getName).collect(Collectors.joining(", "));
        skillsLbl.setText("  Skills: " + skills + "   |   Availability: "
                + (v.getAvailability().isEmpty() ? "not set" : String.join("; ", v.getAvailability()))
                + "   |   Total hours: " + v.getTotalHours());
    }

    public void displayMatches(VolunteerTask t, List<Volunteer> matches) {
        StringBuilder sb = new StringBuilder("Volunteers matching '" + t.getTitle() + "':\n");
        if (matches.isEmpty()) sb.append("(none)\n");
        for (Volunteer v : matches) sb.append(" - ").append(v.getFullName()).append(" <").append(v.getEmail()).append(">\n");
        output.setText(sb.toString());
    }

    public void setSkillChoices(List<Skill> skills) {
        skillChoice.removeAllItems();
        for (Skill s : skills) skillChoice.addItem(s);
        skillListModel.clear();
        for (Skill s : skills) skillListModel.addElement(s);
    }

    public void showStatusMessage(String msg) { Ui.setStatus(status, msg, false); }
    public void showError(String msg) { Ui.setStatus(status, msg, true); }
    public boolean confirm(String msg) { return Ui.confirm(this, msg); }
    public void clearCreateForm() { taskTitle.setText(""); taskDate.setText(""); skillList.clearSelection(); }

    // ---- getters ----
    public VolunteerTask getSelectedTask() { int i = Ui.selected(tasksTable); return i < 0 || i >= shownTasks.size() ? null : shownTasks.get(i); }
    public VolunteerAssignment getSelectedAssignment() { int i = Ui.selected(asgTable); return i < 0 || i >= shownAsg.size() ? null : shownAsg.get(i); }
    public Skill getChosenSkill() { return (Skill) skillChoice.getSelectedItem(); }
    public String getAvailabilityInput() { return availability.getText().trim(); }
    public void clearAvailability() { availability.setText(""); }
    public double getHours() { return ((Number) hours.getValue()).doubleValue(); }
    public String getTaskTitleInput() { return taskTitle.getText(); }
    public String getTaskDateInput() { return taskDate.getText(); }
    public int getSlotsInput() { return (Integer) slots.getValue(); }
    public List<Skill> getSelectedSkills() { return new ArrayList<>(skillList.getSelectedValuesList()); }

    // ---- listeners ----
    public void onAddSkill(ActionListener l) { addSkillBtn.addActionListener(l); }
    public void onAddAvailability(ActionListener l) { addAvailBtn.addActionListener(l); }
    public void onApply(ActionListener l) { applyBtn.addActionListener(l); }
    public void onRecordHours(ActionListener l) { hoursBtn.addActionListener(l); }
    public void onCancelAssignment(ActionListener l) { cancelAsgBtn.addActionListener(l); }
    public void onCertificate(ActionListener l) { certBtn.addActionListener(l); }
    public void onCreateTask(ActionListener l) { createTaskBtn.addActionListener(l); }
    public void onMatch(ActionListener l) { matchBtn.addActionListener(l); }
}
