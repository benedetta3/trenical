package it.trenical.server.promozione;

import it.trenical.common.grpc.ClienteDTO;
import it.trenical.common.grpc.PromozioneDTO;
import it.trenical.common.grpc.TrattaDTO;

public class PromozioneStrategyFactory {

    private static PromozioneStrategyFactory instance;

    private PromozioneStrategyFactory() {}

    public static synchronized PromozioneStrategyFactory getInstance() {
        if (instance == null) {
            instance = new PromozioneStrategyFactory();
        }
        return instance;
    }

    public PromozioneStrategy selezionaStrategia(PromozioneDTO promo, TrattaDTO tratta, ClienteDTO cliente) {
        try {
            String nomeClasse = promo.getClasseStrategy().trim();

            if (nomeClasse.isEmpty()) {
                return new ScontoPercentualeStrategy(promo);
            }

            String nomeCompleto = "it.trenical.server.promozione." + nomeClasse;
            Class<?> clazz = Class.forName(nomeCompleto);
            return (PromozioneStrategy) clazz.getDeclaredConstructor().newInstance();

        } catch (Exception e) {
            System.err.println("Errore nella selezione della strategy: " + e.getMessage());
            return new NessunaPromozioneStrategy();
        }
    }
}
