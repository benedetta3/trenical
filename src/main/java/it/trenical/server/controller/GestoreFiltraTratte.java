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

        for (TrattaDTO tratta : DatabaseTratte.getInstance().getTutteLeTratte()) {
            if (applicaFiltri(tratta, filtro)) {
                tratteFiltrate.add(tratta);
            }
        }

        if (tratteFiltrate.isEmpty()) {
            return RispostaDTO.newBuilder()
                    .setEsito(false)
                    .setMessaggio("Nessuna tratta trovata con i criteri specificati")
                    .build();
        }

        return RispostaDTO.newBuilder()
                .setEsito(true)
                .setMessaggio("Filtraggio completato")
                .addAllTratte(tratteFiltrate)
                .build();
    }

    private boolean applicaFiltri(TrattaDTO tratta, TrattaDTO filtro) {
        String partenzaFiltro = filtro.getStazionePartenza() == null ? "" : filtro.getStazionePartenza().trim().toLowerCase();
        String arrivoFiltro = filtro.getStazioneArrivo() == null ? "" : filtro.getStazioneArrivo().trim().toLowerCase();
        String dataFiltro = filtro.getData() == null ? "" : filtro.getData().trim();
        String tipoTrenoFiltro = filtro.getTipoTreno() == null ? "" : filtro.getTipoTreno().trim().toLowerCase();
        String classeFiltro = filtro.getClasseServizio() == null ? "" : filtro.getClasseServizio().trim();

        String partenzaTratta = tratta.getStazionePartenza() == null ? "" : tratta.getStazionePartenza().trim().toLowerCase();
        String arrivoTratta = tratta.getStazioneArrivo() == null ? "" : tratta.getStazioneArrivo().trim().toLowerCase();
        String dataTratta = tratta.getData() == null ? "" : tratta.getData().trim();
        String tipoTrenoTratta = tratta.getTipoTreno() == null ? "" : tratta.getTipoTreno().trim().toLowerCase();
        String classeTratta = tratta.getClasseServizio() == null ? "" : tratta.getClasseServizio().trim();

        if (!partenzaTratta.equals(partenzaFiltro)) return false;
        if (!arrivoTratta.equals(arrivoFiltro)) return false;

        if (!dataFiltro.isEmpty() && !dataTratta.equals(dataFiltro)) return false;

        if (!tipoTrenoFiltro.isEmpty() && !tipoTrenoTratta.equals(tipoTrenoFiltro)) return false;

        if (!classeFiltro.isEmpty() && !classeTratta.equals(classeFiltro)) return false;

        return true;
    }
}