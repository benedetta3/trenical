package it.trenical.client.notifiche;

import it.trenical.common.grpc.PromozioneDTO;

public interface NotificationObserver {
    void aggiorna(String messaggio);
    void aggiornaPromozione(PromozioneDTO promozione);
}