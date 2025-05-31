package it.trenical.client.gui;

import it.trenical.client.command.*;
import it.trenical.common.grpc.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

public class ModificaBigliettoGUI extends JFrame {

    private final MenuPrincipale menuPrincipale;
    private final ClienteDTO cliente;
    private JTable table;
    private DefaultTableModel tableModel;

    public static void apri(MenuPrincipale menu, ClienteDTO cliente) {
        VisualizzaBigliettiCommand comando = new VisualizzaBigliettiCommand(cliente);
        RispostaDTO risposta = CommandInvoker.getInstance().esegui(comando);

        if (!risposta.getEsito() || risposta.getBigliettiList().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Non hai ancora acquistato alcun biglietto.");
            if (menu != null) menu.setVisible(true);
            return;
        }

        new ModificaBigliettoGUI(menu, cliente).setVisible(true);
    }

    private ModificaBigliettoGUI(MenuPrincipale menu, ClienteDTO cliente) {
        this.menuPrincipale = menu;
        this.cliente = cliente;

        setTitle("Modifica Biglietto");
        setSize(950, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        tableModel = new DefaultTableModel(new Object[]{
                "ID", "Partenza", "Arrivo", "Data", "Ora Part.", "Ora Arrivo", "Tipo", "Classe", "Prezzo"
        }, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton btnModifica = new JButton("Modifica Biglietto");
        btnModifica.addActionListener(e -> apriFinestraModifica());
        add(btnModifica, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if (menuPrincipale != null) menuPrincipale.setVisible(true);
            }
        });

        caricaBiglietti();
    }

    private void caricaBiglietti() {
        tableModel.setRowCount(0);
        RispostaDTO risposta = CommandInvoker.getInstance().esegui(new VisualizzaBigliettiCommand(cliente));

        if (risposta.getEsito()) {
            for (BigliettoDTO b : risposta.getBigliettiList()) {
                TrattaDTO t = b.getTratta();
                tableModel.addRow(new Object[]{
                        b.getId(), t.getStazionePartenza(), t.getStazioneArrivo(), t.getData(),
                        t.getOrarioPartenza(), t.getOrarioArrivo(), t.getTipoTreno(),
                        t.getClasseServizio(), b.getPrezzo()
                });
            }
        }
    }

