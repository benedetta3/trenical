package it.trenical.server.controller;

import it.trenical.common.grpc.*;
import it.trenical.server.db.DatabasePromozioni;

import java.util.List;

public class GestoreVisualizzaPromozioni implements Gestore {

    @Override
    public RispostaDTO gestisci(RichiestaDTO richiesta) {
        List<PromozioneDTO> promozioni = DatabasePromozioni.getInstance().getTutteLePromozioni();

        return RispostaDTO.newBuilder()
                .setEsito(true)
                .setMessaggio("Promozioni caricate con successo.")
                .addAllPromozioni(promozioni)
                .build();
    }
}