package it.trenical.client.command;

import it.trenical.common.grpc.RichiestaDTO;
import it.trenical.common.grpc.RispostaDTO;
import it.trenical.common.grpc.TrenicalServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;


public class CommandInvoker {

    private static CommandInvoker instance;
    private final TrenicalServiceGrpc.TrenicalServiceBlockingStub stub;

    private CommandInvoker(String host, int port) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        this.stub = TrenicalServiceGrpc.newBlockingStub(channel);
    }

    public static synchronized CommandInvoker getInstance() {
        if (instance == null) {
            instance = new CommandInvoker("localhost", 50051);
        }
        return instance;
    }

    public RispostaDTO esegui(Command command) {
        RichiestaDTO richiesta = command.esegui();
        return stub.inviaRichiesta(richiesta);
    }
}