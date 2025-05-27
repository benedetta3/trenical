package it.trenical.server;

import it.trenical.common.grpc.*;
import it.trenical.server.controller.Gestore;
import it.trenical.server.factory.GestoreFactory;
import io.grpc.stub.StreamObserver;

/**
 * Implementazione del servizio Trenical definito nel file proto.
 */

public class TrenicalImpl extends TrenicalServiceGrpc.TrenicalServiceImplBase {

    @Override
    public void inviaRichiesta(RichiestaDTO richiesta, StreamObserver<RispostaDTO> responseObserver) {
        Gestore gestore = GestoreFactory.getInstance().creaGestore(richiesta.getTipo());
        RispostaDTO risposta = gestore.gestisci(richiesta);
        responseObserver.onNext(risposta);
        responseObserver.onCompleted();
    }
}