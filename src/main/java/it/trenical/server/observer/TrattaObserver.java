package it.trenical.server.observer;

import it.trenical.common.grpc.TrattaDTO;

/**
 * Ogni observer di una tratta deve essere notificato quando cambia.
 */
public interface TrattaObserver {
    void update(TrattaDTO trattaAggiornata);
}