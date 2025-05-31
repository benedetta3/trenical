package it.trenical.server.controller;

import it.trenical.common.grpc.*;
import it.trenical.server.db.DatabaseBiglietti;
import it.trenical.server.db.DatabaseTratte;
import it.trenical.server.payment.SimulatorePagamento;
import org.junit.jupiter.api.*;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GestoreModificaTest {

    private GestoreModifica gestore;
    private ClienteDTO cliente;
    private BigliettoDTO bigliettoOriginale;
    private TrattaDTO trattaOriginale;
    private TrattaDTO trattaNuova;

    @BeforeEach
    public void setUp() {
        resetTratteFile();

        gestore = new GestoreModifica();
        DatabaseTratte.getInstance().reset();
        DatabaseBiglietti.getInstance().reset();

        cliente = ClienteDTO.newBuilder()
                .setId(1)
                .setNome("Luca Bianchi")
                .setEmail("luca@bianchi.it")
                .build();

        // Tratta originale
        trattaOriginale = TrattaDTO.newBuilder()
                .setId(10)
                .setStazionePartenza("Roma")
                .setStazioneArrivo("Milano")
                .setOrarioPartenza("09:00")
                .setOrarioArrivo("12:00")
                .setData(LocalDateTime.now().plusDays(2).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                .setTipoTreno("Alta Velocità")
                .setClasseServizio("Seconda")
                .setPrezzo(60.0)
                .setPostiDisponibili(100)
                .setBinario(1)
                .setStato("regolare")
                .build();

        // Tratta nuova per modifica
        trattaNuova = TrattaDTO.newBuilder()
                .setId(20)
                .setStazionePartenza("Roma")
                .setStazioneArrivo("Milano")
                .setOrarioPartenza("18:00")
                .setOrarioArrivo("21:00")
                .setData(LocalDateTime.now().plusDays(2).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                .setTipoTreno("Alta Velocità")
                .setClasseServizio("Prima")
                .setPrezzo(75.0)
                .setPostiDisponibili(99)
                .setBinario(2)
                .setStato("regolare")
                .build();

        DatabaseTratte.getInstance().aggiungiTratta(trattaOriginale);
        DatabaseTratte.getInstance().aggiungiTratta(trattaNuova);

        bigliettoOriginale = BigliettoDTO.newBuilder()
                .setId(100)
                .setCliente(cliente)
                .setTratta(trattaOriginale)
                .setPrezzo(60.0)
                .setStato("ACQUISTATO")
                .setClasseServizio("Seconda")
                .build();

        DatabaseBiglietti.getInstance().aggiungiBiglietto(bigliettoOriginale);

        SimulatorePagamento.simulaAutorizzazione(true); // sempre true per test
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
    public void testModificaValidaConDifferenzaClasse() {
        RichiestaDTO richiesta = RichiestaDTO.newBuilder()
                .setTipo(TipoRichiesta.MODIFICA)
                .setCliente(cliente)
                .setTratta(trattaNuova)
                .setBiglietto(bigliettoOriginale)
                .build();

        RispostaDTO risposta = gestore.gestisci(richiesta);

        assertTrue(risposta.getEsito());
        assertTrue(risposta.getMessaggio().contains("Modifica completata"));
        assertEquals(1, risposta.getBigliettiCount());
        assertEquals("Prima", risposta.getBiglietti(0).getClasseServizio());
    }

    @Test
    @Order(2)
    public void testModificaConDatiMancanti() {
        RichiestaDTO richiesta = RichiestaDTO.newBuilder()
                .setTipo(TipoRichiesta.MODIFICA)
                .build();

        RispostaDTO risposta = gestore.gestisci(richiesta);

        assertFalse(risposta.getEsito());
        assertEquals("Dati mancanti per la modifica.", risposta.getMessaggio());
    }

    @Test
    @Order(3)
    public void testModificaConTrattaNonEsistente() {
        TrattaDTO inesistente = TrattaDTO.newBuilder()
                .setId(999)
                .build();

        RichiestaDTO richiesta = RichiestaDTO.newBuilder()
                .setTipo(TipoRichiesta.MODIFICA)
                .setCliente(cliente)
                .setTratta(inesistente)
                .setBiglietto(bigliettoOriginale)
                .build();

        RispostaDTO risposta = gestore.gestisci(richiesta);

        assertFalse(risposta.getEsito());
        assertEquals("Tratta selezionata non disponibile.", risposta.getMessaggio());
    }
}