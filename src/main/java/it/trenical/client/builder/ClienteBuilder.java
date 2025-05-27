package it.trenical.client.builder;

import it.trenical.common.grpc.ClienteDTO;

/**
 * Builder per creare oggetti ClienteDTO in modo incrementale.
 * Stile conforme a quanto mostrato dal prof nei pattern creazionali.
 */
public class ClienteBuilder {

    private int id;
    private String nome;
    private String cognome;
    private String email;

    public ClienteBuilder setId(int id) {
        this.id = id;
        return this;
    }

    public ClienteBuilder setNome(String nome) {
        this.nome = nome;
        return this;
    }

    public ClienteBuilder setCognome(String cognome) {
        this.cognome = cognome;
        return this;
    }

    public ClienteBuilder setEmail(String email) {
        this.email = email;
        return this;
    }

    /**
     * Costruisce l'oggetto ClienteDTO gRPC.
     *
     * @return ClienteDTO
     */
    public ClienteDTO build() {
        return ClienteDTO.newBuilder()
                .setId(id)
                .setNome(nome)
                .setCognome(cognome)
                .setEmail(email)
                .build();
    }
}