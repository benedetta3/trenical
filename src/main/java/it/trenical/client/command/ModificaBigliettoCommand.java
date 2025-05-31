package it.trenical.client.command;

import it.trenical.client.builder.*;
import it.trenical.common.grpc.*;

public class ModificaBigliettoCommand implements Command {

    private final int idBiglietto;
    private final TrattaDTO nuovaTratta;
    private final ClienteDTO cliente;

    public ModificaBigliettoCommand(int idBiglietto, TrattaDTO nuovaTratta, ClienteDTO cliente) {
        this.idBiglietto = idBiglietto;
        this.nuovaTratta = nuovaTratta;
        this.cliente = cliente;
    }

    @Override
    public RichiestaDTO esegui() {
        BigliettoDTO bigliettoModificato = new BigliettoBuilder()
                .setId(idBiglietto)
                .setTratta(nuovaTratta)
                .setCliente(cliente)
                .setPrezzo(nuovaTratta.getPrezzo())
                .setClasseServizio(nuovaTratta.getClasseServizio())
                .setStato("MODIFICATO")
                .build();

        return new RichiestaBuilder()
                .setTipo(TipoRichiesta.MODIFICA)
                .setCliente(cliente)
                .setTratta(nuovaTratta)
                .setBiglietto(bigliettoModificato)
                .build();
    }
}