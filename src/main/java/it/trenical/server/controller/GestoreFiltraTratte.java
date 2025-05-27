package it.trenical.server.controller;

import it.trenical.common.grpc.*;
import it.trenical.server.db.DatabaseTratte;

import java.util.ArrayList;
import java.util.List;

public class GestoreFiltraTratte implements Gestore {

    @Override
    public RispostaDTO gestisci(RichiestaDTO richiesta) {
        TrattaDTO filtro = richiesta.getTratta();
        List<TrattaDTO> tratteFiltrate = new ArrayList<>();

        // Itera manualmente tutte le tratte e applica i filtri
        for (TrattaDTO tratta : DatabaseTratte.getInstance().getTutteLeTratte()) {
            if (applicaFiltri(tratta, filtro)) {
                tratteFiltrate.add(tratta);
            }
        }

        return RispostaDTO.newBuilder()
                .setEsito(true)
                .setMessaggio("Filtraggio completato.")
                .addAllTratte(tratteFiltrate)
                .build();
    }

    private boolean applicaFiltri(TrattaDTO tratta, TrattaDTO filtro) {
        boolean filtroData = filtro.getData().isEmpty() || tratta.getData().equals(filtro.getData());
        boolean filtroTipoTreno = filtro.getTipoTreno().isEmpty() || tratta.getTipoTreno().equals(filtro.getTipoTreno());

        // Corretto il filtro della classe di servizio
        boolean filtroClasse = filtro.getClasseServizio().isEmpty() || tratta.getClasseServizio().equals(filtro.getClasseServizio());

        return tratta.getStazionePartenza().equals(filtro.getStazionePartenza()) &&
                tratta.getStazioneArrivo().equals(filtro.getStazioneArrivo()) &&
                filtroData &&
                filtroTipoTreno &&
                filtroClasse;
    }
}