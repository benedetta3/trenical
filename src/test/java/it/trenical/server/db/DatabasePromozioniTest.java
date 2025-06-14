package it.trenical.server.db;

import it.trenical.common.grpc.PromozioneDTO;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DatabasePromozioniTest {

    private DatabasePromozioni db;

    @BeforeEach
    public void setup() {
        db = DatabasePromozioni.getInstance();
        db.reset();
    }

    @Test
    @Order(1)
    public void testAggiuntaPromozione() {
        PromozioneDTO p = creaPromozione("Estate", 10.0, false, "NessunaPromozioneStrategy");
        db.aggiungiPromozione(p);

        List<PromozioneDTO> lista = db.getTutteLePromozioni();
        assertEquals(1, lista.size());
        assertEquals("Estate", lista.get(0).getDescrizione());
    }

    @Test
    @Order(2)
    public void testGetTutteLePromozioni() {
        db.aggiungiPromozione(creaPromozione("Promo1", 5.0, false, "ScontoPercentualeStrategy"));
        db.aggiungiPromozione(creaPromozione("Promo2", 8.0, true, "AltaVelocitaEstateStrategy"));

        List<PromozioneDTO> lista = db.getTutteLePromozioni();
        assertEquals(2, lista.size());
    }

    private PromozioneDTO creaPromozione(String descrizione, double sconto, boolean soloFedelta, String strategy) {
        return PromozioneDTO.newBuilder()
                .setDescrizione(descrizione)
                .setSconto(sconto)
                .setSoloFedelta(soloFedelta)
                .setClasseStrategy(strategy)
                .build();
    }
}