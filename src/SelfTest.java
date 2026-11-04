import donation.DonationCreatorRegistry;
import donation.ZakatDonationCreator;
import donation.SadaqahDonationCreator;
import donation.InKindDonationCreator;
import beneficiary.AidDistribution;
import beneficiary.BeneficiaryService;
import beneficiary.Need;
import donation.Donation;
import event.Event;
import event.EventRegistration;
import event.EventService;
import lookup.DonationCategory;
import lookup.LookupService;
import lookup.Skill;
import observer.NotificationObserver;
import observer.Donation.DonationService;
import users.Beneficiary;
import users.Donor;
import users.Registration;
import users.User;
import users.UserService;
import users.Volunteer;
import volunteer.VolunteerAssignment;
import volunteer.VolunteerService;
import volunteer.VolunteerTask;

import java.util.Arrays;
import java.util.Date;

/** Headless checks of the business rules (no UI). Run:  java SelfTest */
public class SelfTest {

    static int passed = 0, failed = 0;

    static void check(String name, boolean ok) {
        if (ok) passed++; else failed++;
        System.out.println((ok ? "  PASS  " : "  FAIL  ") + name);
    }

    static boolean throwsIllegal(Runnable r) {
        try { r.run(); return false; } catch (IllegalArgumentException | IllegalStateException e) { return true; }
    }

    static Registration reg(String role, String email) {
        Registration r = new Registration();
        r.role = role; r.fullName = "Test " + role; r.email = email; r.phone = "01000000000"; r.password = "secret1";
        r.address = "Cairo"; r.preferredChannel = "sms";
        r.nationalId = "29001011234567"; r.familySize = 4; r.monthlyIncome = 2000;
        return r;
    }

