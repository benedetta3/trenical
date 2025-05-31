package it.trenical.server.controller;

import it.trenical.common.grpc.*;
import it.trenical.server.db.DatabaseBiglietti;

import java.util.List;

public class GestoreVisualizzaBiglietti implements Gestore {

    @Override
    public RispostaDTO gestisci(RichiestaDTO richiesta) {
        ClienteDTO cliente = richiesta.getCliente();

        if (!richiesta.hasCliente()) {
            return RispostaDTO.newBuilder()
                    .setEsito(false)
                    .setMessaggio("Cliente non specificato per la visualizzazione biglietti.")
                    .build();
        }

        List<BigliettoDTO> biglietti = DatabaseBiglietti.getInstance().getBigliettiByCliente(cliente);

        RispostaDTO.Builder risposta = RispostaDTO.newBuilder()
                .setEsito(true)
                .setMessaggio("Biglietti trovati: " + biglietti.size());

        for (BigliettoDTO b : biglietti) {
            risposta.addBiglietti(b);
        }

        return risposta.build();
    }
}