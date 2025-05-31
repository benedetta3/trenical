package it.trenical.server.controller;

import it.trenical.common.grpc.*;
import it.trenical.server.db.DatabaseBiglietti;
import it.trenical.server.db.DatabaseTratte;
import it.trenical.server.db.DatabasePromozioni;
import it.trenical.server.payment.SimulatorePagamento;
import org.junit.jupiter.api.*;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GestoreAcquistoTest {

    private GestoreAcquisto gestoreAcquisto;
    private ClienteDTO cliente;
    private TrattaDTO tratta;
    private BigliettoDTO biglietto;

    @BeforeEach
    public void setUp() {
        resetTratteFile();

        gestoreAcquisto = new GestoreAcquisto();
        DatabaseBiglietti.getInstance().reset();
        DatabaseTratte.getInstance().reset();
        DatabasePromozioni.getInstance().reset();
        SimulatorePagamento.simulaAutorizzazione(true);

        cliente = ClienteDTO.newBuilder()
                .setId(1)
                .setNome("Mario Rossi")
                .setEmail("mario@rossi.com")
                .build();

        tratta = TrattaDTO.newBuilder()
                .setId(10)
                .setStazionePartenza("Milano")
                .setStazioneArrivo("Roma")
                .setOrarioPartenza("08:00")
                .setOrarioArrivo("12:00")
                .setData("2025-06-10")
                .setTipoTreno("Alta Velocità")
                .setClasseServizio("1A")
                .setPrezzo(50.0)
                .setPostiDisponibili(10)
                .setBinario(1)
                .setStato("regolare")
                .build();

        DatabaseTratte.getInstance().aggiungiTratta(tratta);

        biglietto = BigliettoDTO.newBuilder()
                .setCliente(cliente)
                .setTratta(tratta)
                .setPrezzo(50.0)
                .setStato("DA_PAGARE")
                .setClasseServizio("1A")
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
    public void testAcquistoValido() {
        RichiestaDTO richiesta = RichiestaDTO.newBuilder()
                .setTipo(TipoRichiesta.ACQUISTA)
                .setCliente(cliente)
                .setTratta(tratta)
                .setBiglietto(biglietto)
                .build();

        RispostaDTO risposta = gestoreAcquisto.gestisci(richiesta);

        assertTrue(risposta.getEsito(), "L'acquisto deve andare a buon fine.");
        assertNotNull(risposta.getMessaggio());
        assertTrue(risposta.getMessaggio().contains("Acquisto completato con successo"));
        assertEquals(1, risposta.getBigliettiCount());
    }

    @Test
    @Order(2)
    public void testDatiMancanti() {
        RichiestaDTO richiesta = RichiestaDTO.newBuilder()
                .setTipo(TipoRichiesta.ACQUISTA)
                .build();

        RispostaDTO risposta = gestoreAcquisto.gestisci(richiesta);

        assertFalse(risposta.getEsito());
        assertEquals("Dati mancanti per l'acquisto.", risposta.getMessaggio());
    }

    @Test
    @Order(3)
    public void testPagamentoNegato() {
        SimulatorePagamento.simulaAutorizzazione(false);

        RichiestaDTO richiesta = RichiestaDTO.newBuilder()
                .setTipo(TipoRichiesta.ACQUISTA)
                .setCliente(cliente)
                .setTratta(tratta)
                .setBiglietto(biglietto)
                .build();

        RispostaDTO risposta = gestoreAcquisto.gestisci(richiesta);

        assertFalse(risposta.getEsito());
        assertEquals("Pagamento non autorizzato.", risposta.getMessaggio());
    }

    @Test
    @Order(4)
    public void testTrattaNonEsistente() {
        TrattaDTO trattaFinta = TrattaDTO.newBuilder(tratta).setId(999).build();

        RichiestaDTO richiesta = RichiestaDTO.newBuilder()
                .setTipo(TipoRichiesta.ACQUISTA)
                .setCliente(cliente)
                .setTratta(trattaFinta)
                .setBiglietto(biglietto)
                .build();

        RispostaDTO risposta = gestoreAcquisto.gestisci(richiesta);

        assertFalse(risposta.getEsito());
        assertEquals("Tratta non trovata.", risposta.getMessaggio());
    }

    @Test
    @Order(5)
    public void testPostiEsauriti() {
        TrattaDTO senzaPosti = TrattaDTO.newBuilder(tratta).setPostiDisponibili(0).build();
        DatabaseTratte.getInstance().aggiornaTratta(senzaPosti);

        RichiestaDTO richiesta = RichiestaDTO.newBuilder()
                .setTipo(TipoRichiesta.ACQUISTA)
                .setCliente(cliente)
                .setTratta(tratta)
                .setBiglietto(biglietto)
                .build();

        RispostaDTO risposta = gestoreAcquisto.gestisci(richiesta);

        assertFalse(risposta.getEsito());
        assertEquals("Posti esauriti per la tratta selezionata.", risposta.getMessaggio());
    }

    @Test
    @Order(6)
    public void testClienteNonValido() {
        ClienteDTO invalido = ClienteDTO.newBuilder().setEmail("nope").setNome("SoloNome").build();

        RichiestaDTO richiesta = RichiestaDTO.newBuilder()
                .setTipo(TipoRichiesta.ACQUISTA)
                .setCliente(invalido)
                .setTratta(tratta)
                .setBiglietto(biglietto)
                .build();

        RispostaDTO risposta = gestoreAcquisto.gestisci(richiesta);

        assertFalse(risposta.getEsito());
        assertEquals("Dati cliente non validi: inserire nome e cognome e una email valida.", risposta.getMessaggio());
    }
}