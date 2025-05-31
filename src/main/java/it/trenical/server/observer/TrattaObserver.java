package it.trenical.server.observer;

import it.trenical.common.grpc.TrattaDTO;

public interface TrattaObserver {
    void update(TrattaDTO trattaAggiornata);
}