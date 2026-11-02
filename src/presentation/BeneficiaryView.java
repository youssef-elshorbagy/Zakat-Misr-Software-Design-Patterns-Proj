package presentation;

import beneficiary.AidDistribution;
import beneficiary.Need;
import donation.Donation;
import users.Beneficiary;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** MVC View - beneficiaries submit needs and track aid; admins distribute donations to needs. */
public class BeneficiaryView extends JPanel {

    private final boolean admin;
    private final DefaultTableModel needsModel;
    private final JTable needsTable;
    private final DefaultTableModel aidModel = Ui.model("Aid #", "Need", "Amount", "Donation", "Date", "Status");
    private final JTable aidTable = Ui.table(aidModel);
    private final DefaultTableModel donationsModel = Ui.model("Receipt", "Donor", "Amount", "Still available");
    private final JTable donationsTable = Ui.table(donationsModel);
    private final JTextArea report = Ui.textArea(4);
    private final JLabel status = Ui.statusLabel();
    private final JLabel eligibility = new JLabel(" ");
    private List<Need> shownNeeds = new ArrayList<>();
    private List<Donation> shownDonations = new ArrayList<>();

    private final JComboBox<String> category = new JComboBox<>(new String[]{"Food", "Medical", "Education", "Housing", "Clothing", "Other"});
    private final JComboBox<String> urgency = new JComboBox<>(new String[]{"LOW", "MEDIUM", "HIGH"});
    private final JTextField amount = new JTextField(10);
    private final JButton submitBtn = Ui.button("Submit need"), impactBtn = Ui.button("Impact report"),
            distributeBtn = Ui.button("Distribute selected donation to selected need");

    public BeneficiaryView(boolean admin) {
        super(new BorderLayout(10, 10));
        this.admin = admin;
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        needsModel = admin ? Ui.model("Need #", "Beneficiary", "Category", "Urgency", "Required", "Remaining", "Status")
                           : Ui.model("Need #", "Category", "Urgency", "Required", "Funded", "Remaining", "Status");
        needsTable = Ui.table(needsModel);
        if (admin) Ui.widths(needsTable, 50, 130, 80, 70, 110, 110, 80); else Ui.widths(needsTable, 50, 90, 70, 110, 110, 110, 80);
        Ui.widths(donationsTable, 60, 120, 110, 120);
        Ui.widths(aidTable, 50, 120, 110, 70, 130, 80);

        if (admin) {
            JPanel donations = new JPanel(new BorderLayout());
            donations.add(Ui.scroll(donationsTable, 140), BorderLayout.CENTER);
            JPanel needs = new JPanel(new BorderLayout());
            needs.add(Ui.scroll(needsTable, 140), BorderLayout.CENTER);
            JPanel top = new JPanel(new GridLayout(1, 2, 10, 0));
            top.add(Ui.section("Donations", donations));
            top.add(Ui.section("Needs", needs));
            JPanel mid = new JPanel(new BorderLayout());
            mid.add(top, BorderLayout.CENTER);
            mid.add(Ui.flow(distributeBtn), BorderLayout.SOUTH);
            JPanel aid = new JPanel(new BorderLayout());
            aid.add(Ui.scroll(aidTable, 140), BorderLayout.CENTER);
            JPanel center = new JPanel(new GridLayout(2, 1, 0, 8));
            center.add(mid);
            center.add(Ui.section("Aid distributions", aid));
            add(center, BorderLayout.CENTER);
        } else {
            JPanel form = Ui.flow(new JLabel("Category:"), category, new JLabel("Urgency:"), urgency,
                    new JLabel("Amount required (EGP):"), amount, submitBtn);
            JPanel north = new JPanel(new BorderLayout());
            north.add(eligibility, BorderLayout.NORTH);
            north.add(form, BorderLayout.CENTER);
            add(Ui.section("Request aid", north), BorderLayout.NORTH);
            JPanel needs = new JPanel(new BorderLayout());
            needs.add(Ui.scroll(needsTable, 140), BorderLayout.CENTER);
            JPanel aid = new JPanel(new BorderLayout());
            aid.add(Ui.scroll(aidTable, 120), BorderLayout.CENTER);
            JPanel rep = new JPanel(new BorderLayout());
            rep.add(new JScrollPane(report), BorderLayout.CENTER);
            rep.add(Ui.flow(impactBtn), BorderLayout.SOUTH);
            JPanel center = new JPanel(new GridLayout(3, 1, 0, 6));
            center.add(Ui.section("My needs", needs));
            center.add(Ui.section("Aid I received", aid));
            center.add(Ui.section("Impact", rep));
            add(center, BorderLayout.CENTER);
        }
        add(status, BorderLayout.SOUTH);
    }

