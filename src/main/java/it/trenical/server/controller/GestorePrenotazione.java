package it.trenical.server.controller;

import it.trenical.common.grpc.*;
import it.trenical.server.db.DatabasePrenotazioni;
import it.trenical.server.notification.NotificationDispatcher;

import java.util.Random;
import java.util.concurrent.*;

/**
 * Gestore per la prenotazione di biglietti con scadenza automatica e notifica di avviso.
 */
public class GestorePrenotazione implements Gestore {

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Override
    public RispostaDTO gestisci(RichiestaDTO richiesta) {
        BigliettoDTO biglietto = richiesta.getBiglietto();
        ClienteDTO cliente = richiesta.getCliente();

        if (biglietto == null || cliente == null) {
            return RispostaDTO.newBuilder()
                    .setEsito(false)
                    .setMessaggio("Dati mancanti per la prenotazione.")
                    .build();
        }

        int id = new Random().nextInt(100000);
        BigliettoDTO prenotato = BigliettoDTO.newBuilder(biglietto)
                .setId(id)
                .setStato("PRENOTATO")
                .build();

        // Aggiungi la prenotazione
        DatabasePrenotazioni.getInstance().aggiungiPrenotazione(id, prenotato, cliente);

        // Avvia il timer per la scadenza (2 minuti)
        scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                if (DatabasePrenotazioni.getInstance().contiene(id)) {
                    DatabasePrenotazioni.getInstance().rimuoviPrenotazione(id);
                    NotificationDispatcher.getInstance().notifyObservers(
                            "Prenotazione scaduta per cliente: " + cliente.getNome()
                    );
                }
            }
        }, 2, TimeUnit.MINUTES);

        // Aggiungi timer per avviso 1 minuto prima della scadenza
        scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                if (DatabasePrenotazioni.getInstance().contiene(id)) {
                    NotificationDispatcher.getInstance().notifyObservers(
                            "La tua prenotazione sta per scadere! Conferma ora!"
                    );
                }
            }
        }, 1, TimeUnit.MINUTES);

        return RispostaDTO.newBuilder()
                .setEsito(true)
                .setMessaggio("Prenotazione registrata. Scade tra 2 minuti.")
                .addBiglietti(prenotato)
                .build();
    }
}