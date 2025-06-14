package it.trenical.server.db;

import it.trenical.common.grpc.BigliettoDTO;
import it.trenical.common.grpc.ClienteDTO;
import it.trenical.common.grpc.TrattaDTO;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DatabaseBigliettiTest {

    private DatabaseBiglietti db;

    @BeforeEach
    public void setup() {
        db = DatabaseBiglietti.getInstance();
        db.reset();
    }

    @Test
    @Order(1)
    public void testAggiuntaBiglietto() {
        BigliettoDTO b = creaBiglietto(1, 100);
        db.aggiungiBiglietto(b);
        assertTrue(db.esisteBigliettoPer(b.getCliente(), b.getTratta()));
    }

    @Test
    @Order(2)
    public void testBigliettoInesistente() {
        ClienteDTO c = ClienteDTO.newBuilder().setId(2).build();
        TrattaDTO t = TrattaDTO.newBuilder().setId(200).build();
        assertFalse(db.esisteBigliettoPer(c, t));
    }

    @Test
    @Order(3)
    public void testRimozioneBiglietto() {
        BigliettoDTO b = creaBiglietto(2, 101);
        db.aggiungiBiglietto(b);
        db.rimuoviBiglietto(b.getId());
        assertFalse(db.esisteBigliettoPer(b.getCliente(), b.getTratta()));
    }

    @Test
    @Order(4)
    public void testAggiornamentoBiglietto() {
        BigliettoDTO b = creaBiglietto(3, 102);
        db.aggiungiBiglietto(b);

        BigliettoDTO aggiornato = BigliettoDTO.newBuilder(b).setPrezzo(99.99).build();
        db.aggiornaBiglietto(aggiornato);

        List<BigliettoDTO> tutti = db.getBigliettiPerTratta(102);
        assertEquals(1, tutti.size());
        assertEquals(99.99, tutti.get(0).getPrezzo());
    }

    @Test
    @Order(5)
    public void testGetBigliettiPerTratta() {
        db.aggiungiBiglietto(creaBiglietto(4, 103));
        db.aggiungiBiglietto(creaBiglietto(5, 103));
        List<BigliettoDTO> lista = db.getBigliettiPerTratta(103);
        assertEquals(2, lista.size());
    }

    @Test
    @Order(6)
    public void testGetBigliettiByCliente() {
        ClienteDTO c = ClienteDTO.newBuilder()
                .setId(10)
                .setEmail("test@prova.it")
                .build();

        TrattaDTO t1 = TrattaDTO.newBuilder().setId(111).build();
        TrattaDTO t2 = TrattaDTO.newBuilder().setId(112).build();

        DatabaseTratte.getInstance().reset();
        DatabaseTratte.getInstance().aggiungiTratta(t1);
        DatabaseTratte.getInstance().aggiungiTratta(t2);

        BigliettoDTO b1 = BigliettoDTO.newBuilder().setId(1001).setCliente(c).setTratta(t1).build();
        BigliettoDTO b2 = BigliettoDTO.newBuilder().setId(1002).setCliente(c).setTratta(t2).build();

        db.aggiungiBiglietto(b1);
        db.aggiungiBiglietto(b2);

        List<BigliettoDTO> trovati = db.getBigliettiByCliente(c);
        assertEquals(2, trovati.size());
    }

    @Test
    @Order(7)
    public void testRimborsoPerTratta() {
        ClienteDTO c = ClienteDTO.newBuilder().setId(20).setEmail("user@x.it").build();
        TrattaDTO t = TrattaDTO.newBuilder().setId(200).build();
        BigliettoDTO b = BigliettoDTO.newBuilder().setId(2001).setCliente(c).setTratta(t).build();

        db.aggiungiBiglietto(b);
        db.rimborsoPerTratta(200, c);

        assertFalse(db.esisteBigliettoPer(c, t));
    }

    @Test
    @Order(8)
    public void testReset() {
        db.aggiungiBiglietto(creaBiglietto(6, 104));
        db.reset();
        assertFalse(db.esisteBigliettoPer(
                ClienteDTO.newBuilder().setId(6).build(),
                TrattaDTO.newBuilder().setId(104).build()
        ));
    }

    private BigliettoDTO creaBiglietto(int idCliente, int idTratta) {
        ClienteDTO c = ClienteDTO.newBuilder().setId(idCliente).setEmail("cliente" + idCliente + "@mail.it").build();
        TrattaDTO t = TrattaDTO.newBuilder().setId(idTratta).build();

        return BigliettoDTO.newBuilder()
                .setId(db.generaNuovoId())
                .setCliente(c)
                .setTratta(t)
                .setPrezzo(29.99)
                .setClasseServizio("2A")
                .setStato("ACQUISTATO")
                .build();
    }
}