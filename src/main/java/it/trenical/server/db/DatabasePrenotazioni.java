package it.trenical.server.db;

import it.trenical.common.grpc.BigliettoDTO;
import it.trenical.common.grpc.ClienteDTO;

import java.util.HashMap;
import java.util.Map;

public class DatabasePrenotazioni {

    private static DatabasePrenotazioni instance;
    private final Map<Integer, BigliettoDTO> prenotati = new HashMap<>();
    private final Map<Integer, ClienteDTO> osservatori = new HashMap<>();

    private DatabasePrenotazioni() {}

    public static synchronized DatabasePrenotazioni getInstance() {
        if (instance == null) {
            instance = new DatabasePrenotazioni();
        }
        return instance;
    }

    // Aggiungi il biglietto e collega il cliente osservatore
    public void aggiungiPrenotazione(int id, BigliettoDTO biglietto, ClienteDTO cliente) {
        prenotati.put(id, biglietto);
        osservatori.put(id, cliente);  // Associa il cliente come osservatore
    }

    public boolean contiene(int id) {
        return prenotati.containsKey(id);
    }

    // Restituisce il cliente osservatore associato alla prenotazione
    public ClienteDTO getClienteOsservatore(int id) {
        return osservatori.get(id);  // Restituisce il cliente osservatore associato all'ID
    }

    public void rimuoviPrenotazione(int id) {
        prenotati.remove(id);
        osservatori.remove(id);  // Rimuove il cliente osservatore
    }
}