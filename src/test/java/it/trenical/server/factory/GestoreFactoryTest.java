package it.trenical.server.factory;

import it.trenical.common.grpc.TipoRichiesta;
import it.trenical.server.controller.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GestoreFactoryTest {

    private GestoreFactory factory;

    @BeforeEach
    public void setUp() {
        factory = GestoreFactory.getInstance();
    }

    @Test
    @Order(1)
    public void testIstanzaUnica() {
        GestoreFactory altra = GestoreFactory.getInstance();
        assertSame(factory, altra, "La factory deve essere singleton");
    }

    @Test
    @Order(2)
    public void testMappaOgniTipo() {
        assertTrue(factory.creaGestore(TipoRichiesta.FILTRA) instanceof GestoreFiltraTratte);
        assertTrue(factory.creaGestore(TipoRichiesta.ACQUISTA) instanceof GestoreAcquisto);
        assertTrue(factory.creaGestore(TipoRichiesta.MODIFICA) instanceof GestoreModifica);
        assertTrue(factory.creaGestore(TipoRichiesta.PRENOTA) instanceof GestorePrenotazione);
        assertTrue(factory.creaGestore(TipoRichiesta.CONFERMA) instanceof GestoreConfermaPrenotazione);
        assertTrue(factory.creaGestore(TipoRichiesta.VISUALIZZA_BIGLIETTI) instanceof GestoreVisualizzaBiglietti);
        assertTrue(factory.creaGestore(TipoRichiesta.VISUALIZZA_PRENOTAZIONI) instanceof GestoreVisualizzaPrenotazioni);
        assertTrue(factory.creaGestore(TipoRichiesta.VISUALIZZA_PROMOZIONI) instanceof GestoreVisualizzaPromozioni);
        assertTrue(factory.creaGestore(TipoRichiesta.REGISTRA_NOTIFICA) instanceof GestoreRegistraNotifica);
        assertTrue(factory.creaGestore(TipoRichiesta.NOTIFICA_CLIENT) instanceof GestoreNotificaClient);
    }

    @Test
    @Order(3)
    public void testTipoNonGestito() {
        Exception eccezione = assertThrows(IllegalArgumentException.class, () -> {
            factory.creaGestore(null);
        });

        assertTrue(eccezione.getMessage().contains("Tipo richiesta non gestito"));
    }
}