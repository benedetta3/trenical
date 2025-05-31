package it.trenical.server.controller;

import it.trenical.common.grpc.*;
import it.trenical.server.db.DatabaseTratte;
import org.junit.jupiter.api.*;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GestoreFiltraTratteTest {

    private GestoreFiltraTratte gestore;

    @BeforeEach
    public void setup() {
        resetTratteFile();

        gestore = new GestoreFiltraTratte();
        DatabaseTratte.getInstance().reset();

        TrattaDTO tratta1 = TrattaDTO.newBuilder()
                .setId(1)
                .setStazionePartenza("Milano")
                .setStazioneArrivo("Roma")
                .setOrarioPartenza("2025-06-01T08:00")
                .setOrarioArrivo("2025-06-01T11:00")
                .setData("2025-06-01")
                .setTipoTreno("Alta Velocità")
                .setClasseServizio("1")
                .setPrezzo(50.0)
                .setPostiDisponibili(100)
                .setBinario(1)
                .setStato("regolare")
                .build();

        TrattaDTO tratta2 = TrattaDTO.newBuilder()
                .setId(2)
                .setStazionePartenza("Torino")
                .setStazioneArrivo("Napoli")
                .setOrarioPartenza("2025-06-01T09:00")
                .setOrarioArrivo("2025-06-01T13:00")
                .setData("2025-06-01")
                .setTipoTreno("Regionale")
                .setClasseServizio("2")
                .setPrezzo(30.0)
                .setPostiDisponibili(100)
                .setBinario(2)
                .setStato("regolare")
                .build();

        DatabaseTratte.getInstance().aggiungiTratta(tratta1);
        DatabaseTratte.getInstance().aggiungiTratta(tratta2);
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
    public void testFiltraTratteEsatte() {
        TrattaDTO filtro = TrattaDTO.newBuilder()
                .setStazionePartenza("Milano")
                .setStazioneArrivo("Roma")
                .setClasseServizio("1")
                .build();

        RichiestaDTO richiesta = RichiestaDTO.newBuilder()
                .setTipo(TipoRichiesta.FILTRA)
                .setTratta(filtro)
                .setMessaggio("1")
                .build();

        RispostaDTO risposta = gestore.gestisci(richiesta);

        assertTrue(risposta.getEsito());
        assertEquals("Filtraggio completato", risposta.getMessaggio());
        assertEquals(1, risposta.getTratteCount());
        assertEquals("Milano", risposta.getTratte(0).getStazionePartenza());
        assertEquals("1", risposta.getTratte(0).getClasseServizio());
    }

    @Test
    @Order(2)
    public void testFiltraTratteInesistenti() {
        TrattaDTO filtro = TrattaDTO.newBuilder()
                .setStazionePartenza("Bari")
                .setStazioneArrivo("Firenze")
                .build();

        RichiestaDTO richiesta = RichiestaDTO.newBuilder()
                .setTipo(TipoRichiesta.FILTRA)
                .setTratta(filtro)
                .setMessaggio("")
                .build();

        RispostaDTO risposta = gestore.gestisci(richiesta);

        assertFalse(risposta.getEsito());
        assertEquals("Nessuna tratta trovata con i criteri specificati", risposta.getMessaggio());
        assertEquals(0, risposta.getTratteCount());
    }

    @Test
    @Order(3)
    public void testFiltraSoloPartenzaEArrivo() {
        TrattaDTO filtro = TrattaDTO.newBuilder()
                .setStazionePartenza("Torino")
                .setStazioneArrivo("Napoli")
                .build();

        RichiestaDTO richiesta = RichiestaDTO.newBuilder()
                .setTipo(TipoRichiesta.FILTRA)
                .setTratta(filtro)
                .build();

        RispostaDTO risposta = gestore.gestisci(richiesta);

        assertTrue(risposta.getEsito());
        assertEquals(1, risposta.getTratteCount());
        assertEquals("Torino", risposta.getTratte(0).getStazionePartenza());
        assertEquals("Napoli", risposta.getTratte(0).getStazioneArrivo());
    }
}