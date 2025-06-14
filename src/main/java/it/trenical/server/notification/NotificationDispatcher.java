package it.trenical.server.notification;

import it.trenical.client.notifiche.NotificationObserver;
import it.trenical.common.grpc.PromozioneDTO;
import it.trenical.server.observer.ClienteOsservatore;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class NotificationDispatcher {

    private static NotificationDispatcher instance;

    private final Map<String, NotificationObserver> osservatori;
    private final Map<String, NotificationObserver> observersPromo;

    private NotificationDispatcher() {
        osservatori = new HashMap<>();
        observersPromo = new HashMap<>();
    }

    public static synchronized NotificationDispatcher getInstance() {
        if (instance == null) {
            instance = new NotificationDispatcher();
        }
        return instance;
    }

    public void registra(String email, NotificationObserver osservatore) {
        osservatori.putIfAbsent(email, osservatore);
    }

    public void notifica(String email, String messaggio) {
        NotificationObserver o = osservatori.get(email);
        if (o != null) {
            o.aggiorna(messaggio);
        }
        System.out.println("Notifica a " + email + ": " + messaggio);
    }

    public Set<String> getClientiRegistrati() {
        return osservatori.keySet();
    }

    public void registraPerPromozioni(String email, NotificationObserver osservatore) {
        observersPromo.putIfAbsent(email, osservatore);
    }

    public void notificaNuovaPromozioneFedelta(PromozioneDTO promo) {
        System.out.println("Inizio invio promozione a " + observersPromo.size() + " clienti");

        for (Map.Entry<String, NotificationObserver> entry : observersPromo.entrySet()) {
            String email = entry.getKey();
            NotificationObserver observer = entry.getValue();

            if (observer != null) {
                System.out.println("Inoltro promozione a " + email);
                observer.aggiornaPromozione(promo);
            } else {
                System.out.println("Observer null per " + email);
                ClienteOsservatore.inviaNotificaPromozione(email, promo.getDescrizione());
            }
        }
    }
}