package it.trenical.server.promozione;

import it.trenical.common.grpc.ClienteDTO;
import it.trenical.common.grpc.PromozioneDTO;
import it.trenical.common.grpc.TrattaDTO;

public class ScontoPercentualeStrategy implements PromozioneStrategy {

    private final PromozioneDTO promo;

    public ScontoPercentualeStrategy(PromozioneDTO promo) {
        this.promo = promo;
    }

    @Override
    public boolean isApplicabile(TrattaDTO tratta, ClienteDTO cliente) {
        return !promo.getSoloFedelta() || cliente.getIsFedelta();
    }

    @Override
    public double calcolaPrezzo(TrattaDTO tratta) {
        return tratta.getPrezzo() * (1.0 - promo.getSconto() / 100.0);
    }
}
