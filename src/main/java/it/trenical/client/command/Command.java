package it.trenical.client.command;

import it.trenical.common.grpc.RichiestaDTO;

public interface Command {
    RichiestaDTO esegui();
}