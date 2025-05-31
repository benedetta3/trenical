package it.trenical.server.controller;

import it.trenical.common.grpc.*;
import it.trenical.server.db.DatabaseBiglietti;
import it.trenical.server.db.DatabaseTratte;
import it.trenical.server.payment.SimulatorePagamento;

import java.time.*;
import java.time.format.DateTimeFormatter;

public class GestoreModifica implements Gestore {

    @Override
    public RispostaDTO gestisci(RichiestaDTO richiesta) {
        if (!richiesta.hasBiglietto() || !richiesta.hasCliente() || !richiesta.hasTratta()) {
            return RispostaDTO.newBuilder()
                    .setEsito(false)
                    .setMessaggio("Dati mancanti per la modifica.")
                    .build();
        }

        BigliettoDTO biglietto = richiesta.getBiglietto();
        ClienteDTO cliente = richiesta.getCliente();
        TrattaDTO nuovaTratta = richiesta.getTratta();

        DatabaseTratte dbTratte = DatabaseTratte.getInstance();
        DatabaseBiglietti dbBiglietti = DatabaseBiglietti.getInstance();

        if (!dbTratte.contiene(nuovaTratta.getId())) {
            return RispostaDTO.newBuilder()
                    .setEsito(false)
                    .setMessaggio("Tratta selezionata non disponibile.")
                    .build();
        }

        TrattaDTO vecchiaTratta = dbTratte.getTratta(biglietto.getTratta().getId());
        nuovaTratta = dbTratte.getTratta(nuovaTratta.getId());

        double prezzoVecchio = vecchiaTratta.getPrezzo();
        double prezzoNuovo = nuovaTratta.getPrezzo();

        double penale = 0.0;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            LocalDateTime partenza = LocalDateTime.parse(vecchiaTratta.getData() + " " + vecchiaTratta.getOrarioPartenza(), formatter);
            Duration tempoResiduo = Duration.between(LocalDateTime.now(), partenza);

            if (tempoResiduo.toHours() < 24) {
                penale = prezzoVecchio * 0.2;
            }
        } catch (Exception e) {
            return RispostaDTO.newBuilder()
                    .setEsito(false)
                    .setMessaggio("Errore nel parsing della data di partenza.")
                    .build();
        }

        double rimborso = 0.0;
        double daPagare = 0.0;

        if (prezzoNuovo > prezzoVecchio) {
            daPagare += prezzoNuovo - prezzoVecchio;
        } else {
            rimborso = prezzoVecchio - prezzoNuovo;
        }

        if (penale > 0.0) {
            daPagare += penale;
        }

        if (daPagare > 0.0) {
            if (!SimulatorePagamento.effettuaPagamento(cliente, daPagare)) {
                return RispostaDTO.newBuilder()
                        .setEsito(false)
                        .setMessaggio("Pagamento non autorizzato per un totale di " + daPagare + " €.")
                        .build();
            }
        }

        if (rimborso > 0.0) {
            System.out.println("Rimborso di " + rimborso + "€ per il cliente " + cliente.getNome());
        }

        TrattaDTO aggiornataVecchia = TrattaDTO.newBuilder(vecchiaTratta)
                .setPostiDisponibili(vecchiaTratta.getPostiDisponibili() + 1)
                .build();

        TrattaDTO aggiornataNuova = TrattaDTO.newBuilder(nuovaTratta)
                .setPostiDisponibili(nuovaTratta.getPostiDisponibili() - 1)
                .build();

        dbTratte.aggiornaTratta(aggiornataVecchia);
        dbTratte.aggiornaTratta(aggiornataNuova);

        BigliettoDTO bigliettoModificato = BigliettoDTO.newBuilder(biglietto)
                .setTratta(aggiornataNuova)
                .setPrezzo(prezzoNuovo)
                .setClasseServizio(aggiornataNuova.getClasseServizio())
                .build();

        dbBiglietti.rimuoviBiglietto(biglietto.getId());
        dbBiglietti.aggiungiBiglietto(bigliettoModificato);

        StringBuilder messaggio = new StringBuilder("Modifica completata. ");
        if (daPagare > 0.0) {
            messaggio.append("Pagamento effettuato di ").append(String.format("%.2f", daPagare)).append(" €. ");
        }
        if (rimborso > 0.0) {
            messaggio.append("Rimborso effettuato di ").append(String.format("%.2f", rimborso)).append(" €. ");
        }

        return RispostaDTO.newBuilder()
                .setEsito(true)
                .setMessaggio(messaggio.toString())
                .addBiglietti(bigliettoModificato)
                .build();
    }
}