package it.trenical.server.promozione;

import it.trenical.common.grpc.ClienteDTO;
import it.trenical.common.grpc.PromozioneDTO;
import it.trenical.common.grpc.TrattaDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ScontoPercentualeStrategyTest {

    private PromozioneDTO creaPromo(double sconto, boolean soloFedelta) {
        return PromozioneDTO.newBuilder()
                .setDescrizione("Promo generica")
                .setSconto(sconto)
                .setSoloFedelta(soloFedelta)
                .build();
    }

    @Test
    public void testApplicabileSempreSeNonFedelta() {
        PromozioneDTO promo = creaPromo(10.0, false);
        ScontoPercentualeStrategy strategy = new ScontoPercentualeStrategy(promo);

        ClienteDTO clienteQualsiasi = ClienteDTO.newBuilder().setIsFedelta(false).build();
        TrattaDTO tratta = TrattaDTO.newBuilder().build();

        assertTrue(strategy.isApplicabile(tratta, clienteQualsiasi));
    }

    @Test
    public void testApplicabileSoloSeClienteFedelta() {
        PromozioneDTO promo = creaPromo(15.0, true);
        ScontoPercentualeStrategy strategy = new ScontoPercentualeStrategy(promo);

        ClienteDTO clienteFedelta = ClienteDTO.newBuilder().setIsFedelta(true).build();
        ClienteDTO clienteNormale = ClienteDTO.newBuilder().setIsFedelta(false).build();
        TrattaDTO tratta = TrattaDTO.newBuilder().build();

        assertTrue(strategy.isApplicabile(tratta, clienteFedelta));
        assertFalse(strategy.isApplicabile(tratta, clienteNormale));
    }

    @Test
    public void testCalcoloPrezzoCorretto() {
        PromozioneDTO promo = creaPromo(20.0, false);
        ScontoPercentualeStrategy strategy = new ScontoPercentualeStrategy(promo);

        TrattaDTO tratta = TrattaDTO.newBuilder().setPrezzo(100.0).build();

        double prezzoScontato = strategy.calcolaPrezzo(tratta);
        assertEquals(80.0, prezzoScontato, 0.01);
    }

    @Test
    public void testZeroScontoRestituiscePrezzoIntero() {
        PromozioneDTO promo = creaPromo(0.0, false);
        ScontoPercentualeStrategy strategy = new ScontoPercentualeStrategy(promo);

        TrattaDTO tratta = TrattaDTO.newBuilder().setPrezzo(55.0).build();

        assertEquals(55.0, strategy.calcolaPrezzo(tratta), 0.01);
    }
}