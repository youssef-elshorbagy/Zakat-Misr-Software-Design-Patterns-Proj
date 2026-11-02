package observer;

import donation.Donation;

public interface Subject {
    void attach(Observer o);
    void detach(Observer o);
    void notifyObservers(Donation d);
}
