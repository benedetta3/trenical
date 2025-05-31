package it.trenical.server.controller;

import it.trenical.common.grpc.*;
import it.trenical.server.db.DatabaseBiglietti;
import it.trenical.server.db.DatabaseTratte;
import it.trenical.server.db.DatabasePromozioni;
import it.trenical.server.payment.SimulatorePagamento;
import it.trenical.server.promozione.PromozioneStrategy;
import it.trenical.server.promozione.PromozioneStrategyFactory;

import java.util.ArrayList;
import java.util.List;

public class GestoreAcquisto implements Gestore {

    @Override
    public RispostaDTO gestisci(RichiestaDTO richiesta) {
        BigliettoDTO biglietto = richiesta.getBiglietto();
        ClienteDTO cliente = richiesta.getCliente();
        TrattaDTO tratta = richiesta.getTratta();

        if (!richiesta.hasCliente() || !richiesta.hasTratta() || !richiesta.hasBiglietto()) {
            return RispostaDTO.newBuilder()
                    .setEsito(false)
                    .setMessaggio("Dati mancanti per l'acquisto.")
                    .build();
        }

        if (!isClienteValido(cliente)) {
            return RispostaDTO.newBuilder()
                    .setEsito(false)
                    .setMessaggio("Dati cliente non validi: inserire nome e cognome e una email valida.")
                    .build();
        }

        int idTratta = tratta.getId();
        DatabaseTratte dbTratte = DatabaseTratte.getInstance();
        if (!dbTratte.contiene(idTratta)) {
            return RispostaDTO.newBuilder()
                    .setEsito(false)
                    .setMessaggio("Tratta non trovata.")
                    .build();
        }

        TrattaDTO trattaSalvata = dbTratte.getTrattaObservable(idTratta).getTratta();
        if (trattaSalvata.getPostiDisponibili() <= 0) {
            return RispostaDTO.newBuilder()
                    .setEsito(false)
                    .setMessaggio("Posti esauriti per la tratta selezionata.")
                    .build();
        }

        if (!SimulatorePagamento.autorizzaPagamento()) {
            return RispostaDTO.newBuilder()
                    .setEsito(false)
                    .setMessaggio("Pagamento non autorizzato.")
                    .build();
        }

        TrattaDTO trattaAggiornata = TrattaDTO.newBuilder(trattaSalvata)
                .setPostiDisponibili(trattaSalvata.getPostiDisponibili() - 1)
                .build();
        dbTratte.aggiornaTratta(trattaAggiornata);

        double prezzoOriginale = trattaSalvata.getPrezzo();
        double prezzoFinale = prezzoOriginale;
        List<PromozioneDTO> promozioniApplicate = new ArrayList<>();

        for (PromozioneDTO promo : DatabasePromozioni.getInstance().getTutteLePromozioni()) {
            PromozioneStrategy strategia = PromozioneStrategyFactory.getInstance().selezionaStrategia(promo, trattaSalvata, cliente);
            if (strategia.isApplicabile(trattaSalvata, cliente)) {
                prezzoFinale = strategia.calcolaPrezzo(trattaSalvata);
                promozioniApplicate.add(promo);
            }
        }

        double scontoTotale = prezzoOriginale - prezzoFinale;

        int idGenerato = DatabaseBiglietti.getInstance().generaNuovoId();
        BigliettoDTO bigliettoAcquistato = BigliettoDTO.newBuilder(biglietto)
                .setId(idGenerato)
                .setStato("ACQUISTATO")
                .setPrezzo(prezzoFinale)
                .build();
        DatabaseBiglietti.getInstance().aggiungiBiglietto(bigliettoAcquistato);

        StringBuilder messaggio = new StringBuilder(String.format(
                "Acquisto completato con successo.\nPrezzo originale: €%.2f\nSconto totale: -€%.2f\nPrezzo finale: €%.2f",
                prezzoOriginale, scontoTotale, prezzoFinale
        ));

        if (!promozioniApplicate.isEmpty()) {
            messaggio.append("\n\nPromozioni applicate:");
            for (int i = 0; i < promozioniApplicate.size(); i++) {
                PromozioneDTO p = promozioniApplicate.get(i);
                messaggio.append("\n- ").append(p.getDescrizione()).append(" (").append((int) p.getSconto()).append("%)");
            }
        }

        return RispostaDTO.newBuilder()
                .setEsito(true)
                .setMessaggio(messaggio.toString())
                .addBiglietti(bigliettoAcquistato)
                .addAllPromozioni(promozioniApplicate)
                .build();
    }

    private boolean isClienteValido(ClienteDTO cliente) {
        String nome = cliente.getNome() == null ? "" : cliente.getNome().trim();
        String email = cliente.getEmail() == null ? "" : cliente.getEmail().trim();
        return nome.split("\\s+").length >= 2 && email.contains("@");
    }
}