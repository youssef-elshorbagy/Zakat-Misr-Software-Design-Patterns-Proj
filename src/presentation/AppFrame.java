package presentation;

import beneficiary.BeneficiaryService;
import donation.DonationCreatorRegistry;
import event.EventService;
import lookup.LookupService;
import observer.Donation.DonationService;
import users.Admin;
import users.Beneficiary;
import users.Donor;
import users.User;
import users.UserService;
import users.Volunteer;
import volunteer.VolunteerService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Application window. Shows the login screen, then a role-based set of tabs.
 * It only wires views to controllers - all rules live in the services/models.
 */
public class AppFrame extends JFrame {

    private final UserService userService;
    private final DonationService donationService;
    private final DonationCreatorRegistry donationCreators;
    private final VolunteerService volunteerService;
    private final EventService eventService;
    private final BeneficiaryService beneficiaryService;
    private final LookupService lookupService;

    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);
    private final AuthView authView = new AuthView();
    private final JPanel mainPanel = new JPanel(new BorderLayout());
    private JTabbedPane tabs;
    private final List<Runnable> refreshers = new ArrayList<>();
    private User currentUser;

    public AppFrame(UserService userService, DonationService donationService, VolunteerService volunteerService,
                    EventService eventService, BeneficiaryService beneficiaryService, LookupService lookupService,
                    DonationCreatorRegistry donationCreators) {
        super("Zakat Misr");
        this.userService = userService;
        this.donationService = donationService;
        this.donationCreators = donationCreators;
        this.volunteerService = volunteerService;
        this.eventService = eventService;
        this.beneficiaryService = beneficiaryService;
        this.lookupService = lookupService;

        new AuthController(userService, authView, this::showMain);
        root.add(authView, "auth");
        root.add(mainPanel, "main");
        setContentPane(root);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1120, 740);
        setMinimumSize(new Dimension(960, 640));
        setLocationRelativeTo(null);
        cards.show(root, "auth");
    }


    public JTabbedPane getTabs() { return tabs; }
    public AuthView getAuthView() { return authView; }

    private void showLogin() {
        currentUser = null;
        cards.show(root, "auth");
        setTitle("Zakat Misr");
    }

    private void showMain(User user) {
        currentUser = user;
        mainPanel.removeAll();
        refreshers.clear();
        tabs = new JTabbedPane();

        // ---- header ----
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Ui.GREEN);
        header.setBorder(new EmptyBorder(8, 14, 8, 14));
        JLabel brand = new JLabel("Zakat Misr");
        brand.setForeground(Color.WHITE);
        brand.setFont(brand.getFont().deriveFont(Font.BOLD, 20f));
        JLabel who = new JLabel(user.getFullName() + "  (" + user.getRole() + ")   ");
        who.setForeground(Color.WHITE);
        JButton logout = Ui.button("Log out");
        logout.addActionListener(e -> showLogin());
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        right.setOpaque(false);
        right.add(who);
        right.add(logout);
        header.add(brand, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);

        // ---- tabs by role ----
        UserControlPanelView panelView = new UserControlPanelView();
        UserControlPanelController panelController = new UserControlPanelController(user, panelView, lookupService, userService);
        panelController.setOnAccountDeleted(() -> {
            Ui.info(this, "Your account was deleted.");
            showLogin();
        });

        if (user instanceof Donor) {
            DonationView dv = new DonationView();
            DonationController dc = new DonationController(donationService, dv, donationCreators);
            dc.start((Donor) user, lookupService);
            addTab("Donate", dv, null);
            addEventsTab(user, false);
        } else if (user instanceof Volunteer) {
            addVolunteerTab(user, false);
            addEventsTab(user, false);
        } else if (user instanceof Beneficiary) {
            BeneficiaryView bv = new BeneficiaryView(false);
            BeneficiaryController bc = new BeneficiaryController(beneficiaryService, bv);
            bc.start(user, donationService);
            addTab("My aid", bv, bc::refresh);
            addEventsTab(user, false);
        } else if (user instanceof Admin) {
            addTab("Users", panelView.buildUsersPanel(), panelController::refreshUsers);
            addTab("Lookup tables", panelView.buildLookupPanel(), () -> panelController.manageLookupTable(null, "read"));
            addEventsTab(user, true);
            addVolunteerTab(user, true);
            BeneficiaryView bv = new BeneficiaryView(true);
            BeneficiaryController bc = new BeneficiaryController(beneficiaryService, bv);
            bc.start(user, donationService);
            addTab("Donations & aid", bv, bc::refresh);
        }
        addTab("My profile", panelView.buildProfilePanel(), null);

        tabs.addChangeListener(e -> {
            int i = tabs.getSelectedIndex();
            if (i >= 0 && i < refreshers.size() && refreshers.get(i) != null) refreshers.get(i).run();
        });

        mainPanel.add(header, BorderLayout.NORTH);
        mainPanel.add(tabs, BorderLayout.CENTER);
        setTitle("Zakat Misr - " + user.getFullName());
        cards.show(root, "main");
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private void addTab(String title, JComponent c, Runnable refresh) {
        tabs.addTab(title, c);
        refreshers.add(refresh);
    }

    private void addEventsTab(User user, boolean admin) {
        EventView ev = new EventView(admin);
        EventController ec = new EventController(eventService, ev);
        ec.start(user);
        addTab(admin ? "Events (manage)" : "Events", ev, ec::refresh);
    }

    private void addVolunteerTab(User user, boolean admin) {
        VolunteerView vv = new VolunteerView(admin);
        VolunteerController vc = new VolunteerController(volunteerService, vv);
        vc.start(user, lookupService);
        addTab(admin ? "Volunteer tasks" : "Volunteering", vv, vc::refresh);
    }
}
