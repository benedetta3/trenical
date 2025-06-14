package it.trenical.server.controller;

import it.trenical.common.grpc.*;
import it.trenical.server.db.DatabasePromozioni;
import org.junit.jupiter.api.*;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GestoreVisualizzaPromozioniTest {

    private GestoreVisualizzaPromozioni gestore;

    @BeforeEach
    public void setUp() {
        resetTratteFile();
        gestore = new GestoreVisualizzaPromozioni();
        DatabasePromozioni.getInstance().reset();
    }

    private void resetTratteFile() {
        try (
                BufferedReader reader = new BufferedReader(new FileReader("tratte_originale.txt"));
                BufferedWriter writer = new BufferedWriter(new FileWriter("tratte.txt"))
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            fail("Errore nel reset del file tratte.txt: " + e.getMessage());
        }
    }

    @Test
    @Order(1)
    public void testPromozioniVuote() {
        RichiestaDTO richiesta = RichiestaDTO.newBuilder().build();

        RispostaDTO risposta = gestore.gestisci(richiesta);

        assertTrue(risposta.getEsito());
        assertEquals("Promozioni caricate con successo.", risposta.getMessaggio());
        assertEquals(0, risposta.getPromozioniCount());
    }

    @Test
    @Order(2)
    public void testPromozioniPresenti() {
        PromozioneDTO promo1 = PromozioneDTO.newBuilder()
                .setDescrizione("Promo Estate")
                .setSconto(10.0)
                .setSoloFedelta(false)
                .setClasseStrategy("AltaVelocitaEstateStrategy")
                .build();

        PromozioneDTO promo2 = PromozioneDTO.newBuilder()
                .setDescrizione("Promo Tutto l’anno")
                .setSconto(15.0)
                .setSoloFedelta(true)
                .setClasseStrategy("PromoTuttoLAnnoStrategy")
                .build();

        DatabasePromozioni.getInstance().aggiungiPromozione(promo1);
        DatabasePromozioni.getInstance().aggiungiPromozione(promo2);

        RichiestaDTO richiesta = RichiestaDTO.newBuilder().build();
        RispostaDTO risposta = gestore.gestisci(richiesta);

        assertTrue(risposta.getEsito());
        assertEquals("Promozioni caricate con successo.", risposta.getMessaggio());
        assertEquals(2, risposta.getPromozioniCount());
        assertEquals("Promo Estate", risposta.getPromozioni(0).getDescrizione());
        assertEquals("Promo Tutto l’anno", risposta.getPromozioni(1).getDescrizione());
    }
}