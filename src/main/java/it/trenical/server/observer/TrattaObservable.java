package it.trenical.server.observer;

import it.trenical.common.grpc.BigliettoDTO;
import it.trenical.common.grpc.TrattaDTO;
import it.trenical.server.db.DatabaseBiglietti;

import java.util.ArrayList;
import java.util.List;

public class TrattaObservable implements TrattaSubject {

    private final List<TrattaObserver> osservatori = new ArrayList<>();
    private TrattaDTO tratta;
    private String stato = "Regolare";

    public TrattaObservable(TrattaDTO tratta) {
        this.tratta = tratta;
    }

    public void aggiornaTratta(TrattaDTO nuovaTratta) {
        boolean modificata = false;

        if (!tratta.getOrarioPartenza().equals(nuovaTratta.getOrarioPartenza())) modificata = true;
        if (!tratta.getOrarioArrivo().equals(nuovaTratta.getOrarioArrivo())) modificata = true;
        if (tratta.getBinario() != nuovaTratta.getBinario()) modificata = true;
        if (!tratta.getStato().equalsIgnoreCase(nuovaTratta.getStato())) modificata = true;

        this.tratta = nuovaTratta;

        if (modificata) {
            for (BigliettoDTO b : DatabaseBiglietti.getInstance().getBigliettiPerTratta(nuovaTratta.getId())) {
                ClienteOsservatore osservatore = new ClienteOsservatore(b.getCliente());
                osservatore.update(nuovaTratta);
            }

            for (TrattaObserver obs : osservatori) {
                obs.update(nuovaTratta);
            }
        }
    }

    public TrattaDTO getTratta() {
        return this.tratta.toBuilder().build();
    }

    public String getStato() {
        return stato;
    }

    public void setStato(String nuovoStato) {
        String vecchioStato = this.stato;
        this.stato = nuovoStato;

        this.tratta = this.tratta.toBuilder().setStato(nuovoStato).build();

        notifyObservers();
    }

    @Override
    public void attach(TrattaObserver o) {
        if (!osservatori.contains(o)) {
            osservatori.add(o);
        }
    }

    @Override
    public void notifyObservers() {
        for (TrattaObserver o : osservatori) {
            try {
                o.update(tratta);
            } catch (Exception e) {
                System.err.println("Errore nella notifica dell'osservatore: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}