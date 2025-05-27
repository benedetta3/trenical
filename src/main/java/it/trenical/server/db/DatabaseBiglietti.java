package it.trenical.server.db;

import it.trenical.common.grpc.BigliettoDTO;
import it.trenical.common.grpc.ClienteDTO;
import it.trenical.common.grpc.TrattaDTO;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseBiglietti {

    private static DatabaseBiglietti instance;
    private final List<BigliettoDTO> biglietti = new ArrayList<>();

    private DatabaseBiglietti() {}

    public static synchronized DatabaseBiglietti getInstance() {
        if (instance == null) {
            instance = new DatabaseBiglietti();
        }
        return instance;
    }

    // Aggiungi un biglietto al database
    public void aggiungiBiglietto(BigliettoDTO biglietto) {
        biglietti.add(biglietto);
    }

    // Verifica se il cliente ha già acquistato un biglietto per la stessa tratta
    public boolean esisteBigliettoPer(ClienteDTO cliente, TrattaDTO tratta) {
        for (BigliettoDTO biglietto : biglietti) {
            if (biglietto.getCliente().getId() == cliente.getId() &&
                    biglietto.getTratta().getId() == tratta.getId()) {
                return true;  // Il cliente ha già acquistato questo biglietto per la tratta
            }
        }
        return false;  // Nessun biglietto trovato per questo cliente e tratta
    }

    // Salvataggio dei biglietti in un file di testo (.txt)
    public void salvaBigliettiSuFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("biglietti.txt"))) {
            for (BigliettoDTO biglietto : biglietti) {
                writer.write(biglietto.getId() + "|" + biglietto.getTratta().getId() + "|" + biglietto.getCliente().getId() + "|"
                        + biglietto.getPrezzo() + "|" + biglietto.getStato() + "|" + biglietto.getClasseServizio());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Caricamento dei biglietti dal file di testo (.txt)
    public void caricaBigliettiDaFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader("biglietti.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split("\\|");
                BigliettoDTO biglietto = BigliettoDTO.newBuilder()
                        .setId(Integer.parseInt(data[0]))
                        .setTratta(TrattaDTO.newBuilder().setId(Integer.parseInt(data[1])).build())
                        .setCliente(ClienteDTO.newBuilder().setId(Integer.parseInt(data[2])).build())
                        .setPrezzo(Double.parseDouble(data[3]))
                        .setStato(data[4])
                        .setClasseServizio(data[5])
                        .build();
                aggiungiBiglietto(biglietto);  // Aggiungi il biglietto al sistema
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}