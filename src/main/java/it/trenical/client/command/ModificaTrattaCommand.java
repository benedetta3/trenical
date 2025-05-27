package it.trenical.client.command;

import it.trenical.client.builder.RichiestaBuilder;
import it.trenical.common.grpc.*;

/**
 * Comando per modificare una tratta (es. cambio orario).
 */
public class ModificaTrattaCommand implements Command {

    private final TrattaDTO nuovaTratta;

    public ModificaTrattaCommand(TrattaDTO nuovaTratta) {
        this.nuovaTratta = nuovaTratta;
    }

    @Override
    public RichiestaDTO esegui() {
        return new RichiestaBuilder()
                .setTipo(TipoRichiesta.MODIFICA)
                .setTratta(nuovaTratta)
                .build();
    }
}