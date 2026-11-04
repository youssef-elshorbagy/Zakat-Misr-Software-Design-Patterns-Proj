package presentation;

import donation.Donation;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/** MVC View - donor screen: donation form, receipt and history. Only displays, no business logic. */
public class DonationView extends JPanel {

    private final JComboBox<String> type = new JComboBox<>();
    private final JTextField amount = new JTextField(12);
    private final JComboBox<String> campaign = new JComboBox<>();
    private final JTextField itemName = new JTextField(14);
    private final JSpinner quantity = new JSpinner(new SpinnerNumberModel(1, 1, 10000, 1));
    private final JCheckBox anonymous = new JCheckBox("Donate anonymously");
    private final JCheckBox recurring = new JCheckBox("Repeat every");
    private final JSpinner months = new JSpinner(new SpinnerNumberModel(1, 1, 12, 1));
    private final JButton donateBtn = Ui.button("Donate");
    private final JTextArea receipt = Ui.textArea(8);
    private final DefaultTableModel historyModel = Ui.model("Receipt", "Date", "Type", "Amount", "Details");
    private final JTable history = Ui.table(historyModel);
    private final JLabel status = Ui.statusLabel();
    private List<Donation> shown = new ArrayList<>();
    private final CardLayout detailCards = new CardLayout();
    private final JPanel detailPanel = new JPanel(detailCards);

    public DonationView() {
        super(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        campaign.setEditable(true);
        Ui.widths(history, 60, 130, 70, 110, 420);
        months.setEnabled(false);
        recurring.addActionListener(e -> months.setEnabled(recurring.isSelected()));

        JPanel zakatInfo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        zakatInfo.add(new JLabel("Zakat is verified against the nisab automatically."));
        JPanel sadaqah = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sadaqah.add(new JLabel("Campaign / category:"));
        sadaqah.add(campaign);
        JPanel inkind = new JPanel(new FlowLayout(FlowLayout.LEFT));
        inkind.add(new JLabel("Item:"));
        inkind.add(itemName);
        inkind.add(new JLabel("Quantity:"));
        inkind.add(quantity);
        detailPanel.add(zakatInfo, "Zakat");
        detailPanel.add(sadaqah, "Sadaqah");
        detailPanel.add(inkind, "In-kind");
        detailPanel.add(new JPanel(), "other");
        type.addActionListener(e -> {
            String selected = (String) type.getSelectedItem();
            boolean known = "Zakat".equals(selected) || "Sadaqah".equals(selected) || "In-kind".equals(selected);
            detailCards.show(detailPanel, known ? selected : "other");
        });

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.add(Ui.flow(new JLabel("Type:"), type, new JLabel("Amount / value (EGP):"), amount));
        form.add(detailPanel);
        form.add(Ui.flow(anonymous, recurring, months, new JLabel("month(s)")));
        form.add(Ui.flow(donateBtn));

        JPanel top = new JPanel(new GridLayout(1, 2, 10, 0));
        top.add(Ui.section("New donation", form));
        top.add(Ui.section("Receipt", new JScrollPane(receipt)));
        top.setPreferredSize(new Dimension(800, 210));

        history.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int i = Ui.selected(history);
            if (i >= 0 && i < shown.size()) displayReceipt(shown.get(i));
        });

        add(top, BorderLayout.NORTH);
        add(Ui.section("My donation history (select a row to see its receipt)", Ui.scroll(history, 200)), BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);
    }

    // ---- diagram methods ----
    public void displayReceipt(Donation d) {
        receipt.setText("----------- DONATION RECEIPT -----------\n"
                + "Receipt No : " + d.getDonationId() + "\n"
                + "Date       : " + Ui.fmt(d.getDate()) + "\n"
                + "Amount     : " + Ui.money(d.getAmount()) + "\n"
                + "Details    : " + d.getDescription() + "\n"
                + "----------------------------------------");
        receipt.setCaretPosition(0);
    }

    public void displayHistory(List<Donation> list) {
        shown = new ArrayList<>(list);
        historyModel.setRowCount(0);
        for (Donation d : list) {
            historyModel.addRow(new Object[]{d.getDonationId(), Ui.fmt(d.getDate()), typeOf(d),
                    Ui.money(d.getAmount()), d.getDescription()});
        }
    }

    private String typeOf(Donation d) {
        String desc = d.getDescription();
        if (desc.startsWith("Zakat")) return "Zakat";
        if (desc.startsWith("Sadaqah")) return "Sadaqah";
        if (desc.startsWith("In-kind")) return "In-kind";
        return "Other";
    }

    public void showStatusMessage(String msg) { Ui.setStatus(status, msg, false); }
    public void showError(String msg) { Ui.setStatus(status, msg, true); }

    public void setTypes(List<String> names) {
        type.removeAllItems();
        for (String name : names) type.addItem(name);
    }

    public void setCampaigns(List<String> names) {
        campaign.removeAllItems();
        campaign.addItem("General Fund");
        for (String n : names) campaign.addItem(n);
    }

    public void clearForm() { amount.setText(""); anonymous.setSelected(false); recurring.setSelected(false); months.setEnabled(false); }

    // ---- getters ----
    public String getSelectedType() { return (String) type.getSelectedItem(); }
    public String getAmountText() { return amount.getText(); }
    public String getDetail() {
        if ("Sadaqah".equals(type.getSelectedItem())) return String.valueOf(campaign.getSelectedItem());
        if ("In-kind".equals(type.getSelectedItem())) return itemName.getText();
        return null;
    }
    public int getQuantity() { return (Integer) quantity.getValue(); }
    public boolean isAnonymous() { return anonymous.isSelected(); }
    public int getRecurringMonths() { return recurring.isSelected() ? (Integer) months.getValue() : 0; }

    public void onDonate(ActionListener l) { donateBtn.addActionListener(l); }
}
