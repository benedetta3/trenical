package it.trenical.server.controller;

import it.trenical.common.grpc.*;
import it.trenical.server.notification.NotificationDispatcher;
import it.trenical.server.observer.ClienteOsservatore;

import java.util.List;

public class GestoreNotificaClient implements Gestore {

    @Override
    public RispostaDTO gestisci(RichiestaDTO richiesta) {
        ClienteDTO cliente = richiesta.getCliente();

        if (cliente == null || cliente.getEmail() == null || cliente.getEmail().trim().isEmpty()) {
            return RispostaDTO.newBuilder()
                    .setEsito(false)
                    .setMessaggio("Email cliente mancante per controllo notifiche.")
                    .build();
        }

        String email = cliente.getEmail();

        if (cliente.getIsFedelta() && cliente.getRiceviPromo()) {
            NotificationDispatcher.getInstance().registraPerPromozioni(email, new ClienteOsservatore(cliente));
            System.out.println("Cliente " + email + " registrato per ricevere promozioni FedeltàTreno");
        }

        List<String> notifiche = ClienteOsservatore.recuperaNotifiche(email);

        RispostaDTO.Builder risposta = RispostaDTO.newBuilder().setEsito(true);

        if (notifiche.isEmpty()) {
            risposta.setMessaggio("Nessuna notifica");
        } else {
            risposta.setMessaggio("Notifiche trovate: " + notifiche.size());
            for (String messaggio : notifiche) {
                NotificaDTO notifica = NotificaDTO.newBuilder()
                        .setEmail(email)
                        .setMessaggio(messaggio)
                        .setTimestamp(java.time.LocalDateTime.now().toString())
                        .build();
                risposta.addNotifiche(notifica);
            }
        }
        return risposta.build();
    }
}