package donation;

public class AnonymousDonor extends DonationDecorator {

    public AnonymousDonor(Donation d) {
        super(d);
    }

    @Override
    public String getDescription() {
        String original = wrappedDonation.getDescription();
        String name = wrappedDonation.getDonor().getFullName();
        return original.replace(name, "Anonymous") + " [Anonymous]";
    }
}
