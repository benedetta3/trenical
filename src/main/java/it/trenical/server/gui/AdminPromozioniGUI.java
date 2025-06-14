package it.trenical.server.gui;

import it.trenical.server.db.DatabasePromozioni;
import it.trenical.server.notification.NotificationDispatcher;
import it.trenical.common.grpc.PromozioneDTO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class AdminPromozioniGUI extends JFrame {

    private DefaultTableModel model;
    private JTable tabella;
    private JTextField descrizioneField, scontoField;
    private JCheckBox soloFedeltaCheck;

    public AdminPromozioniGUI() {
        setTitle("Gestione Promozioni");
        setSize(600, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        model = new DefaultTableModel(new Object[]{"Descrizione", "Sconto (%)", "Solo Fedeltà"}, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabella = new JTable(model);
        aggiornaTabella();

        add(new JScrollPane(tabella), BorderLayout.CENTER);

        JPanel form = new JPanel(new GridLayout(4, 2, 5, 5));
        form.setBorder(BorderFactory.createTitledBorder("Nuova Promozione"));

        descrizioneField = new JTextField();
        scontoField = new JTextField();
        soloFedeltaCheck = new JCheckBox("Riservata a FedeltàTreno");

        form.add(new JLabel("Descrizione:"));
        form.add(descrizioneField);
        form.add(new JLabel("Sconto (%):"));
        form.add(scontoField);
        form.add(new JLabel(""));
        form.add(soloFedeltaCheck);

        JButton aggiungiBtn = new JButton("Aggiungi Promozione");
        aggiungiBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                aggiungiPromozione();
            }
        });

        form.add(new JLabel(""));
        form.add(aggiungiBtn);

        add(form, BorderLayout.SOUTH);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setVisible(true);
    }

    private void aggiornaTabella() {
        model.setRowCount(0);
        List<PromozioneDTO> promozioni = DatabasePromozioni.getInstance().getTutteLePromozioni();
        for (PromozioneDTO p : promozioni) {
            model.addRow(new Object[]{
                    p.getDescrizione(),
                    (int) p.getSconto(),
                    p.getSoloFedelta() ? "Sì" : "No"
            });
        }
    }

    private void aggiungiPromozione() {
        String descrizione = descrizioneField.getText().trim();
        String scontoStr = scontoField.getText().trim();

        if (descrizione.isEmpty() || scontoStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Compila tutti i campi.");
            return;
        }

        double sconto;
        try {
            sconto = Double.parseDouble(scontoStr);
            if (sconto < 0 || sconto > 100) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Inserisci uno sconto valido (0-100).");
            return;
        }

        boolean soloFedelta = soloFedeltaCheck.isSelected();

        PromozioneDTO nuovaPromo = PromozioneDTO.newBuilder()
                .setDescrizione(descrizione)
                .setSconto(sconto)
                .setSoloFedelta(soloFedelta)
                .build();

        DatabasePromozioni.getInstance().aggiungiPromozione(nuovaPromo);

        NotificationDispatcher.getInstance().notificaNuovaPromozioneFedelta(nuovaPromo);

        JOptionPane.showMessageDialog(this, "Promozione aggiunta.");
        descrizioneField.setText("");
        scontoField.setText("");
        soloFedeltaCheck.setSelected(false);
        aggiornaTabella();
    }
}