package it.trenical.client.command;

import it.trenical.common.grpc.ClienteDTO;
import it.trenical.common.grpc.RichiestaDTO;
import it.trenical.common.grpc.TrattaDTO;
import it.trenical.common.grpc.TipoRichiesta;

public class RegistraNotificaCommand implements Command {

    private final ClienteDTO cliente;
    private final TrattaDTO tratta;

    public RegistraNotificaCommand(ClienteDTO cliente, TrattaDTO tratta) {
        this.cliente = cliente;
        this.tratta = tratta;
    }

    @Override
    public RichiestaDTO esegui() {
        return RichiestaDTO.newBuilder()
                .setTipo(TipoRichiesta.REGISTRA_NOTIFICA)
                .setCliente(cliente)
                .setTratta(tratta)
                .build();
    }
}