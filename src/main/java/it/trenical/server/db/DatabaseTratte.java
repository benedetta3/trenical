package it.trenical.server.db;

import it.trenical.common.grpc.BigliettoDTO;
import it.trenical.common.grpc.ClienteDTO;
import it.trenical.common.grpc.TrattaDTO;
import it.trenical.server.notification.NotificationDispatcher;
import it.trenical.server.observer.TrattaObservable;

import java.io.*;
import java.util.*;

public class DatabaseTratte {

    private static DatabaseTratte instance;

    private final List<TrattaDTO> tratte = new ArrayList<>();
    private final Map<Integer, TrattaObservable> tratteOsservabili = new HashMap<>();
    private boolean persistenzaAttiva = true; // di default attiva

    private DatabaseTratte() {}

    public static synchronized DatabaseTratte getInstance() {
        if (instance == null) {
            instance = new DatabaseTratte();
        }
        return instance;
    }

    public synchronized void aggiungiTratta(TrattaDTO tratta) {
        tratte.add(tratta);
        tratteOsservabili.put(tratta.getId(), new TrattaObservable(tratta));
    }

    public synchronized boolean contiene(int idTratta) {
        return tratteOsservabili.containsKey(idTratta);
    }

    public synchronized List<TrattaDTO> getTutteLeTratte() {
        return tratte;
    }

    public synchronized TrattaObservable getTrattaObservable(int idTratta) {
        return tratteOsservabili.get(idTratta);
    }

    public synchronized void caricaTratteDaFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader("tratte.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split("\\|");
                if (data.length < 11) {
                    System.err.println("Linea malformata: " + line);
                    continue;
                }

                double prezzo;
                int postiDisponibili;
                int binario;
                try {
                    prezzo = Double.parseDouble(data[8]);
                    postiDisponibili = Integer.parseInt(data[9]);
                    binario = Integer.parseInt(data[10]);
                } catch (NumberFormatException e) {
                    System.err.println("Prezzo, posti o binario non validi linea: " + line);
                    continue;
                }

                TrattaDTO tratta = TrattaDTO.newBuilder()
                        .setId(Integer.parseInt(data[0]))
                        .setStazionePartenza(data[1])
                        .setStazioneArrivo(data[2])
                        .setOrarioPartenza(data[3])
                        .setOrarioArrivo(data[4])
                        .setData(data[5])
                        .setTipoTreno(data[6])
                        .setClasseServizio(data[7])
                        .setPrezzo(prezzo)
                        .setPostiDisponibili(postiDisponibili)
                        .setBinario(binario)
                        .setStato("regolare")
                        .build();

                aggiungiTratta(tratta);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized TrattaDTO getTratta(int idTratta) {
        TrattaObservable observable = tratteOsservabili.get(idTratta);
        if (observable != null) {
            return observable.getTratta();
        }
        return null;
    }

    public synchronized void aggiornaTratta(TrattaDTO nuovaTratta) {
        for (int i = 0; i < tratte.size(); i++) {
            if (tratte.get(i).getId() == nuovaTratta.getId()) {
                tratte.set(i, nuovaTratta);
                TrattaObservable osservabile = tratteOsservabili.get(nuovaTratta.getId());
                if (osservabile != null) {
                    osservabile.aggiornaTratta(nuovaTratta);
                }
                break;
            }
        }

        List<BigliettoDTO> biglietti =new ArrayList<>(DatabaseBiglietti.getInstance().getBigliettiPerTratta(nuovaTratta.getId()));
        Set<String> emailNotificate = new HashSet<>();

        for (BigliettoDTO b : biglietti) {
            ClienteDTO cliente = b.getCliente();
            String email = cliente.getEmail();

            //Se già notificato, salta
            if (!emailNotificate.add(email)) continue;

            String stato = nuovaTratta.getStato().toLowerCase(Locale.ROOT);
            StringBuilder messaggio = new StringBuilder();

            messaggio.append("Aggiornamento BIGLIETTO\n\n");
            messaggio.append("La tua tratta ")
                    .append(nuovaTratta.getStazionePartenza())
                    .append(" → ")
                    .append(nuovaTratta.getStazioneArrivo())
                    .append(" è stata modificata:\n\n");

            if (stato.equals("cancellato")) {
                DatabaseBiglietti.getInstance().rimborsoPerTratta(nuovaTratta.getId(), cliente);
                messaggio.append("La tratta è stata CANCELLATA.\n")
                        .append("Il tuo biglietto è stato rimborsato.");
            } else if (stato.equals("ritardo")) {
                messaggio.append("Informazioni aggiornate:\n")
                        .append("Orario Partenza: ").append(nuovaTratta.getOrarioPartenza()).append("\n")
                        .append("Orario Arrivo: ").append(nuovaTratta.getOrarioArrivo()).append("\n")
                        .append("Binario: ").append(nuovaTratta.getBinario());
            } else {
                messaggio.append("Informazioni aggiornate:\n")
                        .append("Orario Partenza: ").append(nuovaTratta.getOrarioPartenza()).append("\n")
                        .append("Orario Arrivo: ").append(nuovaTratta.getOrarioArrivo()).append("\n")
                        .append("Binario: ").append(nuovaTratta.getBinario());
            }

            NotificationDispatcher.getInstance().notifica(email, messaggio.toString());
        }

        // Aggiorna tutti i biglietti (anche se non notificati, per sincronizzare i dati)
        for (BigliettoDTO b : biglietti) {
            BigliettoDTO aggiornato = BigliettoDTO.newBuilder(b)
                    .setTratta(nuovaTratta)
                    .build();
            DatabaseBiglietti.getInstance().aggiornaBiglietto(aggiornato);
        }

        salvaTutteSuFile();
    }

    public synchronized List<TrattaObservable> getAll() {
        return new ArrayList<>(tratteOsservabili.values());
    }

    public synchronized void reset() {
        tratte.clear();
        tratteOsservabili.clear();
    }

    public void salvaTutteSuFile() {
        if (!persistenzaAttiva) return;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("tratte.txt"))) {
            for (TrattaDTO t : tratte) {
                String riga = t.getId() + "|" +
                        t.getStazionePartenza() + "|" +
                        t.getStazioneArrivo() + "|" +
                        t.getOrarioPartenza() + "|" +
                        t.getOrarioArrivo() + "|" +
                        t.getData() + "|" +
                        t.getTipoTreno() + "|" +
                        t.getClasseServizio() + "|" +
                        String.format(Locale.US, "%.2f", t.getPrezzo()) + "|" +
                        t.getPostiDisponibili() + "|" +
                        t.getBinario();
                writer.write(riga);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Errore nel salvataggio delle tratte: " + e.getMessage());
        }
    }

    public void setPersistenzaAttiva(boolean attiva) {
        this.persistenzaAttiva = attiva;
    }
}