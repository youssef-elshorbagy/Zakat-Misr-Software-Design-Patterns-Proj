package communication;

import users.User;

public class SMSStrategy implements CommunicationStrategy {

    private String gatewayUrl;

    public SMSStrategy() {
        this.gatewayUrl = "https://sms.zakatmisr.org/send";
    }

    @Override
    public boolean sendNotification(User recipient, String message) {
        if (recipient == null || message == null || message.isEmpty()) return false;
        System.out.println("[SMS via " + gatewayUrl + "] To: " + recipient.getPhone() + " | " + message);
        return true;
    }

    public String getGatewayUrl() { return gatewayUrl; }
}
