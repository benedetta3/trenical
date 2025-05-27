package it.trenical.client;

import it.trenical.client.builder.*;
import it.trenical.client.command.*;
import it.trenical.common.grpc.*;

import java.util.Scanner;

public class MainClient {

    private static final CommandInvoker invoker = new CommandInvoker("localhost", 50051);
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("🎮 Benvenuto nel client TreniCal CLI");

        while (true) {
            System.out.println("\n--- MENU ---");
            System.out.println("1. Filtra tratte");
            System.out.println("2. Acquista biglietto");
            System.out.println("3. Modifica biglietto");
            System.out.println("4. Iscrizione carta fedeltà");
            System.out.println("5. Invia promozione");
            System.out.println("6. Prenota biglietto");
            System.out.println("7. Conferma prenotazione");
            System.out.println("0. Esci");

            System.out.print("Scelta: ");
            int scelta = Integer.parseInt(scanner.nextLine());

            switch (scelta) {
                case 1 -> filtraTratte();
                case 2 -> acquistaBiglietto();
                case 3 -> modificaBiglietto();
                case 4 -> iscrizioneCarta();
                case 5 -> inviaPromozione();
                case 6 -> prenotaBiglietto();
                case 7 -> confermaPrenotazione();
                case 0 -> {
                    System.out.println("👋 Arrivederci!");
                    return;
                }
                default -> System.out.println("Scelta non valida.");
            }
        }
    }

    private static void filtraTratte() {
        System.out.print("Stazione partenza: ");
        String partenza = scanner.nextLine();

        System.out.print("Stazione arrivo: ");
        String arrivo = scanner.nextLine();

        System.out.print("Data (es. 2024-06-15): ");
        String data = scanner.nextLine();

        System.out.print("Tipo treno (es. Alta Velocità, Regionale): ");
        String tipoTreno = scanner.nextLine();

        System.out.print("Classe di servizio (es. Prima, Seconda): ");
        String classeServizio = scanner.nextLine();

        Command comando = new FiltraTratteCommand(partenza, arrivo, data, tipoTreno, classeServizio);
        stampaRisposta(invoker.esegui(comando));
    }

    private static void acquistaBiglietto() {
        TrattaDTO tratta = creaTrattaConId();
        ClienteDTO cliente = creaCliente();
        System.out.print("Prezzo: ");
        double prezzo = Double.parseDouble(scanner.nextLine());

        Command comando = new AcquistaBigliettoCommand(tratta, prezzo, cliente);
        stampaRisposta(invoker.esegui(comando));
    }

    private static void modificaBiglietto() {
        System.out.print("ID biglietto da modificare: ");
        int id = Integer.parseInt(scanner.nextLine());

        TrattaDTO nuovaTratta = creaTrattaConId();
        ClienteDTO cliente = creaCliente();
        System.out.print("Nuovo prezzo: ");
        double prezzo = Double.parseDouble(scanner.nextLine());

        Command comando = new ModificaBigliettoCommand(id, nuovaTratta, prezzo, cliente);
        stampaRisposta(invoker.esegui(comando));
    }

    private static void iscrizioneCarta() {
        ClienteDTO cliente = creaCliente();
        Command comando = new IscrivitiCartaFedeltaCommand(cliente);
        stampaRisposta(invoker.esegui(comando));
    }
    private static void inviaPromozione() {
        System.out.print("Messaggio promozionale: ");
        String messaggio = scanner.nextLine();

        System.out.print("Tipo di treno (es. Alta Velocità, Regionale): ");
        String tipoTreno = scanner.nextLine();

        System.out.print("Periodo (es. Estate 2024): ");
        String periodo = scanner.nextLine();

        // Crea una tratta fittizia (per il test) o una valida
        TrattaDTO tratta = creaTrattaConId();  // Crea una tratta esistente

        // Determina se il cliente è fedeltà (questo dovrebbe essere determinato in base al cliente reale)
        System.out.print("Cliente fedeltà? (true/false): ");
        boolean clienteFedelta = Boolean.parseBoolean(scanner.nextLine());

        // Modifica il costruttore di PromozioneCommand per accettare clienteFedelta
        Command comando = new PromozioneCommand(messaggio, tipoTreno, periodo, tratta, clienteFedelta);

        stampaRisposta(invoker.esegui(comando));
    }

    private static void prenotaBiglietto() {
        TrattaDTO tratta = creaTrattaConId();
        ClienteDTO cliente = creaCliente();
        System.out.print("Prezzo: ");
        double prezzo = Double.parseDouble(scanner.nextLine());

        Command comando = new PrenotaBigliettoCommand(tratta, prezzo, cliente);
        stampaRisposta(invoker.esegui(comando));
    }

    private static void confermaPrenotazione() {
        System.out.print("ID prenotazione da confermare: ");
        int id = Integer.parseInt(scanner.nextLine());

        Command comando = new ConfermaPrenotazioneCommand(id);
        stampaRisposta(invoker.esegui(comando));
    }

    private static ClienteDTO creaCliente() {
        System.out.print("ID cliente: ");
        int id = Integer.parseInt(scanner.nextLine());
        System.out.print("Nome: ");
        String nome = scanner.nextLine();
        System.out.print("Cognome: ");
        String cognome = scanner.nextLine();
        System.out.print("Email: ");
        String email = scanner.nextLine();

        return new ClienteBuilder()
                .setId(id)
                .setNome(nome)
                .setCognome(cognome)
                .setEmail(email)
                .build();
    }

    private static TrattaDTO creaTrattaConId() {
        System.out.print("ID tratta: ");
        int id = Integer.parseInt(scanner.nextLine());
        System.out.print("Partenza: ");
        String partenza = scanner.nextLine();
        System.out.print("Arrivo: ");
        String arrivo = scanner.nextLine();
        System.out.print("Orario partenza: ");
        String orarioP = scanner.nextLine();
        System.out.print("Orario arrivo: ");
        String orarioA = scanner.nextLine();

        return TrattaDTO.newBuilder()
                .setId(id)
                .setStazionePartenza(partenza)
                .setStazioneArrivo(arrivo)
                .setOrarioPartenza(orarioP)
                .setOrarioArrivo(orarioA)
                .build();
    }

    private static void stampaRisposta(RispostaDTO risposta) {
        System.out.println("Risposta:");
        System.out.println("- Esito: " + risposta.getEsito());
        System.out.println("- Messaggio: " + risposta.getMessaggio());

        for (TrattaDTO t : risposta.getTratteList()) {
            System.out.println("Tratta: " + t.getStazionePartenza() + " → " + t.getStazioneArrivo());
        }

        for (BigliettoDTO b : risposta.getBigliettiList()) {
            System.out.println("Biglietto #" + b.getId() + ": " + b.getTratta().getStazionePartenza() + " → " + b.getTratta().getStazioneArrivo());
        }
    }
}
