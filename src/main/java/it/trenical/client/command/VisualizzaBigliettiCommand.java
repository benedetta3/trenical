package it.trenical.client.command;

import it.trenical.client.builder.RichiestaBuilder;
import it.trenical.common.grpc.ClienteDTO;
import it.trenical.common.grpc.RichiestaDTO;
import it.trenical.common.grpc.TipoRichiesta;

public class VisualizzaBigliettiCommand implements Command {

    private final ClienteDTO cliente;

    public VisualizzaBigliettiCommand(ClienteDTO cliente) {
        this.cliente = cliente;
    }

    @Override
    public RichiestaDTO esegui() {
        return new RichiestaBuilder()
                .setTipo(TipoRichiesta.VISUALIZZA_BIGLIETTI)
                .setCliente(cliente)
                .build();
    }
}