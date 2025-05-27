package it.trenical.server.observer;

public interface TrattaSubject {
    void attach(TrattaObserver o);
    void detach(TrattaObserver o);
    void notifyObservers();
}