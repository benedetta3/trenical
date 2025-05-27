package it.trenical.client.command;

import it.trenical.common.grpc.RichiestaDTO;

/**
 * Interfaccia base per tutti i comandi eseguibili dal client.
 * Ogni comando costruisce una RichiestaDTO da inviare al server.
 */
public interface Command {
    RichiestaDTO esegui();
}