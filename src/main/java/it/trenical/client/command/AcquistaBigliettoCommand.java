package it.trenical.client.command;

import it.trenical.client.builder.BigliettoBuilder;
import it.trenical.client.builder.RichiestaBuilder;
import it.trenical.common.grpc.*;

public class AcquistaBigliettoCommand implements Command {

    private final TrattaDTO tratta;
    private final ClienteDTO cliente;
    private final PromozioneDTO promozione;

    public AcquistaBigliettoCommand(TrattaDTO tratta, ClienteDTO cliente, PromozioneDTO promozione) {
        this.tratta = tratta;
        this.cliente = cliente;
        this.promozione = promozione;
    }

    @Override
    public RichiestaDTO esegui() {
        BigliettoDTO biglietto = new BigliettoBuilder()
                .setTratta(tratta)
                .setCliente(cliente)
                .setClasseServizio(tratta.getClasseServizio())
                .setStato("ACQUISTATO")
                .build();

        RichiestaBuilder builder = new RichiestaBuilder()
                .setTipo(TipoRichiesta.ACQUISTA)
                .setCliente(cliente)
                .setTratta(tratta)
                .setBiglietto(biglietto);

        if (promozione != null) {
            builder.setPromozione(promozione);
        }

        return builder.build();
    }
}