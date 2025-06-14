package it.trenical.client.gui;

import it.trenical.client.command.*;
import it.trenical.client.notifiche.SimpleNotificationObserver;
import it.trenical.common.grpc.*;
import it.trenical.server.notification.NotificationDispatcher;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;

public class AcquistaBigliettoGUI extends JFrame {

    private MenuPrincipale menuPrincipale;
    private ClienteDTO clienteAutenticato;

    private JTextField partenzaField = new JTextField(10);
    private JTextField arrivoField = new JTextField(10);
    private JTextField dataField = new JTextField(10);
    private JComboBox<String> tipoTrenoBox = new JComboBox<>(new String[]{"", "Alta Velocità", "Regionale"});
    private JComboBox<String> classeBox = new JComboBox<>(new String[]{"", "1", "2"});

    private DefaultTableModel tableModel;
    private JTable table;

    private JPanel pannelloCliente;
    private JComboBox<String> metodoPagamentoBox;
    private JSpinner quantitySpinner;
    private JButton acquistaButton;

    public AcquistaBigliettoGUI(MenuPrincipale menu, ClienteDTO cliente) {
        this.menuPrincipale = menu;
        this.clienteAutenticato = cliente;

        setTitle("Acquista Biglietto");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel inputPanel = new JPanel(new GridLayout(2, 5, 5, 5));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Filtra Tratte"));

        inputPanel.add(new JLabel("Partenza:"));
        inputPanel.add(new JLabel("Arrivo:"));
        inputPanel.add(new JLabel("Data (yyyy-mm-gg):"));
        inputPanel.add(new JLabel("Tipo Treno:"));
        inputPanel.add(new JLabel("Classe:"));

        inputPanel.add(partenzaField);
        inputPanel.add(arrivoField);
        inputPanel.add(dataField);
        inputPanel.add(tipoTrenoBox);
        inputPanel.add(classeBox);

        JButton filtraButton = new JButton("Filtra");
        filtraButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                eseguiFiltro();
            }
        });

        tableModel = new DefaultTableModel(new Object[]{
                "ID", "Partenza", "Arrivo", "Data", "Orario Part.", "Orario Arrivo",
                "Tipo", "Classe", "Prezzo", "Posti", "Binario"
        }, 0) {
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        JButton selezionaButton = new JButton("Seleziona Tratta");
        selezionaButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (table.getSelectedRow() == -1) {
                    JOptionPane.showMessageDialog(null, "Seleziona una tratta prima di procedere.");
                } else {
                    pannelloCliente.setVisible(true);
                }
            }
        });

        pannelloCliente = new JPanel(new GridLayout(4, 2, 10, 10));
        pannelloCliente.setBorder(BorderFactory.createTitledBorder("Pagamento"));
        pannelloCliente.setVisible(false);

        metodoPagamentoBox = new JComboBox<>(new String[]{"", "Carta di Credito", "PayPal"});
        quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
        acquistaButton = new JButton("Conferma Acquisto");

        pannelloCliente.add(new JLabel("Metodo di Pagamento:"));
        pannelloCliente.add(metodoPagamentoBox);
        pannelloCliente.add(new JLabel("Numero Biglietti:"));
        pannelloCliente.add(quantitySpinner);
        pannelloCliente.add(new JLabel(""));
        pannelloCliente.add(acquistaButton);

        acquistaButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                eseguiAcquisto();
            }
        });

        JPanel panelFiltra = new JPanel(new BorderLayout());
        panelFiltra.add(inputPanel, BorderLayout.NORTH);
        panelFiltra.add(filtraButton, BorderLayout.CENTER);

        add(panelFiltra, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(selezionaButton, BorderLayout.WEST);
        add(pannelloCliente, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if (menuPrincipale != null) {
                    menuPrincipale.setVisible(true);
                }
            }
        });
    }

    private void eseguiFiltro() {
        String partenza = partenzaField.getText().trim();
        String arrivo = arrivoField.getText().trim();
        String data = dataField.getText().trim();

        if (partenza.isEmpty() || arrivo.isEmpty() || data.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Partenza, Arrivo e Data sono obbligatori.");
            return;
        }

        String tipoTreno = (String) tipoTrenoBox.getSelectedItem();
        if (tipoTreno == null || tipoTreno.isEmpty()) tipoTreno = "";

        String classe = (String) classeBox.getSelectedItem();
        if (classe == null || classe.isEmpty()) classe = "";

        FiltraTratteCommand comando = new FiltraTratteCommand(partenza, arrivo, data, tipoTreno, classe);
        RispostaDTO risposta = CommandInvoker.getInstance().esegui(comando);

        tableModel.setRowCount(0);

        if (!risposta.getEsito()) {
            JOptionPane.showMessageDialog(this, risposta.getMessaggio(), "Nessuna Tratta Trovata", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        for (TrattaDTO t : risposta.getTratteList()) {
            tableModel.addRow(new Object[]{
                    t.getId(),
                    t.getStazionePartenza(),
                    t.getStazioneArrivo(),
                    t.getData(),
                    t.getOrarioPartenza(),
                    t.getOrarioArrivo(),
                    t.getTipoTreno(),
                    t.getClasseServizio(),
                    t.getPrezzo(),
                    t.getPostiDisponibili(),
                    t.getBinario()
            });
        }
    }

    private void eseguiAcquisto() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Seleziona una tratta.");
            return;
        }

        String metodoPagamento = (String) metodoPagamentoBox.getSelectedItem();
        int quantity = ((Integer) quantitySpinner.getValue()).intValue();

        if (metodoPagamento == null || metodoPagamento.equals("")) {
            JOptionPane.showMessageDialog(this, "Seleziona un metodo di pagamento.");
            return;
        }

        int postiDisponibili = ((Integer) tableModel.getValueAt(row, 9)).intValue();
        if (quantity > postiDisponibili) {
            JOptionPane.showMessageDialog(this, "Posti non sufficienti per la quantità richiesta.");
            return;
        }

        TrattaDTO tratta = TrattaDTO.newBuilder()
                .setId((Integer) tableModel.getValueAt(row, 0))
                .setStazionePartenza((String) tableModel.getValueAt(row, 1))
                .setStazioneArrivo((String) tableModel.getValueAt(row, 2))
                .setData((String) tableModel.getValueAt(row, 3))
                .setOrarioPartenza((String) tableModel.getValueAt(row, 4))
                .setOrarioArrivo((String) tableModel.getValueAt(row, 5))
                .setTipoTreno((String) tableModel.getValueAt(row, 6))
                .setClasseServizio((String) tableModel.getValueAt(row, 7))
                .setPrezzo((Double) tableModel.getValueAt(row, 8))
                .setPostiDisponibili((Integer) tableModel.getValueAt(row, 9))
                .setBinario((Integer) tableModel.getValueAt(row, 10))
                .build();

        boolean successo = true;
        double prezzoTotale = 0.0;
        StringBuilder riepilogo = new StringBuilder("Biglietti acquistati:\n");

        for (int i = 0; i < quantity; i++) {
            AcquistaBigliettoCommand comando = new AcquistaBigliettoCommand(tratta, clienteAutenticato, null);
            RispostaDTO risposta = CommandInvoker.getInstance().esegui(comando);

            if (!risposta.getEsito()) {
                successo = false;
                riepilogo.append("Errore: ").append(risposta.getMessaggio()).append("\n");
            } else {
                for (BigliettoDTO b : risposta.getBigliettiList()) {
                    riepilogo.append("- ID: ").append(b.getId()).append(" | ")
                            .append(b.getTratta().getStazionePartenza()).append(" → ")
                            .append(b.getTratta().getStazioneArrivo()).append(" | €")
                            .append(String.format("%.2f", b.getPrezzo())).append("\n");

                    riepilogo.append("\nAcquisto completato con successo.\n")
                            .append("Prezzo originale: €").append(String.format("%.2f", tratta.getPrezzo())).append("\n")
                            .append("Sconto totale: -€")
                            .append(String.format("%.2f", tratta.getPrezzo() - b.getPrezzo())).append("\n")
                            .append("Prezzo finale: €").append(String.format("%.2f", b.getPrezzo())).append("\n");

                    if (risposta.getPromozioniCount() > 0) {
                        riepilogo.append("Promozioni applicate:\n");
                        for (int j = 0; j < risposta.getPromozioniCount(); j++) {
                            PromozioneDTO promo = risposta.getPromozioni(j);
                            riepilogo.append("- ").append(promo.getDescrizione())
                                    .append(" (").append((int) promo.getSconto()).append("%)\n");
                        }
                    }

                    riepilogo.append("-----------\n");
                    prezzoTotale += b.getPrezzo();
                }
            }
        }

        riepilogo.append("\nTotale pagato: €").append(String.format("%.2f", prezzoTotale));

        int scelta = JOptionPane.showConfirmDialog(this,
                "Vuoi ricevere aggiornamenti in tempo reale su questa tratta?",
                "Iscrizione a notifiche",
                JOptionPane.YES_NO_OPTION);

        if (scelta == JOptionPane.YES_OPTION) {
            RegistraNotificaCommand cmd = new RegistraNotificaCommand(clienteAutenticato, tratta);
            RispostaDTO rispostaNotifica = CommandInvoker.getInstance().esegui(cmd);
            if (rispostaNotifica.getEsito()) {
                SimpleNotificationObserver observer = new SimpleNotificationObserver(clienteAutenticato.getEmail());
                NotificationDispatcher.getInstance().registra(clienteAutenticato.getEmail(), observer);
            }
        }

        if (successo) {
            JOptionPane.showMessageDialog(this, riepilogo.toString(), "Acquisto Completato", JOptionPane.INFORMATION_MESSAGE);
            this.dispose();
            menuPrincipale.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, riepilogo.toString(), "Errore", JOptionPane.ERROR_MESSAGE);
        }
    }
}