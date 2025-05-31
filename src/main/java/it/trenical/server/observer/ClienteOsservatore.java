package it.trenical.server.observer;

import it.trenical.client.notifiche.NotificationObserver;
import it.trenical.common.grpc.ClienteDTO;
import it.trenical.common.grpc.PromozioneDTO;
import it.trenical.common.grpc.TrattaDTO;
import it.trenical.server.db.DatabaseBiglietti;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClienteOsservatore implements TrattaObserver, NotificationObserver {


    private final ClienteDTO cliente;

    private static final Map<String, List<String>> notificheInCoda = new HashMap<>();

    public ClienteOsservatore(ClienteDTO cliente) {
        this.cliente = cliente;
    }

    @Override
    public void update(TrattaDTO trattaAggiornata) {
        String stato = (trattaAggiornata.getStato() == null || trattaAggiornata.getStato().isEmpty())
                ? "regolare"
                : trattaAggiornata.getStato().toLowerCase();

        String messaggio = "AGGIORNAMENTO BIGLIETTO\n\n" +
                "La tua tratta " + trattaAggiornata.getStazionePartenza() + " → " +
                trattaAggiornata.getStazioneArrivo() + " è stata modificata:\n\n";

        if (stato.equals("ritardo")) {
            messaggio += "RITARDO CONFERMATO\n" +
                    "Nuovo orario partenza: " + trattaAggiornata.getOrarioPartenza() + "\n" +
                    "Nuovo orario arrivo: " + trattaAggiornata.getOrarioArrivo() + "\n";
        } else if (stato.equals("cancellato")) {
            messaggio += "TRENO CANCELLATO\n" +
                    "La tratta è stata cancellata.\n" +
                    "Il tuo biglietto sarà rimborsato automaticamente.\n";
            DatabaseBiglietti.getInstance().rimborsoPerTratta(trattaAggiornata.getId(), cliente);
        } else {
            messaggio += "Informazioni aggiornate:\n" +
                    "Orario partenza: " + trattaAggiornata.getOrarioPartenza() + "\n" +
                    "Orario arrivo: " + trattaAggiornata.getOrarioArrivo() + "\n";
        }

        messaggio += "Binario: " + trattaAggiornata.getBinario() + "\n\n" +
                "Controlla i tuoi biglietti per maggiori dettagli.";

        System.out.println("Invio notifica al client: " + cliente.getEmail());
        inviaNotificaViaGRPC(cliente.getEmail(), messaggio);
    }

    private void inviaNotificaViaGRPC(String email, String messaggio) {
        synchronized (notificheInCoda) {
            notificheInCoda.computeIfAbsent(email, k -> new ArrayList<>()).add(messaggio);
            System.out.println("Notifica salvata per " + email + ": " + messaggio.substring(0, Math.min(50, messaggio.length())) + "...");
        }
    }

    public static List<String> recuperaNotifiche(String email) {
        synchronized (notificheInCoda) {
            List<String> notifiche = notificheInCoda.getOrDefault(email, new ArrayList<>());
            notificheInCoda.remove(email);
            return notifiche;
        }
    }

    public static void notificaATuttiFedelta(String descrizionePromo, List<String> emailFedelta) {
        for (String email : emailFedelta) {
            inviaNotificaPromozione(email, descrizionePromo);
        }
    }

    public static void inviaNotificaPromozione(String email, String descrizionePromo) {
        String messaggio = "NUOVA PROMOZIONE DEDICATA\n\n" +
                descrizionePromo + "\n\n" +
                "Accedi all’app per visualizzare i dettagli e approfittarne!";

        synchronized (notificheInCoda) {
            notificheInCoda.computeIfAbsent(email, k -> new ArrayList<>()).add(messaggio);
            System.out.println("Promo salvata per " + email + ": " + messaggio);
        }
    }

    @Override
    public int hashCode() {
        return cliente.getEmail().hashCode();
    }

    @Override
    public void aggiorna(String messaggio) {
        inviaNotificaViaGRPC(cliente.getEmail(), messaggio);
    }

    @Override
    public void aggiornaPromozione(PromozioneDTO promozione) {
        String destinatari = promozione.getSoloFedelta()
                ? "Riservata ai membri FedeltàTreno"
                : "Disponibile per tutti i clienti";

        String messaggio = "NUOVA PROMOZIONE DISPONIBILE \n\n" +
                "Descrizione: " + promozione.getDescrizione() + "\n" +
                "Sconto: " + promozione.getSconto() + "%\n" +
                destinatari;

        inviaNotificaViaGRPC(cliente.getEmail(), messaggio);
    }

    public static void resetNotifiche() {
        notificheInCoda.clear();
    }
}