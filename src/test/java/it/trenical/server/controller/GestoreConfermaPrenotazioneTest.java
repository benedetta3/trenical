package it.trenical.server.controller;

import it.trenical.common.grpc.*;
import it.trenical.server.db.DatabaseBiglietti;
import it.trenical.server.db.DatabasePrenotazioni;
import it.trenical.server.db.DatabasePromozioni;
import org.junit.jupiter.api.*;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GestoreConfermaPrenotazioneTest {

    private GestoreConfermaPrenotazione gestore;
    private ClienteDTO cliente;
    private TrattaDTO tratta;
    private BigliettoDTO prenotato;

    @BeforeEach
    public void setup() {
        resetTratteFile();

        gestore = new GestoreConfermaPrenotazione();
        DatabasePrenotazioni.getInstance().reset();
        DatabaseBiglietti.getInstance().reset();
        DatabasePromozioni.getInstance().reset(); // se vuoi evitare promozioni nei test

        cliente = ClienteDTO.newBuilder()
                .setId(1)
                .setNome("Luca Rossi")
                .setEmail("luca@rossi.it")
                .build();

        tratta = TrattaDTO.newBuilder()
                .setId(101)
                .setStazionePartenza("Milano")
                .setStazioneArrivo("Torino")
                .setOrarioPartenza("09:00")
                .setOrarioArrivo("11:00")
                .setData("2025-06-20")
                .setTipoTreno("Intercity")
                .setClasseServizio("2A")
                .setPrezzo(45.50)
                .setPostiDisponibili(100)
                .setBinario(4)
                .setStato("regolare")
                .build();

        prenotato = BigliettoDTO.newBuilder()
                .setId(9999)
                .setTratta(tratta)
                .setCliente(cliente)
                .setPrezzo(45.50)
                .setStato("PRENOTATO")
                .setClasseServizio("2A")
                .build();

        DatabasePrenotazioni.getInstance().aggiungiPrenotazione(9999, prenotato, cliente, 1);
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
    public void testConfermaPrenotazioneValida() {
        RichiestaDTO richiesta = RichiestaDTO.newBuilder()
                .setTipo(TipoRichiesta.ACQUISTA)
                .setBiglietto(prenotato)
                .build();

        RispostaDTO risposta = gestore.gestisci(richiesta);

        assertTrue(risposta.getEsito());
        assertTrue(risposta.getMessaggio().contains("Prenotazione confermata"));
        assertEquals(1, risposta.getBigliettiCount());
        assertEquals("ACQUISTATO", risposta.getBiglietti(0).getStato());
    }

    @Test
    @Order(2)
    public void testConfermaPrenotazioneInesistente() {
        RichiestaDTO richiesta = RichiestaDTO.newBuilder()
                .setTipo(TipoRichiesta.ACQUISTA)
                .setBiglietto(BigliettoDTO.newBuilder().setId(123456).build())
                .build();

        RispostaDTO risposta = gestore.gestisci(richiesta);

        assertFalse(risposta.getEsito());
        assertEquals("Prenotazione non trovata o già scaduta.", risposta.getMessaggio());
    }

    @Test
    @Order(3)
    public void testConfermaDatiMancanti() {
        RichiestaDTO richiesta = RichiestaDTO.newBuilder().build();

        RispostaDTO risposta = gestore.gestisci(richiesta);

        assertFalse(risposta.getEsito());
        assertEquals("Prenotazione non trovata o già scaduta.", risposta.getMessaggio());
    }
}