package it.trenical.server.controller;

import it.trenical.common.grpc.*;
import it.trenical.server.db.DatabasePrenotazioni;
import it.trenical.server.db.DatabaseTratte;
import it.trenical.server.model.TrattaObservable;
import it.trenical.server.notification.NotificationDispatcher;

public class GestoreModificaTratta implements Gestore {

    @Override
    public RispostaDTO gestisci(RichiestaDTO richiesta) {
        // Ottieni la tratta da modificare
        TrattaDTO nuovaTratta = richiesta.getTratta();

        // Controlla se la tratta esiste nel database
        if (nuovaTratta == null || !DatabaseTratte.getInstance().contiene(nuovaTratta.getId())) {
            return RispostaDTO.newBuilder()
                    .setEsito(false)
                    .setMessaggio("Tratta da modificare non trovata.")
                    .build();
        }

        // Recupera la tratta osservabile
        TrattaObservable osservabile = DatabaseTratte.getInstance().getTrattaObservable(nuovaTratta.getId());

        // Aggiorna la tratta osservabile
        osservabile.aggiornaTratta(nuovaTratta);

        // Recupera il cliente osservatore dalla prenotazione (se esiste)
        ClienteDTO cliente = DatabasePrenotazioni.getInstance().getClienteOsservatore(richiesta.getCliente().getId());

        // Invio delle notifiche agli osservatori
        NotificationDispatcher.getInstance().notifyObservers(
                "Tratta aggiornata: " + nuovaTratta.getStazionePartenza() + " → " + nuovaTratta.getStazioneArrivo());

        return RispostaDTO.newBuilder()
                .setEsito(true)
                .setMessaggio("Tratta aggiornata e osservatori notificati.")
                .build();
    }
}