package it.trenical.server.promozione;

import it.trenical.common.grpc.ClienteDTO;
import it.trenical.common.grpc.TrattaDTO;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class RegionaleWeekendStrategyTest {

    private final PromozioneStrategy strategy = new RegionaleWeekendStrategy();

    private final ClienteDTO cliente = ClienteDTO.newBuilder().build();

    @Test
    public void testApplicabileSabato() {
        LocalDate sabato = LocalDate.of(2024, 6, 22);
        TrattaDTO tratta = TrattaDTO.newBuilder()
                .setTipoTreno("Regionale")
                .setData(sabato.toString())
                .setPrezzo(100.0)
                .build();

        assertTrue(strategy.isApplicabile(tratta, cliente));
        assertEquals(85.0, strategy.calcolaPrezzo(tratta), 0.01);
    }

    @Test
    public void testApplicabileDomenica() {
        LocalDate domenica = LocalDate.of(2024, 6, 23);
        TrattaDTO tratta = TrattaDTO.newBuilder()
                .setTipoTreno("Regionale")
                .setData(domenica.toString())
                .build();

        assertTrue(strategy.isApplicabile(tratta, cliente));
    }

    @Test
    public void testNonApplicabileLunedi() {
        LocalDate lunedi = LocalDate.of(2024, 6, 24);
        TrattaDTO tratta = TrattaDTO.newBuilder()
                .setTipoTreno("Regionale")
                .setData(lunedi.toString())
                .build();

        assertFalse(strategy.isApplicabile(tratta, cliente));
    }

    @Test
    public void testNonApplicabileTrenoDiverso() {
        TrattaDTO tratta = TrattaDTO.newBuilder()
                .setTipoTreno("Frecciarossa")
                .setData("2024-06-22")
                .build();

        assertFalse(strategy.isApplicabile(tratta, cliente));
    }
}
