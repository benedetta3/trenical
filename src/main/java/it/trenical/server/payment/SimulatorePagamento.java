package it.trenical.server.payment;

public class SimulatorePagamento {

    private static boolean autorizzato = true;

    public static void simulaAutorizzazione(boolean stato) {
        autorizzato = stato;
    }

    public static boolean autorizzaPagamento() {
        return autorizzato;
    }

    public static boolean effettuaPagamento(it.trenical.common.grpc.ClienteDTO cliente, double importo) {return autorizzato;}

}