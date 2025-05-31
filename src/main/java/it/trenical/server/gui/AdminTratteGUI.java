package it.trenical.server.gui;

import it.trenical.server.db.DatabaseTratte;
import it.trenical.server.db.DatabaseBiglietti;
import it.trenical.server.observer.TrattaObservable;
import it.trenical.server.observer.ClienteOsservatore;
import it.trenical.common.grpc.TrattaDTO;
import it.trenical.common.grpc.BigliettoDTO;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class AdminTratteGUI extends JFrame {

    private JComboBox<Integer> trattaBox;
    private JTextField orarioPartField, orarioArrField, binarioField;
    private JComboBox<String> statoBox;

    public AdminTratteGUI() {
        setTitle("Gestione Tratte");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(6, 2, 5, 5));

        // ID Tratta
        add(new JLabel("ID Tratta:"));
        trattaBox = new JComboBox<>();
        for (TrattaObservable t : DatabaseTratte.getInstance().getAll()) {
            trattaBox.addItem(t.getTratta().getId());
        }
        add(trattaBox);

        // Stato
        add(new JLabel("Stato:"));
        statoBox = new JComboBox<>();
        statoBox.addItem(""); // default nullo
        statoBox.addItem("ritardo");
        statoBox.addItem("cancellato");
        add(statoBox);

        // Orario partenza
        add(new JLabel("Orario Partenza:"));
        orarioPartField = new JTextField();
        orarioPartField.setEnabled(false);
        add(orarioPartField);

        // Orario arrivo
        add(new JLabel("Orario Arrivo:"));
        orarioArrField = new JTextField();
        orarioArrField.setEnabled(false);
        add(orarioArrField);

        // Binario
        add(new JLabel("Binario:"));
        binarioField = new JTextField();
        binarioField.setEnabled(true); // binario abilitato di default se stato è vuoto
        add(binarioField);

        // Listener dinamico sulla tendina "Stato"
        statoBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String stato = (String) statoBox.getSelectedItem();
                boolean ritardo = "ritardo".equalsIgnoreCase(stato);
                boolean cancellato = "cancellato".equalsIgnoreCase(stato);

                orarioPartField.setEnabled(ritardo);
                orarioArrField.setEnabled(ritardo);
                binarioField.setEnabled(!cancellato);
            }
        });

        // Pulsante di aggiornamento
        JButton aggiornaBtn = new JButton("Aggiorna e Notifica");
        aggiornaBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                aggiornaTratta();
            }
        });
        add(new JLabel(""));
        add(aggiornaBtn);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
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

        JOptionPane.showMessageDialog(this,
                "Tratta aggiornata con successo!");
    }
}