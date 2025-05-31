package it.trenical.server.promozione;

import it.trenical.common.grpc.ClienteDTO;
import it.trenical.common.grpc.TrattaDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AltaVelocitaEstateStrategyTest {

    private final PromozioneStrategy strategy = new AltaVelocitaEstateStrategy();

    @Test
    public void testApplicabileInGiugnoAltaVelocita() {
        TrattaDTO tratta = TrattaDTO.newBuilder()
                .setData("2024-06-15")
                .setTipoTreno("Alta Velocità")
                .setPrezzo(100.0)
                .build();

        ClienteDTO cliente = ClienteDTO.newBuilder().build();

        assertTrue(strategy.isApplicabile(tratta, cliente));
        assertEquals(80.0, strategy.calcolaPrezzo(tratta), 0.01);
    }

    @Test
    public void testNonApplicabileFuoriPeriodo() {
        TrattaDTO tratta = TrattaDTO.newBuilder()
                .setData("2024-12-01") // dicembre
                .setTipoTreno("Alta Velocità")
                .setPrezzo(100.0)
                .build();

        ClienteDTO cliente = ClienteDTO.newBuilder().build();

        assertFalse(strategy.isApplicabile(tratta, cliente));
    }

    @Test
    public void testNonApplicabileTipoTrenoErrato() {
        TrattaDTO tratta = TrattaDTO.newBuilder()
                .setData("2024-07-10") // luglio (valido)
                .setTipoTreno("Regionale") // tipo errato
                .setPrezzo(100.0)
                .build();

        ClienteDTO cliente = ClienteDTO.newBuilder().build();

        assertFalse(strategy.isApplicabile(tratta, cliente));
    }

    @Test
    public void testDataMalformata() {
        TrattaDTO tratta = TrattaDTO.newBuilder()
                .setData("invalid-date")
                .setTipoTreno("Alta Velocità")
                .setPrezzo(100.0)
                .build();

        ClienteDTO cliente = ClienteDTO.newBuilder().build();

        assertFalse(strategy.isApplicabile(tratta, cliente));
    }
}