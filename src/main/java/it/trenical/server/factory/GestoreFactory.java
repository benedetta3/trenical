package it.trenical.server.factory;

import it.trenical.common.grpc.TipoRichiesta;
import it.trenical.server.controller.*;
import it.trenical.common.grpc.RichiestaDTO;

/**
 * Factory per la creazione dei gestori lato server.
 * Applica i pattern Singleton + Factory Method.
 */
public class GestoreFactory {

    private static GestoreFactory instance;

    private GestoreFactory() {}

    public static synchronized GestoreFactory getInstance() {
        if (instance == null) {
            instance = new GestoreFactory();
        }
        return instance;
    }

    /**
     * Crea il gestore corretto in base al tipo di richiesta.
     */
    public Gestore creaGestore(TipoRichiesta tipo) {
        switch (tipo) {
            case FILTRA:
                return new GestoreFiltraTratte();
            case ACQUISTA:
                return new GestoreAcquisto();
            case MODIFICA:
                return new GestoreModifica();
            case ISCRIVITI:
                return new GestoreIscriviti();
            case PROMOZIONE:
                return new GestorePromozione();
            case CONFERMA:
                return new GestoreConfermaPrenotazione();
            default:
                throw new IllegalArgumentException("Tipo richiesta non gestito: " + tipo);
        }
    }
}