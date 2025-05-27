package it.trenical.server.model;

import it.trenical.common.grpc.TrattaDTO;
import it.trenical.server.observer.TrattaObserver;
import it.trenical.server.observer.TrattaSubject;

import java.util.ArrayList;
import java.util.List;

/**
 * Tratta che può essere osservata da uno o più clienti.
 */
public class TrattaObservable implements TrattaSubject {

    private final List<TrattaObserver> osservatori = new ArrayList<>();
    private TrattaDTO tratta;

    public TrattaObservable(TrattaDTO tratta) {
        this.tratta = tratta;
    }

    public void aggiornaTratta(TrattaDTO nuovaTratta) {
        this.tratta = nuovaTratta;
        notifyObservers();  // Notifica tutti gli osservatori
    }

    @Override
    public void attach(TrattaObserver o) {
        if (!osservatori.contains(o)) {
            osservatori.add(o);
        }
    }

    @Override
    public void detach(TrattaObserver o) {
        osservatori.remove(o);
    }

    @Override
    public void notifyObservers() {
        for (TrattaObserver o : osservatori) {
            o.update(tratta);  // Notifica l'osservatore con l'oggetto tratta aggiornato
        }
    }
}