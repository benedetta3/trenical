package it.trenical.server.controller;

import it.trenical.common.grpc.*;
import it.trenical.server.db.DatabaseBiglietti;
import it.trenical.server.payment.SimulatorePagamento;

import java.util.Random;

public class GestoreAcquisto implements Gestore {

    @Override
    public RispostaDTO gestisci(RichiestaDTO richiesta) {
        BigliettoDTO biglietto = richiesta.getBiglietto();
        ClienteDTO cliente = richiesta.getCliente();
        TrattaDTO tratta = richiesta.getTratta();

        if (biglietto == null || cliente == null) {
            return RispostaDTO.newBuilder()
                    .setEsito(false)
                    .setMessaggio("Dati mancanti per l'acquisto.")
                    .build();
        }

        // Verifica se il cliente ha già acquistato il biglietto per la stessa tratta
        if (DatabaseBiglietti.getInstance().esisteBigliettoPer(cliente, tratta)) {
            return RispostaDTO.newBuilder()
                    .setEsito(false)
                    .setMessaggio("Hai già acquistato questa tratta.")
                    .build();
        }

        // Simulazione di autorizzazione del pagamento
        if (!SimulatorePagamento.autorizzaPagamento()) {
            return RispostaDTO.newBuilder()
                    .setEsito(false)
                    .setMessaggio("Pagamento non autorizzato.")
                    .build();
        }

        // Simulazione di ID biglietto
        int idGenerato = new Random().nextInt(10000);
        BigliettoDTO bigliettoAcquistato = BigliettoDTO.newBuilder(biglietto)
                .setId(idGenerato)
                .setStato("ACQUISTATO")
                .build();

        // Aggiungi il biglietto al database
        DatabaseBiglietti.getInstance().aggiungiBiglietto(bigliettoAcquistato);

        return RispostaDTO.newBuilder()
                .setEsito(true)
                .setMessaggio("Acquisto completato con successo.")
                .addBiglietti(bigliettoAcquistato)
                .build();
    }
}