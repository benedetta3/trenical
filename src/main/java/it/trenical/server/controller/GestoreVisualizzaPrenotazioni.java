package it.trenical.server.controller;

import it.trenical.common.grpc.BigliettoDTO;
import it.trenical.common.grpc.ClienteDTO;
import it.trenical.common.grpc.RichiestaDTO;
import it.trenical.common.grpc.RispostaDTO;
import it.trenical.server.db.DatabasePrenotazioni;

import java.util.List;

public class GestoreVisualizzaPrenotazioni implements Gestore {

    @Override
    public RispostaDTO gestisci(RichiestaDTO richiesta) {
        ClienteDTO cliente = richiesta.getCliente();

        if (cliente == null || cliente.getEmail() == null || cliente.getEmail().trim().isEmpty()) {
            return RispostaDTO.newBuilder()
                    .setEsito(false)
                    .setMessaggio("Email cliente mancante.")
                    .build();
        }

        List<BigliettoDTO> prenotazioni = DatabasePrenotazioni.getInstance()
                .getPrenotazioniPerEmail(cliente.getEmail());

        if (prenotazioni.isEmpty()) {
            return RispostaDTO.newBuilder()
                    .setEsito(false)
                    .setMessaggio("Nessuna prenotazione trovata.")
                    .build();
        }

        RispostaDTO.Builder risposta = RispostaDTO.newBuilder()
                .setEsito(true)
                .setMessaggio("Prenotazioni trovate: " + prenotazioni.size());

        for (BigliettoDTO b : prenotazioni) {
            risposta.addBiglietti(b);
        }
        return risposta.build();
    }
}