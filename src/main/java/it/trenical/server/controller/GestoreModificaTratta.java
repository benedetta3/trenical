package it.trenical.server.controller;

import it.trenical.common.grpc.*;
import it.trenical.server.db.DatabaseTratte;
import it.trenical.server.observer.TrattaObservable;
import it.trenical.server.observer.ClienteOsservatore;

public class GestoreModificaTratta implements Gestore {

    @Override
    public RispostaDTO gestisci(RichiestaDTO richiesta) {
        TrattaDTO nuovaTratta = richiesta.getTratta();

        if (nuovaTratta == null || !DatabaseTratte.getInstance().contiene(nuovaTratta.getId())) {
            return RispostaDTO.newBuilder()
                    .setEsito(false)
                    .setMessaggio("Tratta da modificare non trovata.")
                    .build();
        }

        TrattaObservable osservabile = DatabaseTratte.getInstance().getTrattaObservable(nuovaTratta.getId());

        ClienteDTO cliente = richiesta.getCliente();
        if (cliente != null) {
            osservabile.attach(new ClienteOsservatore(cliente));
        }

        osservabile.aggiornaTratta(nuovaTratta);

        return RispostaDTO.newBuilder()
                .setEsito(true)
                .setMessaggio("Tratta aggiornata e osservatori notificati.")
                .build();
    }
}