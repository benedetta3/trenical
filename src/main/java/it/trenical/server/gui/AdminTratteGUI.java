package it.trenical.server.gui;

import it.trenical.common.grpc.TrattaDTO;
import it.trenical.common.grpc.BigliettoDTO;
import it.trenical.server.db.DatabaseTratte;
import it.trenical.server.db.DatabaseBiglietti;
import it.trenical.server.observer.TrattaObservable;
import it.trenical.server.observer.ClienteOsservatore;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class AdminTratteGUI extends JFrame {

    private JComboBox<Integer> trattaBox;
    private JTextField orarioPartField, orarioArrField, binarioField;
    private JComboBox<String> statoBox;
    private JTable tabellaTratte;

    public AdminTratteGUI() {
        setTitle("Gestione Tratte");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        String[] colonne = {"ID", "Partenza", "Arrivo", "Orario Partenza", "Orario Arrivo", "Binario", "Stato"};
        DefaultTableModel modelloTabella = new DefaultTableModel(colonne, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (TrattaObservable t : DatabaseTratte.getInstance().getAll()) {
            TrattaDTO tr = t.getTratta();
            modelloTabella.addRow(new Object[]{
                    tr.getId(),
                    tr.getStazionePartenza(),
                    tr.getStazioneArrivo(),
                    tr.getOrarioPartenza(),
                    tr.getOrarioArrivo(),
                    tr.getBinario(),
                    tr.getStato()
            });
        }

        tabellaTratte = new JTable(modelloTabella);
        JScrollPane scrollPane = new JScrollPane(tabellaTratte);
        scrollPane.setPreferredSize(new Dimension(850, 250));

        JPanel panel = new JPanel(new GridLayout(6, 2, 5, 5));

        panel.add(new JLabel("ID Tratta:"));
        trattaBox = new JComboBox<>();
        for (TrattaObservable t : DatabaseTratte.getInstance().getAll()) {
            trattaBox.addItem(t.getTratta().getId());
        }
        panel.add(trattaBox);

        panel.add(new JLabel("Stato:"));
        statoBox = new JComboBox<>(new String[]{"", "ritardo", "cancellato"});
        panel.add(statoBox);

        panel.add(new JLabel("Orario Partenza:"));
        orarioPartField = new JTextField();
        orarioPartField.setEnabled(false);
        panel.add(orarioPartField);

        panel.add(new JLabel("Orario Arrivo:"));
        orarioArrField = new JTextField();
        orarioArrField.setEnabled(false);
        panel.add(orarioArrField);

        panel.add(new JLabel("Binario:"));
        binarioField = new JTextField();
        binarioField.setEnabled(true);
        panel.add(binarioField);

        JButton aggiornaBtn = new JButton("Aggiorna e Notifica");
        aggiornaBtn.addActionListener(e -> aggiornaTratta());
        panel.add(new JLabel(""));
        panel.add(aggiornaBtn);

        statoBox.addActionListener(e -> {
            String stato = (String) statoBox.getSelectedItem();
            boolean ritardo = "ritardo".equalsIgnoreCase(stato);
            boolean cancellato = "cancellato".equalsIgnoreCase(stato);
            orarioPartField.setEnabled(ritardo);
            orarioArrField.setEnabled(ritardo);
            binarioField.setEnabled(!cancellato);
        });

        JPanel contenitore = new JPanel();
        contenitore.setLayout(new BoxLayout(contenitore, BoxLayout.Y_AXIS));
        contenitore.add(scrollPane);
        contenitore.add(Box.createVerticalStrut(20));
        contenitore.add(panel);

        add(contenitore);

        setVisible(true);
    }

    private void aggiornaTratta() {
        Integer id = (Integer) trattaBox.getSelectedItem();
        if (id == null) {
            JOptionPane.showMessageDialog(this, "Seleziona un ID tratta valido.");
            return;
        }

        TrattaObservable trattaObs = DatabaseTratte.getInstance().getTrattaObservable(id);
        if (trattaObs == null) {
            JOptionPane.showMessageDialog(this, "Tratta non trovata.");
            return;
        }

        TrattaDTO vecchia = trattaObs.getTratta();

        String nuovoOrarioPartenza = orarioPartField.getText().trim();
        String nuovoOrarioArrivo = orarioArrField.getText().trim();
        String nuovoBinarioStr = binarioField.getText().trim();
        String nuovoStato = (String) statoBox.getSelectedItem();

        boolean modificato = false;
        TrattaDTO.Builder builder = TrattaDTO.newBuilder(vecchia);

        if (orarioPartField.isEnabled() && !nuovoOrarioPartenza.isEmpty() && !nuovoOrarioPartenza.equals(vecchia.getOrarioPartenza())) {
            builder.setOrarioPartenza(nuovoOrarioPartenza);
            modificato = true;
        }

        if (orarioArrField.isEnabled() && !nuovoOrarioArrivo.isEmpty() && !nuovoOrarioArrivo.equals(vecchia.getOrarioArrivo())) {
            builder.setOrarioArrivo(nuovoOrarioArrivo);
            modificato = true;
        }

        if (binarioField.isEnabled() && !nuovoBinarioStr.isEmpty()) {
            try {
                int nuovoBinario = Integer.parseInt(nuovoBinarioStr);
                if (nuovoBinario != vecchia.getBinario()) {
                    builder.setBinario(nuovoBinario);
                    modificato = true;
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Il binario deve essere un numero valido.");
                return;
            }
        }

        if (nuovoStato != null && !nuovoStato.isEmpty() && !nuovoStato.equalsIgnoreCase(vecchia.getStato())) {
            builder.setStato(nuovoStato);
            modificato = true;
        }

        if (!modificato) {
            JOptionPane.showMessageDialog(this, "Modifica almeno un campo rispetto ai valori attuali.");
            return;
        }

        TrattaDTO aggiornata = builder.build();
        trattaObs.aggiornaTratta(aggiornata);
        DatabaseTratte.getInstance().aggiornaTratta(aggiornata);

        List<BigliettoDTO> bigliettiInteressati = DatabaseBiglietti.getInstance().getBigliettiPerTratta(id);
        for (BigliettoDTO biglietto : bigliettiInteressati) {
            ClienteOsservatore osservatore = new ClienteOsservatore(biglietto.getCliente());
            osservatore.update(aggiornata);
        }

        JOptionPane.showMessageDialog(this, "Tratta aggiornata con successo!");
        dispose();
        new AdminTratteGUI();
    }
}