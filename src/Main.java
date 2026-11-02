import beneficiary.BeneficiaryService;
import beneficiary.Need;
import core.DatabaseManager;
import event.Event;
import event.EventService;
import lookup.DonationCategory;
import lookup.LookupService;
import lookup.Skill;
import observer.DonationService;
import observer.NotificationObserver;
import presentation.AppFrame;
import presentation.Ui;
import users.Beneficiary;
import users.Donor;
import users.Registration;
import users.User;
import users.UserService;
import users.Volunteer;
import volunteer.VolunteerService;

import javax.swing.*;
import java.util.Arrays;
import java.util.Date;

/** Entry point: builds the services, loads demo data and opens the Swing UI. */
public class Main {

    public static void main(String[] args) {
        DatabaseManager.getInstance();                                   // SINGLETON
        VolunteerService volunteerService = new VolunteerService();
        BeneficiaryService beneficiaryService = new BeneficiaryService();
        UserService userService = new UserService(volunteerService, beneficiaryService);
        DonationService donationService = new DonationService();
        EventService eventService = new EventService();
        LookupService lookupService = new LookupService();

        donationService.attach(new NotificationObserver());              // OBSERVER (-> STRATEGY per donor)
        seed(userService, donationService, volunteerService, eventService, beneficiaryService, lookupService);

        SwingUtilities.invokeLater(() -> {
            Ui.installLookAndFeel();
            new AppFrame(userService, donationService, volunteerService, eventService, beneficiaryService, lookupService)
                    .setVisible(true);
        });
    }

    private static Registration reg(String role, String name, String email, String phone, String pass) {
        Registration r = new Registration();
        r.role = role; r.fullName = name; r.email = email; r.phone = phone; r.password = pass;
        return r;
    }

    static void seed(UserService users, DonationService donations, VolunteerService volunteers, EventService events,
                     BeneficiaryService beneficiaries, LookupService lookup) {
        // lookup tables
        lookup.create(new DonationCategory(1, "ZKT", "Zakat Al-Mal", true));
        lookup.create(new DonationCategory(2, "SDQ", "Sadaqah Jariyah", false));
        lookup.create(new DonationCategory(3, "RMD", "Ramadan Food Baskets", false));
        Skill teaching = new Skill(4, "TCH", "Teaching", "Teaching and tutoring");
        lookup.create(teaching);
        lookup.create(new Skill(5, "FAD", "First Aid", "Basic medical first aid"));
        lookup.create(new Skill(6, "DRV", "Driving", "Delivering food and supplies"));

        // users
        Registration admin = reg("Admin", "System Admin", "admin@zakatmisr.org", "0227777777", "admin123");
        users.createAccount(admin);

        Registration a = reg("Donor", "Ahmed Ali", "ahmed@mail.com", "01012345678", "pass1234");
        a.address = "Cairo"; a.preferredChannel = "sms";
        Donor ahmed = (Donor) users.createAccount(a);
        Registration m = reg("Donor", "Mona Hassan", "mona@mail.com", "01198765432", "secret99");
        m.address = "Giza"; m.preferredChannel = "email";
        Donor mona = (Donor) users.createAccount(m);

        Volunteer sara = (Volunteer) users.createAccount(reg("Volunteer", "Sara Youssef", "sara@mail.com", "01011112222", "sara2026"));
        sara.addSkill(teaching);
        sara.addAvailability("Saturday 10:00-16:00");
        users.createAccount(reg("Volunteer", "Layla Adel", "layla@mail.com", "01055556666", "layla2026"));

        Registration f = reg("Beneficiary", "Fatma Mahmoud", "fatma@mail.com", "01077778888", "fatma2026");
        f.nationalId = "29001011234567"; f.familySize = 5; f.monthlyIncome = 3000;
        Beneficiary fatma = (Beneficiary) users.createAccount(f);
        beneficiaries.registerNeed(fatma, new Need(beneficiaries.nextNeedId(), "Medical", "HIGH", 4000));
        beneficiaries.registerNeed(fatma, new Need(beneficiaries.nextNeedId(), "Food", "MEDIUM", 1500));

        // donations
        donations.processDonation("zakat", ahmed, 5000);
        donations.processDonation("sadaqah", mona, 300, "Ramadan Food Baskets", 1, false, 0);

        // events
        long day = 24L * 3600 * 1000;
        Event iftar = new Event(events.nextEventId(), "Ramadan Charity Iftar", "Fundraiser",
                new Date(System.currentTimeMillis() + 14 * day), "Cairo Opera House Garden", 100);
        events.createEvent(iftar);
        events.openRegistration(iftar);
        Event workshop = new Event(events.nextEventId(), "Volunteer Orientation", "Workshop",
                new Date(System.currentTimeMillis() + 5 * day), "Giza Community Hall", 30);
        events.createEvent(workshop);
        events.openRegistration(workshop);

        // volunteer tasks
        volunteers.createTask("Teach kids at the orphanage", Arrays.asList(teaching),
                new Date(System.currentTimeMillis() + 3 * day), 2);
        volunteers.createTask("Pack food baskets", Arrays.<Skill>asList(),
                new Date(System.currentTimeMillis() + 7 * day), 10);
    }
}
