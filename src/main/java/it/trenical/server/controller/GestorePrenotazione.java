package it.trenical.server.controller;

import it.trenical.common.grpc.*;
import it.trenical.server.db.DatabasePrenotazioni;
import it.trenical.server.db.DatabaseTratte;
import it.trenical.server.notification.NotificationDispatcher;

import java.util.*;
import java.util.concurrent.*;

public class GestorePrenotazione implements Gestore {

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
    private static final int SECONDI_SCADENZA = 60;
    private static final int SECONDI_AVVISO = 45;

    @Override
    public synchronized RispostaDTO gestisci(RichiestaDTO richiesta) {
        ClienteDTO cliente = richiesta.getCliente();
        TrattaDTO tratta = richiesta.getTratta();

        if (!richiesta.hasCliente() || !richiesta.hasTratta()) {
            return RispostaDTO.newBuilder()
                    .setEsito(false)
                    .setMessaggio("Cliente o tratta mancanti.")
                    .build();
        }

        int quantita = 1;
        try {
            quantita = Integer.parseInt(richiesta.getMessaggio());
        } catch (Exception ignored) {}

        if (quantita <= 0) {
            return RispostaDTO.newBuilder()
                    .setEsito(false)
                    .setMessaggio("Numero di prenotazioni non valido.")
                    .build();
        }

        TrattaDTO trattaCorrente = DatabaseTratte.getInstance().getTratta(tratta.getId());

        if (trattaCorrente == null || trattaCorrente.getPostiDisponibili() < quantita) {
            return RispostaDTO.newBuilder()
                    .setEsito(false)
                    .setMessaggio("Posti disponibili insufficienti.")
                    .build();
        }

        TrattaDTO aggiornata = TrattaDTO.newBuilder(trattaCorrente)
                .setPostiDisponibili(trattaCorrente.getPostiDisponibili() - quantita)
                .build();
        DatabaseTratte.getInstance().aggiornaTratta(aggiornata);

        List<BigliettoDTO> bigliettiCreati = new ArrayList<>();
        List<Integer> idPrenotazioni = new ArrayList<>();

        for (int i = 0; i < quantita; i++) {
            int idPrenotazione = DatabasePrenotazioni.getInstance().generaNuovoId();

            BigliettoDTO bigliettoPrenotato = BigliettoDTO.newBuilder()
                    .setId(idPrenotazione)
                    .setCliente(cliente)
                    .setTratta(aggiornata)
                    .setPrezzo(aggiornata.getPrezzo())
                    .setClasseServizio(aggiornata.getClasseServizio())
                    .setStato("PRENOTATO")
                    .build();

            DatabasePrenotazioni.getInstance().aggiungiPrenotazione(idPrenotazione, bigliettoPrenotato, cliente, 1);

            bigliettiCreati.add(bigliettoPrenotato);
            idPrenotazioni.add(idPrenotazione);
        }

        scheduler.schedule(new AvvisoScadenza(cliente, idPrenotazioni), SECONDI_AVVISO, TimeUnit.SECONDS);
        scheduler.schedule(new ScadenzaPrenotazioni(cliente, idPrenotazioni, trattaCorrente.getId(), quantita), SECONDI_SCADENZA, TimeUnit.SECONDS);

        RispostaDTO.Builder risposta = RispostaDTO.newBuilder()
                .setEsito(true)
                .setMessaggio("Prenotazioni effettuate con successo. Quantità: " + quantita);

        for (BigliettoDTO b : bigliettiCreati) {
            risposta.addBiglietti(b);
        }

        return risposta.build();
    }

    private static class AvvisoScadenza implements Runnable {
        private final ClienteDTO cliente;
        private final List<Integer> ids;

        public AvvisoScadenza(ClienteDTO cliente, List<Integer> ids) {
            this.cliente = cliente;
            this.ids = ids;
        }

        @Override
        public void run() {
            NotificationDispatcher.getInstance().notifica(
                    cliente.getEmail(),
                    "Attenzione: hai " + ids.size() + " prenotazione/i (#" + ids + ") in scadenza tra " + (SECONDI_SCADENZA - SECONDI_AVVISO) + " secondi!"
            );
        }
    }

    private static class ScadenzaPrenotazioni implements Runnable {
        private final ClienteDTO cliente;
        private final List<Integer> ids;
        private final int idTratta;
        private final int quantita;

        public ScadenzaPrenotazioni(ClienteDTO cliente, List<Integer> ids, int idTratta, int quantita) {
            this.cliente = cliente;
            this.ids = ids;
            this.idTratta = idTratta;
            this.quantita = quantita;
        }

        @Override
        public void run() {
            for (int id : ids) {
                DatabasePrenotazioni.getInstance().rimuoviPrenotazione(id);
            }

            TrattaDTO tratta = DatabaseTratte.getInstance().getTratta(idTratta);
            if (tratta != null) {
                TrattaDTO aggiornata = TrattaDTO.newBuilder(tratta)
                        .setPostiDisponibili(tratta.getPostiDisponibili() + quantita)
                        .build();
                DatabaseTratte.getInstance().aggiornaTratta(aggiornata);
            }

            NotificationDispatcher.getInstance().notifica(
                    cliente.getEmail(),
                    "Le prenotazioni " + ids + " sono scadute. I posti sono stati resi nuovamente disponibili."
            );
        }
    }
}
