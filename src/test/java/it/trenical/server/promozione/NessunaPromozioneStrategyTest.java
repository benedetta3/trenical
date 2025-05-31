package it.trenical.server.promozione;

import it.trenical.common.grpc.ClienteDTO;
import it.trenical.common.grpc.TrattaDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class NessunaPromozioneStrategyTest {

    private final PromozioneStrategy strategy = new NessunaPromozioneStrategy();

    @Test
    public void testIsApplicabileSempreFalse() {
        TrattaDTO tratta = TrattaDTO.newBuilder()
                .setPrezzo(50.0)
                .setData("2024-06-20")
                .build();

        ClienteDTO cliente = ClienteDTO.newBuilder().setIsFedelta(true).build();

        assertFalse(strategy.isApplicabile(tratta, cliente));
    }

    @Test
    public void testCalcolaPrezzoRestituiscePrezzoIntero() {
        TrattaDTO tratta = TrattaDTO.newBuilder()
                .setPrezzo(77.5)
                .build();

        double prezzo = strategy.calcolaPrezzo(tratta);
        assertEquals(77.5, prezzo, 0.01);
    }
}