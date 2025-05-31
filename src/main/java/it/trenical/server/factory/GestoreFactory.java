package it.trenical.server.factory;

import it.trenical.common.grpc.TipoRichiesta;
import it.trenical.server.controller.*;
import it.trenical.common.grpc.RichiestaDTO;

public class GestoreFactory {

    private static GestoreFactory instance;

    private GestoreFactory() {}

    public static synchronized GestoreFactory getInstance() {
        if (instance == null) {
            instance = new GestoreFactory();
        }
        return instance;
    }

    public Gestore creaGestore(TipoRichiesta tipo) {
        if (tipo == null) {
            throw new IllegalArgumentException("Tipo richiesta non gestito: null");
        }

        switch (tipo) {
            case FILTRA: return new GestoreFiltraTratte();
            case ACQUISTA: return new GestoreAcquisto();
            case MODIFICA: return new GestoreModifica();
            case PRENOTA: return new GestorePrenotazione();
            case CONFERMA: return new GestoreConfermaPrenotazione();
            case VISUALIZZA_BIGLIETTI: return new GestoreVisualizzaBiglietti();
            case VISUALIZZA_PRENOTAZIONI: return new GestoreVisualizzaPrenotazioni();
            case VISUALIZZA_PROMOZIONI: return new GestoreVisualizzaPromozioni();
            case REGISTRA_NOTIFICA: return new GestoreRegistraNotifica();
            case NOTIFICA_CLIENT: return new GestoreNotificaClient();
            default:
                throw new IllegalArgumentException("Tipo richiesta non gestito: " + tipo);
        }
    }
}