package it.trenical.server.promozione;

import it.trenical.common.grpc.ClienteDTO;
import it.trenical.common.grpc.TrattaDTO;

import java.time.LocalDate;

public class AltaVelocitaEstateStrategy implements PromozioneStrategy {

    public boolean isApplicabile(TrattaDTO tratta, ClienteDTO cliente) {
        String data = tratta.getData();
        try {
            LocalDate giorno = LocalDate.parse(data);
            return (giorno.getMonthValue() >= 6 && giorno.getMonthValue() <= 8) &&
                    tratta.getTipoTreno().equalsIgnoreCase("Alta Velocità");
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public double calcolaPrezzo(TrattaDTO tratta) {
        return tratta.getPrezzo() * 0.8;
    }
}