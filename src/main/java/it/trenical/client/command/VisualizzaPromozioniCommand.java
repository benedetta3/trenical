package it.trenical.client.command;

import it.trenical.client.builder.RichiestaBuilder;
import it.trenical.common.grpc.RichiestaDTO;
import it.trenical.common.grpc.TipoRichiesta;

public class VisualizzaPromozioniCommand implements Command {

    @Override
    public RichiestaDTO esegui() {
        return new RichiestaBuilder()
                .setTipo(TipoRichiesta.VISUALIZZA_PROMOZIONI)
                .build();
    }
}