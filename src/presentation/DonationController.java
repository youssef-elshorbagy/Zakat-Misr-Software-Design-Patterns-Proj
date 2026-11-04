package presentation;

import donation.Donation;
import lookup.DonationCategory;
import lookup.LookupService;
import observer.Donation.DonationService;
import users.Donor;

import java.util.ArrayList;
import java.util.List;

/** MVC Controller - donation use cases for the logged-in donor. */
public class DonationController {

    private DonationService service;
    private DonationView view;
    private Donor donor;
    private LookupService lookupService;

    public DonationController(DonationService service, DonationView view) {
        this.service = service;
        this.view = view;
    }

    /** Binds the screen to a donor (called after login). */
    public void start(Donor donor, LookupService lookupService) {
        this.donor = donor;
        this.lookupService = lookupService;
        view.onDonate(e -> onDonateClicked());
        refreshCampaigns();
        view.displayHistory(donor.getDonationHistory());
    }

    private void refreshCampaigns() {
        List<String> names = new ArrayList<>();
        for (DonationCategory c : lookupService.getActiveCategories()) names.add(c.getName());
        view.setCampaigns(names);
    }

    private void onDonateClicked() {
        try {
            String type = view.getSelectedType().toLowerCase().replace("-", "");
            double amount = Ui.parseAmount(view.getAmountText(), "Amount");
            submitDonation(type, donor, amount, view.getDetail(), view.getQuantity(),
                    view.isAnonymous(), view.getRecurringMonths());
            view.clearForm();
        } catch (IllegalArgumentException ex) {
            view.showError(ex.getMessage());
        }
    }

    public void submitDonation(String type, Donor donor, double amount) {
        submitDonation(type, donor, amount, null, 1, false, 0);
    }

    public void submitDonation(String type, Donor donor, double amount, String detail, int quantity,
                               boolean anonymous, int recurringMonths) {
        Donation d = service.processDonation(type, donor, amount, detail, quantity, anonymous, recurringMonths);
        view.displayReceipt(d);
        view.displayHistory(donor.getDonationHistory());
        view.showStatusMessage("Thank you! Donation #" + d.getDonationId() + " received - confirmation sent by "
                + donor.getPreferredChannel() + ".");
    }
}
