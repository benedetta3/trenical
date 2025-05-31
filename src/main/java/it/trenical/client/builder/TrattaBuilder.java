package it.trenical.client.builder;

import it.trenical.common.grpc.TrattaDTO;

public class TrattaBuilder {

    private TrattaDTO.Builder builder;

    public TrattaBuilder() {
        builder = TrattaDTO.newBuilder();
    }

    public TrattaBuilder setStazionePartenza(String stazionePartenza) {
        if (stazionePartenza != null && !stazionePartenza.isEmpty()) {
            builder.setStazionePartenza(stazionePartenza);
        }
        return this;
    }

    public TrattaBuilder setStazioneArrivo(String stazioneArrivo) {
        if (stazioneArrivo != null && !stazioneArrivo.isEmpty()) {
            builder.setStazioneArrivo(stazioneArrivo);
        }
        return this;
    }

    public TrattaBuilder setData(String data) {
        if (data != null && !data.isEmpty()) {
            builder.setData(data);
        }
        return this;
    }

    public TrattaBuilder setTipoTreno(String tipoTreno) {
        if (tipoTreno != null && !tipoTreno.isEmpty()) {
            builder.setTipoTreno(tipoTreno);
        }
        return this;
    }

    public TrattaBuilder setClasseServizio(String classeServizio) {
        if (classeServizio != null && !classeServizio.isEmpty()) {
            builder.setClasseServizio(classeServizio);
        }
        return this;
    }

    public TrattaDTO build() {
        return builder.build();
    }
}