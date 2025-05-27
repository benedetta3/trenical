package it.trenical.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

/**
 * Main che avvia il server gRPC Trenical.
 */
public class MainServer {

    public static void main(String[] args) {
        int port = 50051;

        try {
            Server server = ServerBuilder
                    .forPort(port)
                    .addService(new TrenicalImpl())
                    .build()
                    .start();

            System.out.println("Server avviato sulla porta " + port);

            server.awaitTermination(); // blocca l'esecuzione
        } catch (IOException | InterruptedException e) {
            System.err.println("Errore nell'avvio del server: " + e.getMessage());
        }
    }
}