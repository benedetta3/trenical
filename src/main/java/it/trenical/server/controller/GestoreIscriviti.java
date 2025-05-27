package it.trenical.server.controller;

import it.trenical.common.grpc.*;
import it.trenical.server.notification.NotificationDispatcher;

/**
 * Gestore per l'iscrizione alla carta fedeltà.
 * Registra il cliente come osservatore.
 */
public class GestoreIscriviti implements Gestore {

    @Override
    public RispostaDTO gestisci(RichiestaDTO richiesta) {
        ClienteDTO cliente = richiesta.getCliente();

        if (!richiesta.hasCliente()) {
            return RispostaDTO.newBuilder()
                    .setEsito(false)
                    .setMessaggio("Dati cliente mancanti.")
                    .build();
        }

        // Registrazione all'Observer (cliente fedeltà)
        NotificationDispatcher.getInstance().attach(cliente);

        return RispostaDTO.newBuilder()
                .setEsito(true)
                .setMessaggio("Carta fedeltà attivata per " + cliente.getNome())
                .build();
    }
}