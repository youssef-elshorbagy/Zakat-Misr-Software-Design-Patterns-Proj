package presentation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/** MVC View - login / register screen. */
public class AuthView extends JPanel {

    private final JTextField email = new JTextField(20);
    private final JPasswordField password = new JPasswordField(20);
    private final JButton loginBtn = Ui.button("Log in");
    private final JLabel status = Ui.statusLabel();
    private final RegistrationForm registration = new RegistrationForm(false, "Create account");

    public AuthView() {
        super(new GridBagLayout());
        JLabel title = new JLabel("Zakat Misr", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 30f));
        title.setForeground(Ui.GREEN);
        JLabel sub = new JLabel("Donations, volunteering, events and aid - in one place", SwingConstants.CENTER);

        Ui.Form login = new Ui.Form();
        login.add("E-mail", email);
        login.add("Password", password);
        JPanel loginTop = new JPanel(new BorderLayout());
        loginTop.add(login, BorderLayout.NORTH);
        loginTop.add(Ui.flow(loginBtn), BorderLayout.CENTER);
        JLabel hint = new JLabel("<html><small>Demo accounts: admin@zakatmisr.org / admin123 &nbsp;|&nbsp; ahmed@mail.com / pass1234"
                + "<br>sara@mail.com / sara2026 (volunteer) &nbsp;|&nbsp; fatma@mail.com / fatma2026 (beneficiary)</small></html>");
        loginTop.add(hint, BorderLayout.SOUTH);
        JPanel loginPanel = new JPanel(new BorderLayout());
        loginPanel.add(loginTop, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Log in", loginPanel);
        tabs.addTab("Register", registration);

        JPanel box = new JPanel(new BorderLayout(0, 8));
        box.add(title, BorderLayout.NORTH);
        JPanel mid = new JPanel(new BorderLayout(0, 8));
        mid.add(sub, BorderLayout.NORTH);
        mid.add(tabs, BorderLayout.CENTER);
        mid.add(status, BorderLayout.SOUTH);
        box.add(mid, BorderLayout.CENTER);
        box.setPreferredSize(new Dimension(500, 420));
        add(box);
        getRootPane();
    }

    public String getEmail() { return email.getText(); }
    public String getPassword() { return new String(password.getPassword()); }
    public RegistrationForm getRegistrationForm() { return registration; }

    public void onLogin(ActionListener l) {
        loginBtn.addActionListener(l);
        password.addActionListener(l);
    }

    public void showStatusMessage(String msg) { Ui.setStatus(status, msg, false); }
    public void showError(String msg) { Ui.setStatus(status, msg, true); }

    public void reset() {
        email.setText(""); password.setText(""); status.setText(" ");
    }
}
