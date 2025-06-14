package it.trenical.server.db;

import it.trenical.common.grpc.BigliettoDTO;
import it.trenical.common.grpc.ClienteDTO;
import it.trenical.common.grpc.TrattaDTO;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DatabasePrenotazioniTest {

    private DatabasePrenotazioni db;

    @BeforeEach
    public void setup() {
        db = DatabasePrenotazioni.getInstance();
        db.reset();
    }

    @Test
    @Order(1)
    public void testAggiuntaPrenotazione() {
        int id = db.generaNuovoId();
        BigliettoDTO b = creaBiglietto(1, 101);
        ClienteDTO c = creaCliente(1, "user1@test.it");
        db.aggiungiPrenotazione(id, b, c, 2);

        assertTrue(db.contiene(id));
    }

    @Test
    @Order(2)
    public void testGetPrenotazione() {
        int id = db.generaNuovoId();
        BigliettoDTO b = creaBiglietto(2, 102);
        db.aggiungiPrenotazione(id, b, creaCliente(2, "u2@t.it"), 1);

        BigliettoDTO trovato = db.getPrenotazione(id);
        assertNotNull(trovato);
        assertEquals(102, trovato.getTratta().getId());
    }

    @Test
    @Order(3)
    public void testGetClienteOsservatore() {
        int id = db.generaNuovoId();
        ClienteDTO c = creaCliente(3, "test@abc.it");
        db.aggiungiPrenotazione(id, creaBiglietto(3, 103), c, 1);

        ClienteDTO trovato = db.getClienteOsservatore(id);
        assertNotNull(trovato);
        assertEquals("test@abc.it", trovato.getEmail());
    }

    @Test
    @Order(4)
    public void testGetPrenotazioniPerEmail() {
        ClienteDTO c = creaCliente(4, "email@esempio.it");
        db.aggiungiPrenotazione(db.generaNuovoId(), creaBiglietto(4, 104, c), c, 1);
        db.aggiungiPrenotazione(db.generaNuovoId(), creaBiglietto(4, 105, c), c, 1);

        List<BigliettoDTO> lista = db.getPrenotazioniPerEmail("email@esempio.it");
        assertEquals(2, lista.size());
    }

    @Test
    @Order(5)
    public void testRimuoviPrenotazione() {
        int id = db.generaNuovoId();
        db.aggiungiPrenotazione(id, creaBiglietto(5, 106), creaCliente(5, "user5@mail.it"), 2);

        db.rimuoviPrenotazione(id);
        assertFalse(db.contiene(id));
        assertNull(db.getPrenotazione(id));
        assertNull(db.getClienteOsservatore(id));
    }

    @Test
    @Order(6)
    public void testReset() {
        db.aggiungiPrenotazione(1, creaBiglietto(6, 107), creaCliente(6, "reset@mail.it"), 1);
        db.reset();
        assertFalse(db.contiene(1));
        assertEquals(1, db.generaNuovoId());
    }

    private ClienteDTO creaCliente(int id, String email) {
        return ClienteDTO.newBuilder()
                .setId(id)
                .setEmail(email)
                .build();
    }

    private BigliettoDTO creaBiglietto(int idCliente, int idTratta) {
        return creaBiglietto(idCliente, idTratta, creaCliente(idCliente, "c" + idCliente + "@mail.it"));
    }

    private BigliettoDTO creaBiglietto(int idCliente, int idTratta, ClienteDTO cliente) {
        TrattaDTO tratta = TrattaDTO.newBuilder()
                .setId(idTratta)
                .setStazionePartenza("A")
                .setStazioneArrivo("B")
                .setData("2025-06-01")
                .setOrarioPartenza("08:00")
                .setOrarioArrivo("10:00")
                .setPrezzo(25.0)
                .setClasseServizio("1A")
                .setTipoTreno("Regionale")
                .setPostiDisponibili(100)
                .setBinario(1)
                .setStato("regolare")
                .build();

        return BigliettoDTO.newBuilder()
                .setId(db.generaNuovoId())
                .setCliente(cliente)
                .setTratta(tratta)
                .setPrezzo(25.0)
                .setStato("PRENOTATO")
                .setClasseServizio("1A")
                .build();
    }
}