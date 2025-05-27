package it.trenical.server.controller;

import it.trenical.common.grpc.*;
import it.trenical.server.db.DatabaseBiglietti;

import java.time.Duration;
import java.time.LocalDateTime;

public class GestoreModifica implements Gestore {

    @Override
    public RispostaDTO gestisci(RichiestaDTO richiesta) {
        BigliettoDTO biglietto = richiesta.getBiglietto();
        ClienteDTO cliente = richiesta.getCliente();
        TrattaDTO tratta = richiesta.getTratta();

        if (biglietto == null || cliente == null) {
            return RispostaDTO.newBuilder()
                    .setEsito(false)
                    .setMessaggio("Dati mancanti per la modifica.")
                    .build();
        }

        // Controllo se il biglietto è in uno stato che permette la modifica
        if (!biglietto.getStato().equals("ACQUISTATO")) {
            return RispostaDTO.newBuilder()
                    .setEsito(false)
                    .setMessaggio("Biglietto non modificabile in questo stato.")
                    .build();
        }

        // Calcolo della penale se la modifica avviene troppo vicina alla partenza
        LocalDateTime orarioPartenza = LocalDateTime.parse(tratta.getOrarioPartenza());
        LocalDateTime oraCorrente = LocalDateTime.now();
        long oreAllaPartenza = Duration.between(oraCorrente, orarioPartenza).toHours();

        double penale = calcolaPenale(oreAllaPartenza, biglietto.getPrezzo());
        double differenzaTariffaria = calcolaDifferenzaTariffaria(biglietto.getClasseServizio(), tratta.getClasseServizio());

        // Calcola il nuovo prezzo
        double nuovoPrezzo = biglietto.getPrezzo() + penale + differenzaTariffaria;

        // Creiamo un nuovo biglietto con la penale e la differenza tariffaria
        BigliettoDTO bigliettoModificato = BigliettoDTO.newBuilder(biglietto)
                .setStato("MODIFICATO")
                .setPrezzo(nuovoPrezzo)  // Nuovo prezzo con penale e differenza
                .build();

        // Aggiungiamo il biglietto modificato al database
        DatabaseBiglietti.getInstance().aggiungiBiglietto(bigliettoModificato);

        return RispostaDTO.newBuilder()
                .setEsito(true)
                .setMessaggio("Biglietto modificato. Penale applicata: " + penale + " e differenza tariffaria: " + differenzaTariffaria)
                .addBiglietti(bigliettoModificato)
                .build();
    }

    // Calcola la penale in base alle ore prima della partenza
    private double calcolaPenale(long oreAllaPartenza, double prezzoBiglietto) {
        if (oreAllaPartenza <= 24) {
            // Penale 10% se la modifica avviene entro 24 ore dalla partenza
            return prezzoBiglietto * 0.10;
        }
        return 0.0; // Nessuna penale se la modifica avviene oltre le 24 ore
    }

    // Calcola la differenza tariffaria se c'è un cambio di classe
    private double calcolaDifferenzaTariffaria(String classeBiglietto, String classeTratta) {
        double differenza = 0.0;

        // Se c'è un cambio di classe, calcoliamo la differenza
        if (!classeBiglietto.equals(classeTratta)) {
            if (classeTratta.equals("Prima") && classeBiglietto.equals("Seconda")) {
                differenza = 20.0; // Ad esempio, la differenza fissa se si passa da Seconda a Prima
            } else if (classeTratta.equals("Seconda") && classeBiglietto.equals("Prima")) {
                differenza = -15.0; // La differenza se si passa da Prima a Seconda
            }
        }
        return differenza;
    }
}