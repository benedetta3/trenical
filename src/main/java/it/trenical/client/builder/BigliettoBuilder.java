package it.trenical.client.builder;

import it.trenical.common.grpc.*;

public class BigliettoBuilder {

    private int id;
    private TrattaDTO tratta;
    private ClienteDTO cliente;
    private double prezzo;
    private String stato;
    private String classeServizio;

    public BigliettoBuilder setId(int id) {
        this.id = id;
        return this;
    }

    public BigliettoBuilder setTratta(TrattaDTO tratta) {
        this.tratta = tratta;
        return this;
    }

    public BigliettoBuilder setCliente(ClienteDTO cliente) {
        this.cliente = cliente;
        return this;
    }

    public BigliettoBuilder setPrezzo(double prezzo) {
        this.prezzo = prezzo;
        return this;
    }

    public BigliettoBuilder setStato(String stato) {
        this.stato = stato;
        return this;
    }

    public BigliettoBuilder setClasseServizio(String classeServizio) {
        this.classeServizio = classeServizio;
        return this;
    }

    public BigliettoDTO build() {
        return BigliettoDTO.newBuilder()
                .setId(id)
                .setTratta(tratta)
                .setCliente(cliente)
                .setPrezzo(prezzo)
                .setStato(stato)
                .setClasseServizio(classeServizio)
                .build();
    }
}