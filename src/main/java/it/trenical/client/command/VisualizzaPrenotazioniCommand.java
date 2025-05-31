package it.trenical.client.command;

import it.trenical.common.grpc.ClienteDTO;
import it.trenical.common.grpc.RichiestaDTO;
import it.trenical.common.grpc.TipoRichiesta;

public class VisualizzaPrenotazioniCommand implements Command {

    private final ClienteDTO cliente;

    public VisualizzaPrenotazioniCommand(ClienteDTO cliente) {
        this.cliente = cliente;
    }

    @Override
    public RichiestaDTO esegui() {
        return RichiestaDTO.newBuilder()
                .setTipo(TipoRichiesta.VISUALIZZA_PRENOTAZIONI)
                .setCliente(cliente)
                .build();
    }
}