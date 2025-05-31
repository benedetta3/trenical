package it.trenical.server.controller;

import it.trenical.common.grpc.*;
import it.trenical.server.db.DatabaseBiglietti;
import it.trenical.server.db.DatabasePrenotazioni;
import it.trenical.server.db.DatabasePromozioni;
import it.trenical.server.promozione.PromozioneStrategy;
import it.trenical.server.promozione.PromozioneStrategyFactory;

import java.util.ArrayList;
import java.util.List;

public class GestoreConfermaPrenotazione implements Gestore {

    @Override
    public RispostaDTO gestisci(RichiestaDTO richiesta) {
        BigliettoDTO richiesto = richiesta.getBiglietto();

        if (richiesto == null || !DatabasePrenotazioni.getInstance().contiene(richiesto.getId())) {
            return RispostaDTO.newBuilder()
                    .setEsito(false)
                    .setMessaggio("Prenotazione non trovata o già scaduta.")
                    .build();
        }

        ClienteDTO cliente = DatabasePrenotazioni.getInstance().getClienteOsservatore(richiesto.getId());
        BigliettoDTO prenotato = DatabasePrenotazioni.getInstance().getPrenotazione(richiesto.getId());
        TrattaDTO tratta = prenotato.getTratta();

        DatabasePrenotazioni.getInstance().rimuoviPrenotazione(richiesto.getId());

        double prezzoOriginale = tratta.getPrezzo();
        double prezzoFinale = prezzoOriginale;
        List<PromozioneDTO> promozioniApplicate = new ArrayList<>();

        for (PromozioneDTO promo : DatabasePromozioni.getInstance().getTutteLePromozioni()) {
            PromozioneStrategy strategia = PromozioneStrategyFactory.getInstance().selezionaStrategia(promo, tratta, cliente);
            if (strategia.isApplicabile(tratta, cliente)) {
                prezzoFinale = strategia.calcolaPrezzo(tratta);
                promozioniApplicate.add(promo);
            }
        }

        double scontoTotale = prezzoOriginale - prezzoFinale;

        int nuovoId = DatabaseBiglietti.getInstance().generaNuovoId();

        BigliettoDTO confermato = BigliettoDTO.newBuilder(prenotato)
                .setId(nuovoId)
                .setCliente(cliente)
                .setStato("ACQUISTATO")
                .setPrezzo(prezzoFinale)
                .build();

        DatabaseBiglietti.getInstance().aggiungiBiglietto(confermato);

        StringBuilder messaggio = new StringBuilder(String.format(
                "Prenotazione confermata.\nPrezzo originale: €%.2f\nSconto totale: -€%.2f\nPrezzo finale: €%.2f",
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
                .addBiglietti(confermato)
                .addAllPromozioni(promozioniApplicate)
                .build();
    }
}