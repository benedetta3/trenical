package it.trenical.server.promozione;

import it.trenical.common.grpc.ClienteDTO;
import it.trenical.common.grpc.TrattaDTO;

public class PromoTuttoLAnnoStrategy implements PromozioneStrategy {

    @Override
    public boolean isApplicabile(TrattaDTO tratta, ClienteDTO cliente) {
        return cliente.getIsFedelta();
    }

    @Override
    public double calcolaPrezzo(TrattaDTO tratta) {
        return tratta.getPrezzo() * 0.9;
    }
}