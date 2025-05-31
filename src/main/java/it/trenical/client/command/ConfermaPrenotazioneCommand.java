package it.trenical.client.command;

import it.trenical.client.builder.RichiestaBuilder;
import it.trenical.common.grpc.*;

public class ConfermaPrenotazioneCommand implements Command {

    private final BigliettoDTO biglietto;
    private final PromozioneDTO promozione;

    public ConfermaPrenotazioneCommand(BigliettoDTO biglietto) {
        this(biglietto, null);
    }

    public ConfermaPrenotazioneCommand(BigliettoDTO biglietto, PromozioneDTO promozione) {
        this.biglietto = biglietto;
        this.promozione = promozione;
    }

    @Override
    public RichiestaDTO esegui() {
        RichiestaBuilder builder = new RichiestaBuilder()
                .setTipo(TipoRichiesta.CONFERMA)
                .setBiglietto(biglietto);
        if (promozione != null) {
            builder.setPromozione(promozione);
        }
        return builder.build();
    }
}
