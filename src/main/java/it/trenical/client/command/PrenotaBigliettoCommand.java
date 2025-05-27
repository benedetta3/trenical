package it.trenical.client.command;

import it.trenical.client.builder.*;
import it.trenical.common.grpc.*;

public class PrenotaBigliettoCommand implements Command {

    private final TrattaDTO tratta;
    private final double prezzo;
    private final ClienteDTO cliente;

    public PrenotaBigliettoCommand(TrattaDTO tratta, double prezzo, ClienteDTO cliente) {
        this.tratta = tratta;
        this.prezzo = prezzo;
        this.cliente = cliente;
    }

    @Override
    public RichiestaDTO esegui() {
        BigliettoDTO biglietto = new BigliettoBuilder()
                .setTratta(tratta)
                .setCliente(cliente)
                .setPrezzo(prezzo)
                .setStato("PRENOTATO")
                .build();

        return new RichiestaBuilder()
                .setTipo(TipoRichiesta.PRENOTA)
                .setTratta(tratta)
                .setCliente(cliente)
                .setBiglietto(biglietto)
                .build();
    }
}