    // ---- diagram methods ----
    public void displayAidStatus(AidDistribution aid) {
        Ui.info(this, "AID STATUS\n\nDistribution #" + aid.getDistributionId() + "\n" + Ui.money(aid.getAmount())
                + " of " + aid.getResourceType() + "\nStatus: " + aid.getStatus());
    }

    public void displayImpactReport(String text) { report.setText(text); }

    // ---- extra ----
    public void displayEligibility(boolean eligible, String text) {
        eligibility.setText("  " + text);
        eligibility.setForeground(eligible ? Ui.GREEN : Ui.ERROR);
    }

    public void displayNeeds(List<Need> needs, Map<Need, Beneficiary> owners) {
        shownNeeds = new ArrayList<>(needs);
        needsModel.setRowCount(0);
        for (Need n : needs) {
            if (admin) needsModel.addRow(new Object[]{n.getNeedId(), owners.get(n).getFullName(), n.getCategory(), n.getUrgency(),
                    Ui.money(n.getAmountRequired()), Ui.money(n.getRemaining()), n.getStatus()});
            else needsModel.addRow(new Object[]{n.getNeedId(), n.getCategory(), n.getUrgency(), Ui.money(n.getAmountRequired()),
                    Ui.money(n.getFundedAmount()), Ui.money(n.getRemaining()), n.getStatus()});
        }
    }

    public void displayDistributions(List<AidDistribution> list) {
        aidModel.setRowCount(0);
        for (AidDistribution a : list) {
            aidModel.addRow(new Object[]{a.getDistributionId(), a.getNeed() == null ? "" : "#" + a.getNeed().getNeedId() + " " + a.getResourceType(),
                    Ui.money(a.getAmount()), a.getDonation() == null ? "" : "#" + a.getDonation().getDonationId(),
                    Ui.fmt(a.getDistributionDate()), a.getStatus()});
        }
    }

    public void displayDonations(List<Donation> donations, Map<Donation, Double> available) {
        shownDonations = new ArrayList<>(donations);
        donationsModel.setRowCount(0);
        for (Donation d : donations) {
            donationsModel.addRow(new Object[]{d.getDonationId(), d.getDonor().getFullName(), Ui.money(d.getAmount()),
                    Ui.money(available.get(d))});
        }
    }

    public void showStatusMessage(String msg) { Ui.setStatus(status, msg, false); }
    public void showError(String msg) { Ui.setStatus(status, msg, true); }

    // ---- getters ----
    public Need getSelectedNeed() { int i = Ui.selected(needsTable); return i < 0 || i >= shownNeeds.size() ? null : shownNeeds.get(i); }
    public Donation getSelectedDonation() { int i = Ui.selected(donationsTable); return i < 0 || i >= shownDonations.size() ? null : shownDonations.get(i); }
    public String getCategoryInput() { return (String) category.getSelectedItem(); }
    public String getUrgencyInput() { return (String) urgency.getSelectedItem(); }
    public String getAmountInput() { return amount.getText(); }
    public void clearForm() { amount.setText(""); }

    // ---- listeners ----
    public void onSubmitNeed(ActionListener l) { submitBtn.addActionListener(l); }
    public void onImpact(ActionListener l) { impactBtn.addActionListener(l); }
    public void onDistribute(ActionListener l) { distributeBtn.addActionListener(l); }
}
