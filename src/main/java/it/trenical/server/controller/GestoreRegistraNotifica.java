package it.trenical.server.controller;

import it.trenical.common.grpc.*;
import it.trenical.server.db.DatabaseTratte;
import it.trenical.server.observer.TrattaObservable;
import it.trenical.server.observer.ClienteOsservatore;

public class GestoreRegistraNotifica implements Gestore {

    @Override
    public RispostaDTO gestisci(RichiestaDTO richiesta) {
        ClienteDTO cliente = richiesta.getCliente();
        TrattaDTO tratta = richiesta.getTratta();

        if (!richiesta.hasCliente() || !richiesta.hasTratta()){
            return RispostaDTO.newBuilder()
                    .setEsito(false)
                    .setMessaggio("Cliente o tratta mancanti per registrazione a notifiche.")
                    .build();
        }

        if (!DatabaseTratte.getInstance().contiene(tratta.getId())) {
            return RispostaDTO.newBuilder()
                    .setEsito(false)
                    .setMessaggio("Tratta non trovata.")
                    .build();
        }

        TrattaObservable osservabile = DatabaseTratte.getInstance().getTrattaObservable(tratta.getId());
        osservabile.attach(new ClienteOsservatore(cliente));

        return RispostaDTO.newBuilder()
                .setEsito(true)
                .setMessaggio("Registrazione a notifiche completata.")
                .build();
    }
}