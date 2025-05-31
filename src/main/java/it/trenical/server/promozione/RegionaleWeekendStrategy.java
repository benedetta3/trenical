package it.trenical.server.promozione;

import it.trenical.common.grpc.ClienteDTO;
import it.trenical.common.grpc.TrattaDTO;

import java.time.DayOfWeek;
import java.time.LocalDate;

public class RegionaleWeekendStrategy implements PromozioneStrategy {

    @Override
    public boolean isApplicabile(TrattaDTO tratta, ClienteDTO cliente) {
        try {
            if (!"Regionale".equalsIgnoreCase(tratta.getTipoTreno())) return false;

            LocalDate data = LocalDate.parse(tratta.getData());
            DayOfWeek giorno = data.getDayOfWeek();

            return giorno == DayOfWeek.SATURDAY || giorno == DayOfWeek.SUNDAY;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public double calcolaPrezzo(TrattaDTO tratta) {
        return tratta.getPrezzo() * 0.85;
    }
}
