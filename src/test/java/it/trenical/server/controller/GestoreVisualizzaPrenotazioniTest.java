package it.trenical.server.controller;

import it.trenical.common.grpc.*;
import it.trenical.server.db.DatabasePrenotazioni;
import org.junit.jupiter.api.*;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GestoreVisualizzaPrenotazioniTest {

    private GestoreVisualizzaPrenotazioni gestore;
    private ClienteDTO cliente;

    @BeforeEach
    public void setUp() {
        resetTratteFile();

        gestore = new GestoreVisualizzaPrenotazioni();
        DatabasePrenotazioni.getInstance().reset();

        cliente = ClienteDTO.newBuilder()
                .setId(1)
                .setNome("Luca")
                .setEmail("luca@rossi.it")
                .build();
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
    public void testClienteSenzaEmail() {
        ClienteDTO clienteSenzaEmail = ClienteDTO.newBuilder().build();

        RichiestaDTO richiesta = RichiestaDTO.newBuilder()
                .setCliente(clienteSenzaEmail)
                .build();

        RispostaDTO risposta = gestore.gestisci(richiesta);

        assertFalse(risposta.getEsito());
        assertEquals("Email cliente mancante.", risposta.getMessaggio());
    }

    @Test
    @Order(2)
    public void testNessunaPrenotazione() {
        RichiestaDTO richiesta = RichiestaDTO.newBuilder()
                .setCliente(cliente)
                .build();

        RispostaDTO risposta = gestore.gestisci(richiesta);

        assertFalse(risposta.getEsito());
        assertEquals("Nessuna prenotazione trovata.", risposta.getMessaggio());
    }

    @Test
    @Order(3)
    public void testPrenotazioniPresenti() {
        TrattaDTO tratta = TrattaDTO.newBuilder()
                .setId(101)
                .setStazionePartenza("Firenze")
                .setStazioneArrivo("Napoli")
                .setData("2025-06-10")
                .setOrarioPartenza("08:00")
                .setOrarioArrivo("11:00")
                .setTipoTreno("Intercity")
                .setClasseServizio("2A")
                .setPrezzo(30.0)
                .setPostiDisponibili(100)
                .setBinario(2)
                .setStato("regolare")
                .build();

        BigliettoDTO prenotazione = BigliettoDTO.newBuilder()
                .setId(200)
                .setCliente(cliente)
                .setTratta(tratta)
                .setPrezzo(30.0)
                .setStato("PRENOTATO")
                .setClasseServizio("2A")
                .build();

        DatabasePrenotazioni.getInstance().aggiungiPrenotazione(200, prenotazione, cliente, 1);

        RichiestaDTO richiesta = RichiestaDTO.newBuilder()
                .setCliente(cliente)
                .build();

        RispostaDTO risposta = gestore.gestisci(richiesta);

        assertTrue(risposta.getEsito());
        assertEquals("Prenotazioni trovate: 1", risposta.getMessaggio());
        assertEquals(1, risposta.getBigliettiCount());
        assertEquals("Firenze", risposta.getBiglietti(0).getTratta().getStazionePartenza());
    }
}