package it.trenical.server.controller;

import it.trenical.common.grpc.*;
import it.trenical.server.db.DatabaseTratte;
import org.junit.jupiter.api.*;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GestoreRegistraNotificaTest {

    private GestoreRegistraNotifica gestore;
    private ClienteDTO cliente;
    private TrattaDTO trattaValida;

    @BeforeEach
    public void setUp() {
        resetTratteFile();

        gestore = new GestoreRegistraNotifica();
        DatabaseTratte.getInstance().reset();

        cliente = ClienteDTO.newBuilder()
                .setId(1)
                .setNome("Giulia Verdi")
                .setEmail("giulia@verdi.it")
                .build();

        trattaValida = TrattaDTO.newBuilder()
                .setId(123)
                .setStazionePartenza("Milano")
                .setStazioneArrivo("Roma")
                .setOrarioPartenza("08:00")
                .setOrarioArrivo("11:00")
                .setData("2025-07-01")
                .setTipoTreno("Frecciarossa")
                .setClasseServizio("1A")
                .setPrezzo(60.0)
                .setPostiDisponibili(100)
                .setBinario(1)
                .setStato("regolare")
                .build();

        DatabaseTratte.getInstance().aggiungiTratta(trattaValida);
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
    public void testRegistrazioneCompleta() {
        RichiestaDTO richiesta = RichiestaDTO.newBuilder()
                .setCliente(cliente)
                .setTratta(trattaValida)
                .build();

        RispostaDTO risposta = gestore.gestisci(richiesta);

        assertTrue(risposta.getEsito());
        assertEquals("Registrazione a notifiche completata.", risposta.getMessaggio());
    }

    @Test
    @Order(2)
    public void testTrattaNonPresente() {
        TrattaDTO trattaInesistente = TrattaDTO.newBuilder()
                .setId(999)
                .build();

        RichiestaDTO richiesta = RichiestaDTO.newBuilder()
                .setCliente(cliente)
                .setTratta(trattaInesistente)
                .build();

        RispostaDTO risposta = gestore.gestisci(richiesta);

        assertFalse(risposta.getEsito());
        assertEquals("Tratta non trovata.", risposta.getMessaggio());
    }

    @Test
    @Order(3)
    public void testClienteOTRattaNull() {
        RichiestaDTO richiesta = RichiestaDTO.newBuilder().build();

        RispostaDTO risposta = gestore.gestisci(richiesta);

        assertFalse(risposta.getEsito());
        assertEquals("Cliente o tratta mancanti per registrazione a notifiche.", risposta.getMessaggio());
    }
}