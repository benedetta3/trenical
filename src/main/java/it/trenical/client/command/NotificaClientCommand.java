package it.trenical.client.command;

import it.trenical.common.grpc.ClienteDTO;
import it.trenical.common.grpc.RichiestaDTO;
import it.trenical.common.grpc.TipoRichiesta;

public class NotificaClientCommand implements Command {
    private final ClienteDTO cliente;

    public NotificaClientCommand(ClienteDTO cliente) {
        this.cliente = cliente;
    }

    @Override
    public RichiestaDTO esegui() {
        return RichiestaDTO.newBuilder()
                .setTipo(TipoRichiesta.NOTIFICA_CLIENT)
                .setCliente(cliente)
                .build();
    }
}