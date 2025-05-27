package it.trenical.client.command;

import it.trenical.client.builder.BigliettoBuilder;
import it.trenical.client.builder.RichiestaBuilder;
import it.trenical.common.grpc.*;

/**
 * Comando per acquistare un biglietto (con uso di builder pattern).
 */
public class AcquistaBigliettoCommand implements Command {

    private final TrattaDTO tratta;
    private final double prezzo;
    private final ClienteDTO cliente;

    public AcquistaBigliettoCommand(TrattaDTO tratta, double prezzo, ClienteDTO cliente) {
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
                .setStato("ACQUISTATO")
                .build();

        return new RichiestaBuilder()
                .setTipo(TipoRichiesta.ACQUISTA)
                .setCliente(cliente)
                .setTratta(tratta)
                .setBiglietto(biglietto)
                .build();
    }
}