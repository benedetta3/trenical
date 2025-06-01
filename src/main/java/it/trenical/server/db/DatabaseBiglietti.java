package it.trenical.server.db;

import it.trenical.common.grpc.BigliettoDTO;
import it.trenical.common.grpc.ClienteDTO;
import it.trenical.common.grpc.TrattaDTO;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class DatabaseBiglietti {

    private static DatabaseBiglietti instance;
    private final List<BigliettoDTO> biglietti = new ArrayList<>();
    private final AtomicInteger idGenerator = new AtomicInteger(1000);

    private DatabaseBiglietti() {}

    public static synchronized DatabaseBiglietti getInstance() {
        if (instance == null) {
            instance = new DatabaseBiglietti();
        }
        return instance;
    }

    public synchronized void aggiungiBiglietto(BigliettoDTO biglietto) {
        biglietti.add(biglietto);
    }

    public synchronized void rimuoviBiglietto(int idDaRimuovere) {
        for (int i = 0; i < biglietti.size(); i++) {
            BigliettoDTO b = biglietti.get(i);
            if (b.getId() == idDaRimuovere) {
                biglietti.remove(i);
                break;
            }
        }
    }

    public synchronized void aggiornaBiglietto(BigliettoDTO aggiornato) {
        for (int i = 0; i < biglietti.size(); i++) {
            if (biglietti.get(i).getId() == aggiornato.getId()) {
                biglietti.set(i, aggiornato);
                return;
            }
        }
    }

    public synchronized int generaNuovoId() {
        return idGenerator.getAndIncrement();
    }

    public synchronized boolean esisteBigliettoPer(ClienteDTO cliente, TrattaDTO tratta) {
        for (BigliettoDTO biglietto : biglietti) {
            if (biglietto.getCliente().getId() == cliente.getId() &&
                    biglietto.getTratta().getId() == tratta.getId()) {
                return true;
            }
        }
        return false;
    }

    public synchronized List<BigliettoDTO> getBigliettiByCliente(ClienteDTO cliente) {
        List<BigliettoDTO> risultato = new ArrayList<>();
        DatabaseTratte dbTratte = DatabaseTratte.getInstance();
        for (BigliettoDTO b : biglietti) {
            if (b.hasCliente() && b.getCliente().getEmail().equalsIgnoreCase(cliente.getEmail())) {
                TrattaDTO trattaCompleta = dbTratte.getTratta(b.getTratta().getId());
                // Ricostruzione del biglietto con tratta aggiornata
                BigliettoDTO aggiornato = BigliettoDTO.newBuilder(b)
                        .setTratta(trattaCompleta)
                        .build();
                risultato.add(aggiornato);
            }
        }
        return risultato;
    }

    public synchronized List<BigliettoDTO> getBigliettiPerTratta(int idTratta) {
        List<BigliettoDTO> risultato = new ArrayList<>();
        synchronized (this) {
            for (BigliettoDTO b : biglietti) {
                if (b.getTratta().getId() == idTratta) {
                    risultato.add(b);
                }
            }
        }
        return risultato;
    }

    public synchronized void rimborsoPerTratta(int idTratta, ClienteDTO cliente) {
        for (BigliettoDTO b : new ArrayList<>(biglietti)) {
            if (b.getTratta().getId() == idTratta && b.getCliente().getEmail().equals(cliente.getEmail())) {
                biglietti.remove(b);
            }
        }
    }

    public synchronized void reset() {
        biglietti.clear();
    }
}