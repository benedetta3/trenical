package it.trenical.server.db;

import it.trenical.common.grpc.PromozioneDTO;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class DatabasePromozioni {

    private static DatabasePromozioni instance;
    private final List<PromozioneDTO> lista;

    private DatabasePromozioni() {
        this.lista = new ArrayList<>();
        caricaDaFile("promozioni.txt");
    }

    public static synchronized DatabasePromozioni getInstance() {
        if (instance == null) {
            instance = new DatabasePromozioni();
        }
        return instance;
    }

    private void caricaDaFile(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String riga;
            int count = 0;

            while ((riga = reader.readLine()) != null) {
                String[] parti = riga.split("\\|");

                if (parti.length >= 4) {
                    String descrizione = parti[0].trim();
                    double sconto = Double.parseDouble(parti[1].trim());
                    boolean soloFedelta = Boolean.parseBoolean(parti[2].trim());
                    String classeStrategy = parti[3].trim();

                    PromozioneDTO promo = PromozioneDTO.newBuilder()
                            .setDescrizione(descrizione)
                            .setSconto(sconto)
                            .setSoloFedelta(soloFedelta)
                            .setClasseStrategy(classeStrategy)
                            .build();

                    lista.add(promo);
                    count++;
                } else {
                    System.err.println("Riga malformata in promozioni.txt: " + riga);
                }
            }
        } catch (Exception e) {
            System.err.println("Errore nella lettura delle promozioni: " + e.getMessage());
        }
    }

    public void aggiungiPromozione(PromozioneDTO promo) {
        lista.add(promo);
        // Nessuna scrittura su file
    }

    public void ricarica() {
        lista.clear();
        caricaDaFile("promozioni.txt");
    }

    public void reset() {
        lista.clear();
    }

    public List<PromozioneDTO> getTutteLePromozioni() {
        return new ArrayList<>(lista);
    }
}