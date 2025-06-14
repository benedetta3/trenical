package it.trenical.server.controller;

import it.trenical.common.grpc.*;
import it.trenical.server.db.DatabaseBiglietti;
import it.trenical.server.db.DatabaseTratte;
import org.junit.jupiter.api.*;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GestoreVisualizzaBigliettiTest {

    private GestoreVisualizzaBiglietti gestore;
    private ClienteDTO cliente;

    @BeforeEach
    public void setUp() {
        resetTratteFile();

        gestore = new GestoreVisualizzaBiglietti();
        DatabaseBiglietti.getInstance().reset();
        DatabaseTratte.getInstance().reset();

        cliente = ClienteDTO.newBuilder()
                .setId(1)
                .setNome("Elena Rossi")
                .setEmail("elena@rossi.it")
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
    public void testClienteNonSpecificato() {
        RichiestaDTO richiesta = RichiestaDTO.newBuilder().build();

        RispostaDTO risposta = gestore.gestisci(richiesta);

        assertFalse(risposta.getEsito());
        assertEquals("Cliente non specificato per la visualizzazione biglietti.", risposta.getMessaggio());
    }

    @Test
    @Order(2)
    public void testNessunBiglietto() {
        RichiestaDTO richiesta = RichiestaDTO.newBuilder()
                .setCliente(cliente)
                .build();

        RispostaDTO risposta = gestore.gestisci(richiesta);

        assertTrue(risposta.getEsito());
        assertEquals("Biglietti trovati: 0", risposta.getMessaggio());
        assertEquals(0, risposta.getBigliettiCount());
    }

    @Test
    @Order(3)
    public void testBigliettiPresenti() {
        TrattaDTO tratta = TrattaDTO.newBuilder()
                .setId(101)
                .setStazionePartenza("Milano")
                .setStazioneArrivo("Roma")
                .setData("2025-06-10")
                .setOrarioPartenza("09:00")
                .setOrarioArrivo("12:00")
                .setTipoTreno("Alta Velocità")
                .setClasseServizio("1A")
                .setPrezzo(59.99)
                .setPostiDisponibili(100)
                .setBinario(3)
                .setStato("regolare")
                .build();

        BigliettoDTO biglietto = BigliettoDTO.newBuilder()
                .setId(1)
                .setCliente(cliente)
                .setTratta(tratta)
                .setPrezzo(59.99)
                .setStato("ACQUISTATO")
                .setClasseServizio("1A")
                .build();

        DatabaseTratte.getInstance().aggiungiTratta(tratta);
        DatabaseBiglietti.getInstance().aggiungiBiglietto(biglietto);

        RichiestaDTO richiesta = RichiestaDTO.newBuilder()
                .setCliente(cliente)
                .build();

        RispostaDTO risposta = gestore.gestisci(richiesta);

        assertTrue(risposta.getEsito());
        assertEquals("Biglietti trovati: 1", risposta.getMessaggio());
        assertEquals(1, risposta.getBigliettiCount());
        assertEquals("Milano", risposta.getBiglietti(0).getTratta().getStazionePartenza());
    }
}