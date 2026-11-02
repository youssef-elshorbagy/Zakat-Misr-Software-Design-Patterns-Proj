package presentation;

import lookup.DonationCategory;
import lookup.LookupItem;
import lookup.Skill;
import users.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * MVC View - user control panel.
 *  profilePanel : view / update profile, change password, delete account  (R, U, D)
 *  usersPanel   : admin only - list users, create (RegistrationForm), delete (C, R, D)
 *  lookupPanel  : admin only - CRUD on lookup tables (Donation categories, Skills)
 */
public class UserControlPanelView {

    private final JLabel status = Ui.statusLabel();

    // profile
    private final JLabel idLbl = new JLabel(), roleLbl = new JLabel(), emailLbl = new JLabel();
    private final JTextField nameField = new JTextField(20), phoneField = new JTextField(20);
    private final JPasswordField newPass = new JPasswordField(20);
    private final JButton updateBtn = Ui.button("Update profile"), passBtn = Ui.button("Change password"),
            deleteBtn = Ui.button("Delete my account");

    // users (admin)
    private final DefaultTableModel usersModel = Ui.model("ID", "Role", "Name", "E-mail", "Phone");
    private final JTable usersTable = Ui.table(usersModel);
    private final JButton deleteUserBtn = Ui.button("Delete selected user");
    private final RegistrationForm createForm = new RegistrationForm(true, "Create user");
    private List<User> shownUsers = new ArrayList<>();

    // lookup (admin)
    private final DefaultTableModel lookupModel = Ui.model("ID", "Table", "Code", "Name", "Active", "Details");
    private final JTable lookupTable = Ui.table(lookupModel);
    private final JComboBox<String> lookupType = new JComboBox<>(new String[]{"Donation category", "Skill"});
    private final JTextField lookupCode = new JTextField(10), lookupName = new JTextField(16), lookupExtra = new JTextField(16);
    private final JCheckBox taxDeductible = new JCheckBox("Tax deductible");
    private final JButton lkCreate = Ui.button("Create"), lkRead = Ui.button("Read / refresh"),
            lkUpdate = Ui.button("Update selected"), lkDelete = Ui.button("Delete selected"),
            lkActivate = Ui.button("Activate"), lkDeactivate = Ui.button("Deactivate");
    private List<LookupItem> shownLookup = new ArrayList<>();

