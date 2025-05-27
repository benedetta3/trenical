package it.trenical.client.command;

import it.trenical.client.builder.RichiestaBuilder;
import it.trenical.common.grpc.*;

public class ConfermaPrenotazioneCommand implements Command {

    private final int idPrenotazione;

    public ConfermaPrenotazioneCommand(int idPrenotazione) {
        this.idPrenotazione = idPrenotazione;
    }

    @Override
    public RichiestaDTO esegui() {
        BigliettoDTO biglietto = BigliettoDTO.newBuilder()
                .setId(idPrenotazione)
                .build();

        return new RichiestaBuilder()
                .setTipo(TipoRichiesta.CONFERMA)
                .setBiglietto(biglietto)
                .build();
    }
}