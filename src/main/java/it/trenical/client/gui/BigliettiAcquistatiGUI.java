package it.trenical.client.gui;

import it.trenical.client.command.VisualizzaBigliettiCommand;
import it.trenical.common.grpc.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class BigliettiAcquistatiGUI extends JFrame {

    private final ClienteDTO cliente;
    private final DefaultTableModel model;

    public BigliettiAcquistatiGUI(ClienteDTO cliente) {
        this.cliente = cliente;

        setTitle("I miei Biglietti");
        setSize(1100, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        model = new DefaultTableModel(new Object[]{
                "ID", "Partenza", "Arrivo", "Data", "Orario Partenza", "Orario Arrivo",
                "Tipo", "Classe", "Prezzo", "Stato", "Binario"
        }, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable tabella = new JTable(model);
        add(new JScrollPane(tabella), BorderLayout.CENTER);

        caricaBiglietti();
    }

    private void caricaBiglietti() {
        model.setRowCount(0);
        VisualizzaBigliettiCommand comando = new VisualizzaBigliettiCommand(cliente);
        RispostaDTO risposta = it.trenical.client.command.CommandInvoker.getInstance().esegui(comando);

        if (risposta.getEsito()) {
            List<BigliettoDTO> lista = risposta.getBigliettiList();
            for (BigliettoDTO b : lista) {
                TrattaDTO t = b.getTratta();
                model.addRow(new Object[]{
                        b.getId(),
                        t.getStazionePartenza(),
                        t.getStazioneArrivo(),
                        t.getData(),
                        t.getOrarioPartenza(),
                        t.getOrarioArrivo(),
                        t.getTipoTreno(),
                        t.getClasseServizio(),
                        b.getPrezzo(),
                        t.getStato(),
                        t.getBinario()
                });
            }
        } else {
            JOptionPane.showMessageDialog(this, "Errore nel recupero dei biglietti.");
        }
    }
}