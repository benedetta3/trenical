package it.trenical.server.observer;

import it.trenical.common.grpc.ClienteDTO;

public interface Subject {
    void attach(ClienteDTO cliente);
    void detach(ClienteDTO cliente);
    void notifyObservers(String messaggio);
}