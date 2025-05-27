package it.trenical.server.observer;

import it.trenical.common.grpc.ClienteDTO;
import it.trenical.common.grpc.TrattaDTO;

/**
 * Observer di una tratta: rappresenta un cliente che l'ha prenotata/acquistata.
 */
public class ClienteOsservatore implements TrattaObserver {

    private final ClienteDTO cliente;

    public ClienteOsservatore(ClienteDTO cliente) {
        this.cliente = cliente;
    }

    @Override
    public void update(TrattaDTO trattaAggiornata) {
        // In futuro: invio notifica gRPC
        System.out.println("Cliente " + cliente.getNome() +
                " notificato: tratta aggiornata → " +
                trattaAggiornata.getStazionePartenza() + " → " +
                trattaAggiornata.getStazioneArrivo() + " alle " +
                trattaAggiornata.getOrarioPartenza());
    }
}
