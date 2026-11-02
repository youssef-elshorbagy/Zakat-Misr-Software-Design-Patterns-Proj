package presentation;

import users.Registration;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/** Reusable registration form (used by the login screen and by the admin's "create user"). */
public class RegistrationForm extends JPanel {

    private final JComboBox<String> role;
    private final JTextField name = new JTextField(18), email = new JTextField(18), phone = new JTextField(18);
    private final JPasswordField password = new JPasswordField(18);
    private final JTextField address = new JTextField(18);
    private final JComboBox<String> channel = new JComboBox<>(new String[]{"email", "sms", "social"});
    private final JTextField nationalId = new JTextField(18);
    private final JSpinner familySize = new JSpinner(new SpinnerNumberModel(1, 1, 30, 1));
    private final JTextField income = new JTextField("0");
    private final JSpinner roleLevel = new JSpinner(new SpinnerNumberModel(1, 1, 5, 1));
    private final CardLayout cards = new CardLayout();
    private final JPanel roleCards = new JPanel(cards);
    private final JButton submit;

    public RegistrationForm(boolean allowAdmin, String submitText) {
        super(new BorderLayout(0, 6));
        role = new JComboBox<>(allowAdmin
                ? new String[]{"Donor", "Volunteer", "Beneficiary", "Admin"}
                : new String[]{"Donor", "Volunteer", "Beneficiary"});
        Ui.Form common = new Ui.Form();
        common.add("Account type", role);
        common.add("Full name", name);
        common.add("E-mail", email);
        common.add("Phone", phone);
        common.add("Password (6+)", password);

        Ui.Form donor = new Ui.Form();
        donor.add("Address", address);
        donor.add("Notify me by", channel);
        Ui.Form ben = new Ui.Form();
        ben.add("National ID (14 digits)", nationalId);
        ben.add("Family size", familySize);
        ben.add("Monthly income (EGP)", income);
        Ui.Form admin = new Ui.Form();
        admin.add("Role level", roleLevel);
        roleCards.add(new JPanel(), "Volunteer");
        roleCards.add(donor, "Donor");
        roleCards.add(ben, "Beneficiary");
        roleCards.add(admin, "Admin");
        role.addActionListener(e -> cards.show(roleCards, (String) role.getSelectedItem()));
        cards.show(roleCards, "Donor");

        submit = Ui.button(submitText);
        add(common, BorderLayout.NORTH);
        add(roleCards, BorderLayout.CENTER);
        add(Ui.flow(submit), BorderLayout.SOUTH);
    }

    public void onSubmit(ActionListener l) { submit.addActionListener(l); }

    /** Reads the form into a plain Registration object (parsing errors are reported as IllegalArgumentException). */
    public Registration read() {
        Registration r = new Registration();
        r.role = (String) role.getSelectedItem();
        r.fullName = name.getText();
        r.email = email.getText();
        r.phone = phone.getText();
        r.password = new String(password.getPassword());
        r.address = address.getText();
        r.preferredChannel = (String) channel.getSelectedItem();
        r.nationalId = nationalId.getText().trim();
        r.familySize = (Integer) familySize.getValue();
        try {
            r.monthlyIncome = Double.parseDouble(income.getText().trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Monthly income must be a number");
        }
        r.roleLevel = (Integer) roleLevel.getValue();
        return r;
    }

    public void clear() {
        name.setText(""); email.setText(""); phone.setText(""); password.setText("");
        address.setText(""); nationalId.setText(""); income.setText("0"); familySize.setValue(1);
    }
}
