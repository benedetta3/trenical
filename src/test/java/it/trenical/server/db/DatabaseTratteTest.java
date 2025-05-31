package it.trenical.server.db;

import it.trenical.common.grpc.TrattaDTO;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DatabaseTratteTest {

    private DatabaseTratte db;

    @BeforeEach
    public void setup() {
        db = DatabaseTratte.getInstance();
        db.reset(); // azzera per ogni test
    }

    @Test
    @Order(1)
    public void testAggiuntaTratta() {
        TrattaDTO tratta = creaTratta(1);
        db.aggiungiTratta(tratta);

        List<TrattaDTO> tratte = db.getTutteLeTratte();
        assertEquals(1, tratte.size());
        assertEquals(tratta, tratte.get(0));
    }

    @Test
    @Order(2)
    public void testContieneTratta() {
        TrattaDTO tratta = creaTratta(2);
        db.aggiungiTratta(tratta);

        assertTrue(db.contiene(2));
        assertFalse(db.contiene(999));
    }

    @Test
    @Order(3)
    public void testGetTrattaEsistente() {
        TrattaDTO tratta = creaTratta(3);
        db.aggiungiTratta(tratta);

        TrattaDTO trovata = db.getTratta(3);
        assertNotNull(trovata);
        assertEquals(3, trovata.getId());
    }

    @Test
    @Order(4)
    public void testGetTrattaInesistente() {
        TrattaDTO trovata = db.getTratta(999);
        assertNull(trovata);
    }

    @Test
    @Order(5)
    public void testReset() {
        db.aggiungiTratta(creaTratta(4));
        db.reset();

        assertTrue(db.getTutteLeTratte().isEmpty());
        assertNull(db.getTratta(4));
    }

    @Test
    @Order(6)
    public void testAggiornaTratta() {
        TrattaDTO originale = creaTratta(5);
        db.aggiungiTratta(originale);

        TrattaDTO aggiornata = TrattaDTO.newBuilder(originale)
                .setOrarioPartenza("10:00")
                .setOrarioArrivo("14:00")
                .setBinario(3)
                .build();

        db.aggiornaTratta(aggiornata);

        TrattaDTO ottenuta = db.getTratta(5);
        assertNotNull(ottenuta);
        assertEquals("10:00", ottenuta.getOrarioPartenza());
        assertEquals("14:00", ottenuta.getOrarioArrivo());
        assertEquals(3, ottenuta.getBinario());
    }

    // Metodo di utilità per creare una tratta fittizia
    private TrattaDTO creaTratta(int id) {
        return TrattaDTO.newBuilder()
                .setId(id)
                .setStazionePartenza("Milano")
                .setStazioneArrivo("Roma")
                .setOrarioPartenza("08:00")
                .setOrarioArrivo("12:00")
                .setData("2025-06-01")
                .setTipoTreno("Frecciarossa")
                .setClasseServizio("1A")
                .setPrezzo(89.90)
                .setPostiDisponibili(100)
                .setBinario(1)
                .setStato("regolare")
                .build();
    }
}