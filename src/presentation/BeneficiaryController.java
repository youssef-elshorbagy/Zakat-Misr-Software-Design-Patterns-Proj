package presentation;

import beneficiary.AidDistribution;
import beneficiary.BeneficiaryService;
import beneficiary.Need;
import donation.Donation;
import observer.DonationService;
import users.Admin;
import users.Beneficiary;
import users.User;

import java.util.LinkedHashMap;
import java.util.Map;

/** MVC Controller - beneficiary use cases (submit needs, impact) and admin aid distribution. */
public class BeneficiaryController {

    private BeneficiaryService service;
    private BeneficiaryView view;
    private DonationService donationService;
    private User currentUser;

    public BeneficiaryController(BeneficiaryService service, BeneficiaryView view) {
        this.service = service;
        this.view = view;
    }

    public void start(User user, DonationService donationService) {
        this.currentUser = user;
        this.donationService = donationService;
        view.onSubmitNeed(e -> run(this::submitFromForm));
        view.onImpact(e -> run(() -> view.displayImpactReport(service.evaluateImpact(beneficiary()))));
        view.onDistribute(e -> run(this::distributeSelected));
        refresh();
    }

    private void run(Runnable r) {
        try {
            r.run();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            view.showError(ex.getMessage());
        }
    }

    private boolean isAdmin() { return currentUser instanceof Admin; }

    private Beneficiary beneficiary() {
        if (!(currentUser instanceof Beneficiary)) throw new IllegalStateException("Only beneficiaries can do this");
        return (Beneficiary) currentUser;
    }

    public void refresh() {
        if (isAdmin()) {
            Map<Donation, Double> available = new LinkedHashMap<>();
            for (Donation d : donationService.getAllDonations()) available.put(d, service.getAvailable(d));
            view.displayDonations(donationService.getAllDonations(), available);
            Map<Need, Beneficiary> owners = new LinkedHashMap<>();
            for (Need n : service.getAllNeeds()) owners.put(n, service.getOwner(n));
            view.displayNeeds(service.getAllNeeds(), owners);
            view.displayDistributions(service.getDistributions());
        } else {
            Beneficiary b = beneficiary();
            boolean ok = service.verifyEligibility(b);
            view.displayEligibility(ok, ok ? "You are eligible for aid."
                    : "You are NOT eligible for aid (check national ID and income per family member).");
            view.displayNeeds(b.getNeeds(), java.util.Collections.emptyMap());
            view.displayDistributions(service.getDistributionsFor(b));
        }
    }

    private void submitFromForm() {
        double amount = Ui.parseAmount(view.getAmountInput(), "Amount");
        Need n = new Need(service.nextNeedId(), view.getCategoryInput(), view.getUrgencyInput(), amount);
        submitNeedRequest(beneficiary(), n);
        view.clearForm();
    }

    public void submitNeedRequest(Beneficiary b, Need n) {
        service.registerNeed(b, n);
        refresh();
        view.showStatusMessage("Need #" + n.getNeedId() + " submitted");
    }

    private void distributeSelected() {
        if (!isAdmin()) throw new IllegalStateException("Only an admin can distribute aid");
        Donation d = view.getSelectedDonation();
        Need n = view.getSelectedNeed();
        if (d == null) throw new IllegalStateException("Select a donation first");
        if (n == null) throw new IllegalStateException("Select a need first");
        distributeAid(d, n);
    }

    public void distributeAid(Donation d, Need n) {
        AidDistribution aid = service.distributeAid(d, n);
        aid.confirmReceipt();
        refresh();
        view.displayAidStatus(aid);
        view.showStatusMessage("Distributed " + Ui.money(aid.getAmount()) + " from donation #" + d.getDonationId()
                + " to need #" + n.getNeedId());
    }
}