    public static void main(String[] args) {
        VolunteerService vs = new VolunteerService();
        BeneficiaryService bs = new BeneficiaryService();
        UserService users = new UserService(vs, bs);
        DonationService ds = new DonationService();
        EventService es = new EventService();
        LookupService lookup = new LookupService();
        ds.attach(new NotificationObserver());
        final int[] notifications = {0};
        ds.attach(donation -> notifications[0]++);

        System.out.println("\n== User CRUD ==");
        User donor = users.createAccount(reg("Donor", "d@x.com"));
        check("create donor", users.getAll().size() == 1);
        check("duplicate e-mail rejected", throwsIllegal(() -> users.createAccount(reg("Donor", "D@X.com"))));
        Registration bad = reg("Donor", "bad"); 
        check("invalid e-mail rejected", throwsIllegal(() -> users.createAccount(bad)));
        check("login ok", users.authenticate("d@x.com", "secret1") == donor);
        check("login wrong password", users.authenticate("d@x.com", "nope") == null);
        donor.updateProfile("New Name", "01111111111");
        users.update(donor);
        check("update profile", donor.getFullName().equals("New Name"));
        check("short password rejected", throwsIllegal(() -> donor.changePassword("123")));
        donor.changePassword("newpass1");
        check("new password works", users.authenticate("d@x.com", "newpass1") == donor);
        User tmp = users.createAccount(reg("Volunteer", "v@x.com"));
        users.delete(tmp);
        check("delete user (also from volunteer registry)", users.getAll().size() == 1 && vs.getVolunteers().isEmpty());

        System.out.println("\n== Lookup CRUD ==");
        lookup.create(new DonationCategory(1, "ZKT", "Zakat", true));
        Skill teach = new Skill(2, "TCH", "Teaching", "tutoring");
        lookup.create(teach);
        check("create + read", lookup.readAll().size() == 2);
        check("duplicate id rejected", throwsIllegal(() -> lookup.create(new Skill(2, "X", "X", "x"))));
        lookup.update(new Skill(2, "TCH", "Teaching & Tutoring", "tutoring"));
        check("update in place (same object)", teach.getName().equals("Teaching & Tutoring"));
        lookup.delete(1);
        check("delete", lookup.readAll().size() == 1);
        check("delete missing rejected", throwsIllegal(() -> lookup.delete(99)));

        DonationCreatorRegistry creators = new DonationCreatorRegistry();
        ZakatDonationCreator zakatCreator = new ZakatDonationCreator();
        creators.register("Zakat", zakatCreator);
        creators.register("Sadaqah", new SadaqahDonationCreator());
        creators.register("In-kind", new InKindDonationCreator());
        check("registry returns registered creator", creators.getCreator("Zakat") == zakatCreator);
        check("registry retains registration order", creators.getTypes().equals(Arrays.asList("Zakat", "Sadaqah", "In-kind")));
        check("registry rejects unknown type", throwsIllegal(() -> creators.getCreator("unknown")));
        check("registry rejects duplicate type", throwsIllegal(() -> creators.register("Zakat", new ZakatDonationCreator())));
        creators.getTypes().clear();
        check("registry types cannot change registrations", creators.getTypes().size() == 3);

        System.out.println("\n== Donations (Factory / Decorator / Observer / Strategy) ==");
        Donor d = (Donor) donor;
        Donation z = ds.processDonation(new ZakatDonationCreator(), d, 5000);
        Donation s = ds.processDonation(new SadaqahDonationCreator(), d, 300, "Ramadan", 1, true, 3);
        Donation k = ds.processDonation(new InKindDonationCreator(), d, 800, "Blankets", 10, false, 0);
        check("history has 3", d.getDonationHistory().size() == 3);
        check("anonymous hides name", !s.getDescription().contains(d.getFullName()) && s.getDescription().contains("Anonymous"));
        check("recurring in description", s.getDescription().contains("Recurring every 3"));
        check("in-kind item and quantity", k.getDescription().contains("10 x Blankets"));
        check("negative amount rejected", throwsIllegal(() -> ds.processDonation(new ZakatDonationCreator(), d, -5)));
        check("missing creator rejected", throwsIllegal(() -> ds.processDonation(null, d, 5)));
        check("all donation types share history", ds.getAllDonations().size() == 3);
        check("observer notified for every donation type", notifications[0] == 3);
        check("zakat creator creates zakat", z instanceof donation.ZakatDonation);
        check("invalid in-kind quantity rejected", throwsIllegal(() -> ds.processDonation(new InKindDonationCreator(), d, 5, "Goods", 0, false, 0)));

        System.out.println("\n== Volunteers ==");
        Volunteer sara = (Volunteer) users.createAccount(reg("Volunteer", "s@x.com"));
        Volunteer hany = (Volunteer) users.createAccount(reg("Volunteer", "h@x.com"));
        sara.addSkill(teach);
        VolunteerTask t = vs.createTask("Teach", Arrays.asList(teach), new Date(), 1);
        check("match by skill", vs.matchVolunteers(t).size() == 1);
        VolunteerAssignment a = vs.assign(sara, t);
        a.confirm();
        check("task full after assignment", t.isFull());
        check("full task rejected", throwsIllegal(() -> vs.assign(sara, t)));
        a.cancel();
        check("cancel frees the slot", !t.isFull());
        check("missing skill rejected", throwsIllegal(() -> vs.assign(hany, t)));
        VolunteerAssignment a2 = vs.assign(sara, t);
        a2.confirm();
        check("duplicate application rejected", throwsIllegal(() -> vs.assign(sara, t)));
        sara.logHours(a2, 12);
        check("certificate after 10h", sara.getCertificates().size() == 1);
        check("invalid hours rejected", throwsIllegal(() -> sara.logHours(a2, 0)));

        System.out.println("\n== Events ==");
        Event e = new Event(es.nextEventId(), "Iftar", "Fundraiser", new Date(), "Cairo", 1);
        es.createEvent(e);
        check("register before open rejected", throwsIllegal(() -> es.registerAttendee(e, donor)));
        es.openRegistration(e);
        EventRegistration r1 = es.registerAttendee(e, donor);
        check("duplicate registration rejected", throwsIllegal(() -> es.registerAttendee(e, donor)));
        check("full event rejected", throwsIllegal(() -> es.registerAttendee(e, sara)));
        es.checkInAttendee(r1);
        check("check-in", r1.isAttended());
        es.cancelRegistration(r1);
        check("cancel registration frees seat", !e.isFull());
        check("check-in after cancel rejected", throwsIllegal(() -> es.checkInAttendee(r1)));
        es.cancelEvent(e);
        check("cancelled event cannot open", throwsIllegal(() -> es.openRegistration(e)));

        System.out.println("\n== Beneficiaries & aid ==");
        Beneficiary fatma = (Beneficiary) users.createAccount(reg("Beneficiary", "f@x.com"));
        Registration rich = reg("Beneficiary", "r@x.com"); rich.monthlyIncome = 50000;
        Beneficiary rb = (Beneficiary) users.createAccount(rich);
        check("eligible", bs.verifyEligibility(fatma));
        check("not eligible", !bs.verifyEligibility(rb));
        Need medical = new Need(bs.nextNeedId(), "Medical", "HIGH", 4000);
        Need food = new Need(bs.nextNeedId(), "Food", "LOW", 1500);
        bs.registerNeed(fatma, medical);
        bs.registerNeed(fatma, food);
        check("ineligible cannot submit", throwsIllegal(() -> bs.registerNeed(rb, new Need(99, "Food", "LOW", 10))));
        AidDistribution aid1 = bs.distributeAid(z, medical);
        check("need fully funded", medical.isFulfilled() && aid1.getAmount() == 4000);
        check("donation remainder tracked", bs.getAvailable(z) == 1000);
        AidDistribution aid2 = bs.distributeAid(z, food);
        check("remainder used for partial aid", aid2.getAmount() == 1000 && !food.isFulfilled() && food.getRemaining() == 500);
        check("donation cannot be reused", throwsIllegal(() -> bs.distributeAid(z, food)));
        bs.distributeAid(k, food);
        check("second donation completes need", food.isFulfilled());
        check("fulfilled need rejected", throwsIllegal(() -> bs.distributeAid(s, food)));
        check("impact report", bs.evaluateImpact(fatma).contains("2 of 2"));

        System.out.println("\n" + passed + " passed, " + failed + " failed");
        if (failed > 0) System.exit(1);
    }
}
