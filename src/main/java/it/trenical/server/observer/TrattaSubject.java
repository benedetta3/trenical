package it.trenical.server.observer;

public interface TrattaSubject {
    void attach(TrattaObserver o);
    void notifyObservers();
}