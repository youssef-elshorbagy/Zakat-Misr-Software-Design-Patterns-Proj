package presentation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/** Small Swing helpers shared by all views (no business logic). */
public final class Ui {

    public static final Color GREEN = new Color(0x1B5E3F);
    public static final Color LIGHT = new Color(0xF3F7F4);
    public static final Color ERROR = new Color(0xB3261E);
    public static final String DATE_HINT = "yyyy-MM-dd HH:mm";

    private Ui() { }

    public static void installLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) { }
        UIManager.put("nimbusBase", GREEN);
        UIManager.put("nimbusSelectionBackground", new Color(0x2E7D5B));
    }

    public static DefaultTableModel model(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    public static JTable table(DefaultTableModel m) {
        JTable t = new JTable(m);
        t.setRowHeight(24);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setAutoCreateRowSorter(true);
        t.setFillsViewportHeight(true);
        t.getTableHeader().setReorderingAllowed(false);
        return t;
    }

    /** Selected row converted to the model index (or -1). */
    public static int selected(JTable t) {
        int r = t.getSelectedRow();
        return r < 0 ? -1 : t.convertRowIndexToModel(r);
    }

    public static JScrollPane scroll(JComponent c, int height) {
        JScrollPane sp = new JScrollPane(c);
        sp.setPreferredSize(new Dimension(300, height));
        return sp;
    }

    public static JPanel section(String title, JComponent content) {
        JPanel p = new JPanel(new BorderLayout());
        TitledBorder tb = BorderFactory.createTitledBorder(title);
        tb.setTitleColor(GREEN);
        p.setBorder(BorderFactory.createCompoundBorder(tb, new EmptyBorder(4, 6, 6, 6)));
        p.add(content, BorderLayout.CENTER);
        return p;
    }

    public static JPanel flow(Component... comps) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        for (Component c : comps) p.add(c);
        return p;
    }

    public static JButton button(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        return b;
    }

    public static JTextArea textArea(int rows) {
        JTextArea a = new JTextArea(rows, 30);
        a.setEditable(false);
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        a.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        return a;
    }

    public static JLabel statusLabel() {
        JLabel l = new JLabel(" ");
        l.setBorder(new EmptyBorder(4, 8, 4, 8));
        return l;
    }

    public static void setStatus(JLabel l, String msg, boolean error) {
        l.setForeground(error ? ERROR : GREEN);
        l.setText(msg == null || msg.isEmpty() ? " " : msg);
    }

    public static void info(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Zakat Misr", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void error(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Zakat Misr", JOptionPane.ERROR_MESSAGE);
    }

    public static boolean confirm(Component parent, String msg) {
        return JOptionPane.showConfirmDialog(parent, msg, "Zakat Misr", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }

    public static String money(double v) {
        return String.format("%,.2f EGP", v);
    }

    public static String fmt(Date d) {
        return d == null ? "" : new SimpleDateFormat("yyyy-MM-dd HH:mm").format(d);
    }

    /** Parses "yyyy-MM-dd HH:mm" or "yyyy-MM-dd". */
    public static Date parseDate(String s) {
        if (s == null || s.isBlank()) throw new IllegalArgumentException("Date is required (" + DATE_HINT + ")");
        s = s.trim();
        String pattern = s.length() <= 10 ? "yyyy-MM-dd" : "yyyy-MM-dd HH:mm";
        SimpleDateFormat f = new SimpleDateFormat(pattern);
        f.setLenient(false);
        try {
            return f.parse(s);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid date '" + s + "'. Use " + DATE_HINT);
        }
    }

    public static double parseAmount(String s, String what) {
        try {
            double v = Double.parseDouble(s.trim().replace(",", ""));
            if (v <= 0) throw new IllegalArgumentException(what + " must be greater than zero");
            return v;
        } catch (NumberFormatException | NullPointerException e) {
            throw new IllegalArgumentException(what + " must be a number");
        }
    }

    public static void widths(JTable t, int... w) {
        for (int i = 0; i < w.length && i < t.getColumnCount(); i++) t.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
    }

    /** Label / field form using GridBagLayout (rows stay at the top when the panel is stretched). */
    public static class Form extends JPanel {
        private int row = 0;
        private final JPanel filler = new JPanel();

        public Form() { super(new GridBagLayout()); filler.setOpaque(false); }

        public JComponent add(String label, JComponent field) {
            remove(filler);
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0; c.gridy = row; c.anchor = GridBagConstraints.EAST; c.insets = new Insets(3, 4, 3, 6);
            super.add(new JLabel(label), c);
            c = new GridBagConstraints();
            c.gridx = 1; c.gridy = row; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL; c.insets = new Insets(3, 0, 3, 4);
            super.add(field, c);
            row++;
            c = new GridBagConstraints();
            c.gridx = 0; c.gridy = row; c.weighty = 1; c.gridwidth = 2; c.fill = GridBagConstraints.VERTICAL;
            super.add(filler, c);
            return field;
        }
    }
}
