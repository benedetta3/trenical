package it.trenical.server;

import it.trenical.common.grpc.TrattaDTO;
import it.trenical.server.db.DatabaseTratte;
import it.trenical.server.db.DatabaseBiglietti;
import it.trenical.server.gui.AdminMainGUI;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import javax.swing.*;
import java.io.*;

public class MainServer {

    public static void main(String[] args) {
        int port = 50051;
        boolean testMode = Boolean.parseBoolean(System.getProperty("testMode", "false"));

        if (!testMode) {
            ripristinaFileTratte();
        }

        try {
            SwingUtilities.invokeLater(() -> new AdminMainGUI().setVisible(true));

            DatabaseTratte.getInstance().reset();
            DatabaseTratte.getInstance().caricaTratteDaFile();
            DatabaseBiglietti.getInstance().reset();

            System.out.println("Tratte caricate: " + DatabaseTratte.getInstance().getTutteLeTratte().size());
            for (TrattaDTO t : DatabaseTratte.getInstance().getTutteLeTratte()) {
                System.out.println(t.getId() + " " + t.getStazionePartenza() + " - " + t.getStazioneArrivo());
            }

            Server server = ServerBuilder
                    .forPort(port)
                    .addService(new TrenicalImpl())
                    .build()
                    .start();

            System.out.println("Server avviato sulla porta " + port);
            server.awaitTermination();

        } catch (IOException | InterruptedException e) {
            System.err.println("Errore nell'avvio del server: " + e.getMessage());
        }
    }

    private static void ripristinaFileTratte() {
        try (
                BufferedReader reader = new BufferedReader(new FileReader("tratte_originale.txt"));
                BufferedWriter writer = new BufferedWriter(new FileWriter("tratte.txt"))
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Errore nel ripristino di tratte.txt: " + e.getMessage());
        }
    }
}