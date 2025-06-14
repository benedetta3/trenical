package it.trenical.client.gui;

import it.trenical.client.command.*;
import it.trenical.client.notifiche.NotificationObserver;
import it.trenical.client.notifiche.SimpleNotificationObserver;
import it.trenical.common.grpc.*;
import it.trenical.server.notification.NotificationDispatcher;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class GestionePrenotazioniGUI extends JFrame {

    private final ClienteDTO cliente;
    private final DefaultTableModel model;
    private final JTable tabella;
    private final Timer refreshTimer = new Timer();

    private static final int SECONDI_AVVISO = 45;
    private static final int SECONDI_SCADENZA = 60;

    public GestionePrenotazioniGUI(ClienteDTO cliente) {
        this.cliente = cliente;

        setTitle("Le mie prenotazioni");
        setSize(1000, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        model = new DefaultTableModel(new Object[]{
                "ID", "Partenza", "Arrivo", "Data", "Partenza", "Arrivo", "Classe", "Prezzo", "Binario", "Stato"
        }, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabella = new JTable(model);
        add(new JScrollPane(tabella), BorderLayout.CENTER);

        JPanel pannelloBottoni = new JPanel();

        JButton aggiungiBtn = new JButton("Aggiungi Prenotazione");
        JButton pagaBtn = new JButton("Paga Prenotazione");

        aggiungiBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                apriPrenotaTratta();
            }
        });

        pagaBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                confermaPrenotazione();
            }
        });

        pannelloBottoni.add(aggiungiBtn);
        pannelloBottoni.add(pagaBtn);
        add(pannelloBottoni, BorderLayout.SOUTH);

        caricaPrenotazioni();

        refreshTimer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        caricaPrenotazioni();
                    }
                });
            }
        }, 3000, 3000);
    }

    private void caricaPrenotazioni() {
        int selectedRow = tabella.getSelectedRow();
        Integer idSelezionato = null;
        if (selectedRow >= 0 && selectedRow < model.getRowCount()) {
            idSelezionato = (Integer) model.getValueAt(selectedRow, 0);
        }

        model.setRowCount(0);
        RispostaDTO risposta = CommandInvoker.getInstance().esegui(new VisualizzaPrenotazioniCommand(cliente));

        if (risposta.getEsito()) {
            for (BigliettoDTO b : risposta.getBigliettiList()) {
                TrattaDTO t = b.getTratta();
                model.addRow(new Object[]{
                        b.getId(), t.getStazionePartenza(), t.getStazioneArrivo(), t.getData(),
                        t.getOrarioPartenza(), t.getOrarioArrivo(), t.getClasseServizio(), b.getPrezzo(), t.getBinario(), b.getStato()
                });
            }
        }

        if (idSelezionato != null) {
            for (int i = 0; i < model.getRowCount(); i++) {
                if (model.getValueAt(i, 0).equals(idSelezionato)) {
                    tabella.setRowSelectionInterval(i, i);
                    break;
                }
            }
        }
    }

    private void apriPrenotaTratta() {
        FinestraFiltraPerPrenotazione finestra = new FinestraFiltraPerPrenotazione(cliente, this);
        finestra.setVisible(true);
    }

    protected void refresh() {
        caricaPrenotazioni();
    }

    private void confermaPrenotazione() {
        int row = tabella.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Seleziona una prenotazione da pagare.");
            return;
        }

        int id = (Integer) model.getValueAt(row, model.findColumn("ID"));
        String stato = model.getValueAt(row, model.findColumn("Stato")).toString();

        if (!"PRENOTATO".equalsIgnoreCase(stato)) {
            JOptionPane.showMessageDialog(this, "Questa prenotazione non è in stato PRENOTATO.");
            return;
        }

        int scelta = JOptionPane.showConfirmDialog(this,
                "Vuoi confermare e pagare la prenotazione #" + id + "?",
                "Conferma Prenotazione", JOptionPane.YES_NO_OPTION);

        if (scelta != JOptionPane.YES_OPTION) return;

        BigliettoDTO biglietto = BigliettoDTO.newBuilder().setId(id).build();
        ConfermaPrenotazioneCommand comando = new ConfermaPrenotazioneCommand(biglietto);
        RispostaDTO risposta = CommandInvoker.getInstance().esegui(comando);

        StringBuilder messaggio = new StringBuilder();

        for (BigliettoDTO b : risposta.getBigliettiList()) {
            double prezzoOriginale = b.getTratta().getPrezzo();
            double prezzoFinale = b.getPrezzo();
            double sconto = prezzoOriginale - prezzoFinale;

            messaggio.append("- ID: ").append(b.getId()).append(" | ")
                    .append(b.getTratta().getStazionePartenza()).append(" → ")
                    .append(b.getTratta().getStazioneArrivo()).append(" | €")
                    .append(String.format("%.2f", prezzoFinale)).append("\n\n");

            messaggio.append("Acquisto completato con successo.\n");
            messaggio.append("Prezzo originale: €").append(String.format("%.2f", prezzoOriginale)).append("\n");
            messaggio.append("Sconto totale: -€").append(String.format("%.2f", sconto)).append("\n");
            messaggio.append("Prezzo finale: €").append(String.format("%.2f", prezzoFinale)).append("\n");

            if (risposta.getPromozioniCount() > 0) {
                messaggio.append("Promozioni applicate:\n");
                for (PromozioneDTO promo : risposta.getPromozioniList()) {
                    messaggio.append("- ").append(promo.getDescrizione())
                            .append(" (").append((int) promo.getSconto()).append("%)\n");
                }
            }

            messaggio.append("-----------\n");
        }

        JOptionPane.showMessageDialog(this, messaggio.toString(), "Prenotazione Confermata", JOptionPane.INFORMATION_MESSAGE);
        caricaPrenotazioni();

        if (!NotificationDispatcher.getInstance().getClientiRegistrati().contains(cliente.getEmail())) {
            int sceltaNotifica = JOptionPane.showConfirmDialog(this,
                    "Vuoi ricevere aggiornamenti in tempo reale su questa tratta?",
                    "Iscrizione a notifiche",
                    JOptionPane.YES_NO_OPTION);

            if (sceltaNotifica == JOptionPane.YES_OPTION) {
                TrattaDTO tratta = risposta.getBiglietti(0).getTratta();
                RegistraNotificaCommand cmd = new RegistraNotificaCommand(cliente, tratta);
                RispostaDTO rispostaNotifica = CommandInvoker.getInstance().esegui(cmd);

                if (rispostaNotifica.getEsito()) {
                    NotificationDispatcher.getInstance().registra(
                            cliente.getEmail(),
                            new SimpleNotificationObserver(cliente.getEmail())
                    );
                }
            }
        }
    }

    public void notificaPrenotazioniMultiple(final List<Integer> idPrenotazioni) {
        if (idPrenotazioni.isEmpty()) return;

        javax.swing.Timer avviso = new javax.swing.Timer(SECONDI_AVVISO * 1000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                boolean esistonoAncora = false;
                for (Integer id : idPrenotazioni) {
                    if (contienePrenotazioneAttiva(id)) {
                        esistonoAncora = true;
                        break;
                    }
                }
                if (!esistonoAncora) return;

                int tempoResiduo = SECONDI_SCADENZA - SECONDI_AVVISO;
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        String messaggio = idPrenotazioni.size() == 1
                                ? "La tua prenotazione #" + idPrenotazioni.get(0) + " scadrà tra " + tempoResiduo + " secondi!"
                                : "ATTENZIONE: alcune prenotazioni scadranno tra " + tempoResiduo + " secondi!";
                        JOptionPane.showMessageDialog(GestionePrenotazioniGUI.this, messaggio, "Avviso Scadenza", JOptionPane.WARNING_MESSAGE);
                    }
                });
            }
        });
        avviso.setRepeats(false);
        avviso.start();

        javax.swing.Timer scadenza = new javax.swing.Timer(SECONDI_SCADENZA * 1000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                boolean esistonoAncora = false;
                for (Integer id : idPrenotazioni) {
                    if (contienePrenotazioneAttiva(id)) {
                        esistonoAncora = true;
                        break;
                    }
                }
                if (!esistonoAncora) return;

                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        String messaggio = idPrenotazioni.size() == 1
                                ? "Prenotazione #" + idPrenotazioni.get(0) + " scaduta e rimossa."
                                : "ATTENZIONE: alcune prenotazioni sono scadute e sono state rimosse.";
                        JOptionPane.showMessageDialog(GestionePrenotazioniGUI.this, messaggio, "Prenotazioni Scadute", JOptionPane.ERROR_MESSAGE);
                        refresh();
                    }
                });
            }
        });
        scadenza.setRepeats(false);
        scadenza.start();
    }

    private boolean contienePrenotazioneAttiva(int id) {
        for (int i = 0; i < model.getRowCount(); i++) {
            int idTabella = (Integer) model.getValueAt(i, 0);
            String stato = (String) model.getValueAt(i, 9);
            if (idTabella == id && "PRENOTATO".equalsIgnoreCase(stato)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void dispose() {
        refreshTimer.cancel();
        super.dispose();
    }
}