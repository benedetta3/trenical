package it.trenical.server.controller;

import it.trenical.common.grpc.*;
import it.trenical.server.db.DatabasePrenotazioni;

public class GestoreConfermaPrenotazione implements Gestore {

    @Override
    public RispostaDTO gestisci(RichiestaDTO richiesta) {
        BigliettoDTO richiesto = richiesta.getBiglietto();

        if (richiesto == null || !DatabasePrenotazioni.getInstance().contiene(richiesto.getId())) {
            return RispostaDTO.newBuilder()
                    .setEsito(false)
                    .setMessaggio("Prenotazione non trovata o già scaduta.")
                    .build();
        }

        // Recupera e rimuove la prenotazione
        ClienteDTO cliente = DatabasePrenotazioni.getInstance().getClienteOsservatore(richiesto.getId());
        DatabasePrenotazioni.getInstance().rimuoviPrenotazione(richiesto.getId());

        BigliettoDTO confermato = BigliettoDTO.newBuilder(richiesto)
                .setCliente(cliente)
                .setStato("ACQUISTATO")
                .build();

        return RispostaDTO.newBuilder()
                .setEsito(true)
                .setMessaggio("Prenotazione confermata.")
                .addBiglietti(confermato)
                .build();
    }
}