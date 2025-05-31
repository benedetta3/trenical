package it.trenical.client.gui;

import it.trenical.client.command.CommandInvoker;
import it.trenical.client.command.FiltraTratteCommand;
import it.trenical.client.command.PrenotaBigliettoCommand;
import it.trenical.client.command.RegistraNotificaCommand;
import it.trenical.client.notifiche.SimpleNotificationObserver;
import it.trenical.common.grpc.*;
import it.trenical.server.notification.NotificationDispatcher;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;

public class FinestraFiltraPerPrenotazione extends JFrame {

    private final ClienteDTO cliente;
    private final GestionePrenotazioniGUI parentGUI;

    private final JTextField campoPartenza = new JTextField(10);
    private final JTextField campoArrivo = new JTextField(10);
    private final JTextField campoData = new JTextField(10);
    private final JComboBox<String> comboClasse = new JComboBox<>(new String[]{"", "1", "2"});
    private final JComboBox<String> comboTipo = new JComboBox<>(new String[]{"", "Frecciarossa", "Intercity", "Regionale"});
    private final DefaultTableModel model;
    private final JTable tabella;

    public FinestraFiltraPerPrenotazione(ClienteDTO cliente, GestionePrenotazioniGUI parentGUI) {
        this.cliente = cliente;
        this.parentGUI = parentGUI;

        setTitle("Filtra Tratte per Prenotazione");
        setSize(1050, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel panelRicerca = new JPanel(new FlowLayout());

        panelRicerca.add(new JLabel("Partenza:"));
        panelRicerca.add(campoPartenza);

        panelRicerca.add(new JLabel("Arrivo:"));
        panelRicerca.add(campoArrivo);

        panelRicerca.add(new JLabel("Data:"));
        panelRicerca.add(campoData);

        panelRicerca.add(new JLabel("Classe:"));
        panelRicerca.add(comboClasse);

        panelRicerca.add(new JLabel("Tipo:"));
        panelRicerca.add(comboTipo);

        JButton btnFiltra = new JButton("Filtra");
        btnFiltra.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                filtra();
            }
        });

        panelRicerca.add(btnFiltra);
        add(panelRicerca, BorderLayout.NORTH);

        model = new DefaultTableModel(new Object[]{
                "ID", "Partenza", "Arrivo", "Data", "Partenza", "Arrivo", "Tipo", "Classe", "Prezzo", "Posti", "Binario"
        }, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabella = new JTable(model);
        add(new JScrollPane(tabella), BorderLayout.CENTER);

        JButton btnPrenota = new JButton("Seleziona");
        btnPrenota.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                prenota();
            }
        });

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(btnPrenota);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void filtra() {
        String partenza = campoPartenza.getText().trim();
        String arrivo = campoArrivo.getText().trim();
        String data = campoData.getText().trim();
        String classe = (String) comboClasse.getSelectedItem();
        String tipo = (String) comboTipo.getSelectedItem();

        FiltraTratteCommand comando = new FiltraTratteCommand(partenza, arrivo, data, tipo, classe);
        RispostaDTO risposta = CommandInvoker.getInstance().esegui(comando);

        model.setRowCount(0);
        if (risposta.getEsito()) {
            List<TrattaDTO> tratte = risposta.getTratteList();
            for (TrattaDTO t : tratte) {
                model.addRow(new Object[]{
                        t.getId(), t.getStazionePartenza(), t.getStazioneArrivo(), t.getData(),
                        t.getOrarioPartenza(), t.getOrarioArrivo(), t.getTipoTreno(), t.getClasseServizio(),
                        t.getPrezzo(), t.getPostiDisponibili(), t.getBinario()
                });
            }
        } else {
            JOptionPane.showMessageDialog(this, "Nessuna tratta trovata.");
        }
    }
    private void prenota() {
        int row = tabella.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Seleziona una tratta da prenotare.");
            return;
        }

        int id = (Integer) model.getValueAt(row, 0);
        String partenza = (String) model.getValueAt(row, 1);
        String arrivo = (String) model.getValueAt(row, 2);
        String data = (String) model.getValueAt(row, 3);
        String orarioPartenza = (String) model.getValueAt(row, 4);
        String orarioArrivo = (String) model.getValueAt(row, 5);
        String tipo = (String) model.getValueAt(row, 6);
        String classe = (String) model.getValueAt(row, 7);
        double prezzo = (Double) model.getValueAt(row, 8);
        int posti = (Integer) model.getValueAt(row, 9);
        int binario = (Integer) model.getValueAt(row, 10);

        String input = JOptionPane.showInputDialog(this, "Quante prenotazioni vuoi effettuare?", "Numero Prenotazioni", JOptionPane.QUESTION_MESSAGE);
        if (input == null) return;

        int numero;
        try {
            numero = Integer.parseInt(input.trim());
            if (numero <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Inserisci un numero valido (> 0).");
            return;
        }

        if (numero > posti) {
            JOptionPane.showMessageDialog(this, "Posti disponibili insufficienti. Disponibili: " + posti);
            return;
        }

        TrattaDTO tratta = TrattaDTO.newBuilder()
                .setId(id)
                .setStazionePartenza(partenza)
                .setStazioneArrivo(arrivo)
                .setData(data)
                .setOrarioPartenza(orarioPartenza)
                .setOrarioArrivo(orarioArrivo)
                .setTipoTreno(tipo)
                .setClasseServizio(classe)
                .setPrezzo(prezzo)
                .setPostiDisponibili(posti)
                .setBinario(binario)
                .build();

        List<Integer> idPrenotazioni = new ArrayList<>();
        boolean tutteRiuscite = true;
        String messaggioErrore = "";

        for (int i = 0; i < numero; i++) {
            PrenotaBigliettoCommand comando = new PrenotaBigliettoCommand(tratta, cliente);
            RispostaDTO risposta = CommandInvoker.getInstance().esegui(comando);

            if (risposta.getEsito()) {
                List<BigliettoDTO> biglietti = risposta.getBigliettiList();
                if (biglietti != null && !biglietti.isEmpty()) {
                    int idPrenotazione = biglietti.get(0).getId();
                    idPrenotazioni.add(idPrenotazione);
                }
            } else {
                tutteRiuscite = false;
                messaggioErrore = risposta.getMessaggio();
                break;
            }
        }

        if (!tutteRiuscite) {
            JOptionPane.showMessageDialog(this, messaggioErrore, "Errore", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!idPrenotazioni.isEmpty()) {
            parentGUI.notificaPrenotazioniMultiple(idPrenotazioni);
        }

        JOptionPane.showMessageDialog(this,
                "Prenotazioni completate con successo.\nNumero prenotazioni: " + idPrenotazioni.size());

        //Popup iscrizione a notifiche subito dopo la prenotazione
        int scelta = JOptionPane.showConfirmDialog(this,
                "Vuoi ricevere aggiornamenti in tempo reale su questa tratta prenotata?",
                "Iscrizione a notifiche",
                JOptionPane.YES_NO_OPTION);

        if (scelta == JOptionPane.YES_OPTION) {
            RegistraNotificaCommand cmd = new RegistraNotificaCommand(cliente, tratta);
            RispostaDTO rispostaNotifica = CommandInvoker.getInstance().esegui(cmd);
            if (rispostaNotifica.getEsito()) {
                NotificationDispatcher.getInstance().registra(
                        cliente.getEmail(),
                        new SimpleNotificationObserver(cliente.getEmail())
                );
            }
        }

        parentGUI.refresh();
        dispose();
    }
}