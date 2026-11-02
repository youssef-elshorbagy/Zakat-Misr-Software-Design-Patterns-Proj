package communication;

import users.User;

public class SocialMediaStrategy implements CommunicationStrategy {

    private String platformApi;

    public SocialMediaStrategy() {
        this.platformApi = "https://api.social.zakatmisr.org";
    }

    @Override
    public boolean sendNotification(User recipient, String message) {
        if (recipient == null || message == null || message.isEmpty()) return false;
        System.out.println("[SOCIAL via " + platformApi + "] To: @" + recipient.getFullName().replace(" ", "").toLowerCase()
                + " | " + message);
        return true;
    }

    public String getPlatformApi() { return platformApi; }
}
