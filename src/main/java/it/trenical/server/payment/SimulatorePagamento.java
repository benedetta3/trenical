package it.trenical.server.payment;

public class SimulatorePagamento {

    private static boolean autorizzato = true; // default

    public static void simulaAutorizzazione(boolean stato) {
        autorizzato = stato;
    }

    public static boolean autorizzaPagamento() {
        return autorizzato;
    }
}