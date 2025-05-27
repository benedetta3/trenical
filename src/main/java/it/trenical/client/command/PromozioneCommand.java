package it.trenical.client.command;

import it.trenical.client.builder.RichiestaBuilder;
import it.trenical.common.grpc.*;

/**
 * Comando per inviare una promozione ai clienti fedeltà.
 */
public class PromozioneCommand implements Command {

    private final String messaggio;
    private final String tipoTreno;  // Esempio: Alta Velocità, Regionale
    private final String periodo;    // Esempio: Estate 2024
    private final String tratta;     // Stringa che rappresenta la tratta, ad esempio "Milano - Roma"
    private final boolean clienteFedelta;  // Indica se il cliente è fedeltà o no

    public PromozioneCommand(String messaggio, String tipoTreno, String periodo, TrattaDTO tratta, boolean clienteFedelta) {
        this.messaggio = messaggio;
        this.tipoTreno = tipoTreno;
        this.periodo = periodo;
        this.tratta = tratta.getStazionePartenza() + " - " + tratta.getStazioneArrivo(); // Concatenazione delle stazioni di partenza e arrivo
        this.clienteFedelta = clienteFedelta;
    }

    @Override
    public RichiestaDTO esegui() {
        // Verifica se la promozione è applicabile
        if (!verificaCondizioniPromozione()) {
            // Restituisce una richiesta con esito negativo se la promozione non è applicabile
            return new RichiestaBuilder()
                    .setTipo(TipoRichiesta.PROMOZIONE)
                    .setMessaggio("La promozione non è applicabile.")
                    .build();
        }

        // Se la promozione è applicabile, restituisce una richiesta con esito positivo
        return new RichiestaBuilder()
                .setTipo(TipoRichiesta.PROMOZIONE)
                .setMessaggio("Promozione applicata con successo!")
                .build();
    }

    // Verifica le condizioni per applicare la promozione
    private boolean verificaCondizioniPromozione() {
        boolean tipoTrenoValido = tipoTreno.equals("Alta Velocità") || tipoTreno.equals("Regionale");
        boolean periodoValido = periodo.equals("Estate 2024");  // Può essere esteso a più periodi
        boolean trattaValida = tratta.equals("Milano - Roma");  // Può essere esteso a più tratte

        // Verifica se il cliente è fedeltà
        boolean clienteFedeltaValido = clienteFedelta;

        // La promozione è valida solo se tutte le condizioni sono soddisfatte
        return tipoTrenoValido && periodoValido && trattaValida && clienteFedeltaValido;
    }
}
