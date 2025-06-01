package it.trenical.server.controller;

import it.trenical.common.grpc.*;
import it.trenical.server.db.DatabaseTratte;
import org.junit.jupiter.api.*;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GestoreModificaTrattaTest {

    private GestoreModificaTratta gestore;

    @BeforeEach
    public void setup() {
        DatabaseTratte.getInstance().setPersistenzaAttiva(false);
        resetTratteFile();

        gestore = new GestoreModificaTratta();
        DatabaseTratte.getInstance().reset();

        TrattaDTO trattaIniziale = TrattaDTO.newBuilder()
                .setId(1)
                .setStazionePartenza("Torino")
                .setStazioneArrivo("Genova")
                .setOrarioPartenza("09:00")
                .setOrarioArrivo("12:00")
                .setData("2025-06-01")
                .setTipoTreno("Regionale")
                .setClasseServizio("2")
                .setPrezzo(25.0)
                .setPostiDisponibili(100)
                .setBinario(3)
                .setStato("regolare")
                .build();

        DatabaseTratte.getInstance().aggiungiTratta(trattaIniziale);
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
    public void testModificaTrattaEsistente() {
        TrattaDTO nuovaTratta = TrattaDTO.newBuilder()
                .setId(1)
                .setStazionePartenza("Torino")
                .setStazioneArrivo("Genova")
                .setOrarioPartenza("10:00")
                .setOrarioArrivo("13:00")
                .setData("2025-06-01")
                .setTipoTreno("Regionale")
                .setClasseServizio("2")
                .setPrezzo(25.0)
                .setPostiDisponibili(100)
                .setBinario(5)
                .setStato("regolare")
                .build();

        RichiestaDTO richiesta = RichiestaDTO.newBuilder()
                .setTipo(TipoRichiesta.MODIFICA)
                .setTratta(nuovaTratta)
                .build();

        RispostaDTO risposta = gestore.gestisci(richiesta);

        assertTrue(risposta.getEsito());
        assertEquals("Tratta aggiornata e osservatori notificati.", risposta.getMessaggio());

        TrattaDTO aggiornata = DatabaseTratte.getInstance().getTratta(1);
        assertEquals("10:00", aggiornata.getOrarioPartenza());
        assertEquals("13:00", aggiornata.getOrarioArrivo());
        assertEquals(5, aggiornata.getBinario());
    }

    @Test
    @Order(2)
    public void testModificaTrattaInesistente() {
        TrattaDTO trattaNonEsistente = TrattaDTO.newBuilder()
                .setId(999)
                .setStazionePartenza("Bari")
                .setStazioneArrivo("Lecce")
                .setOrarioPartenza("10:00")
                .setOrarioArrivo("11:00")
                .setData("2025-06-02")
                .setTipoTreno("Regionale")
                .setClasseServizio("2")
                .setPrezzo(20.0)
                .setPostiDisponibili(50)
                .setBinario(1)
                .setStato("regolare")
                .build();

        RichiestaDTO richiesta = RichiestaDTO.newBuilder()
                .setTipo(TipoRichiesta.MODIFICA)
                .setTratta(trattaNonEsistente)
                .build();

        RispostaDTO risposta = gestore.gestisci(richiesta);

        assertFalse(risposta.getEsito());
        assertEquals("Tratta da modificare non trovata.", risposta.getMessaggio());
    }

    @Test
    @Order(3)
    public void testModificaTrattaConCliente() {
        TrattaDTO nuovaTratta = TrattaDTO.newBuilder()
                .setId(1)
                .setStazionePartenza("Torino")
                .setStazioneArrivo("Genova")
                .setOrarioPartenza("11:00")
                .setOrarioArrivo("14:00")
                .setData("2025-06-01")
                .setTipoTreno("Regionale")
                .setClasseServizio("2")
                .setPrezzo(25.0)
                .setPostiDisponibili(90)
                .setBinario(6)
                .setStato("regolare")
                .build();

        ClienteDTO cliente = ClienteDTO.newBuilder()
                .setId(10)
                .setNome("Mario Rossi")
                .setEmail("mario@rossi.it")
                .build();

        RichiestaDTO richiesta = RichiestaDTO.newBuilder()
                .setTipo(TipoRichiesta.MODIFICA)
                .setTratta(nuovaTratta)
                .setCliente(cliente)
                .build();

        RispostaDTO risposta = gestore.gestisci(richiesta);

        assertTrue(risposta.getEsito());
        assertEquals("Tratta aggiornata e osservatori notificati.", risposta.getMessaggio());

        TrattaDTO aggiornata = DatabaseTratte.getInstance().getTratta(1);
        assertEquals("11:00", aggiornata.getOrarioPartenza());
    }
}