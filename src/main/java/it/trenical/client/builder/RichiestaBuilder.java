package it.trenical.client.builder;

import it.trenical.common.grpc.*;

public class RichiestaBuilder {

    private TipoRichiesta tipo;
    private ClienteDTO cliente;
    private TrattaDTO tratta;
    private BigliettoDTO biglietto;
    private String messaggio;
    private PromozioneDTO promozione;

    public RichiestaBuilder setTipo(TipoRichiesta tipo) {
        this.tipo = tipo;
        return this;
    }

    public RichiestaBuilder setCliente(ClienteDTO cliente) {
        this.cliente = cliente;
        return this;
    }

    public RichiestaBuilder setTratta(TrattaDTO tratta) {
        this.tratta = tratta;
        return this;
    }

    public RichiestaBuilder setBiglietto(BigliettoDTO biglietto) {
        this.biglietto = biglietto;
        return this;
    }

    public RichiestaBuilder setMessaggio(String messaggio) {
        this.messaggio = messaggio;
        return this;
    }

    public RichiestaBuilder setPromozione(PromozioneDTO promozione) {
        this.promozione = promozione;
        return this;
    }

    public RichiestaDTO build() {
        RichiestaDTO.Builder richiesta = RichiestaDTO.newBuilder();

        if (tipo != null) richiesta.setTipo(tipo);
        if (cliente != null) richiesta.setCliente(cliente);
        if (tratta != null) richiesta.setTratta(tratta);
        if (biglietto != null) richiesta.setBiglietto(biglietto);
        if (messaggio != null) richiesta.setMessaggio(messaggio);
        if (promozione != null) richiesta.setPromozione(promozione);

        return richiesta.build();
    }
}