    public UserControlPanelView() {
        lookupType.addActionListener(e -> {
            boolean cat = lookupType.getSelectedIndex() == 0;
            taxDeductible.setEnabled(cat);
            lookupExtra.setEnabled(!cat);
        });
        lookupExtra.setEnabled(false);
        Ui.widths(usersTable, 40, 90, 140, 200, 110);
        Ui.widths(lookupTable, 40, 120, 60, 170, 50, 230);
        lookupTable.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int i = Ui.selected(lookupTable);
            if (i < 0 || i >= shownLookup.size()) return;
            LookupItem it = shownLookup.get(i);
            lookupCode.setText(it.getCode());
            lookupName.setText(it.getName());
            if (it instanceof DonationCategory) {
                lookupType.setSelectedIndex(0);
                taxDeductible.setSelected(((DonationCategory) it).isTaxDeductible());
            } else if (it instanceof Skill) {
                lookupType.setSelectedIndex(1);
                lookupExtra.setText(((Skill) it).getDescription());
            }
        });
    }

    // ================= panels =================
    public JPanel buildProfilePanel() {
        Ui.Form info = new Ui.Form();
        info.add("User ID", idLbl);
        info.add("Account type", roleLbl);
        info.add("E-mail", emailLbl);
        info.add("Full name", nameField);
        info.add("Phone", phoneField);
        JPanel profile = new JPanel(new BorderLayout());
        profile.add(info, BorderLayout.CENTER);
        profile.add(Ui.flow(updateBtn), BorderLayout.SOUTH);

        Ui.Form pw = new Ui.Form();
        pw.add("New password", newPass);
        JPanel pwPanel = new JPanel(new BorderLayout());
        pwPanel.add(pw, BorderLayout.CENTER);
        pwPanel.add(Ui.flow(passBtn), BorderLayout.SOUTH);

        JPanel danger = Ui.flow(deleteBtn);
        deleteBtn.setForeground(Ui.ERROR);

        JPanel col = new JPanel();
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.add(Ui.section("My profile", profile));
        col.add(Ui.section("Change password", pwPanel));
        col.add(Ui.section("Delete account", danger));

        JPanel root = new JPanel(new BorderLayout());
        JPanel wrap = new JPanel(new FlowLayout(FlowLayout.LEFT));
        wrap.add(col);
        root.add(wrap, BorderLayout.NORTH);
        root.add(status, BorderLayout.SOUTH);
        return root;
    }

    public JPanel buildUsersPanel() {
        JPanel left = new JPanel(new BorderLayout());
        left.add(Ui.scroll(usersTable, 300), BorderLayout.CENTER);
        left.add(Ui.flow(deleteUserBtn), BorderLayout.SOUTH);
        JPanel root = new JPanel(new GridLayout(1, 2, 10, 0));
        root.add(Ui.section("All users (Read / Delete)", left));
        root.add(Ui.section("Create user", createForm));
        return root;
    }

    public JPanel buildLookupPanel() {
        Ui.Form f = new Ui.Form();
        f.add("Table", lookupType);
        f.add("Code", lookupCode);
        f.add("Name", lookupName);
        f.add("Description", lookupExtra);
        f.add("", taxDeductible);
        JPanel buttons = Ui.flow(lkCreate, lkUpdate, lkDelete, lkRead);
        JPanel buttons2 = Ui.flow(lkActivate, lkDeactivate);
        JPanel right = new JPanel(new BorderLayout());
        JPanel formAndButtons = new JPanel(new BorderLayout());
        formAndButtons.add(f, BorderLayout.CENTER);
        right.add(formAndButtons, BorderLayout.NORTH);
        JPanel bb = new JPanel(new GridLayout(2, 1));
        bb.add(buttons);
        bb.add(buttons2);
        formAndButtons.add(bb, BorderLayout.SOUTH);
        right.setPreferredSize(new Dimension(380, 300));

        JPanel root = new JPanel(new BorderLayout(10, 0));
        root.add(Ui.section("Lookup tables (CRUD)", Ui.scroll(lookupTable, 300)), BorderLayout.CENTER);
        root.add(Ui.section("Item", right), BorderLayout.EAST);
        return root;
    }

    // ================= display (diagram methods) =================
    public void renderProfile(User u) {
        idLbl.setText(String.valueOf(u.getUserId()));
        roleLbl.setText(u.getRole());
        emailLbl.setText(u.getEmail());
        nameField.setText(u.getFullName());
        phoneField.setText(u.getPhone());
    }

    public void showStatusMessage(String msg) { Ui.setStatus(status, msg, false); }
    public void showError(String msg) { Ui.setStatus(status, msg, true); }

    public boolean confirm(String msg) { return Ui.confirm(null, msg); }

    public void displayUsers(List<User> users) {
        shownUsers = new ArrayList<>(users);
        usersModel.setRowCount(0);
        for (User u : users) usersModel.addRow(new Object[]{u.getUserId(), u.getRole(), u.getFullName(), u.getEmail(), u.getPhone()});
    }

    public void displayLookup(List<LookupItem> items) {
        shownLookup = new ArrayList<>(items);
        lookupModel.setRowCount(0);
        for (LookupItem i : items) {
            String table = i instanceof DonationCategory ? "Donation category" : "Skill";
            String details = i instanceof DonationCategory ? "tax deductible: " + ((DonationCategory) i).isTaxDeductible()
                    : ((Skill) i).getDescription();
            lookupModel.addRow(new Object[]{i.getId(), table, i.getCode(), i.getName(), i.isActive() ? "yes" : "no", details});
        }
    }

    // ================= getters =================
    public String getNameInput() { return nameField.getText(); }
    public String getPhoneInput() { return phoneField.getText(); }
    public String getNewPassword() { return new String(newPass.getPassword()); }
    public void clearPassword() { newPass.setText(""); }
    public RegistrationForm getCreateForm() { return createForm; }
    public User getSelectedUser() { int i = Ui.selected(usersTable); return i < 0 ? null : shownUsers.get(i); }
    public LookupItem getSelectedLookup() { int i = Ui.selected(lookupTable); return i < 0 ? null : shownLookup.get(i); }
    public boolean isCategorySelected() { return lookupType.getSelectedIndex() == 0; }
    public String getLookupCode() { return lookupCode.getText().trim(); }
    public String getLookupName() { return lookupName.getText().trim(); }
    public String getLookupExtra() { return lookupExtra.getText().trim(); }
    public boolean isTaxDeductible() { return taxDeductible.isSelected(); }

    // ================= listeners =================
    public void onUpdateProfile(ActionListener l) { updateBtn.addActionListener(l); }
    public void onChangePassword(ActionListener l) { passBtn.addActionListener(l); }
    public void onDeleteAccount(ActionListener l) { deleteBtn.addActionListener(l); }
    public void onDeleteUser(ActionListener l) { deleteUserBtn.addActionListener(l); }
    public void onLookupCreate(ActionListener l) { lkCreate.addActionListener(l); }
    public void onLookupRead(ActionListener l) { lkRead.addActionListener(l); }
    public void onLookupUpdate(ActionListener l) { lkUpdate.addActionListener(l); }
    public void onLookupDelete(ActionListener l) { lkDelete.addActionListener(l); }
    public void onLookupActivate(ActionListener l) { lkActivate.addActionListener(l); }
    public void onLookupDeactivate(ActionListener l) { lkDeactivate.addActionListener(l); }
}
