package it.trenical.server.db;

import it.trenical.common.grpc.TrattaDTO;
import it.trenical.server.model.TrattaObservable;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseTratte {

    private static DatabaseTratte instance;
    private final List<TrattaDTO> tratte = new ArrayList<>();
    private final Map<Integer, TrattaObservable> tratteOsservabili = new HashMap<>();

    private DatabaseTratte() {}

    public static synchronized DatabaseTratte getInstance() {
        if (instance == null) {
            instance = new DatabaseTratte();
        }
        return instance;
    }

    // Aggiungi una tratta al sistema
    public void aggiungiTratta(TrattaDTO tratta) {
        tratte.add(tratta);
        tratteOsservabili.put(tratta.getId(), new TrattaObservable(tratta));
    }

    // Verifica se la tratta esiste nel sistema in base all'ID
    public boolean contiene(int idTratta) {
        return tratteOsservabili.containsKey(idTratta);  // Usa la mappa per una ricerca veloce
    }

    // Restituisce tutte le tratte nel sistema
    public List<TrattaDTO> getTutteLeTratte() {
        return tratte;  // Restituisce la lista completa delle tratte
    }

    // Restituisce l'osservabile della tratta dato il suo ID
    public TrattaObservable getTrattaObservable(int idTratta) {
        return tratteOsservabili.get(idTratta);
    }

    // Salvataggio delle tratte in un file di testo (.txt)
    public void salvaTratteSuFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("tratte.txt"))) {
            for (TrattaDTO tratta : tratte) {
                writer.write(tratta.getId() + "|" + tratta.getStazionePartenza() + "|" + tratta.getStazioneArrivo() + "|"
                        + tratta.getOrarioPartenza() + "|" + tratta.getOrarioArrivo() + "|" + tratta.getData() + "|"
                        + tratta.getTipoTreno());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Caricamento delle tratte dal file di testo (.txt)
    public void caricaTratteDaFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader("tratte.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split("\\|");
                TrattaDTO tratta = TrattaDTO.newBuilder()
                        .setId(Integer.parseInt(data[0]))
                        .setStazionePartenza(data[1])
                        .setStazioneArrivo(data[2])
                        .setOrarioPartenza(data[3])
                        .setOrarioArrivo(data[4])
                        .setData(data[5])
                        .setTipoTreno(data[6])
                        .build();
                aggiungiTratta(tratta);  // Aggiunge la tratta al sistema
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}