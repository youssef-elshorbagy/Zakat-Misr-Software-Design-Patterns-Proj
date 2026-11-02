package communication;

import users.User;

public class EmailStrategy implements CommunicationStrategy {

    private String smtpHost;

    public EmailStrategy() {
        this.smtpHost = "smtp.zakatmisr.org";
    }

    @Override
    public boolean sendNotification(User recipient, String message) {
        if (recipient == null || message == null || message.isEmpty()) return false;
        System.out.println("[EMAIL via " + smtpHost + "] To: " + recipient.getEmail() + " | " + message);
        return true;
    }

    public String getSmtpHost() { return smtpHost; }
}