    private void apriFinestraModifica() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Seleziona un biglietto da modificare.");
            return;
        }

        int idBiglietto = (Integer) tableModel.getValueAt(row, 0);
        String partenza = (String) tableModel.getValueAt(row, 1);
        String arrivo = (String) tableModel.getValueAt(row, 2);
        String dataAttuale = (String) tableModel.getValueAt(row, 3);
        String orarioAttuale = (String) tableModel.getValueAt(row, 4);
        String classeAttuale = (String) tableModel.getValueAt(row, 7);
        double prezzoVecchio = (Double) tableModel.getValueAt(row, 8);

        while (true) {
            JTextField nuovaDataField = new JTextField();
            JTextField nuovoOrarioField = new JTextField();
            JComboBox<String> nuovaClasseBox = new JComboBox<>(new String[]{"", "1", "2"});

            JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
            panel.add(new JLabel("Nuova Data (formato: yyyy-MM-dd):"));
            panel.add(nuovaDataField);
            panel.add(new JLabel("Nuovo Orario (formato: HH:mm):"));
            panel.add(nuovoOrarioField);
            panel.add(new JLabel("Nuova Classe:"));
            panel.add(nuovaClasseBox);

            int result = JOptionPane.showConfirmDialog(this, panel, "Modifica Parametri", JOptionPane.OK_CANCEL_OPTION);
            if (result != JOptionPane.OK_OPTION) {
                return;
            }

            String nuovaData = nuovaDataField.getText().trim().isEmpty() ? dataAttuale : nuovaDataField.getText().trim();
            String nuovoOrario = nuovoOrarioField.getText().trim();
            String nuovaClasse = nuovaClasseBox.getSelectedItem() == null || nuovaClasseBox.getSelectedItem().toString().isEmpty()
                    ? classeAttuale : nuovaClasseBox.getSelectedItem().toString();

            FiltraTratteCommand filtro = new FiltraTratteCommand(partenza, arrivo, nuovaData, "", nuovaClasse);
            RispostaDTO risposta = CommandInvoker.getInstance().esegui(filtro);

            if (!risposta.getEsito() || risposta.getTratteList().isEmpty()) {
                int scelta = JOptionPane.showConfirmDialog(this,
                        "Nessuna tratta disponibile con i parametri selezionati.\nVuoi riprovare con parametri diversi?",
                        "Tratta non disponibile",
                        JOptionPane.YES_NO_OPTION);
                if (scelta != JOptionPane.YES_OPTION) {
                    return;
                }
                continue;
            }

            TrattaDTO nuovaTratta = null;
            for (TrattaDTO t : risposta.getTratteList()) {
                if ((nuovoOrario.isEmpty() || t.getOrarioPartenza().equals(nuovoOrario)) && t.getPostiDisponibili() > 0) {
                    nuovaTratta = t;
                    break;
                }
            }

            if (nuovaTratta == null) {
                int scelta = JOptionPane.showConfirmDialog(this,
                        "Nessuna tratta disponibile con l'orario specificato o senza posti liberi.\nVuoi riprovare con parametri diversi?",
                        "Tratta non disponibile",
                        JOptionPane.YES_NO_OPTION);
                if (scelta != JOptionPane.YES_OPTION) {
                    return;
                }
                continue;
            }


            if (processoModifica(idBiglietto, nuovaTratta, dataAttuale, orarioAttuale, prezzoVecchio)) {
                break;
            } else {
                return;
            }
        }
    }

    private boolean processoModifica(int idBiglietto, TrattaDTO nuovaTratta, String dataAttuale, String orarioAttuale, double prezzoVecchio) {
        double prezzoNuovo = nuovaTratta.getPrezzo();
        double differenzaPrezzo = prezzoNuovo - prezzoVecchio;

        double penaleStimata = 0.0;
        boolean modificaUrgente = false;
        try {
            String dataOraPartenza = dataAttuale + " " + orarioAttuale;
            LocalDateTime partenzaData = LocalDateTime.parse(dataOraPartenza, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            Duration tempoResiduo = Duration.between(LocalDateTime.now(), partenzaData);
            if (tempoResiduo.toHours() < 24) {
                penaleStimata = prezzoVecchio * 0.2;
                modificaUrgente = true;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Errore nel calcolo della penale: " + e.getMessage());
        }

        double rimborso = 0.0;
        double daPagare = 0.0;

        if (prezzoNuovo > prezzoVecchio) {
            daPagare += prezzoNuovo - prezzoVecchio;
        } else {
            rimborso = prezzoVecchio - prezzoNuovo;
        }

        if (penaleStimata > 0.0) {
            daPagare += penaleStimata;
        }

        StringBuilder riepilogo = new StringBuilder("Riepilogo Modifica:\n\n");
        riepilogo.append("Nuova Tratta:\n");
        riepilogo.append("Partenza: ").append(nuovaTratta.getStazionePartenza()).append(" → ")
                .append(nuovaTratta.getStazioneArrivo()).append("\n")
                .append("Data: ").append(nuovaTratta.getData()).append("\n")
                .append("Orario: ").append(nuovaTratta.getOrarioPartenza()).append(" - ").append(nuovaTratta.getOrarioArrivo()).append("\n")
                .append("Classe: ").append(nuovaTratta.getClasseServizio()).append("\n\n")
                .append("Dettagli Economici:\n")
                .append("Prezzo Originale: ").append(String.format("%.2f", prezzoVecchio)).append(" €\n")
                .append("Prezzo Nuovo: ").append(String.format("%.2f", prezzoNuovo)).append(" €\n")
                .append("Differenza: ").append(differenzaPrezzo >= 0 ? "+" : "").append(String.format("%.2f", differenzaPrezzo)).append(" €\n");

        if (modificaUrgente) {
            riepilogo.append("Penale (modifica < 24h): ").append(String.format("%.2f", penaleStimata)).append(" €\n");
        }

        riepilogo.append("\nTOTALE:\n");
        if (daPagare > 0) {
            riepilogo.append("Da pagare: ").append(String.format("%.2f", daPagare)).append(" €\n");
        }
        if (rimborso > 0) {
            riepilogo.append("Rimborso: ").append(String.format("%.2f", rimborso)).append(" €\n");
        }

        int conferma = JOptionPane.showConfirmDialog(this, riepilogo.toString(), "Conferma Modifica", JOptionPane.YES_NO_OPTION);
        if (conferma != JOptionPane.YES_OPTION) {
            return false;
        }

        if (daPagare > 0) {
            if (!gestisciPagamento(daPagare)) {
                return false;
            }
        }

        ModificaBigliettoCommand comando = new ModificaBigliettoCommand(idBiglietto, nuovaTratta, cliente);
        RispostaDTO rispostaFinale = CommandInvoker.getInstance().esegui(comando);

        String messaggio = rispostaFinale.getMessaggio();

        if (messaggio.contains("Rimborso effettuato")) {
            int inizio = messaggio.indexOf("Rimborso effettuato di ");
            int fine = messaggio.indexOf("€", inizio);
            if (inizio >= 0 && fine > inizio) {
                String importo = messaggio.substring(inizio + 23, fine).trim();
                JOptionPane.showMessageDialog(this,
                        "Riceverai un rimborso di " + importo + " €",
                        "Rimborso",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }

        if (rispostaFinale.getEsito()) {
            JOptionPane.showMessageDialog(this, messaggio, "Modifica Completata", JOptionPane.INFORMATION_MESSAGE);
            caricaBiglietti();
            return true;
        } else {
            JOptionPane.showMessageDialog(this, messaggio, "Errore", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private boolean gestisciPagamento(double importo) {
        Object[] opzioni = {"Carta di Credito", "Portafoglio Digitale", "Bancomat", "Annulla"};
        String scelta = (String) JOptionPane.showInputDialog(
                this,
                "Importo da pagare: " + String.format("%.2f", importo) + " €\n\nSeleziona il metodo di pagamento:",
                "Metodo di Pagamento",
                JOptionPane.PLAIN_MESSAGE,
                null,
                opzioni,
                opzioni[0]
        );

        if (scelta == null || scelta.equals("Annulla")) {
            return false;
        }

        JOptionPane.showMessageDialog(this,
                "Pagamento di " + String.format("%.2f", importo) + " € effettuato con: " + scelta,
                "Pagamento Confermato",
                JOptionPane.INFORMATION_MESSAGE);

        return true;
    }
}