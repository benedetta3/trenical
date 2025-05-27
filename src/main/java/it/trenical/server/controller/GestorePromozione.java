package it.trenical.server.controller;

import it.trenical.common.grpc.*;
import it.trenical.server.notification.NotificationDispatcher;
import it.trenical.server.promozione.PromozioneStrategy;
import it.trenical.server.promozione.PromozioneStrategyFactory;

public class GestorePromozione implements Gestore {

    @Override
    public RispostaDTO gestisci(RichiestaDTO richiesta) {
        String messaggio = richiesta.getMessaggio();
        TrattaDTO tratta = richiesta.getTratta();

        if (messaggio == null || messaggio.trim().isEmpty()) {
            return RispostaDTO.newBuilder()
                    .setEsito(false)
                    .setMessaggio("Messaggio promozionale mancante.")
                    .build();
        }

        PromozioneStrategy strategy = PromozioneStrategyFactory.selezionaStrategy(richiesta.getTratta());

        if (!strategy.isApplicabile(tratta)) {
            return RispostaDTO.newBuilder()
                    .setEsito(false)
                    .setMessaggio("Promozione non applicabile.")
                    .build();
        }

        NotificationDispatcher.getInstance().notifyObservers(messaggio);

        return RispostaDTO.newBuilder()
                .setEsito(true)
                .setMessaggio("Promozione inviata a tutti i clienti fedeltà.")
                .build();
    }
}