package it.trenical.server.controller;

import it.trenical.common.grpc.*;
import it.trenical.server.db.DatabaseTratte;
import it.trenical.server.db.DatabasePrenotazioni;
import org.junit.jupiter.api.*;

import java.io.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GestorePrenotazioneTest {

    private GestorePrenotazione gestore;
    private ClienteDTO cliente;
    private TrattaDTO tratta;

    @BeforeEach
    public void setUp() {
        DatabaseTratte.getInstance().setPersistenzaAttiva(false);
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

    @Test
    @Order(6)
    public void testConcorrenzaSuPostiDisponibili() throws InterruptedException {
        // Imposta solo 3 posti disponibili
        TrattaDTO trattaLimitata = TrattaDTO.newBuilder(tratta).setPostiDisponibili(3).build();
        DatabaseTratte.getInstance().aggiornaTratta(trattaLimitata);

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successi = new AtomicInteger(0);
        AtomicInteger fallimenti = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final int id = i;
            executor.submit(() -> {
                ClienteDTO c = ClienteDTO.newBuilder()
                        .setId(id)
                        .setNome("Cliente " + id)
                        .setEmail("cliente" + id + "@mail.com")
                        .build();

                RichiestaDTO richiesta = RichiestaDTO.newBuilder()
                        .setCliente(c)
                        .setTratta(tratta)
                        .setMessaggio("1")
                        .build();

                RispostaDTO risposta = gestore.gestisci(richiesta);

                if (risposta.getEsito()) {
                    successi.incrementAndGet();
                } else {
                    fallimenti.incrementAndGet();
                }

                latch.countDown();
            });
        }

        latch.await();
        executor.shutdown();

        System.out.println("Prenotazioni riuscite: " + successi.get());
        System.out.println("Prenotazioni fallite: " + fallimenti.get());

        assertEquals(3, successi.get(), "Devono essere riuscite solo 3 prenotazioni.");
    }

}