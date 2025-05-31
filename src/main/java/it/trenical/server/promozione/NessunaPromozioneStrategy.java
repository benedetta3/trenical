package it.trenical.server.promozione;

import it.trenical.common.grpc.ClienteDTO;
import it.trenical.common.grpc.TrattaDTO;

public class NessunaPromozioneStrategy implements PromozioneStrategy {

    @Override
    public boolean isApplicabile(TrattaDTO tratta, ClienteDTO cliente) {
        return false;
    }

    @Override
    public double calcolaPrezzo(TrattaDTO tratta) {
        return tratta.getPrezzo();
    }
}