package it.trenical.server.payment;

import java.util.Random;

public class SimulatorePagamento {
    private static boolean pagamentoAutorizzato=false;
    /**
     * Simula il pagamento con una probabilità di successo.
     * 90% di probabilità che il pagamento venga accettato.
     */
    public static boolean autorizzaPagamento() {
        Random random = new Random();
        pagamentoAutorizzato = random.nextDouble() <= 0.9; // 90% di successo
        return pagamentoAutorizzato;
    }

    public static void simulaAutorizzazione(boolean autorizzato) {
        pagamentoAutorizzato = autorizzato;
    }
}