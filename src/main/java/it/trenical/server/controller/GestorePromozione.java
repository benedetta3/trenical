package it.trenical.server.controller;

import it.trenical.common.grpc.*;
import it.trenical.server.notification.NotificationDispatcher;

public class GestorePromozione implements Gestore {

    @Override
    public RispostaDTO gestisci(RichiestaDTO richiesta) {
        String messaggio = richiesta.getMessaggio();
        String tipoTreno = richiesta.getTratta().getTipoTreno();  // Tipo di treno per la promozione
        String periodo = richiesta.getTratta().getData();         // Periodo promozionale, es. estate 2024
        String tratta = richiesta.getTratta().getStazionePartenza() + " - " + richiesta.getTratta().getStazioneArrivo(); // Tratta

        // Verifica se la promozione è applicabile
        if (tipoTreno.equals("Alta Velocità") && periodo.equals("Estate 2024") && tratta.equals("Milano - Roma")) {
            // Inviamo la promozione
            NotificationDispatcher.getInstance().notifyObservers(messaggio);
            return RispostaDTO.newBuilder()
                    .setEsito(true)
                    .setMessaggio("Promozione inviata con successo.")
                    .build();
        }

        return RispostaDTO.newBuilder()
                .setEsito(false)
                .setMessaggio("Promozione non applicabile.")
                .build();
    }
}