package it.trenical.server.db;

import it.trenical.common.grpc.BigliettoDTO;
import it.trenical.common.grpc.ClienteDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class DatabasePrenotazioni {

    private static DatabasePrenotazioni instance;

    private final Map<Integer, BigliettoDTO> prenotati = new HashMap<>();
    private final Map<Integer, ClienteDTO> osservatori = new HashMap<>();
    private final Map<Integer, Integer> postiRiservati = new HashMap<>();
    private final AtomicInteger idGenerator = new AtomicInteger(1);

    private DatabasePrenotazioni() {}

    public static synchronized DatabasePrenotazioni getInstance() {
        if (instance == null) {
            instance = new DatabasePrenotazioni();
        }
        return instance;
    }

    public int generaNuovoId() {
        return idGenerator.getAndIncrement();
    }

    public void aggiungiPrenotazione(int id, BigliettoDTO biglietto, ClienteDTO cliente, int posti) {
        prenotati.put(id, biglietto);
        osservatori.put(id, cliente);
        postiRiservati.put(id, posti);
    }

    public boolean contiene(int id) {
        return prenotati.containsKey(id);
    }

    public BigliettoDTO getPrenotazione(int id) {
        return prenotati.get(id);
    }

    public ClienteDTO getClienteOsservatore(int id) {
        return osservatori.get(id);
    }

    public List<BigliettoDTO> getPrenotazioniPerEmail(String email) {
        List<BigliettoDTO> risultato = new ArrayList<>();
        for (BigliettoDTO b : prenotati.values()) {
            if (b.hasCliente() && b.getCliente().getEmail().equalsIgnoreCase(email)) {
                risultato.add(b);
            }
        }
        return risultato;
    }

    public void rimuoviPrenotazione(int id) {
        prenotati.remove(id);
        osservatori.remove(id);
        postiRiservati.remove(id);
    }

    public void reset() {
        prenotati.clear();
        osservatori.clear();
        postiRiservati.clear();
        idGenerator.set(1);
    }
}