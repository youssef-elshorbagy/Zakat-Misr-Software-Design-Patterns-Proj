package observer;

import communication.CommunicationService;
import communication.EmailStrategy;
import communication.SMSStrategy;
import communication.SocialMediaStrategy;
import donation.Donation;
import users.Donor;

/**
 * OBSERVER (concrete observer) - picks a STRATEGY from the donor's preferredChannel.
 */
public class NotificationObserver implements Observer {

    private CommunicationService commService;

    public NotificationObserver() {
        this.commService = new CommunicationService();
    }

    @Override
    public void update(Donation d) {
        Donor donor = d.getDonor();
        String channel = donor.getPreferredChannel() == null ? "email" : donor.getPreferredChannel().toLowerCase();

        switch (channel) {
            case "sms":
                commService.setStrategy(new SMSStrategy());
                break;
            case "social":
            case "socialmedia":
                commService.setStrategy(new SocialMediaStrategy());
                break;
            default:
                commService.setStrategy(new EmailStrategy());
        }
        commService.notifyUser(donor, "Thank you for your donation! " + d.getDescription());
    }
}
