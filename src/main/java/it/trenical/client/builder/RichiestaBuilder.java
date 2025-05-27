package it.trenical.client.builder;

import it.trenical.common.grpc.*;

/**
 * Builder per creare oggetti RichiestaDTO in modo modulare.
 */
public class RichiestaBuilder {

    private TipoRichiesta tipo;
    private TrattaDTO tratta;
    private BigliettoDTO biglietto;
    private ClienteDTO cliente;
    private String messaggio;

    public RichiestaBuilder setTipo(TipoRichiesta tipo) {
        this.tipo = tipo;
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

    public RichiestaBuilder setCliente(ClienteDTO cliente) {
        this.cliente = cliente;
        return this;
    }

    public RichiestaBuilder setMessaggio(String messaggio) {
        this.messaggio = messaggio;
        return this;
    }

    public RichiestaDTO build() {
        RichiestaDTO.Builder builder = RichiestaDTO.newBuilder()
                .setTipo(tipo);

        if (tratta != null) builder.setTratta(tratta);
        if (biglietto != null) builder.setBiglietto(biglietto);
        if (cliente != null) builder.setCliente(cliente);
        if (messaggio != null) builder.setMessaggio(messaggio);

        return builder.build();
    }
}
