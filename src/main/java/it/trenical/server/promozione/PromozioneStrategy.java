package it.trenical.server.promozione;

import it.trenical.common.grpc.ClienteDTO;
import it.trenical.common.grpc.TrattaDTO;

public interface PromozioneStrategy {
    boolean isApplicabile(TrattaDTO tratta, ClienteDTO cliente);
    double calcolaPrezzo(TrattaDTO tratta);
}