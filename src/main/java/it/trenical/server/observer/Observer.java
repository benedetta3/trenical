package it.trenical.server.observer;

import it.trenical.common.grpc.ClienteDTO;

/**
 * Interfaccia Observer: ogni osservatore riceve una notifica.
 */
public interface Observer {
    void update(String messaggio, ClienteDTO cliente);
}