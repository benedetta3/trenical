package it.trenical.server.controller;

import it.trenical.common.grpc.*;
import it.trenical.server.db.DatabaseTratte;
import it.trenical.server.db.DatabasePrenotazioni;
import org.junit.jupiter.api.*;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GestorePrenotazioneTest {

    private GestorePrenotazione gestore;
    private ClienteDTO cliente;
    private TrattaDTO tratta;

    @BeforeEach
    public void setUp() {
        resetTratteFile();

        DatabaseTratte.getInstance().reset();
        DatabasePrenotazioni.getInstance().reset();

        gestore = new GestorePrenotazione();

        cliente = ClienteDTO.newBuilder()
                .setId(1)
                .setNome("Luca Rossi")
                .setEmail("luca@rossi.it")
                .build();

        tratta = TrattaDTO.newBuilder()
                .setId(101)
                .setStazionePartenza("Milano")
                .setStazioneArrivo("Roma")
                .setOrarioPartenza("08:00")
                .setOrarioArrivo("11:00")
                .setData("2025-08-01")
                .setTipoTreno("Frecciarossa")
                .setClasseServizio("1A")
                .setPrezzo(39.99)
                .setPostiDisponibili(10)
                .setBinario(5)
                .setStato("regolare")
                .build();

        DatabaseTratte.getInstance().aggiungiTratta(tratta);
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
    public void testPrenotazioneValida() {
        RichiestaDTO richiesta = RichiestaDTO.newBuilder()
                .setCliente(cliente)
                .setTratta(tratta)
                .setMessaggio("1")
                .build();

        RispostaDTO risposta = gestore.gestisci(richiesta);

        assertTrue(risposta.getEsito());
        assertTrue(risposta.getMessaggio().contains("Prenotazioni effettuate con successo"));
        assertEquals(1, risposta.getBigliettiCount());
        assertEquals("PRENOTATO", risposta.getBiglietti(0).getStato());
    }

    @Test
    @Order(2)
    public void testPrenotazioneQuantitaMultipla() {
        RichiestaDTO richiesta = RichiestaDTO.newBuilder()
                .setCliente(cliente)
                .setTratta(tratta)
                .setMessaggio("3")
                .build();

        RispostaDTO risposta = gestore.gestisci(richiesta);

        assertTrue(risposta.getEsito());
        assertEquals(3, risposta.getBigliettiCount());
        assertTrue(risposta.getMessaggio().contains("Quantità: 3"));
    }

    @Test
    @Order(3)
    public void testPrenotazioneDatiMancanti() {
        DatabaseTratte.getInstance().reset();

        RichiestaDTO richiesta = RichiestaDTO.newBuilder().build();

        RispostaDTO risposta = gestore.gestisci(richiesta);

        assertFalse(risposta.getEsito());
        assertEquals("Cliente o tratta mancanti.", risposta.getMessaggio());
    }

    @Test
    @Order(4)
    public void testPrenotazionePostiInsufficienti() {
        TrattaDTO esaurita = TrattaDTO.newBuilder(tratta)
                .setPostiDisponibili(0)
                .build();
        DatabaseTratte.getInstance().aggiornaTratta(esaurita);

        RichiestaDTO richiesta = RichiestaDTO.newBuilder()
                .setCliente(cliente)
                .setTratta(tratta)
                .setMessaggio("1")
                .build();

        RispostaDTO risposta = gestore.gestisci(richiesta);

        assertFalse(risposta.getEsito());
        assertEquals("Posti disponibili insufficienti.", risposta.getMessaggio());
    }

    @Test
    @Order(5)
    public void testPrenotazioneQuantitaZeroONegativa() {
        RichiestaDTO richiesta = RichiestaDTO.newBuilder()
                .setCliente(cliente)
                .setTratta(tratta)
                .setMessaggio("0")
                .build();

        RispostaDTO risposta = gestore.gestisci(richiesta);

        assertFalse(risposta.getEsito());
        assertEquals("Numero di prenotazioni non valido.", risposta.getMessaggio());
    }
}