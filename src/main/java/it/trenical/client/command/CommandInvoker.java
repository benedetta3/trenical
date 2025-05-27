package it.trenical.client.command;

import it.trenical.common.grpc.RichiestaDTO;
import it.trenical.common.grpc.RispostaDTO;
import it.trenical.common.grpc.TrenicalServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

/**
 * Invoker che esegue un comando e invia la richiesta al server gRPC.
 */
public class CommandInvoker {

    private final TrenicalServiceGrpc.TrenicalServiceBlockingStub stub;

    public CommandInvoker(String host, int port) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        this.stub = TrenicalServiceGrpc.newBlockingStub(channel);
    }

    public RispostaDTO esegui(Command command) {
        RichiestaDTO richiesta = command.esegui();
        return stub.inviaRichiesta(richiesta);
    }
}