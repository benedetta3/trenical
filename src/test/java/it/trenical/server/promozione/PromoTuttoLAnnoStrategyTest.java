package it.trenical.server.promozione;

import it.trenical.common.grpc.ClienteDTO;
import it.trenical.common.grpc.TrattaDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PromoTuttoLAnnoStrategyTest {

    private final PromozioneStrategy strategy = new PromoTuttoLAnnoStrategy();

    @Test
    public void testApplicabileClienteFedelta() {
        ClienteDTO cliente = ClienteDTO.newBuilder().setIsFedelta(true).build();
        TrattaDTO tratta = TrattaDTO.newBuilder().setPrezzo(100.0).build();

        assertTrue(strategy.isApplicabile(tratta, cliente));
        assertEquals(90.0, strategy.calcolaPrezzo(tratta), 0.01);
    }

    @Test
    public void testNonApplicabileClienteNonFedelta() {
        ClienteDTO cliente = ClienteDTO.newBuilder().setIsFedelta(false).build();
        TrattaDTO tratta = TrattaDTO.newBuilder().setPrezzo(100.0).build();

        assertFalse(strategy.isApplicabile(tratta, cliente));
    }

    @Test
    public void testPrezzoCorrettoConSconto() {
        TrattaDTO tratta = TrattaDTO.newBuilder().setPrezzo(80.0).build();
        double prezzoScontato = strategy.calcolaPrezzo(tratta);
        assertEquals(72.0, prezzoScontato, 0.01);
    }
}