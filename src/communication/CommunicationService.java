package communication;

import users.User;

/** STRATEGY PATTERN (context). */
public class CommunicationService {

    private CommunicationStrategy strategy;

    public void setStrategy(CommunicationStrategy s) {
        this.strategy = s;
    }

    public boolean notifyUser(User user, String message) {
        if (strategy == null) {
            System.out.println("[COMM] No communication strategy selected");
            return false;
        }
        return strategy.sendNotification(user, message);
    }
}
