package it.trenical.server.notification;

import it.trenical.common.grpc.ClienteDTO;
import it.trenical.server.observer.Observer;
import it.trenical.server.observer.Subject;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe che gestisce gli osservatori registrati (clienti fedeltà).
 */
public class NotificationDispatcher implements Subject {

    private final List<ClienteDTO> osservatori = new ArrayList<>();
    private static NotificationDispatcher instance;

    private NotificationDispatcher() {}

    public static synchronized NotificationDispatcher getInstance() {
        if (instance == null) {
            instance = new NotificationDispatcher();
        }
        return instance;
    }

    @Override
    public void attach(ClienteDTO cliente) {
        if (!osservatori.contains(cliente)) {
            osservatori.add(cliente);
        }
    }

    @Override
    public void detach(ClienteDTO cliente) {
        osservatori.remove(cliente);
    }

    @Override
    public void notifyObservers(String messaggio) {
        for (ClienteDTO c : osservatori) {
            // In futuro: invio notifica vera (gRPC, socket, ecc.)
            System.out.println("Notifica per " + c.getNome() + ": " + messaggio);
        }
    }
}