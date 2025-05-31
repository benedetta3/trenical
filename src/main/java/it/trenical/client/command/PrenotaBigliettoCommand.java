package it.trenical.client.command;

import it.trenical.client.builder.BigliettoBuilder;
import it.trenical.client.builder.RichiestaBuilder;
import it.trenical.common.grpc.*;

public class PrenotaBigliettoCommand implements Command {

    private final TrattaDTO tratta;
    private final ClienteDTO cliente;

    public PrenotaBigliettoCommand(TrattaDTO tratta, ClienteDTO cliente) {
        this.tratta = tratta;
        this.cliente = cliente;
    }

    @Override
    public RichiestaDTO esegui() {
        BigliettoDTO biglietto = new BigliettoBuilder()
                .setTratta(tratta)
                .setCliente(cliente)
                .setPrezzo(tratta.getPrezzo())
                .setClasseServizio(tratta.getClasseServizio())
                .setStato("PRENOTATO")
                .build();

        return new RichiestaBuilder()
                .setTipo(TipoRichiesta.PRENOTA)
                .setCliente(cliente)
                .setTratta(tratta)
                .setBiglietto(biglietto)
                .build();
    }
}