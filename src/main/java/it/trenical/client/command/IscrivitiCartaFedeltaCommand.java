package it.trenical.client.command;

import it.trenical.client.builder.RichiestaBuilder;
import it.trenical.common.grpc.*;

/**
 * Comando per iscrivere un cliente alla carta fedeltà.
 */
public class IscrivitiCartaFedeltaCommand implements Command {

    private final ClienteDTO cliente;

    public IscrivitiCartaFedeltaCommand(ClienteDTO cliente) {
        this.cliente = cliente;
    }

    @Override
    public RichiestaDTO esegui() {
        return new RichiestaBuilder()
                .setTipo(TipoRichiesta.ISCRIVITI)
                .setCliente(cliente)
                .build();
    }
}