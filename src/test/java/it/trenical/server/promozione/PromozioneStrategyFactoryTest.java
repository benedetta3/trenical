package it.trenical.server.promozione;

import it.trenical.common.grpc.ClienteDTO;
import it.trenical.common.grpc.PromozioneDTO;
import it.trenical.common.grpc.TrattaDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PromozioneStrategyFactoryTest {

    private final PromozioneStrategyFactory factory = PromozioneStrategyFactory.getInstance();

    @Test
    public void testScontoPercentualeStrategyPerClasseVuota() {
        PromozioneDTO promo = PromozioneDTO.newBuilder()
                .setDescrizione("Promo base")
                .setSconto(10.0)
                .setSoloFedelta(false)
                .setClasseStrategy("")
                .build();

        TrattaDTO tratta = TrattaDTO.newBuilder()
                .setPrezzo(100.0)
                .build();

        ClienteDTO cliente = ClienteDTO.newBuilder().build();

        PromozioneStrategy strategy = factory.selezionaStrategia(promo, tratta, cliente);

        assertTrue(strategy instanceof ScontoPercentualeStrategy);
        assertTrue(strategy.isApplicabile(tratta, cliente));
        assertEquals(90.0, strategy.calcolaPrezzo(tratta), 0.01);
    }

    @Test
    public void testAltaVelocitaEstateStrategy() {
        PromozioneDTO promo = PromozioneDTO.newBuilder()
                .setDescrizione("Alta Velocità Estate")
                .setSconto(20.0)
                .setSoloFedelta(false)
                .setClasseStrategy("AltaVelocitaEstateStrategy")
                .build();

        TrattaDTO tratta = TrattaDTO.newBuilder()
                .setData("2024-07-01")
                .setTipoTreno("Alta Velocità")
                .setPrezzo(100.0)
                .build();

        ClienteDTO cliente = ClienteDTO.newBuilder().build();

        PromozioneStrategy strategy = factory.selezionaStrategia(promo, tratta, cliente);

        assertTrue(strategy instanceof AltaVelocitaEstateStrategy);
        assertTrue(strategy.isApplicabile(tratta, cliente));
        assertEquals(80.0, strategy.calcolaPrezzo(tratta), 0.01);
    }

    @Test
    public void testClasseStrategyInesistenteRitornaFallback() {
        PromozioneDTO promo = PromozioneDTO.newBuilder()
                .setDescrizione("Errore strategy")
                .setSconto(5.0)
                .setSoloFedelta(false)
                .setClasseStrategy("StrategiaInventata")
                .build();

        TrattaDTO tratta = TrattaDTO.newBuilder().build();
        ClienteDTO cliente = ClienteDTO.newBuilder().build();

        PromozioneStrategy strategy = factory.selezionaStrategia(promo, tratta, cliente);

        assertTrue(strategy instanceof NessunaPromozioneStrategy);
        assertFalse(strategy.isApplicabile(tratta, cliente));
    }
}