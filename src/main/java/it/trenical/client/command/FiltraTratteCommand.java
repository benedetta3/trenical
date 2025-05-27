package it.trenical.client.command;

import it.trenical.client.builder.RichiestaBuilder;
import it.trenical.common.grpc.*;

public class FiltraTratteCommand implements Command {

    private final String stazionePartenza;
    private final String stazioneArrivo;
    private final String data;
    private final String tipoTreno;
    private final String classeServizio;

    public FiltraTratteCommand(String stazionePartenza, String stazioneArrivo, String data,
                               String tipoTreno, String classeServizio) {
        this.stazionePartenza = stazionePartenza;
        this.stazioneArrivo = stazioneArrivo;
        this.data = data;
        this.tipoTreno = tipoTreno;
        this.classeServizio = classeServizio;
    }

    @Override
    public RichiestaDTO esegui() {
        TrattaDTO tratta = TrattaDTO.newBuilder()
                .setStazionePartenza(stazionePartenza)
                .setStazioneArrivo(stazioneArrivo)
                .setData(data)
                .setTipoTreno(tipoTreno)
                .build();

        return new RichiestaBuilder()
                .setTipo(TipoRichiesta.FILTRA)
                .setTratta(tratta)
                .setMessaggio(classeServizio) // messaggio usato come filtro per classe
                .build();
    }
}