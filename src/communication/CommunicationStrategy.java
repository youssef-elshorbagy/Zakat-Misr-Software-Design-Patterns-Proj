package communication;

import users.User;

public interface CommunicationStrategy {
    boolean sendNotification(User recipient, String message);
}
